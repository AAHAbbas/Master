package com.search.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.search.repository.ElasticSearchRepository;
import com.search.types.Constants;
import com.search.types.DataType;
import com.search.types.Field;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.query.BindingSet;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.FieldSort;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch._types.mapping.BooleanProperty;
import co.elastic.clients.elasticsearch._types.mapping.DateProperty;
import co.elastic.clients.elasticsearch._types.mapping.DoubleNumberProperty;
import co.elastic.clients.elasticsearch._types.mapping.FloatNumberProperty;
import co.elastic.clients.elasticsearch._types.mapping.IntegerNumberProperty;
import co.elastic.clients.elasticsearch._types.mapping.KeywordProperty;
import co.elastic.clients.elasticsearch._types.mapping.LongNumberProperty;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TextProperty;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping.Builder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.ClosePointInTimeRequest;
import co.elastic.clients.elasticsearch.core.ClosePointInTimeResponse;
import co.elastic.clients.elasticsearch.core.OpenPointInTimeRequest;
import co.elastic.clients.elasticsearch.core.OpenPointInTimeResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.core.bulk.IndexOperation;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.PointInTimeReference;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.PutMappingRequest;
import co.elastic.clients.elasticsearch.indices.PutMappingResponse;
import co.elastic.clients.util.ObjectBuilder;

import tech.oxfordsemantic.jrdfox.client.Cursor;
import tech.oxfordsemantic.jrdfox.exceptions.JRDFoxException;

public class ESService {
    private static final Logger LOGGER = LogManager.getLogger(ESService.class);
    private ElasticSearchRepository repo;

    public ESService() {
        repo = new ElasticSearchRepository();
    }

    // Create an index by specifying index name and fields
    // TODO: Add settings later
    public void createIndex(String indexName, ArrayList<Field> fields) {
        TypeMapping mapping = createMapping(fields);
        CreateIndexRequest request = new CreateIndexRequest.Builder()
                .index(indexName)
                .mappings(mapping)
                .build();

        try {
            CreateIndexResponse response = repo.createIndex(request);
            LOGGER.info("Successfully created an index: " + response.index());
        } catch (ElasticsearchException | IOException e) {
            LOGGER.error("An exception occured when trying to create an ES index");
            e.printStackTrace();
        }
    }

    // Delete an index by specifying index name
    public void deleteIndex(String indexName) {
        try {
            repo.deleteIndex(indexName);
            LOGGER.info("Successfully deleted index [" + indexName + "]");
        } catch (ElasticsearchException | IOException e) {
            if (e.getMessage().contains("index_not_found_exception")) {
                LOGGER.error("Cannot delete index [" + indexName + "] because it doesn't exists");
            } else {
                e.printStackTrace();
            }
        }
    }

    // Add field to an already exsisting index by specifying index name and a field
    public void addFieldToIndex(String indexName, Field field) {
        ObjectBuilder<Property> property = getProperty(field.type);

        if (property == null) {
            LOGGER.error("Invalid property, failed to add a field [" + field.name + "] to index [" + indexName + "]");
            return;
        }

        PutMappingRequest request = new PutMappingRequest.Builder()
                .index(Arrays.asList(indexName))
                .properties(field.name, property.build()).build();

        try {
            PutMappingResponse response = repo.updateMapping(request);
            LOGGER.info("Successfully updated the mapping of index [" + indexName + "]:" + response);
        } catch (ElasticsearchException | IOException e) {
            LOGGER.error("An exception occured when trying to update the mapping to index [" + indexName + "]");
            e.printStackTrace();
        }
    }

    // Add documents to an index by specifying index name, list of documents to add
    // and number of fields in the index
    public void addDocuments(String indexName, List<BindingSet> data, int numOfVariables) {
        List<BulkOperation> body = new ArrayList<>();

        for (int i = 0; i < data.size(); i++) {
            Map<String, Object> documents = new HashMap<>();
            BindingSet document = data.get(i);

            for (int j = 0; j < numOfVariables; j++) {
                String bindingName = "o" + Integer.toString(j);

                if (document.getBinding(bindingName) != null) {
                    documents.put(Constants.FIELD_PREFIX + j,
                            document.getBinding(bindingName).getValue().stringValue());
                }
            }

            body.add(new BulkOperation.Builder()
                    .index(new IndexOperation.Builder<Map<String, Object>>()
                            .index(indexName)
                            .document(documents)
                            .build())
                    .build());
        }

        BulkRequest request = new BulkRequest.Builder()
                .operations(body)
                .build();

        try {
            repo.bulkIndex(request);
            LOGGER.info("Successfully added documents to index [" + indexName + "]");
        } catch (ElasticsearchException | IOException e) {
            LOGGER.error("Cannot add documents to index [" + indexName + "]");
            e.printStackTrace();
        }
    }

    // Same as above but works with RDFox
    public int addDocuments(String indexName, Cursor cursor, int numOfVariables) {
        List<BulkOperation> body = new ArrayList<>();
        int arity;
        int numOfDocuments = 0;

        try {
            arity = cursor.getArity();

            // Iterate trough the result tuples
            for (long i = cursor.open(); i != 0; i = cursor.advance()) {
                Map<String, Object> documents = new HashMap<>();
                numOfDocuments++;

                // Iterate trough the terms of each tuple
                for (int j = 0; j < arity; j++) {
                    String value = cursor.getResourceValue(j).m_lexicalForm;

                    if (!value.equals("UNDEF")) {
                        documents.put(Constants.FIELD_PREFIX + j, value);
                    }
                }

                body.add(new BulkOperation.Builder()
                        .index(new IndexOperation.Builder<Map<String, Object>>()
                                .index(indexName)
                                .document(documents)
                                .build())
                        .build());
            }

            BulkRequest request = new BulkRequest.Builder()
                    .operations(body)
                    .build();

            BulkResponse response = repo.bulkIndex(request);

            if (response.errors()) {
                LOGGER.error("Failed to add documents to index [" + indexName + "]");

                for (Iterator<BulkResponseItem> it = response.items().iterator(); it.hasNext();) {
                    BulkResponseItem item = it.next();
                    LOGGER.debug(item.error().reason());
                }
            } else {
                LOGGER.info("Successfully added documents to index [" + indexName + "]");
            }
        } catch (ElasticsearchException | IOException | JRDFoxException e) {
            LOGGER.error("Cannot add documents to index [" + indexName + "]");
            e.printStackTrace();
        }

        return numOfDocuments;
    }

    // Perform search operation on an index by specifying index name and a query
    @SuppressWarnings("rawtypes")
    public List<Hit<HashMap>> search(String indexName, BoolQuery query) {
        List<Hit<HashMap>> result = new ArrayList<>();

        try {
            OpenPointInTimeRequest openRequest = new OpenPointInTimeRequest.Builder()
                    .index(indexName)
                    .keepAlive(new Time.Builder()
                            .time(Constants.ES_KEEP_ALIVE)
                            .build())
                    .build();

            OpenPointInTimeResponse openResponse = repo.openPoint(openRequest);
            String pitId = openResponse.id();
            Boolean done = false;
            Boolean firstPass = true;
            List<String> sortResult = new ArrayList<>();

            List<SortOptions> sort = new ArrayList<>();
            sort.add(new SortOptions.Builder()
                    .field(new FieldSort.Builder()
                            .field(Constants.FIELD_PREFIX + 0)
                            .build())
                    .build());

            while (!done) {
                co.elastic.clients.elasticsearch.core.SearchRequest.Builder request = new SearchRequest.Builder()
                        .size(Constants.ES_SEARCH_REQUEST_SIZE)
                        .pit(new PointInTimeReference.Builder()
                                .id(pitId)
                                .keepAlive(new Time.Builder()
                                        .time(Constants.ES_KEEP_ALIVE).build())
                                .build())
                        .sort(sort)
                        .query(query._toQuery());

                if (!firstPass) {
                    request.searchAfter(sortResult);
                }

                SearchResponse<HashMap> response = repo.search(request.build(), HashMap.class);
                result.addAll(response.hits().hits());
                int size = response.hits().hits().size();

                if (size != 0) {
                    sortResult = response.hits().hits().get(size - 1).sort();
                }

                if (size < Constants.ES_SEARCH_REQUEST_SIZE) {
                    done = true;
                }

                firstPass = false;
            }

            ClosePointInTimeResponse closeResponse = repo.closePoint(new ClosePointInTimeRequest.Builder()
                    .id(pitId)
                    .build());

            if (closeResponse.succeeded()) {
                LOGGER.info("Point in Time closed successfully");
            } else {
                LOGGER.error("Point in Time failed to close");
            }

        } catch (ElasticsearchException | IOException e) {
            LOGGER.error("Couldn't execute a query on index " + indexName + "");
            e.printStackTrace();
        }

        return result;
    }

    // Create an ES mapping by specifying a list of fields that should added to an
    // index
    private TypeMapping createMapping(ArrayList<Field> fields) {
        Builder builder = new TypeMapping.Builder();

        for (Field field : fields) {
            ObjectBuilder<Property> property = getProperty(field.type);

            if (property != null) {
                builder.properties(field.name, property.build());
            }
        }

        return builder.build();
    }

    // Returns an ES property for the index mapping
    private ObjectBuilder<Property> getProperty(DataType type) {
        co.elastic.clients.elasticsearch._types.mapping.Property.Builder property = new Property.Builder();

        switch (type) {
            case TEXT:
                return property.text(new TextProperty.Builder().analyzer("standard").build());
            case KEYWORD:
                return property.keyword(new KeywordProperty.Builder().build());
            case INTEGER:
                return property.integer(new IntegerNumberProperty.Builder().build());
            case LONG:
                return property.long_(new LongNumberProperty.Builder().nullValue(0L).build());
            case FLOAT:
                return property.float_(new FloatNumberProperty.Builder().build());
            case DOUBLE:
                return property.double_(new DoubleNumberProperty.Builder().build());
            case BOOLEAN:
                return property.boolean_(new BooleanProperty.Builder().build());
            case DATETIME:
                return property.date(new DateProperty.Builder().build());
            default:
                LOGGER.error("Invalid Elasticsearch field type [" + type.toString() + "]");
                return null;
        }
    }

    public void closeConnection() {
        try {
            repo.close();
        } catch (IOException e) {
            LOGGER.error("Failed to close Elasticsearch rest client connection");
            e.printStackTrace();
        }
    }
}
