package com.search.utils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.search.core.ConceptConfiguration;
import com.search.core.EndpointDataset;
import com.search.core.Ontology;
import com.search.core.VqsQuery;
import com.search.graph.ConceptEdge;
import com.search.graph.ConceptVariable;

// File containing all the provided assets (ontologies, configs, endpoints)
public class AssetManager {
    private Map<String, Ontology> ontologies;
    private Map<String, ConceptConfiguration> configs;
    private Map<String, EndpointDataset> endpoints;

    public AssetManager() throws Exception {
        this.ontologies = new HashMap<String, Ontology>();
        this.endpoints = new HashMap<String, EndpointDataset>();
        this.configs = new HashMap<String, ConceptConfiguration>();

        this.ontologies.put("ontology-npd",
                new Ontology("file:///Users/abdul/Master/Data/npd-db.ttl.owl"));

        this.endpoints.put("dataset-local-npd",
                new EndpointDataset("http://192.168.0.103:9999/blazegraph/namespace/kb/sparql"));

        loadConceptConfiguration();
    }

    public ConceptConfiguration getConceptConfiguration(String ccId) throws Exception {
        return this.configs.get(ccId);
    }

    public Map<String, ConceptConfiguration> getConceptConfiguration() throws Exception {
        return this.configs;
    }

    public EndpointDataset getDataset(String datasetId) {
        return this.endpoints.get(datasetId);
    }

    public Ontology getOntology(String ontologyId) {
        return this.ontologies.get(ontologyId);
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

        cc = new ConceptConfiguration(npdOntology, "config-npd-wellbore-1-1", c1, variables, edges);
        configs.put(cc.getId(), cc);

        // // wellbore-1 grows from version 1 to 5.
        // cc = new ConceptConfiguration(npdOntology, "config-npd-wellbore-1-2");
        // c1 = new ConceptVariable(cc.generateVariableId(),
        // "http://sws.ifi.uio.no/vocab/npd-v2#Wellbore");
        // c2 = new ConceptVariable(cc.generateVariableId(),
        // "http://sws.ifi.uio.no/vocab/npd-v2#Field");
        // cc.addEdge(c1,
        // "http://sws.ifi.uio.no/vocab/npd-v2#discoveryWellbore_inverseProp", c2);
        // cc.setRoot(c1);
        // cc.addAllMissingDatatypePropertiesToAllVariables();
        // configs.put(cc.getId(), cc);

        // // wellbore-1 grows from version 1 to 5.
        // cc = new ConceptConfiguration(npdOntology, "config-npd-wellbore-1-3");
        // c1 = new ConceptVariable(cc.generateVariableId(),
        // "http://sws.ifi.uio.no/vocab/npd-v2#Wellbore");
        // c2 = new ConceptVariable(cc.generateVariableId(),
        // "http://sws.ifi.uio.no/vocab/npd-v2#Field");
        // c3 = new ConceptVariable(cc.generateVariableId(),
        // "http://sws.ifi.uio.no/vocab/npd-v2#Company");
        // cc.addEdge(c1,
        // "http://sws.ifi.uio.no/vocab/npd-v2#discoveryWellbore_inverseProp", c2);
        // cc.addEdge(c2, "http://sws.ifi.uio.no/vocab/npd-v2#currentFieldOperator",
        // c3);
        // cc.setRoot(c1);
        // cc.addAllMissingDatatypePropertiesToAllVariables();
        // configs.put(cc.getId(), cc);

        // // wellbore-1 grows from version 1 to 5.
        // cc = new ConceptConfiguration(npdOntology, "config-npd-wellbore-1-4");
        // c1 = new ConceptVariable(cc.generateVariableId(),
        // "http://sws.ifi.uio.no/vocab/npd-v2#Wellbore");
        // c2 = new ConceptVariable(cc.generateVariableId(),
        // "http://sws.ifi.uio.no/vocab/npd-v2#Field");
        // c3 = new ConceptVariable(cc.generateVariableId(),
        // "http://sws.ifi.uio.no/vocab/npd-v2#Company");
        // c4 = new ConceptVariable(cc.generateVariableId(),
        // "http://sws.ifi.uio.no/vocab/npd-v2#Pipeline");
        // cc.addEdge(c1,
        // "http://sws.ifi.uio.no/vocab/npd-v2#discoveryWellbore_inverseProp", c2);
        // cc.addEdge(c2, "http://sws.ifi.uio.no/vocab/npd-v2#currentFieldOperator",
        // c3);
        // cc.addEdge(c3,
        // "http://sws.ifi.uio.no/vocab/npd-v2#pipelineOperator_inverseProp", c4);
        // cc.setRoot(c1);
        // cc.addAllMissingDatatypePropertiesToAllVariables();
        // configs.put(cc.getId(), cc);

        // // wellbore-1 grows from version 1 to 5.
        // cc = new ConceptConfiguration(npdOntology, "config-npd-wellbore-1-5");
        // c1 = new ConceptVariable(cc.generateVariableId(),
        // "http://sws.ifi.uio.no/vocab/npd-v2#Wellbore");
        // c2 = new ConceptVariable(cc.generateVariableId(),
        // "http://sws.ifi.uio.no/vocab/npd-v2#Field");
        // c3 = new ConceptVariable(cc.generateVariableId(),
        // "http://sws.ifi.uio.no/vocab/npd-v2#Company");
        // c4 = new ConceptVariable(cc.generateVariableId(),
        // "http://sws.ifi.uio.no/vocab/npd-v2#Pipeline");
        // c5 = new ConceptVariable(cc.generateVariableId(),
        // "http://sws.ifi.uio.no/vocab/npd-v2#WellboreDocument");
        // cc.addEdge(c1,
        // "http://sws.ifi.uio.no/vocab/npd-v2#discoveryWellbore_inverseProp", c2);
        // cc.addEdge(c2, "http://sws.ifi.uio.no/vocab/npd-v2#currentFieldOperator",
        // c3);
        // cc.addEdge(c3,
        // "http://sws.ifi.uio.no/vocab/npd-v2#pipelineOperator_inverseProp", c4);
        // cc.addEdge(c1,
        // "http://sws.ifi.uio.no/vocab/npd-v2#documentForWellbore_inverseProp", c5);
        // cc.setRoot(c1);
        // cc.addAllMissingDatatypePropertiesToAllVariables();
        // configs.put(cc.getId(), cc);

        // // Wellbore-2 is a config which does not fit any query yet
        // cc = new ConceptConfiguration(npdOntology, "config-npd-wellbore-2-1");
        // c1 = new ConceptVariable(cc.generateVariableId(),
        // "http://sws.ifi.uio.no/vocab/npd-v2#Wellbore");
        // c2 = new ConceptVariable(cc.generateVariableId(),
        // "http://sws.ifi.uio.no/vocab/npd-v2#Discovery");
        // c3 = new ConceptVariable(cc.generateVariableId(),
        // "http://sws.ifi.uio.no/vocab/npd-v2#Field");
        // c4 = new ConceptVariable(cc.generateVariableId(),
        // "http://sws.ifi.uio.no/vocab/npd-v2#Company");
        // c5 = new ConceptVariable(cc.generateVariableId(),
        // "http://sws.ifi.uio.no/vocab/npd-v2#BAA");
        // cc.addEdge(c1,
        // "http://sws.ifi.uio.no/vocab/npd-v2#discoveryWellbore_inverseProp", c2);
        // cc.addEdge(c2, "http://sws.ifi.uio.no/vocab/npd-v2#includedInField", c3);
        // cc.addEdge(c3, "http://sws.ifi.uio.no/vocab/npd-v2#currentFieldOperator",
        // c4);
        // cc.addEdge(c4,
        // "http://sws.ifi.uio.no/vocab/npd-v2#baaOperatorCompany_inverseProp", c5);
        // cc.setRoot(c1);
        // cc.addAllMissingDatatypePropertiesToAllVariables();
        // configs.put(cc.getId(), cc);

        // expwellbore used to debug
        c1 = new ConceptVariable("http://sws.ifi.uio.no/vocab/npd-v2#ExplorationWellbore");
        c2 = new ConceptVariable("http://sws.ifi.uio.no/vocab/npd-v2#Field");
        c3 = new ConceptVariable("http://sws.ifi.uio.no/vocab/npd-v2#ProductionLicence");
        c4 = new ConceptVariable("http://sws.ifi.uio.no/vocab/npd-v2#Company");
        c5 = new ConceptVariable("http://sws.ifi.uio.no/vocab/npd-v2#FieldStatus");
        c6 = new ConceptVariable("http://sws.ifi.uio.no/vocab/npd-v2#Discovery");
        c7 = new ConceptVariable("http://sws.ifi.uio.no/vocab/npd-v2#ProductionlicenceStatus");
        c8 = new ConceptVariable("http://sws.ifi.uio.no/vocab/npd-v2#ProductionLicenceArea");
        // c1 = new
        // ConceptVariable("http://sws.ifi.uio.no/vocab/npd-v2#ExplorationWellbore");
        // c2 = new ConceptVariable("http://sws.ifi.uio.no/vocab/npd-v2#Field");
        // c3 = new
        // ConceptVariable("http://sws.ifi.uio.no/vocab/npd-v2#ProductionLicence");
        // c4 = new ConceptVariable("http://sws.ifi.uio.no/vocab/npd-v2#Company");
        // c5 = new ConceptVariable("http://sws.ifi.uio.no/vocab/npd-v2#Discovery");

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
        // e1.add(new ConceptEdge(c1,
        // "http://sws.ifi.uio.no/vocab/npd-v2#explorationWellboreForField", c2));
        // e1.add(new ConceptEdge(c1,
        // "http://sws.ifi.uio.no/vocab/npd-v2#wellboreForDiscovery", c5));
        // e1.add(new ConceptEdge(c1,
        // "http://sws.ifi.uio.no/vocab/npd-v2#explorationWellboreForLicence", c3));
        // e1.add(new ConceptEdge(c2,
        // "http://sws.ifi.uio.no/vocab/npd-v2#currentFieldOperator", c4));
        // e1.add(new ConceptEdge(c5,
        // "http://sws.ifi.uio.no/vocab/npd-v2#includedInField", c2));

        e1.add(new ConceptEdge(c1, "http://sws.ifi.uio.no/vocab/npd-v2#explorationWellboreForField", c2));
        e1.add(new ConceptEdge(c1, "http://sws.ifi.uio.no/vocab/npd-v2#explorationWellboreForLicence", c5));
        e1.add(new ConceptEdge(c2, "http://sws.ifi.uio.no/vocab/npd-v2#currentFieldOperator", c3));
        e1.add(new ConceptEdge(c4, "http://sws.ifi.uio.no/vocab/npd-v2#statusForField", c2));
        e1.add(new ConceptEdge(c8, "http://sws.ifi.uio.no/vocab/npd-v2#includedInField", c2));
        e1.add(new ConceptEdge(c6, "http://sws.ifi.uio.no/vocab/npd-v2#statusForLicence", c5));
        e1.add(new ConceptEdge(c7, "http://sws.ifi.uio.no/vocab/npd-v2#isGeometryOfFeature", c5));

        cc = new ConceptConfiguration(npdOntology, "config-npd-expwellbore-1-1", c1, v1, e1);
        configs.put(cc.getId(), cc);
    }

    // Get the partial query
    public VqsQuery getVQSQuery(String keyword) throws Exception {
        // Ontolgies are used to queries
        Ontology npdOntology = this.ontologies.get("ontology-npd");

        String fileName = "queries/" + keyword + ".rq";
        String q = new String(Files.readAllBytes(Paths.get(fileName)));
        return new VqsQuery(npdOntology, q, "c1");
    }
}
