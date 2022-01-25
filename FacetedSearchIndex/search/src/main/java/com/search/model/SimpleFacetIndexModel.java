package com.search.model;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;

import com.search.core.ConceptConfiguration;
import com.search.core.EndpointDataset;
import com.search.core.VqsQuery;
import uk.ac.ox.cs.JRDFox.store.DataStore;

// The all values facet index model. It calculates all the possible values given in the data. 
public class SimpleFacetIndexModel extends FacetIndexModel {

    private EndpointDataset dataset; // Store the sourceEndpoint and use it when executing query over model.

    public int constructFacetIndex(EndpointDataset datasource, Set<ConceptConfiguration> conceptConfigurations,
            DataStore store) throws SQLException, IOException {
        this.dataset = datasource;
        System.out.println(
                "The full facet index model does not need any index, so nothing is created. Only data source is stored.");

        return 0;
    }

    // Executes the a vqs query over the cache. It returns a map containing distinct
    // facet values for each local facet.
    public Map<String, Set<String>> executeAbstractQuery(VqsQuery vqsQuery, Set<ConceptConfiguration> ccs)
            throws IllegalArgumentException, Exception {

        String pivotConcept = vqsQuery.getRoot().getType();

        List<String> orderedLocalDataProperties = new ArrayList<String>(
                vqsQuery.getOntology().getDataProperties(pivotConcept));
        Collections.sort(orderedLocalDataProperties);

        // Constuct the full model query
        String fullModelSparqlQuery = constructFullModelQuery(pivotConcept, orderedLocalDataProperties);
        System.out.println("sparql query used to calculate simple data model.");
        System.out.println(fullModelSparqlQuery);

        // Running query over dataset
        List<BindingSet> rows = this.dataset.runQuery(fullModelSparqlQuery);
        System.out.println("Rows number " + rows.size());

        // Get all the distinct values for each data property
        Map<String, Set<String>> values = calculateDistinctValues(rows, orderedLocalDataProperties);

        return values;
    }

    // Calculate distinct values for each property using the results and a map from
    // the results
    // An alternative is to construct one smaller query for each of the properties.
    public Map<String, Set<String>> calculateDistinctValues(List<BindingSet> rows,
            List<String> orderedLocalDataProperties) throws IOException {
        // Start to collect the distinct values
        Map<String, Set<String>> dataPropertyDistinctValues = new HashMap<String, Set<String>>();

        for (BindingSet tuple : rows) {
            for (int i = 0; i < orderedLocalDataProperties.size(); i++) {
                String dataProperty = orderedLocalDataProperties.get(i);
                String variableName = "v" + Integer.toString(i);
                Binding b = tuple.getBinding(variableName);
                String value = null;

                if (b != null)
                    value = b.getValue().stringValue();

                Set<String> existingValues = dataPropertyDistinctValues.get(dataProperty);

                if (existingValues == null)
                    dataPropertyDistinctValues.put(dataProperty, new HashSet<String>());

                if (value != null)
                    dataPropertyDistinctValues.get(dataProperty).add(value);
            }
        }

        return dataPropertyDistinctValues;
    }

    public String constructFullModelQuery(String pivotConcept, List<String> orderedLocalDataProperties) {
        // Start to construct the sparql query that fetches all possible values for any
        // data property.
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT");

        for (int i = 0; i < orderedLocalDataProperties.size(); i++)
            sb.append(" ?v" + Integer.toString(i) + " ");

        sb.append(" WHERE {\n");
        sb.append("?v <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <" + pivotConcept + ">.\n");

        // Adding one optional triple for each of the local data properties.
        for (int i = 0; i < orderedLocalDataProperties.size(); i++)
            sb.append("OPTIONAL { ?v <" + orderedLocalDataProperties.get(i) + "> ?v" + Integer.toString(i) + ". }\n");

        sb.append("}");

        return sb.toString();
    }
}
