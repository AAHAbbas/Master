package com.search.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.search.core.ConceptConfiguration;
import com.search.core.EndpointDataset;
import com.search.core.Ontology;
import com.search.core.RDFoxDataset;
import com.search.core.VQSQuery;
import com.search.graph.ConceptEdge;
import com.search.graph.ConceptVariable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// File containing all the provided assets (ontologies, configs, endpoints)
public class AssetManager {
    private static final Logger LOGGER = LogManager.getLogger(AssetManager.class);
    private Map<String, Ontology> ontologies;
    private Map<String, ConceptConfiguration> configs;
    private Map<String, EndpointDataset> endpoints;
    private Map<String, RDFoxDataset> rdfoxDataset;

    public AssetManager() throws Exception {
        this.ontologies = new HashMap<String, Ontology>();
        this.endpoints = new HashMap<String, EndpointDataset>();
        this.configs = new HashMap<String, ConceptConfiguration>();
        this.rdfoxDataset = new HashMap<String, RDFoxDataset>();

        this.ontologies.put("ontology-npd",
                new Ontology("file:///Users/abdul/Master/Data/npd-db.ttl.owl"));

        this.endpoints.put("dataset-local-npd",
                new EndpointDataset("http://192.168.0.103:9999/blazegraph/namespace/kb/sparql"));

        this.rdfoxDataset.put("rdfox-npd",
                new RDFoxDataset("C:/Users/abdul/Master/Data/a-box.ttl"));

        LOGGER.info("Loading configs");
        loadConceptConfiguration();
        LOGGER.info("Done loading configs");
    }

    public ConceptConfiguration getConceptConfiguration(String ccId) throws Exception {
        return this.configs.get(ccId);
    }

    public Map<String, ConceptConfiguration> getConceptConfiguration() throws Exception {
        return this.configs;
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

    // Load the configs into the list of configs
    public void loadConceptConfiguration() throws Exception {
        createWellbore1();
        createWellbore2();
        createWellbore3();
        createWellbore4();
        createWellbore5();
        createExpWellbore1();
    }

    private void createWellbore1() {
        Ontology ontology = this.ontologies.get("ontology-npd");
        List<ConceptVariable> variables = new ArrayList<>();
        List<ConceptEdge> edges = new ArrayList<>();

        ConceptVariable c1 = new ConceptVariable("http://sws.ifi.uio.no/vocab/npd-v2#Wellbore");

        variables.add(c1);

        ConceptConfiguration cc = new ConceptConfiguration(ontology, "npd-wellbore-1-1", c1, variables, edges, true,
                false, null, null);

        configs.put(cc.getId(), cc);
    }

    private void createWellbore2() {
        Ontology ontology = this.ontologies.get("ontology-npd");
        List<ConceptVariable> variables = new ArrayList<>();
        List<ConceptEdge> edges = new ArrayList<>();

        ConceptVariable c1 = new ConceptVariable("http://sws.ifi.uio.no/vocab/npd-v2#Wellbore");
        ConceptVariable c2 = new ConceptVariable("http://sws.ifi.uio.no/vocab/npd-v2#Field");

        variables.add(c1);
        variables.add(c2);
        edges.add(new ConceptEdge(c1, "http://sws.ifi.uio.no/vocab/npd-v2#discoveryWellbore_inverseProp", c2));

        ConceptConfiguration cc = new ConceptConfiguration(ontology, "npd-wellbore-1-2", c1, variables, edges, true,
                false, null, null);

        configs.put(cc.getId(), cc);
    }

    private void createWellbore3() {
        Ontology ontology = this.ontologies.get("ontology-npd");
        List<ConceptVariable> variables = new ArrayList<>();
        List<ConceptEdge> edges = new ArrayList<>();

        ConceptVariable c1 = new ConceptVariable("http://sws.ifi.uio.no/vocab/npd-v2#Wellbore");
        ConceptVariable c2 = new ConceptVariable("http://sws.ifi.uio.no/vocab/npd-v2#Field");
        ConceptVariable c3 = new ConceptVariable("http://sws.ifi.uio.no/vocab/npd-v2#Company");

        variables.add(c1);
        variables.add(c2);
        variables.add(c3);
        edges.add(new ConceptEdge(c1, "http://sws.ifi.uio.no/vocab/npd-v2#discoveryWellbore_inverseProp", c2));
        edges.add(new ConceptEdge(c2, "http://sws.ifi.uio.no/vocab/npd-v2#currentFieldOperator", c3));

        ConceptConfiguration cc = new ConceptConfiguration(ontology, "npd-wellbore-1-3", c1, variables, edges, true,
                false, null, null);

        configs.put(cc.getId(), cc);
    }

    private void createWellbore4() {
        Ontology ontology = this.ontologies.get("ontology-npd");
        List<ConceptVariable> variables = new ArrayList<>();
        List<ConceptEdge> edges = new ArrayList<>();

        ConceptVariable c1 = new ConceptVariable("http://sws.ifi.uio.no/vocab/npd-v2#Wellbore");
        ConceptVariable c2 = new ConceptVariable("http://sws.ifi.uio.no/vocab/npd-v2#Field");
        ConceptVariable c3 = new ConceptVariable("http://sws.ifi.uio.no/vocab/npd-v2#Company");
        ConceptVariable c4 = new ConceptVariable("http://sws.ifi.uio.no/vocab/npd-v2#Pipeline");

        variables.add(c1);
        variables.add(c2);
        variables.add(c3);
        variables.add(c4);
        edges.add(new ConceptEdge(c1, "http://sws.ifi.uio.no/vocab/npd-v2#discoveryWellbore_inverseProp", c2));
        edges.add(new ConceptEdge(c2, "http://sws.ifi.uio.no/vocab/npd-v2#currentFieldOperator", c3));
        edges.add(new ConceptEdge(c3, "http://sws.ifi.uio.no/vocab/npd-v2#pipelineOperator_inverseProp", c4));

        ConceptConfiguration cc = new ConceptConfiguration(ontology, "npd-wellbore-1-4", c1, variables, edges, true,
                false, null, null);

        configs.put(cc.getId(), cc);
    }

    private void createWellbore5() {
        Ontology ontology = this.ontologies.get("ontology-npd");
        List<ConceptVariable> variables = new ArrayList<>();
        List<ConceptEdge> edges = new ArrayList<>();

        ConceptVariable c1 = new ConceptVariable("http://sws.ifi.uio.no/vocab/npd-v2#Wellbore");
        ConceptVariable c2 = new ConceptVariable("http://sws.ifi.uio.no/vocab/npd-v2#Field");
        ConceptVariable c3 = new ConceptVariable("http://sws.ifi.uio.no/vocab/npd-v2#Company");
        ConceptVariable c4 = new ConceptVariable("http://sws.ifi.uio.no/vocab/npd-v2#Pipeline");
        ConceptVariable c5 = new ConceptVariable("http://sws.ifi.uio.no/vocab/npd-v2#WellboreDocument");

        variables.add(c1);
        variables.add(c2);
        variables.add(c3);
        variables.add(c4);
        variables.add(c5);
        edges.add(new ConceptEdge(c1, "http://sws.ifi.uio.no/vocab/npd-v2#discoveryWellbore_inverseProp", c2));
        edges.add(new ConceptEdge(c2, "http://sws.ifi.uio.no/vocab/npd-v2#currentFieldOperator", c3));
        edges.add(new ConceptEdge(c3, "http://sws.ifi.uio.no/vocab/npd-v2#pipelineOperator_inverseProp", c4));
        edges.add(new ConceptEdge(c1, "http://sws.ifi.uio.no/vocab/npd-v2#documentForWellbore_inverseProp", c5));

        ConceptConfiguration cc = new ConceptConfiguration(ontology, "npd-wellbore-1-5", c1, variables, edges, true,
                false, null, null);

        configs.put(cc.getId(), cc);
    }

    private void createExpWellbore1() {
        Ontology ontology = this.ontologies.get("ontology-npd");
        List<ConceptVariable> variables = new ArrayList<>();
        List<ConceptEdge> edges = new ArrayList<>();

        ConceptVariable c1 = new ConceptVariable("http://sws.ifi.uio.no/vocab/npd-v2#ExplorationWellbore");
        ConceptVariable c2 = new ConceptVariable("http://sws.ifi.uio.no/vocab/npd-v2#Field");
        ConceptVariable c3 = new ConceptVariable("http://sws.ifi.uio.no/vocab/npd-v2#ProductionLicence");
        ConceptVariable c4 = new ConceptVariable("http://sws.ifi.uio.no/vocab/npd-v2#Company");
        ConceptVariable c5 = new ConceptVariable("http://sws.ifi.uio.no/vocab/npd-v2#Discovery");

        variables.add(c1);
        variables.add(c2);
        variables.add(c3);
        variables.add(c4);
        variables.add(c5);

        edges.add(new ConceptEdge(c1, "http://sws.ifi.uio.no/vocab/npd-v2#explorationWellboreForField", c2));
        edges.add(new ConceptEdge(c1, "http://sws.ifi.uio.no/vocab/npd-v2#wellboreForDiscovery", c5));
        edges.add(new ConceptEdge(c1, "http://sws.ifi.uio.no/vocab/npd-v2#explorationWellboreForLicence", c3));
        edges.add(new ConceptEdge(c2, "http://sws.ifi.uio.no/vocab/npd-v2#currentFieldOperator", c4));
        edges.add(new ConceptEdge(c5, "http://sws.ifi.uio.no/vocab/npd-v2#includedInField", c2));

        ConceptConfiguration cc = new ConceptConfiguration(ontology, "npd-expwellbore-1", c1, variables, edges, true,
                false, null, null);

        configs.put(cc.getId(), cc);
    }

    // Get the partial query
    public VQSQuery getVQSQuery(String keyword) {
        String fileName = "queries/" + keyword + ".rq";
        Ontology ontology = this.ontologies.get("ontology-npd");
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
