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

import com.search.core.ConceptConfiguration;
import com.search.core.EndpointDataset;
import com.search.core.VQSQuery;
import com.search.graph.ConceptVariable;
import com.search.graph.LabeledEdge;
import com.search.graph.Variable;
import com.search.utils.Filter;

import uk.ac.ox.cs.JRDFox.JRDFoxException;
import uk.ac.ox.cs.JRDFox.store.DataStore;
import uk.ac.ox.cs.JRDFox.store.Resource;
import uk.ac.ox.cs.JRDFox.store.TupleIterator;

// The perfect model perfectly calculates the possible remaining values for each data property, without any approximation.
// The model may use a lot of time with large input.
public class PerfectFacetIndexModel extends FacetIndexModel {
    DataStore store;

    public void constructFacetIndex(EndpointDataset datasource, Set<ConceptConfiguration> conceptConfigurations,
            DataStore store) throws SQLException, IOException {
        this.store = store;
        System.out.println(
                "The full facet index model does not need any index, so nothing is created. Only data source is stored.");
    }

    // Executes the a vqs query over the cache. It returns a map containing distinct
    // facet values for each local facet.
    public Map<String, Set<String>> executeAbstractQuery(VQSQuery vqsQuery, Set<ConceptConfiguration> ccs)
            throws IllegalArgumentException, Exception {
        System.out.println("Start calculate values for perfect model");
        String pivotConcept = vqsQuery.getRoot().getType();

        List<String> orderedLocalDataProperties = new ArrayList<String>(
                vqsQuery.getOntology().getDataProperties(pivotConcept));
        Collections.sort(orderedLocalDataProperties);

        // Constuct the full model query
        String perfectModelSparqlQuery = constructPerfectModelQuery(vqsQuery, orderedLocalDataProperties);
        System.out.println("Perfect model query");
        System.out.println(perfectModelSparqlQuery);

        // Running query over dataset
        TupleIterator tupleIterator = store.compileQuery(perfectModelSparqlQuery);
        System.out.println("rows has been calculated. Start to calculate distinct values.");

        // Get all the distinct values for each data property
        Map<String, Set<String>> values = calculateDistinctValuesRDFox(tupleIterator, orderedLocalDataProperties);
        System.out.println("Done calculating distinct values");
        tupleIterator.dispose();

        return values;
    }

    public Map<String, Set<String>> calculateDistinctValuesRDFox(TupleIterator tupleIterator,
            List<String> orderedLocalDataProperties) throws JRDFoxException, IOException {

        // Start to collect the distinct values
        Map<String, Set<String>> dataPropertyDistinctValues = new HashMap<String, Set<String>>();

        for (long multiplicityOrRow = tupleIterator.open(); multiplicityOrRow != 0; multiplicityOrRow = tupleIterator
                .advance()) {
            for (int termIndex = 0; termIndex < orderedLocalDataProperties.size(); termIndex++) {
                String dataProperty = orderedLocalDataProperties.get(termIndex);
                Resource resource = tupleIterator.getResource(termIndex);
                String value = resource.m_lexicalForm;

                Set<String> existingValues = dataPropertyDistinctValues.get(dataProperty);

                if (existingValues == null)
                    dataPropertyDistinctValues.put(dataProperty, new HashSet<String>());

                if (!value.equals(""))
                    dataPropertyDistinctValues.get(dataProperty).add(value);
            }
        }

        return dataPropertyDistinctValues;
    }

    public String constructPerfectModelQuery(VQSQuery vqsQuery, List<String> orderedLocalDataProperties)
            throws IOException {
        // Start to make the sparql query used by the perfect system.
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT");

        for (int i = 0; i < orderedLocalDataProperties.size(); i++)
            sb.append(" ?v" + Integer.toString(i) + " ");

        sb.append(" WHERE {\n");

        // Adding the concepts with types
        Set<Variable> vertices = vqsQuery.getGraph().vertexSet();

        for (Variable vertex : vertices) {
            if (vertex instanceof ConceptVariable) {
                sb.append("?" + vertex.getLabel() + " <"
                        + "http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <" + vertex.getType() + ">.\n");
            }
        }

        // Adding the edges. Remove inversed edges.
        Set<LabeledEdge> edges = vqsQuery.getGraph().edgeSet();

        for (LabeledEdge edge : edges) {
            Variable sourceVar = vqsQuery.getGraph().getEdgeSource(edge);
            Variable targetVar = vqsQuery.getGraph().getEdgeTarget(edge);

            if (edge.getLabel().endsWith("_inverseProp")) {
                sb.append(
                        "?" + targetVar.getLabel() + " <" + edge.getLabel().substring(0, edge.getLabel().length() - 12)
                                + "> ?" + sourceVar.getLabel() + ".\n");
            } else {
                sb.append("?" + sourceVar.getLabel() + " <" + edge.getLabel() + "> ?" + targetVar.getLabel() + ".\n");
            }
        }

        // Adding filters TODO: Rewrite this in a proper way. Fix the types.
        for (Variable variable : vqsQuery.getGraph().vertexSet()) {
            Set<Filter> filters = vqsQuery.getFiltersForVariable(variable);

            if (filters != null) {
                for (Filter f : filters) {
                    if (f.getOperator().toString().equals("EQ")) {
                        if (variable.getType().equals("integer")) {
                            sb.append("FILTER(?" + variable.getLabel() + " = " + f.getValue().stringValue() + ").\n");
                        } else {
                            sb.append(
                                    "FILTER(?" + variable.getLabel() + " = \"" + f.getValue().stringValue() + "\").\n");
                        }
                    } else {
                        sb.append("FILTER(?" + variable.getLabel());

                        if (f.getOperator().toString().equals("LE"))
                            sb.append(" <= ");

                        if (f.getOperator().toString().equals("GE"))
                            sb.append(" >= ");

                        sb.append(f.getValue().stringValue() + ").\n");
                    }
                }
            }
        }

        // Adding the optionals for each of the local data properties
        for (int i = 0; i < orderedLocalDataProperties.size(); i++) {
            sb.append("OPTIONAL { ?" + vqsQuery.getRoot().getLabel() + " <" + orderedLocalDataProperties.get(i) + "> ?v"
                    + Integer.toString(i) + ". }\n");
        }

        sb.append("}");
        System.out.println("Done with perfect query;");
        System.out.println(sb.toString());

        return sb.toString();
    }
}
