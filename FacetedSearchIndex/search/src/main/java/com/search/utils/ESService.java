package com.search.utils;

import java.io.IOException;
import java.util.ArrayList;

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
            LOGGER.info("configIndex finished : " + response);
        } catch (IOException e) {
            LOGGER.error("Caught IOException when trying to create an ES index");
            e.printStackTrace();
        }
    }

    private TypeMapping createMapping(ArrayList<Field> fields) {
        Builder builder = new TypeMapping.Builder();
        for (Field field : fields) {
            co.elastic.clients.elasticsearch._types.mapping.Property.Builder property = new Property.Builder();

            if (field.type.equals("text")) {
                property.text(new TextProperty.Builder().analyzer("standard").build());
            } else if (field.type.equals("keyword")) {
                property.keyword(new KeywordProperty.Builder().build());
            } else if (field.type.equals("long")) {
                property.long_(new LongNumberProperty.Builder().nullValue(0L).build());
            } else if (field.type.equals("float")) {
                property.float_(new FloatNumberProperty.Builder().build());
            } else {
                LOGGER.error("Invalid Elasticsearch field type");
            }

            builder.properties(field.name, property.build());
        }

        return builder.build();
    }
}
