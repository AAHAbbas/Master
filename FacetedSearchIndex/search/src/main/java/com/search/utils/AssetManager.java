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
        ConceptConfiguration cc = null;
        ConceptVariable c1 = null;
        ConceptVariable c2 = null;
        ConceptVariable c3 = null;
        ConceptVariable c4 = null;
        ConceptVariable c5 = null;
        ConceptVariable c6 = null;
        ConceptVariable c7 = null;
        ConceptVariable c8 = null;

        // Ontolgies are used to create configs.
        Ontology npdOntology = this.ontologies.get("ontology-npd");

        // wellbore-1 grows from version 1 to 5.
        List<ConceptVariable> variables = new ArrayList<>();
        List<ConceptEdge> edges = new ArrayList<>();
        c1 = new ConceptVariable("http://sws.ifi.uio.no/vocab/npd-v2#Wellbore");
        variables.add(c1);

        cc = new ConceptConfiguration(npdOntology,
                "config-npd-wellbore-1-1", c1, variables, edges, false, false, c1, null);
        configs.put(cc.getId(), cc);

        // expwellbore used to debug
        c1 = new ConceptVariable("http://sws.ifi.uio.no/vocab/npd-v2#ExplorationWellbore");
        c2 = new ConceptVariable("http://sws.ifi.uio.no/vocab/npd-v2#Field");
        c3 = new ConceptVariable("http://sws.ifi.uio.no/vocab/npd-v2#ProductionLicence");
        c4 = new ConceptVariable("http://sws.ifi.uio.no/vocab/npd-v2#Company");
        c5 = new ConceptVariable("http://sws.ifi.uio.no/vocab/npd-v2#FieldStatus");
        c6 = new ConceptVariable("http://sws.ifi.uio.no/vocab/npd-v2#Discovery");
        c7 = new ConceptVariable("http://sws.ifi.uio.no/vocab/npd-v2#ProductionlicenceStatus");
        c8 = new ConceptVariable("http://sws.ifi.uio.no/vocab/npd-v2#ProductionLicenceArea");

        List<ConceptVariable> v1 = new ArrayList<>();
        v1.add(c1);
        v1.add(c2);
        v1.add(c3);
        v1.add(c4);
        v1.add(c5);
        v1.add(c6);
        v1.add(c7);
        v1.add(c8);

        List<ConceptEdge> e1 = new ArrayList<>();
        e1.add(new ConceptEdge(c1, "http://sws.ifi.uio.no/vocab/npd-v2#explorationWellboreForField", c2));
        e1.add(new ConceptEdge(c1, "http://sws.ifi.uio.no/vocab/npd-v2#explorationWellboreForLicence", c5));
        e1.add(new ConceptEdge(c2, "http://sws.ifi.uio.no/vocab/npd-v2#currentFieldOperator", c3));
        e1.add(new ConceptEdge(c4, "http://sws.ifi.uio.no/vocab/npd-v2#statusForField", c2));
        e1.add(new ConceptEdge(c8, "http://sws.ifi.uio.no/vocab/npd-v2#includedInField", c2));
        e1.add(new ConceptEdge(c6, "http://sws.ifi.uio.no/vocab/npd-v2#statusForLicence", c5));
        e1.add(new ConceptEdge(c7, "http://sws.ifi.uio.no/vocab/npd-v2#isGeometryOfFeature", c5));

        cc = new ConceptConfiguration(npdOntology, "config-npd-expwellbore-1-1", c1, v1, e1, false, false, c1, null);
        configs.put(cc.getId(), cc);

        // expwellbore2 used to debug
        c1 = new ConceptVariable("http://sws.ifi.uio.no/vocab/npd-v2#ExplorationWellbore");
        c2 = new ConceptVariable("http://sws.ifi.uio.no/vocab/npd-v2#Field");
        c3 = new ConceptVariable("http://sws.ifi.uio.no/vocab/npd-v2#ProductionLicence");
        c4 = new ConceptVariable("http://sws.ifi.uio.no/vocab/npd-v2#Company");
        c5 = new ConceptVariable("http://sws.ifi.uio.no/vocab/npd-v2#Discovery");

        List<ConceptVariable> v2 = new ArrayList<>();
        v2.add(c1);
        v2.add(c2);
        v2.add(c3);
        v2.add(c4);
        v2.add(c5);

        List<ConceptEdge> e2 = new ArrayList<>();
        e2.add(new ConceptEdge(c1, "http://sws.ifi.uio.no/vocab/npd-v2#explorationWellboreForField", c2));
        e2.add(new ConceptEdge(c1, "http://sws.ifi.uio.no/vocab/npd-v2#wellboreForDiscovery", c5));
        e2.add(new ConceptEdge(c1, "http://sws.ifi.uio.no/vocab/npd-v2#explorationWellboreForLicence", c3));
        e2.add(new ConceptEdge(c2, "http://sws.ifi.uio.no/vocab/npd-v2#currentFieldOperator", c4));
        e2.add(new ConceptEdge(c5, "http://sws.ifi.uio.no/vocab/npd-v2#includedInField", c2));

        cc = new ConceptConfiguration(npdOntology, "config-npd-expwellbore-1-2", c1, v2, e2, false, false, c1, null);
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
