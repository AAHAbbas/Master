package com.search.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;

import tech.oxfordsemantic.jrdfox.client.Cursor;
import tech.oxfordsemantic.jrdfox.exceptions.JRDFoxException;

import com.search.core.ConceptConfiguration;
import com.search.core.EndpointDataset;
import com.search.core.RDFoxDataset;
import com.search.core.VQSQuery;

// The all values facet index model. It calculates all the possible values given in the data. 
public class SimpleFacetIndexModel extends FacetIndexModel {
    private EndpointDataset dataset;
    private RDFoxDataset rdfoxDataset;

    @Override
    public void constructFacetIndex(EndpointDataset datasource, Set<ConceptConfiguration> conceptConfigurations,
            RDFoxDataset rdfoxDataset) {
        this.dataset = datasource;
        this.rdfoxDataset = rdfoxDataset;
    }

    // Executes the a VQS query over the cache. It returns a map containing distinct
    // facet values for each local facet.
    @Override
    public int executeAbstractQuery(VQSQuery VQSQuery, ConceptConfiguration config) {
        String root = VQSQuery.getRoot().getType();
        List<String> dataProperties = new ArrayList<String>(VQSQuery.getOntology().getDataProperties(root));
        Collections.sort(dataProperties);
        String sparqlQuery = construtQuery(root, dataProperties);

        if (dataset != null) {
            return calculateDistinctValues(dataset.runQuery(sparqlQuery), dataProperties);
        } else {
            return calculateDistinctValues(rdfoxDataset.runQuery(sparqlQuery), dataProperties);
        }
    }

    // Calculate distinct values for each property using the results and a map from
    // the results
    public int calculateDistinctValues(List<BindingSet> rows, List<String> dataProperties) {
        Map<String, Set<String>> values = new HashMap<String, Set<String>>();
        int count = 0;

        for (BindingSet row : rows) {
            count += 1;

            for (int i = 0; i < dataProperties.size(); i++) {
                String dataProperty = dataProperties.get(i);
                Binding binding = row.getBinding("v" + Integer.toString(i));
                String value = null;
                Set<String> existingValues = values.get(dataProperty);

                if (binding != null) {
                    value = binding.getValue().stringValue();
                }

                if (existingValues == null) {
                    values.put(dataProperty, new HashSet<String>());
                }

                if (value != null) {
                    values.get(dataProperty).add(value);
                }
            }
        }

        return count;
    }

    public int calculateDistinctValues(Cursor cursor, List<String> dataProperties) {
        Map<String, Set<String>> values = new HashMap<String, Set<String>>();
        int count = 0;

        try {
            for (long row = cursor.open(); row != 0; row = cursor.advance()) {
                count += 1;

                for (int i = 0; i < dataProperties.size(); i++) {
                    String dataProperty = dataProperties.get(i);
                    String value = cursor.getResourceValue(i).m_lexicalForm;

                    Set<String> existingValues = values.get(dataProperty);
                    if (existingValues == null) {
                        values.put(dataProperty, new HashSet<String>());
                    }

                    if (!value.equals("")) {
                        values.get(dataProperty).add(value);
                    }
                }
            }
        } catch (JRDFoxException e) {
            e.printStackTrace();
        }

        return count;
    }

    // Construct sparql query that fetches all possible values for any data
    // property.
    public String construtQuery(String root, List<String> dataProperties) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT");

        for (int i = 0; i < dataProperties.size(); i++) {
            sb.append(" ?v" + Integer.toString(i) + " ");
        }

        sb.append(" WHERE {\n");
        sb.append("?v <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <" + root + ">.\n");

        // Adding one optional triple for each of the local data properties.
        for (int i = 0; i < dataProperties.size(); i++) {
            sb.append("OPTIONAL { ?v <" + dataProperties.get(i) + "> ?v" + Integer.toString(i) + ". }\n");
        }

        sb.append("}");

        return sb.toString();
    }
}
