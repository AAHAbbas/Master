package com.search.model;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.rdf4j.query.BindingSet;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.search.core.ConceptConfiguration;
import com.search.core.EndpointDataset;
import com.search.core.VqsQuery;
import com.search.graph.ConceptVariable;
import com.search.graph.DatatypeVariable;
import com.search.graph.LabeledEdge;
import com.search.graph.Variable;
import com.search.types.Constants;
import com.search.types.DataType;
import com.search.types.Field;
import com.search.utils.Filter;
import com.search.utils.ESService;

import uk.ac.ox.cs.JRDFox.store.DataStore;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The default ES Facet Index Model where the index is stored in ES
// This index uses full columns on all the variables.
public class ESFacetIndexModel extends FacetIndexModel {
    private ESService service;
    private static final Logger LOGGER = LogManager.getLogger(ESFacetIndexModel.class);
    private Map<String, ArrayList<Field>> fieldsInIndex = new HashMap<>();

    public ESFacetIndexModel() {
        this.service = new ESService();
    }

    // For each listed class, we create a new table with data from the source
    // endpoint. The properties of each concept is tagged with the type, but if the
    // type is "objectProperty", then we use the VARCHAR type, and we just use the
    // name of the related object in the cell.
    @Override
    public int constructFacetIndex(EndpointDataset dataset, Set<ConceptConfiguration> configs,
            DataStore store) {
        // Just to have something to return. We need to rewrite this so that it only
        // uses one concept config
        int numberOfDocuments = 0;
        LOGGER.info("Start constructing the facet index");
        LOGGER.debug("Concepts to include in the index:");

        // For each concept, construct an index
        for (ConceptConfiguration config : configs) {
            String indexName = config.getId();
            Variable root = config.getRoot();
            String type = root.getType();

            LOGGER.debug(config.getRoot().getType());
            LOGGER.info("Start constructing facet index for " + type);

            // Get an ordered list of all the variables in the conceptConfig
            List<Variable> variables = config.getVariableOrderingList();

            service.deleteIndex(indexName);

            // Create an index with fields that are defined in the concept configuration
            ArrayList<Field> fields = new ArrayList<Field>();

            for (int i = 0; i < variables.size(); i++) {
                Variable variable = variables.get(i);
                DataType fieldType = DataType.TEXT;

                if (variable.getType().equals("float") || variable.getType().equals("decimal"))
                    fieldType = DataType.FLOAT;

                if (variable.getType().equals("double"))
                    fieldType = DataType.DOUBLE;

                if (variable.getType().equals("long"))
                    fieldType = DataType.LONG;

                if (variable.getType().equals("integer") || variable.getType().equals("int"))
                    fieldType = DataType.INTEGER;

                if (variable.getType().equals("boolean"))
                    fieldType = DataType.BOOLEAN;

                if (variable.getType().equals("dateTime") || variable.getType().equals("date"))
                    fieldType = DataType.DATETIME;

                if (variable instanceof ConceptVariable)
                    fieldType = DataType.BOOLEAN;

                fields.add(new Field(i, fieldType));
            }

            fieldsInIndex.put(indexName, fields);
            service.createIndex(indexName, fields);

            String query = buildQuery(config, variables, root);

            LOGGER.debug("SPARQL query used to fetch data for an index:\n" + query);

            LOGGER.info("Running SPARQL query to fetch data ...");
            List<BindingSet> data = dataset.runQuery(query);
            LOGGER.info("Done running query over dataset");

            LOGGER.debug("Source endpoint returned " + data.size() + " documents in total");
            LOGGER.debug("Number of fields in the index: " + variables.size());

            LOGGER.info("Adding documents to index [" + indexName + "]");
            service.addDocuments(indexName, data, variables.size());
            LOGGER.info("Added documents to the index " + type);

            numberOfDocuments += data.size();

            LOGGER.info("Done creating index for concept: " + type + ". Index/Config id: "
                    + indexName + ". " + variables.size() + " fields and " + data.size() + " documents");
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

    // Executes the a vqs query over the cache. It returns a map containing distinct
    // facet values for each local facet.
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Set<String>> executeAbstractQuery(VqsQuery abstractQuery, Set<ConceptConfiguration> configs)
            throws SQLException, IOException {
        String root = abstractQuery.getRoot().getType();

        // Find the right concept configuration. Assuming that it exists in the ccs
        // TODO: set executeAbstractQuery to only have one config
        ConceptConfiguration config = null;

        for (ConceptConfiguration c : configs) {
            if (c.getRoot().getType().equals(root)) {
                config = c;
                continue;
            }
        }

        if (config == null) {
            LOGGER.error("Couldn't find common root in both the abstract query and concept configuration");
            return null;
        }

        String indexName = config.getId();
        LOGGER.info("Calculating the homomorphic map");

        // Find the largest mapping from a subset of variables in the VQSQuery to the
        // variables of the concept configuration.
        Map<Variable, Variable> homomorphicMap = getMapping(abstractQuery, config);

        // Get a list of all the variables in the config.
        List<Variable> variables = config.getVariableOrderingList();
        Map<Variable, String> localVariables = config.getLocalVariables();

        LOGGER.debug("Finding local attributes (size = " + localVariables.size() + "): " + localVariables.values());

        BoolQuery query = buildQuery(abstractQuery, config, homomorphicMap);
        List<Hit<Test>> result = service.search(indexName, query);
        // TODO: Find another solution to large Test data model

        // Turn the results into a map object, which will be returned
        Map<String, Set<String>> propertyValues = new HashMap<String, Set<String>>();
        int fields = fieldsInIndex.get(indexName).size();
        int documents = 0;

        // Loop over the results, and record each distinct value for each of the
        // properties/attributes. I do not know how heavy it is to do this filtering,
        // but it has to be done.
        for (Hit<Test> hit : result) {
            HashMap<String, String> data = new ObjectMapper().convertValue(hit.source(), HashMap.class);
            documents += 1;

            for (int i = 0; i < fields; i++) {
                String fieldName = fieldsInIndex.get(indexName).get(i).name;
                int fieldIndex = Integer.parseInt(fieldName.substring(5, fieldName.length()));

                Variable correspondingCCVar = variables.get(fieldIndex);
                String attributeURI = localVariables.get(correspondingCCVar);

                // Add empty set if it does not exist yet.
                // TODO: Fix first attribute is always null Example:
                // "01:45:07.857 [main] INFO com.search.App.lambda$0() - null: [true, false]"
                if (propertyValues.get(attributeURI) == null) {
                    propertyValues.put(attributeURI, new HashSet<String>());
                }

                String value = data.get(fieldName);
                if (value != null) {
                    propertyValues.get(attributeURI).add(value);
                }
            }
        }

        LOGGER.info("Done running approx. query over the index [" + indexName + "]");
        LOGGER.info("Number of columns in the results: " + fields);
        LOGGER.info("Results contains " + documents + " documents");

        return propertyValues;
    }

    // Get the mapping from the abstract query to the concept config.
    private Map<Variable, Variable> getMapping(VqsQuery abstractQuery, ConceptConfiguration cc) {

        Map<Variable, Variable> map = new HashMap<Variable, Variable>(); // Map from query vars to config vars

        Variable queryRoot = abstractQuery.getRoot();
        ConceptVariable ccRoot = cc.getRoot();
        map.put(queryRoot, ccRoot);

        findMapRec(abstractQuery, cc, queryRoot, map);

        return map;
    }

    // Recursive helper function
    private void findMapRec(VqsQuery abstractQuery, ConceptConfiguration conceptConfiguration, Variable var,
            Map<Variable, Variable> map) {
        Variable ccVar = map.get(var); // Find corresponding cc var. This has to exist already if this method has been
                                       // called
        Set<LabeledEdge> localQueryEdges = abstractQuery.getGraph().outgoingEdgesOf(var);
        Set<LabeledEdge> localCcEdges = conceptConfiguration.getGraph().outgoingEdgesOf(ccVar);

        for (LabeledEdge lqe : localQueryEdges) {
            for (LabeledEdge lcce : localCcEdges) {
                Variable targetQueryVar = abstractQuery.getGraph().getEdgeTarget(lqe);
                Variable targetCCVar = conceptConfiguration.getGraph().getEdgeTarget(lcce);

                if (lcce.getLabel().equals(lqe.getLabel()) && targetQueryVar.getType().equals(targetCCVar.getType())
                        && !map.values().contains(targetCCVar)) {
                    map.put(targetQueryVar, targetCCVar);
                    findMapRec(abstractQuery, conceptConfiguration, targetQueryVar, map);

                    break;
                }
            }
        }
    }

    private BoolQuery buildQuery(VqsQuery abstractQuery, ConceptConfiguration config,
            Map<Variable, Variable> queryToConfigMap) {
        Map<Variable, String> variables = config.getLocalVariables();
        LOGGER.debug("Finding local attributes (size = " + variables.size() + "): " + variables.values());

        // Build a query to fetch data from the facet index.
        Builder query = QueryBuilders.bool();
        List<Query> filterQueries = new ArrayList<>();
        List<Query> notQueries = new ArrayList<>();

        for (Entry<Variable, Variable> entry : queryToConfigMap.entrySet()) {
            Variable variable = entry.getKey();
            String fieldName = Constants.FIELD_PREFIX + config.getVariableOrdering(queryToConfigMap.get(variable));

            // If we have a concept variable in the approximated query, we have to add a not
            // null filter
            if (variable instanceof ConceptVariable) {
                filterQueries.add(new Query.Builder()
                        .term(new TermQuery.Builder()
                                .field(fieldName)
                                .value(new FieldValue.Builder()
                                        .booleanValue(true)
                                        .build())
                                .build())
                        .build());
            }

            // TODO: Handle regex filters
            for (Filter filter : abstractQuery.getFiltersForVariable(variable)) {
                JsonData value = JsonData.of(filter.getValue().stringValue());
                co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery.Builder rangeQuery = new RangeQuery.Builder()
                        .field(fieldName);

                switch (filter.getOperator()) {
                    case LT:
                        filterQueries.add(new Query.Builder()
                                .range(rangeQuery.lt(value)
                                        .build())
                                .build());
                        break;
                    case LE:
                        filterQueries.add(new Query.Builder()
                                .range(rangeQuery.lte(value)
                                        .build())
                                .build());
                        break;
                    case GT:
                        filterQueries.add(new Query.Builder()
                                .range(rangeQuery.gt(value)
                                        .build())
                                .build());
                        break;
                    case GE:
                        filterQueries.add(new Query.Builder()
                                .range(rangeQuery.gte(value)
                                        .build())
                                .build());
                        break;
                    case EQ: // TODO: add support for different value types
                        filterQueries.add(new Query.Builder()
                                .term(new TermQuery.Builder()
                                        .field(fieldName)
                                        .value(new FieldValue.Builder()
                                                .stringValue(filter
                                                        .getValue()
                                                        .stringValue())
                                                .build())
                                        .build())
                                .build());
                        break;
                    case NE:
                        notQueries.add(new Query.Builder()
                                .term(new TermQuery.Builder()
                                        .field(fieldName)
                                        .value(new FieldValue.Builder()
                                                .stringValue(filter.getValue().stringValue())
                                                .build())
                                        .build())
                                .build());

                    default:
                        LOGGER.error("Invalid filter type '" + filter.getOperator().getSymbol() + "'");
                }
            }
        }

        if (!notQueries.isEmpty()) {
            query.must(notQueries);
        }

        if (!filterQueries.isEmpty()) {
            query.filter(filterQueries);
        }

        return query.build();
    }
}
