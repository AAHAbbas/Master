package com.search.model;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.rdf4j.query.BindingSet;

import com.search.core.ConceptConfiguration;
import com.search.core.EndpointDataset;
import com.search.core.VqsQuery;
import com.search.graph.ConceptVariable;
import com.search.graph.DatatypeVariable;
import com.search.graph.LabeledEdge;
import com.search.graph.Variable;
import com.search.types.Field;
import com.search.utils.ESService;

import uk.ac.ox.cs.JRDFox.JRDFoxException;
import uk.ac.ox.cs.JRDFox.store.DataStore;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The default ES Facet Index Model where the index is stored in ES
// This index uses full columns on all the variables.
public class ESFacetIndexModel extends FacetIndexModel {
    ESService service;
    private static final Logger LOGGER = LogManager.getLogger(ESFacetIndexModel.class);

    public ESFacetIndexModel() {
        this.service = new ESService();
    }

    // For each listed class, we create a new table with data from the source
    // endpoint.
    // The properties of each concept is tagged with the type, but if the type is
    // "objectProperty", then we use the VARCHAR type, and we just use the name of
    // the related object in the cell.
    @Override
    public int constructFacetIndex(EndpointDataset dataset, Set<ConceptConfiguration> configs,
            DataStore store) throws SQLException, IOException, JRDFoxException {

        // Just to have something to return. We need to rewrite this so that it only
        // uses one concept config
        int numberOfDocuments = 0;
        LOGGER.info("Start constructing the facet index");
        LOGGER.debug("Concepts to include in the index:");

        for (ConceptConfiguration config : configs) {
            LOGGER.debug(config.getRoot().getType());
        }

        // For each concept, construct an index
        for (ConceptConfiguration config : configs) {
            String indexName = config.getId();
            Variable root = config.getRoot();
            String type = root.getType();

            LOGGER.info("Start constructing facet index for " + type);

            // Get an ordered list of all the variables in the conceptConfig
            List<Variable> variables = config.getVariableOrderingList();

            service.deleteIndex(indexName);

            // Create an index with fields that are defined in the concept configuration
            ArrayList<Field> fields = new ArrayList<Field>();

            for (int i = 0; i < variables.size(); i++) {
                Variable variable = variables.get(i);
                String fieldType = "text"; // Default field type

                if (variable.getType().equals("decimal"))
                    fieldType = "float";

                if (variable.getType().equals("boolean"))
                    fieldType = "boolean";

                if (variable.getType().equals("integer"))
                    fieldType = "integer";

                if (variable instanceof ConceptVariable)
                    fieldType = "boolean";

                fields.add(new Field("field" + i, fieldType));
            }

            service.createIndex(indexName, fields);

            String query = buildQuery(config, variables, root);

            LOGGER.debug("SPARQL query used to fetch data for an index:\n" + query);

            LOGGER.info("Running SPARQL query to fetch data ...");
            List<BindingSet> queryResult = dataset.runQuery(query);
            LOGGER.info("Done running query over dataset");

            LOGGER.debug("Source endpoint returned " + queryResult.size() + " documents in total");
            LOGGER.debug("Number of fields in the index: " + variables.size());

            LOGGER.info("Adding documents to index [" + indexName + "]");
            service.addDocuments(indexName, queryResult, variables);
            LOGGER.info("Added documents to the index " + type);

            numberOfDocuments += queryResult.size();

            LOGGER.info("Done creating index for concept: " + type + ". Index/Config id: "
                    + indexName + ". " + variables.size() + " fields and " + numberOfDocuments + " documents");
        }

        return numberOfDocuments;
    }

    // Create a SPARQL query to fetch all the data for an index
    private String buildQuery(ConceptConfiguration config, List<Variable> variables, Variable root) {
        StringBuilder query = new StringBuilder();
        query.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        query.append("PREFIX npdv: <http://sws.ifi.uio.no/vocab/npd-v2#>\n");
        query.append("SELECT DISTINCT ");

        for (int i = 0; i < variables.size(); i++) {
            Variable variable = variables.get(i);

            if (variable instanceof ConceptVariable)
                // Use this if we want boolean values. RDFox does not support bound. Uppercase
                // bound actually works!!
                query.append("(BOUND(?C_" + variable.getLabel() + ") as ?o" + i + ") ");

            if (variable instanceof DatatypeVariable)
                query.append("(?A_" + variable.getLabel() + " as ?o" + i + ") ");
        }

        query.append("\n");
        query.append("WHERE {\n");
        query.append(addWhereClauses(config, root));
        query.append("}");

        return query.toString();
    }

    // Recursive function which is used to construct the SPARQL query that fetches
    // the data we want to index.
    private Object addWhereClauses(ConceptConfiguration conceptConfig, Variable currentVariable) {
        StringBuilder sb = new StringBuilder();
        sb.append("?C_" + currentVariable.getLabel() + " rdf:type <" + currentVariable.getType() + ">.\n");

        for (LabeledEdge edge : conceptConfig.getGraph().outgoingEdgesOf(currentVariable)) {
            Variable targetVariable = conceptConfig.getGraph().getEdgeTarget(edge);

            if (targetVariable instanceof ConceptVariable) {
                sb.append("OPTIONAL {\n");

                if (edge.getLabel().endsWith("_inverseProp")) {
                    sb.append("?C_" + targetVariable.getLabel() + " <"
                            + edge.getLabel().substring(0, edge.getLabel().length() - 12) + "> ?C_"
                            + currentVariable.getLabel() + ".\n");
                } else {
                    sb.append("?C_" + currentVariable.getLabel() + " <" + edge.getLabel() + "> ?C_"
                            + targetVariable.getLabel() + ".\n");
                }

                sb.append(addWhereClauses(conceptConfig, targetVariable));
                sb.append("}\n");
            }

            if (targetVariable instanceof DatatypeVariable) {
                sb.append("OPTIONAL { ?C_" + currentVariable.getLabel() + " <" + edge.getLabel() + "> ?A_"
                        + targetVariable.getLabel() + ". }\n");
            }
        }

        return sb.toString();
    }

    @Override
    public Map<String, Set<String>> executeAbstractQuery(VqsQuery vqsQuery,
            Set<ConceptConfiguration> conceptConfiguration)
            throws SQLException, IOException, IllegalArgumentException, Exception {
        return null;
    }
}
