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
import com.search.graph.ConceptVariable;
import com.search.graph.LabeledEdge;
import com.search.graph.Variable;
import com.search.utils.Filter;

// The perfect model perfectly calculates the possible remaining values for each data property, without any approximation.
// The model may use a lot of time with large input.
public class PerfectFacetIndexModel extends FacetIndexModel {
    private EndpointDataset dataset;
    private RDFoxDataset rdfoxDataset;

    @Override
    public void constructFacetIndex(EndpointDataset datasource, Set<ConceptConfiguration> conceptConfigurations,
            RDFoxDataset rdfoxDataset) {
        this.dataset = datasource;
        this.rdfoxDataset = rdfoxDataset;
    }

    // Executes the a vqs query over the cache. It returns a map containing distinct
    // facet values for each local facet.
    @Override
    public Map<String, Set<String>> executeAbstractQuery(VQSQuery vqsQuery, ConceptConfiguration config) {
        String root = vqsQuery.getRoot().getType();
        List<String> dataProperties = new ArrayList<String>(vqsQuery.getOntology().getDataProperties(root));
        Collections.sort(dataProperties);
        String sparqlQuery = constructQuery(vqsQuery, dataProperties);

        if (dataset != null) {
            return calculateDistinctValues(dataset.runQuery(sparqlQuery), dataProperties);
        } else {
            return calculateDistinctValues(rdfoxDataset.runQuery(sparqlQuery), dataProperties);
        }
    }

    // Calculate distinct values for each property using the results and a map from
    // the results
    public Map<String, Set<String>> calculateDistinctValues(List<BindingSet> rows, List<String> dataProperties) {
        Map<String, Set<String>> values = new HashMap<String, Set<String>>();

        for (BindingSet row : rows) {
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

        return values;
    }

    public Map<String, Set<String>> calculateDistinctValues(Cursor cursor, List<String> dataProperties) {
        Map<String, Set<String>> values = new HashMap<String, Set<String>>();

        try {
            for (long row = cursor.open(); row != 0; row = cursor.advance()) {
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

        return values;
    }

    public String constructQuery(VQSQuery vqsQuery, List<String> orderedLocalDataProperties) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT");

        for (int i = 0; i < orderedLocalDataProperties.size(); i++) {
            sb.append(" ?v" + Integer.toString(i) + " ");
        }

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

            if (edge.getLabel().endsWith("_inverseProp"))
                sb.append(
                        "?" + targetVar.getLabel() + " <" + edge.getLabel().substring(0, edge.getLabel().length() - 12)
                                + "> ?" + sourceVar.getLabel() + ".\n");
            else
                sb.append("?" + sourceVar.getLabel() + " <" + edge.getLabel() + "> ?" + targetVar.getLabel() + ".\n");
        }

        // Adding filters
        for (Variable variable : vqsQuery.getGraph().vertexSet()) {
            Set<Filter> filters = vqsQuery.getFiltersForVariable(variable);

            if (filters != null) {
                for (Filter f : filters) {
                    if (f.getOperator().toString().equals("EQ")) {
                        sb.append("FILTER(?" + variable.getLabel() + " = \"" + f.getValue().stringValue() + "\").\n");
                    } else {
                        sb.append("FILTER(?" + variable.getLabel());

                        if (f.getOperator().toString().equals("LE")) {
                            sb.append(" <= ");
                        }

                        if (f.getOperator().toString().equals("GE")) {
                            sb.append(" >= ");
                        }

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

        return sb.toString();
    }
}
