package com.search.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.cluster.HealthResponse;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.indices.*;
import co.elastic.clients.elasticsearch.indices.update_aliases.Action;
import co.elastic.clients.elasticsearch.indices.update_aliases.AddAction;

import java.io.IOException;
import java.net.ConnectException;

import com.search.config.ElasticsearchConfig;

public class ElasticSearchRepository {
    private ElasticsearchClient client;

    public ElasticSearchRepository() {
        client = ElasticsearchConfig.open();
    }

    public HealthResponse getHealth() throws ElasticsearchException, IOException {
        try {
            return client.cluster().health();
        } catch (ConnectException e) {
            throw e;
        }
    }

    public <TDocument> SearchResponse<TDocument> search(SearchRequest request, Class<TDocument> tDocumentClass)
            throws ElasticsearchException, IOException {
        // Gson gson = new Gson();
        // String jsonString = gson.toJson(request);
        // logger.info("elasticsearch request ::::: " + jsonString);

        return client.search(request, tDocumentClass);
    }

    public CreateIndexResponse indexCreate(CreateIndexRequest request) throws ElasticsearchException, IOException {
        return client.indices().create(request);
    }

    public DeleteIndexResponse deleteIndex(String indexName) throws ElasticsearchException, IOException {
        DeleteIndexRequest request = new DeleteIndexRequest.Builder().index(indexName).build();
        return client.indices().delete(request);
    }

    public PutMappingResponse updateMapping(PutMappingRequest request) throws ElasticsearchException, IOException {
        return client.indices().putMapping(request);
    }

    public GetAliasResponse getAllAlias() throws ElasticsearchException, IOException {
        return client.indices().getAlias(new GetAliasRequest.Builder().build());
    }

    public UpdateAliasesResponse addAlias(String indexName, String aliasName)
            throws ElasticsearchException, IOException {
        UpdateAliasesRequest request = new UpdateAliasesRequest.Builder()
                .actions(new Action.Builder()
                        .add(new AddAction.Builder()
                                .index(indexName)
                                .alias(aliasName)
                                .build())
                        .build())
                .build();

        return client.indices().updateAliases(request);
    }

    public BulkResponse bulkIndex(BulkRequest bulkRequest) throws ElasticsearchException, IOException {
        // Gson gson = new Gson();
        // String jsonString = gson.toJson(bulkRequest);
        // logger.info("elasticsearch request ::::: " + jsonString);

        return client.bulk(bulkRequest);
    }
}
