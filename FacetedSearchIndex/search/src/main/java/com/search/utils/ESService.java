package com.search.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.search.graph.Variable;
import com.search.repository.ElasticSearchRepository;
import com.search.types.Field;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.query.BindingSet;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.mapping.BooleanProperty;
import co.elastic.clients.elasticsearch._types.mapping.FloatNumberProperty;
import co.elastic.clients.elasticsearch._types.mapping.IntegerNumberProperty;
import co.elastic.clients.elasticsearch._types.mapping.KeywordProperty;
import co.elastic.clients.elasticsearch._types.mapping.LongNumberProperty;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TextProperty;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping.Builder;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.IndexOperation;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.PutMappingRequest;
import co.elastic.clients.elasticsearch.indices.PutMappingResponse;
import co.elastic.clients.util.ObjectBuilder;

public class ESService {
    private static final Logger LOGGER = LogManager.getLogger(ESManager.class);

    ElasticSearchRepository repo;

    public ESService() {
        repo = new ElasticSearchRepository();
    }

    // Create an index with index by specifying index name and fields
    // TODO: Add settings later
    public void createIndex(String indexName, ArrayList<Field> fields) throws ElasticsearchException {
        TypeMapping mapping = createMapping(fields);
        CreateIndexRequest request = new CreateIndexRequest.Builder()
                .index(indexName)
                .mappings(mapping)
                .build();

        try {
            CreateIndexResponse response = repo.createIndex(request);
            LOGGER.info("Successfully created an index: " + response);
        } catch (ElasticsearchException | IOException e) {
            LOGGER.error("An exception occured when trying to create an ES index");
            e.printStackTrace();
        }
    }

    public void addFieldToIndex(String indexName, Field field) {
        ObjectBuilder<Property> property = getProperty(field.type);
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

    public void addDocuments(String indexName, List<BindingSet> queryResult, List<Variable> variables) {
        List<BulkOperation> body = new ArrayList<>();

        for (int i = 0; i < queryResult.size(); i++) {
            Map<String, Object> document = new HashMap<>();
            BindingSet data = queryResult.get(i);

            for (int j = 0; j < variables.size(); j++) {
                String bindingName = "o" + Integer.toString(j);

                if (data.getBinding(bindingName) != null) {
                    document.put("field" + j, data.getBinding(bindingName).getValue().stringValue());
                }
            }

            body.add(new BulkOperation.Builder()
                    .index(new IndexOperation.Builder<Map<String, Object>>()
                            .index(indexName)
                            .document(document)
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

    // TODO: Use Enum of valid datatypes instead
    private ObjectBuilder<Property> getProperty(String type) {
        co.elastic.clients.elasticsearch._types.mapping.Property.Builder property = new Property.Builder();

        if (type.equals("text")) {
            return property.text(new TextProperty.Builder().analyzer("standard").build());
        } else if (type.equals("keyword")) {
            return property.keyword(new KeywordProperty.Builder().build());
        } else if (type.equals("long")) {
            return property.long_(new LongNumberProperty.Builder().nullValue(0L).build());
        } else if (type.equals("float")) {
            return property.float_(new FloatNumberProperty.Builder().build());
        } else if (type.equals("boolean")) {
            return property.boolean_(new BooleanProperty.Builder().build());
        } else if (type.equals("integer")) {
            return property.integer(new IntegerNumberProperty.Builder().build());
        } else {
            LOGGER.error("Invalid Elasticsearch field type");
            return null;
        }
    }
}
