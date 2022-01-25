package com.search.core;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;

import uio.ifi.ontology.toolkit.projection.controller.triplestore.RDFoxSessionManager;
import uio.ifi.ontology.toolkit.projection.view.OptiqueVQSAPI;

// Class representing an ontology
// This has later been replaced by what we call a navigation graph.
public class Ontology {
    String ontologyURI; // The URI of the ontology
    Set<String> conceptURIs;
    Map<String, HashSet<Entry<String, String>>> objectProperties;
    Map<String, HashSet<Entry<String, String>>> datatypeProperties; // Maps conceptURIs to pairs of datatype property
                                                                    // URIs and their type.

    // Constructor. This takes the ontology and extracts everything we need for the
    // facet index.
    public Ontology(String ontologyURI) throws Exception {
        RDFoxSessionManager session = new RDFoxSessionManager();
        OptiqueVQSAPI vqsAPI = new OptiqueVQSAPI(session);
        vqsAPI.loadOntologySession(ontologyURI);

        System.out.println("Extract data from ontology file into ontology class for fast lookup.");

        // Put all concept URIs into a set:
        conceptURIs = new HashSet<String>();
        JSONArray concepts = vqsAPI.getCoreConcepts(ontologyURI).getJSONObject("result").getJSONArray("options");
        for (int i = 0; i < concepts.length(); i++)
            conceptURIs.add(concepts.getJSONObject(i).getString("id"));

        // Get data properties and corresponding target type
        datatypeProperties = new HashMap<String, HashSet<Entry<String, String>>>();
        for (String conceptURI : conceptURIs) {
            JSONArray propertiesJSON = vqsAPI.getConceptFacets(ontologyURI, conceptURI).getJSONObject("result")
                    .getJSONArray("fields");
            HashSet<Entry<String, String>> collectedProperties = new HashSet<Entry<String, String>>();

            for (int i = 0; i < propertiesJSON.length(); i++) {
                String property = propertiesJSON.getJSONObject(i).getString("id");
                String targetType = propertiesJSON.getJSONObject(i).getString("type");
                collectedProperties.add(new AbstractMap.SimpleEntry<String, String>(property, targetType));
            }

            datatypeProperties.put(conceptURI, collectedProperties);
        }

        // Get all object properties and corresponsing target type
        objectProperties = new HashMap<String, HashSet<Entry<String, String>>>();
        for (String conceptURI : conceptURIs) {
            JSONArray propertiesJSON = vqsAPI.getNeighbourConcepts(ontologyURI, conceptURI).getJSONObject("result")
                    .getJSONArray("options");
            HashSet<Entry<String, String>> collectedProperties = new HashSet<Entry<String, String>>();

            for (int i = 0; i < propertiesJSON.length(); i++) {
                String targetType = propertiesJSON.getJSONObject(i).getString("id");
                String property = propertiesJSON.getJSONObject(i).getJSONObject("prop").getString("id");
                collectedProperties.add(new AbstractMap.SimpleEntry<String, String>(property, targetType));
            }

            objectProperties.put(conceptURI, collectedProperties);
        }

        System.out.println("Done loading the ontology.");
    }

    // Return the set of all concept URIs.
    public Set<String> getConceptURIs() throws JSONException {
        return conceptURIs;
    }

    // Return a the set of data property URIs of a concept.
    // Returns entries (pairs) of propertyURI and type.
    public Set<Entry<String, String>> getDataPropertiesWithType(String conceptURI)
            throws IllegalArgumentException, Exception {

        return datatypeProperties.get(conceptURI);
    }

    public Set<Entry<String, String>> getObjectPropertiesWithType(String conceptURI) {
        return objectProperties.get(conceptURI);
    }

    // Return a the set of data property URIs of a concept.
    // Returns just a set of strings.
    public Set<String> getDataProperties(String conceptURI) throws IllegalArgumentException, Exception {
        Set<String> returnSet = new HashSet<String>();

        for (Entry<String, String> a : this.datatypeProperties.get(conceptURI))
            returnSet.add(a.getKey());

        return returnSet;
    }

    // Returns all the object properties going out from a given conceptURI.
    public Set<Entry<String, String>> getObjectProperties(String conceptURI)
            throws IllegalArgumentException, JSONException {

        return objectProperties.get(conceptURI);
    }

    // Given a concept uri and a property uri, give the type of the target of the
    // property
    public String getPropertyTargetType(String sourceConceptURI, String propertyURI)
            throws IllegalArgumentException, JSONException {

        for (Entry<String, String> propEntry : this.datatypeProperties.get(sourceConceptURI)) {
            if (propEntry.getKey().equals(propertyURI))
                return propEntry.getValue();
        }

        return null;
    }
}
