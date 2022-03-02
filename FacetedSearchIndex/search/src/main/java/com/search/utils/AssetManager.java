package com.search.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.search.core.ConceptConfiguration;
import com.search.core.EndpointDataset;
import com.search.core.Ontology;
import com.search.core.RDFoxDataset;
import com.search.core.VQSQuery;
import com.search.graph.ConceptEdge;
import com.search.graph.ConceptVariable;
import com.search.types.Concept;
import com.search.types.Config;
import com.search.types.Dataset;
import com.search.types.DatasetType;
import com.search.types.Edge;
import com.search.types.Index;
import com.search.types.Package;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// File containing all the provided assets (ontologies, configs, endpoints)
public class AssetManager {
    private static final Logger LOGGER = LogManager.getLogger(AssetManager.class);
    private Map<String, Ontology> ontologies;
    private Map<String, ConceptConfiguration> configs;
    private Map<String, ConceptConfiguration> configsToUseAtStartup;
    private Map<String, EndpointDataset> endpoints;
    private Map<String, RDFoxDataset> rdfoxDataset;
    private ObjectMapper objectMapper;

    public AssetManager(String configFileName) {
        ontologies = new HashMap<String, Ontology>();
        endpoints = new HashMap<String, EndpointDataset>();
        configs = new HashMap<String, ConceptConfiguration>();
        configsToUseAtStartup = new HashMap<String, ConceptConfiguration>();
        rdfoxDataset = new HashMap<String, RDFoxDataset>();
        objectMapper = new ObjectMapper();

        try {
            loadIndexConfigurations(objectMapper.readValue(new File(configFileName),
                    Config.class));
        } catch (IOException e) {
            LOGGER.error("Failed to parse config: " + configFileName);
            e.printStackTrace();
        }
    }

    public void loadIndexConfigurations(Config config) {
        LOGGER.info("Loading ontologies and datasets");

        for (Package pack : config.getPackages()) {
            com.search.types.Ontology ontology = pack.getOntology();
            Dataset dataset = pack.getDataset();

            ontologies.put(ontology.getName(), new Ontology(ontology.getEndpoint()));

            if (dataset.getType() == DatasetType.RDFOX) {
                rdfoxDataset.put(dataset.getName(), new RDFoxDataset(dataset.getEndpoint()));
            } else {
                endpoints.put(dataset.getName(), new EndpointDataset(dataset.getEndpoint()));
            }

            loadConceptConfiguration(pack.getIndices(), ontology.getName(), config.getIndiciesToCreateAtStartup());
        }

        LOGGER.info("Done loading ontologies and datasets");
    }

    public void loadConceptConfiguration(List<Index> indices, String ontologyName,
            HashSet<String> indiciesToCreateAtStartup) {
        for (Index index : indices) {
            LOGGER.info("Creating conceptConfiguration: " + index.getName());

            List<ConceptVariable> variables = new ArrayList<>();
            List<ConceptEdge> edges = new ArrayList<>();
            Concept concept = null;

            try {
                concept = objectMapper.readValue(new File(index.getPath()), Concept.class);
            } catch (IOException e) {
                LOGGER.error("Failed to parse concept: " + index.getPath());
                e.printStackTrace();
                continue;
            }

            for (String variable : concept.getVariables()) {
                variables.add(new ConceptVariable(variable));
            }

            for (Edge edge : concept.getEdges()) {
                edges.add(new ConceptEdge(variables.get(edge.getSource()), edge.getProperty(),
                        variables.get(edge.getTarget())));
            }

            int datatypeMissingVariable = concept.getAddAllMissingDatatypePropertiesToVariable();
            int objectPropMissingVariable = concept.getAddAllMissingObjectPropertiesToVariable();

            ConceptConfiguration cc = new ConceptConfiguration(
                    this.ontologies.get(ontologyName),
                    index.getName(),
                    variables.get(concept.getRoot()),
                    new ArrayList<ConceptVariable>(variables),
                    edges,
                    concept.getAddAllMissingDatatypePropertiesToAllVariables(),
                    concept.getAddAllMissingObjectPropertiesToAllVariables(),
                    datatypeMissingVariable == -1 ? null : variables.get(datatypeMissingVariable),
                    objectPropMissingVariable == -1 ? null : variables.get(objectPropMissingVariable));

            configs.put(cc.getId(), cc);

            if (indiciesToCreateAtStartup.contains(cc.getId())) {
                configsToUseAtStartup.put(cc.getId(), cc);
            }

            LOGGER.info("Finished creating conceptConfiguration: " + index.getName());
        }
    }

    public ConceptConfiguration getConfig(String ccId) {
        return this.configs.get(ccId);
    }

    public Map<String, ConceptConfiguration> getConfigs() {
        return this.configs;
    }

    public Map<String, ConceptConfiguration> getConfigsToUseAtStartup() {
        return this.configsToUseAtStartup;
    }

    public EndpointDataset getDataset(String id) {
        return this.endpoints.get(id);
    }

    public RDFoxDataset getRDFoxDataset(String id) {
        return this.rdfoxDataset.get(id);
    }

    public Ontology getOntology(String id) {
        return this.ontologies.get(id);
    }

    // Get the partial query
    public VQSQuery getVQSQuery(String keyword, String ontologyName) {
        String fileName = "queries/" + keyword;
        Ontology ontology = this.ontologies.get(ontologyName);
        try {
            String query = new String(Files.readAllBytes(Paths.get(fileName)));

            return new VQSQuery(ontology, query, "c1");
        } catch (IOException e) {
            LOGGER.error("Failed to read file: " + fileName);
            e.printStackTrace();
        }

        return null;
    }
}
