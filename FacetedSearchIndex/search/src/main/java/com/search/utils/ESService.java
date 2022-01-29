package com.search.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import com.fasterxml.jackson.databind.introspect.DefaultAccessorNamingStrategy.FirstCharBasedValidator;
import com.search.repository.ElasticSearchRepository;
import com.search.types.Field;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.mapping.FloatNumberProperty;
import co.elastic.clients.elasticsearch._types.mapping.KeywordProperty;
import co.elastic.clients.elasticsearch._types.mapping.LongNumberProperty;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TextProperty;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping.Builder;
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
    public void createIndex(String indexName, ArrayList<Field> fields) throws ElasticsearchException {
        TypeMapping mapping = createMapping(fields);
        CreateIndexRequest request = new CreateIndexRequest.Builder()
                .index(indexName)
                .mappings(mapping)
                .build();

        try {
            CreateIndexResponse response = repo.indexCreate(request);
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
            }

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
        } else {
            LOGGER.error("Invalid Elasticsearch field type");
            return null;
        }
    }
}
