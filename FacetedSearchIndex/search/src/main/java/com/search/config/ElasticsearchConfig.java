package com.search.config;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;

public class ElasticsearchConfig {
    private static RestClient restClient;
    private static ElasticsearchClient client;

    public static ElasticsearchClient open() {
        if (client == null) {
            // Create the low-level client
            restClient = RestClient.builder(new HttpHost("localhost", 9200)).build();

            // Create the transport with a Jackson mapper
            ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

            // And create the API client
            client = new ElasticsearchClient(transport);
        }

        return client;
    }

    public static void close() throws IOException {
        restClient.close();
        restClient = null;
        client = null;
    }
}
