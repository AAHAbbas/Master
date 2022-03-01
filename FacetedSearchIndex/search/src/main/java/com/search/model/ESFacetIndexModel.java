package com.search.model;

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
import co.elastic.clients.elasticsearch._types.query_dsl.ExistsQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.WildcardQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import tech.oxfordsemantic.jrdfox.client.Cursor;

import com.search.core.ConceptConfiguration;
import com.search.core.EndpointDataset;
import com.search.core.RDFoxDataset;
import com.search.core.VQSQuery;
import com.search.graph.ConceptVariable;
import com.search.graph.DatatypeVariable;
import com.search.graph.LabeledEdge;
import com.search.graph.Variable;
import com.search.types.Constants;
import com.search.types.DataType;
import com.search.types.Field;
import com.search.utils.Filter;
import com.search.utils.ESService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The default ES Facet Index Model where the index is stored in ES
public class ESFacetIndexModel extends FacetIndexModel {
    private ESService service;
    private static final Logger LOGGER = LogManager.getLogger(ESFacetIndexModel.class);
    private Map<String, ArrayList<Field>> fieldsInIndex = new HashMap<>();

    public ESFacetIndexModel() {
        this.service = new ESService();
    }

    // For each listed class, we create a new index with data from the source
    // endpoint. The properties of each concept is tagged with the type, but if the
    // type is "objectProperty", then we use the VARCHAR type, and we just use the
    // name of the related object in the cell
    @Override
    public void constructFacetIndex(EndpointDataset dataset, Set<ConceptConfiguration> configs,
            RDFoxDataset rdfoxDataset) {
        LOGGER.info("Start constructing the facet index");
        LOGGER.debug("Concepts to include in the index:");

        if (dataset == null && rdfoxDataset == null) {
            LOGGER.error("No dataset provided");
            return;
        }

        // For each concept, construct an index
        for (ConceptConfiguration config : configs) {
            String indexName = config.getId();
            Variable root = config.getRoot();
            String type = root.getType();

            LOGGER.debug(config.getRoot().getType());
            LOGGER.info("Start constructing facet index for " + type);

            // Get an ordered list of all the variables in the config
            List<Variable> variables = config.getVariables();

            service.deleteIndex(indexName);

            // Create an index with fields that are defined in the concept configuration
            ArrayList<Field> fields = new ArrayList<>();

            for (int i = 0; i < variables.size(); i++) {
                Variable variable = variables.get(i);
                DataType fieldType = DataType.KEYWORD;

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
                    fieldType = DataType.KEYWORD;

                fields.add(new Field(i, fieldType));
            }

            fieldsInIndex.put(indexName, fields);
            service.createIndex(indexName, fields);

            String query = buildQuery(config, variables, root);

            LOGGER.debug("SPARQL query used to fetch data for an index:\n" + query);
            LOGGER.info("Running SPARQL query to fetch data ...");

            int documents = 0;
            List<BindingSet> data;
            Cursor cursor;

            if (dataset != null) {
                data = dataset.runQuery(query);
                documents = data.size();
                LOGGER.info("Done running query over dataset");
                LOGGER.info("Adding documents to index [" + indexName + "]");
                service.addDocuments(indexName, data, variables.size());
            } else {
                cursor = rdfoxDataset.runQuery(query);

                if (cursor == null) {
                    LOGGER.error("Failed to execute a query over the datastore");
                    return;
                }

                LOGGER.info("Done running query over dataset");
                LOGGER.info("Adding documents to index [" + indexName + "]");
                documents = service.addDocuments(indexName, cursor, variables.size());
            }

            LOGGER.info("Done creating index for concept: " + type + ". Index/Config id: "
                    + indexName + ". " + variables.size() + " fields and " + documents + " documents");
        }
    }

    // Executes the a vqs query over the cache. It returns a map containing distinct
    // facet values for each local facet
    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public int executeAbstractQuery(VQSQuery abstractQuery, ConceptConfiguration config) {
        if (!config.getRoot().getType().equals(abstractQuery.getRoot().getType())) {
            LOGGER.error("Couldn't find common root in both the abstract query and concept configuration");
            return 0;
        }

        String indexName = config.getId();
        LOGGER.info("Calculating the homomorphic map");

        // Find the largest mapping from a subset of variables in the VQSQuery to the
        // variables of the concept configuration
        Map<Variable, Variable> homomorphicMap = getMapping(abstractQuery, config);

        // Get a list of all the variables in the config
        List<Variable> variables = config.getVariables();
        Map<Variable, String> localVariables = config.getDataPropertyVariables();

        LOGGER.debug("Finding local attributes (size = " + localVariables.size() + "): " + localVariables.values());

        BoolQuery query = buildQuery(abstractQuery, config, homomorphicMap);

        List<Hit<HashMap>> result = service.search(indexName, query);

        // Turn the results into a map object, which will be returned
        Map<String, Set<String>> properties = new HashMap<>();
        int fields = fieldsInIndex.get(indexName).size();
        int documents = 0;

        // Loop over the results, and record each distinct value for each of the
        // properties/attributes. I do not know how heavy it is to do this filtering,
        // but it has to be done
        for (Hit<HashMap> hit : result) {
            HashMap<String, String> data = hit.source();
            documents += 1;

            for (int i = 0; i < fields; i++) {
                String fieldName = fieldsInIndex.get(indexName).get(i).name;
                int fieldIndex = Integer
                        .parseInt(fieldName.substring(Constants.FIELD_PREFIX.length(), fieldName.length()));

                Variable correspondingCCVar = variables.get(fieldIndex);
                String attributeURI = localVariables.get(correspondingCCVar);

                // TODO: Current solution is to skip concept variables, not sure if this
                // is okay?? Concept variables are always set to true in the query
                if (attributeURI != null) {
                    if (properties.get(attributeURI) == null) {
                        properties.put(attributeURI, new HashSet<String>());
                    }

                    String value = data.get(fieldName);
                    if (value != null) {
                        properties.get(attributeURI).add(value);
                    }
                }
            }
        }

        LOGGER.info("Done running approx. query over the index [" + indexName + "]");
        LOGGER.info("Number of columns in the results: " + fields);
        LOGGER.info("Results contains " + documents + " documents");

        return documents;
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
    // the data we want to index
    private Object addWhereClauses(ConceptConfiguration config, Variable variable) {
        StringBuilder sb = new StringBuilder();
        sb.append("?C_" + variable.getLabel() + " rdf:type <" + variable.getType() + ">.\n");

        for (LabeledEdge edge : config.getGraph().outgoingEdgesOf(variable)) {
            Variable target = config.getGraph().getEdgeTarget(edge);

            if (target instanceof ConceptVariable) {
                sb.append("OPTIONAL {\n");

                if (edge.getLabel().endsWith("_inverseProp")) {
                    sb.append("?C_" + target.getLabel() + " <"
                            + edge.getLabel().substring(0, edge.getLabel().length() - 12) + "> ?C_"
                            + variable.getLabel() + ".\n");
                } else {
                    sb.append("?C_" + variable.getLabel() + " <" + edge.getLabel() + "> ?C_"
                            + target.getLabel() + ".\n");
                }

                sb.append(addWhereClauses(config, target));
                sb.append("}\n");
            }

            if (target instanceof DatatypeVariable) {
                sb.append("OPTIONAL { ?C_" + variable.getLabel() + " <" + edge.getLabel() + "> ?A_"
                        + target.getLabel() + ". }\n");
            }
        }

        return sb.toString();
    }

    // Get the mapping from the abstract query to the concept config
    private Map<Variable, Variable> getMapping(VQSQuery abstractQuery, ConceptConfiguration config) {
        Map<Variable, Variable> mapping = new HashMap<>(); // Map from query vars to config vars

        Variable queryRoot = abstractQuery.getRoot();
        ConceptVariable configRoot = config.getRoot();
        mapping.put(queryRoot, configRoot);

        createMapping(abstractQuery, config, queryRoot, configRoot, mapping);

        return mapping;
    }

    // Recursive helper function which creates mapping between the abstract query
    // and the concept config
    private void createMapping(VQSQuery abstractQuery, ConceptConfiguration config, Variable queryVariable,
            Variable configVariable, Map<Variable, Variable> mapping) {
        Set<LabeledEdge> queryEdges = abstractQuery.getGraph().outgoingEdgesOf(queryVariable);
        Set<LabeledEdge> configEdges = config.getGraph().outgoingEdgesOf(configVariable);

        for (LabeledEdge qEdge : queryEdges) {
            for (LabeledEdge cEdge : configEdges) {
                Variable targetQueryVariable = abstractQuery.getGraph().getEdgeTarget(qEdge);
                Variable targetConfigVariable = config.getGraph().getEdgeTarget(cEdge);

                if (cEdge.getLabel().equals(qEdge.getLabel())
                        && targetQueryVariable.getType().equals(targetConfigVariable.getType())
                        && !mapping.values().contains(targetConfigVariable)) {
                    mapping.put(targetQueryVariable, targetConfigVariable);
                    createMapping(abstractQuery, config, targetQueryVariable, targetConfigVariable, mapping);

                    break;
                }
            }
        }
    }

    // Build the query to run over the index
    private BoolQuery buildQuery(VQSQuery abstractQuery, ConceptConfiguration config,
            Map<Variable, Variable> queryToConfigMap) {
        Map<Variable, String> variables = config.getDataPropertyVariables();
        LOGGER.debug("Finding local attributes (size = " + variables.size() + "): " + variables.values());

        // Build a query to fetch data from the facet index.
        Builder query = QueryBuilders.bool();
        List<Query> filterQueries = new ArrayList<>();
        List<Query> notQueries = new ArrayList<>();

        for (Entry<Variable, Variable> entry : queryToConfigMap.entrySet()) {
            Variable variable = entry.getKey();
            String fieldName = Constants.FIELD_PREFIX + config.getVariableOrder(queryToConfigMap.get(variable));

            // If we have a concept variable in the approximated query, we have to add a not
            // null filter
            if (variable instanceof ConceptVariable) {
                filterQueries.add(new Query.Builder()
                        .term(new TermQuery.Builder()
                                .field(fieldName)
                                .value(new FieldValue.Builder()
                                        .stringValue("true")
                                        .build())
                                .build())
                        .build());
            }

            if (variable instanceof DatatypeVariable) {
                filterQueries.add(new Query.Builder()
                        .exists(new ExistsQuery.Builder()
                                .field(fieldName)
                                .build())
                        .build());
            }

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
                    case EQ:
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
                        break;
                    case REG:
                        filterQueries.add(new Query.Builder()
                                .wildcard(new WildcardQuery.Builder()
                                        .field(fieldName)
                                        .value("*" + value + "*")
                                        .build())
                                .build());
                        break;
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

    public void closeConnection() {
        service.closeConnection();
    }
}
