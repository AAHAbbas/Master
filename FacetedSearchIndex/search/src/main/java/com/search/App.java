package com.search;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.search.core.ConceptConfiguration;
import com.search.core.EndpointDataset;
import com.search.model.FacetIndexModel;
import com.search.model.SimpleFacetIndexModel;
import com.search.repository.ElasticSearchRepository;
import com.search.utils.AssetManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import co.elastic.clients.elasticsearch.cluster.HealthResponse;

/* TODOS:
1. Use RDFox datastore and not dataset endpoint
2. Add comments
3. Cleanup code
4. Use ElasticsearchProperties: RestClient.builder(properties.hosts()).build();
5. WARNING: request [GET http://localhost:9200/_cluster/health] returned 1 warnings: [299 Elasticsearch-7.16.2-2b937c44140b6559905130a8650c64dbd0879cfb "Elasticsearch built-in security features are not enabled. Without authentication, your cluster could be accessible to anyone. See https://www.elastic.co/guide/en/elasticsearch/reference/7.16/security-minimal-setup.html to enable security."]
6. Either update jgrapht version or don't use jgpraht at all
7. Change rdf4j to jena
8. Use newer RDFox version, from 1.2776.2017 and ontology-services-toolkit 1.0.0-SNAPSHOT to 4.1.0 and ontology-services-toolkit 1.0.0-OST
*/

/*
useFacetIndex( link til Indeksen, spørringa[vqsquery], config )
	- køyre ei spørring over indeks
	- returnere filterverdiar
*/

public class App {
    private static final Logger LOGGER = LogManager.getLogger(App.class);

    // File used to construct the facet index.
    // To construct such an index, one needs a source endpoint, a model and a
    // configuration.
    public static void main(String[] args) throws Exception {
        // constructFacetIndex();
        constructFacetIndexTest();
    }

    private static void constructFacetIndexTest() throws Exception {
        AssetManager assetManager = new AssetManager();
        assetManager.loadConceptConfiguration();

        FacetIndexModel indexModel = new SimpleFacetIndexModel();
        EndpointDataset dataset = assetManager.getDataset("dataset-local-npd");
        Set<ConceptConfiguration> configs = new HashSet<ConceptConfiguration>(
                assetManager.getConceptConfiguration().values());

        // Construct the index.
        indexModel.constructFacetIndex(dataset, configs, null);
        indexModel.executeAbstractQuery(assetManager.getVQSQuery("keyword"), configs);
    }

    public static void test() {
        System.out.println("Hello World!");
        ElasticSearchRepository repo = new ElasticSearchRepository();
        try {
            HealthResponse response = repo.getHealth();
            System.out.println(response.status().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // LOGGER.trace("We've just greeted the user!");
        // LOGGER.debug("We've just greeted the user!");
        LOGGER.info("We've just greeted the user!");
        // LOGGER.warn("We've just greeted the user!");
        // LOGGER.error("We've just greeted the user!");
        // LOGGER.fatal("We've just greeted the user!");
    }
}
