package com.search.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.net.ssl.SSLContext;

import com.search.types.Constants;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ElasticsearchConfig {
    private static RestClient restClient;
    private static ElasticsearchClient client;
    private static final Logger LOGGER = LogManager.getLogger(ElasticsearchConfig.class);

    public static ElasticsearchClient open() {
        if (client == null) {
            try {
                // Basic authentication
                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(AuthScope.ANY,
                        new UsernamePasswordCredentials(Constants.ES_USER, Constants.ES_PASSWORD));

                // Encrypted communication using TLS. Setting up the client to trust the CA that
                // has signed the certificate that Elasticsearch is using
                Path certificatePath = Paths.get(Constants.CA_LOCATION);
                CertificateFactory factory = CertificateFactory.getInstance("X.509");
                InputStream inputStream = Files.newInputStream(certificatePath);
                Certificate certificate = factory.generateCertificate(inputStream);
                KeyStore keystore = KeyStore.getInstance("pkcs12");
                keystore.load(null, null);
                keystore.setCertificateEntry("ca", certificate);
                final SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(keystore, null).build();

                // Create the low-level client
                restClient = RestClient.builder(new HttpHost(Constants.ES_HOSTNAME, Constants.ES_PORT, "https"))
                        .setHttpClientConfigCallback(
                                new RestClientBuilder.HttpClientConfigCallback() {
                                    @Override
                                    public HttpAsyncClientBuilder customizeHttpClient(
                                            HttpAsyncClientBuilder httpClientBuilder) {
                                        // httpClientBuilder.disableAuthCaching();
                                        return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
                                                .setSSLContext(sslContext);
                                    }
                                })
                        .build();

                // Create the transport with a Jackson mapper
                ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

                // And create the API client
                client = new ElasticsearchClient(transport);
            } catch (KeyManagementException | KeyStoreException | NoSuchAlgorithmException | CertificateException
                    | IOException e) {
                e.printStackTrace();
                LOGGER.error("Failed to establish a connection with Elasticsearch");
            }
        }

        return client;
    }

    public static void close() throws IOException {
        restClient.close();
        restClient = null;
        client = null;
    }
}
