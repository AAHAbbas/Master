package com.search.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.analysis.LowercaseNormalizer;
import co.elastic.clients.elasticsearch._types.analysis.Normalizer;
import co.elastic.clients.elasticsearch.cluster.HealthResponse;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.ClosePointInTimeRequest;
import co.elastic.clients.elasticsearch.core.ClosePointInTimeResponse;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.OpenPointInTimeRequest;
import co.elastic.clients.elasticsearch.core.OpenPointInTimeResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.indices.*;
import co.elastic.clients.elasticsearch.indices.update_aliases.Action;
import co.elastic.clients.elasticsearch.indices.update_aliases.AddAction;

import java.io.IOException;
import java.net.ConnectException;
import java.util.HashMap;

import com.search.config.ElasticsearchConfig;

public class ElasticSearchRepository {
    private ElasticsearchClient client;

    public ElasticSearchRepository() {
        client = ElasticsearchConfig.open();
    }

    public void close() throws IOException {
        ElasticsearchConfig.close();
    }

    public HealthResponse getHealth() throws ElasticsearchException, IOException {
        try {
            return client.cluster().health();
        } catch (ConnectException e) {
            throw e;
        }
    }

    @SuppressWarnings("rawtypes")
    public SearchResponse<HashMap> search(SearchRequest request, Class<HashMap> document)
            throws ElasticsearchException, IOException {
        return client.search(request, document);
    }

    public CreateIndexResponse createIndex(CreateIndexRequest request) throws ElasticsearchException, IOException {
        return client.indices().create(request);
    }

    public DeleteIndexResponse deleteIndex(String indexName) throws ElasticsearchException, IOException {
        DeleteIndexRequest request = new DeleteIndexRequest.Builder().index(indexName).build();
        return client.indices().delete(request);
    }

    public PutMappingResponse updateMapping(PutMappingRequest request) throws ElasticsearchException, IOException {
        return client.indices().putMapping(request);
    }

    public IndexResponse createDocument(IndexRequest<Object> request) throws ElasticsearchException, IOException {
        return client.index(request);
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
        return client.bulk(bulkRequest);
    }

    public OpenPointInTimeResponse openPoint(OpenPointInTimeRequest openRequest)
            throws ElasticsearchException, IOException {
        return client.openPointInTime(openRequest);
    }

    public ClosePointInTimeResponse closePoint(ClosePointInTimeRequest closeRequest)
            throws ElasticsearchException, IOException {
        return client.closePointInTime(closeRequest);
    }

    public IndexSettingsAnalysis getKeywordNormalizer() {
        return new IndexSettingsAnalysis.Builder()
                .normalizer("rebuilt_keyword", new Normalizer.Builder()
                        .lowercase(new LowercaseNormalizer.Builder()
                                .build())
                        .build())
                .build();
    }
}
