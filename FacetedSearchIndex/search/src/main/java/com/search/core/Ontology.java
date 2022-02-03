package com.search.core;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;

import uio.ifi.ontology.toolkit.projection.controller.triplestore.RDFoxSessionManager;
import uio.ifi.ontology.toolkit.projection.view.OptiqueVQSAPI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Class representing an ontology
// This has later been replaced by what we call a navigation graph
public class Ontology {
    private static final Logger LOGGER = LogManager.getLogger(Ontology.class);
    private Set<String> concepts;
    private Map<String, HashSet<Entry<String, String>>> objectProperties;
    // Maps conceptURIs to pairs of datatype property, URIs and their type
    private Map<String, HashSet<Entry<String, String>>> datatypeProperties;

    // This takes the ontology and extracts everything we need for the facet index
    public Ontology(String ontologyURI) {
        RDFoxSessionManager session = new RDFoxSessionManager();
        OptiqueVQSAPI vqsApi = new OptiqueVQSAPI(session);
        vqsApi.clearAllSessions();
        vqsApi.loadOntologySession(ontologyURI);

        LOGGER.info("Extracting data from the ontology file into ontology class for fast lookup");

        // Put all concept URIs into a set
        concepts = new HashSet<>();
        JSONArray conceptJSON = vqsApi.getCoreConcepts(ontologyURI).getJSONObject("result").getJSONArray("options");
        for (int i = 0; i < conceptJSON.length(); i++)
            concepts.add(conceptJSON.getJSONObject(i).getString("id"));

        // Get data properties and corresponding target type
        datatypeProperties = new HashMap<>();
        for (String conceptURI : concepts) {
            JSONArray properties = vqsApi.getConceptFacets(ontologyURI, conceptURI).getJSONObject("result")
                    .getJSONArray("fields");
            HashSet<Entry<String, String>> collectedProperties = new HashSet<>();

            for (int i = 0; i < properties.length(); i++) {
                String property = properties.getJSONObject(i).getString("id");
                String targetType = properties.getJSONObject(i).getString("type");
                collectedProperties.add(new AbstractMap.SimpleEntry<String, String>(property, targetType));
            }

            datatypeProperties.put(conceptURI, collectedProperties);
        }

        // Get all object properties and corresponsing target type
        objectProperties = new HashMap<>();
        for (String conceptURI : concepts) {
            JSONArray properties = vqsApi.getNeighbourConcepts(ontologyURI, conceptURI).getJSONObject("result")
                    .getJSONArray("options");
            HashSet<Entry<String, String>> collectedProperties = new HashSet<>();

            for (int i = 0; i < properties.length(); i++) {
                String targetType = properties.getJSONObject(i).getString("id");
                String property = properties.getJSONObject(i).getJSONObject("prop").getString("id");
                collectedProperties.add(new AbstractMap.SimpleEntry<String, String>(property, targetType));
            }

            objectProperties.put(conceptURI, collectedProperties);
        }

        LOGGER.info("Done loading the ontology");
    }

    // Return the set of all concept URIs
    public Set<String> getConceptURIs() {
        return concepts;
    }

    // Return a the set of data property URIs of a concept
    // Returns entries (pairs) of propertyURI and type
    public Set<Entry<String, String>> getDataPropertiesWithType(String concept) {
        return datatypeProperties.get(concept);
    }

    public Set<Entry<String, String>> getObjectPropertiesWithType(String concept) {
        return objectProperties.get(concept);
    }

    // Return a the set of data property URIs of a concept
    public Set<String> getDataProperties(String concept) {
        Set<String> result = new HashSet<>();

        for (Entry<String, String> entry : this.datatypeProperties.get(concept))
            result.add(entry.getKey());

        return result;
    }

    // Returns all the object properties going out from a given concept URI
    public Set<Entry<String, String>> getObjectProperties(String concept) {
        return objectProperties.get(concept);
    }

    // Given a concept URI and a property URI, give the type of the target of the
    // property
    public String getPropertyTargetType(String source, String property) {
        for (Entry<String, String> entry : this.datatypeProperties.get(source)) {
            if (entry.getKey().equals(property))
                return entry.getValue();
        }

        return null;
    }
}
