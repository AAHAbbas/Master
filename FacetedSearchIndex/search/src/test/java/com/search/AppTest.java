package com.search;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.TreeSet;

import com.search.core.ConceptConfiguration;
import com.search.core.RDFoxDataset;
import com.search.core.VQSQuery;
import com.search.model.ESFacetIndexModel;
import com.search.repository.ElasticSearchRepository;
import com.search.types.Constants;
import com.search.utils.AssetManager;

import org.junit.Test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.indices.GetMappingResponse;
import co.elastic.clients.elasticsearch.indices.IndicesStatsResponse;
import co.elastic.clients.elasticsearch.indices.stats.IndicesStats;

public class AppTest {
    private static final Logger LOGGER_SIZE = LogManager.getLogger("index-size");
    private static final Logger LOGGER_TIME = LogManager.getLogger("index-time");
    private static final Logger LOGGER_QUERY = LogManager.getLogger("index-query");

    @Test
    public void benchmarkIndexSizeAndTime() throws ElasticsearchException, IOException, InterruptedException {
        AssetManager assetManager = new AssetManager("src/main/resources/config.json");
        ESFacetIndexModel indexModel = new ESFacetIndexModel();
        RDFoxDataset rdfox = assetManager.getRDFoxDataset("rdfox-npd");
        TreeSet<ConceptConfiguration> configs = new TreeSet<ConceptConfiguration>(
                assetManager.getConfigsToUseAtStartup().values());

        for (ConceptConfiguration config : configs) {
            HashSet<ConceptConfiguration> configSet = new HashSet<ConceptConfiguration>();
            configSet.add(config);

            long startTime = System.nanoTime();
            indexModel.constructFacetIndex(null, configSet, rdfox);
            long endTime = System.nanoTime();

            LOGGER_TIME.info(config.getId() + ": " + (endTime - startTime) / 1000000 + " ms");
        }

        LOGGER_TIME.info("");

        Thread.sleep(10000);

        ElasticSearchRepository repo = new ElasticSearchRepository();
        IndicesStatsResponse statsResponse = repo.getIndexStats();
        GetMappingResponse mappingResponse = repo.getMapping();

        for (String file : new TreeSet<String>(assetManager.getConfigNames())) {
            IndicesStats stats = statsResponse.indices().get(file);
            LOGGER_SIZE.info(file + ": " + stats.primaries().docs().count() + " docs, "
                    + stats.primaries().store().sizeInBytes() / 1000000.0 + " mb and " +
                    mappingResponse.result().get(file).mappings().properties().size() + " fields");
        }

        LOGGER_SIZE.info("");

        rdfox.closeConnections();
        indexModel.closeConnection();
    }

    @Test
    public void benchmarkQueryTime() throws ElasticsearchException, IOException, InterruptedException {
        AssetManager assetManager = new AssetManager("src/main/resources/config.json");
        ESFacetIndexModel indexModel = new ESFacetIndexModel();
        RDFoxDataset rdfox = assetManager.getRDFoxDataset("rdfox-npd");
        TreeSet<ConceptConfiguration> configs = new TreeSet<ConceptConfiguration>(
                assetManager.getConfigsToUseAtStartup().values());

        indexModel.constructFacetIndex(null, configs, rdfox);

        Thread.sleep(10000);

        TreeSet<String> queries = new TreeSet<>(
                Arrays.asList(new File("C:/Users/abdul/Master/FacetedSearchIndex/search/queries").list()));

        for (ConceptConfiguration config : configs) {
            for (String queryName : queries) {
                VQSQuery query = assetManager.getVQSQuery(queryName, "ontology-npd");

                for (int i = 0; i < 10; i++) {
                    long startTime = System.nanoTime();
                    int docs = indexModel.executeAbstractQuery(query, config);
                    long endTime = System.nanoTime();

                    LOGGER_QUERY.info("Config: " + config.getId() + " - Query: " + queryName
                            + " - executeAbstractQuery(): " + (endTime - startTime) / 1000000
                            + " ms, ES time took " + Constants.ES_TIME_TOOK + " ms and " + docs
                            + " docs");

                    Constants.ES_TIME_TOOK = 0;

                    if (docs == 0) {
                        break;
                    }
                }

                LOGGER_QUERY.info("");
            }
        }

        rdfox.closeConnections();
        indexModel.closeConnection();
    }

    @Test
    public void benchmarkAccuracy() throws ElasticsearchException, IOException, InterruptedException {

    }

    @Test
    public void cleanUp() throws ElasticsearchException, IOException {
        ElasticSearchRepository repo = new ElasticSearchRepository();
        repo.deleteIndex("*");
        repo.close();
    }
}
