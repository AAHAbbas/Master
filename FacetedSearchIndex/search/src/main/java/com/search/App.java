package com.search;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.search.core.ConceptConfiguration;
import com.search.core.EndpointDataset;
import com.search.model.ESFacetIndexModel;
import com.search.model.FacetIndexModel;
import com.search.utils.AssetManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* TODOS:
1. Create ES Model
2. Use Enum of valid datatypes instead
3. Add comments and clean up code 
4. Use RDFox datastore and not dataset endpoint
5. Add settings when creating an index
6. WARNING: request [GET http://localhost:9200/_cluster/health] returned 1 warnings: [299 Elasticsearch-7.16.2-2b937c44140b6559905130a8650c64dbd0879cfb "Elasticsearch built-in security features are not enabled. Without authentication, your cluster could be accessible to anyone. See https://www.elastic.co/guide/en/elasticsearch/reference/7.16/security-minimal-setup.html to enable security."]

7. Use ElasticsearchProperties: RestClient.builder(properties.hosts()).build();
8. Either update jgrapht version or don't use jgpraht at all
9. Change rdf4j to jena
10. Use newer RDFox version, from 1.2776.2017 and ontology-services-toolkit 1.0.0-SNAPSHOT to 4.1.0 and ontology-services-toolkit 1.0.0-OST
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
        constructFacetIndex();
        System.exit(0);
    }

    private static void constructFacetIndex() throws Exception {
        AssetManager assetManager = new AssetManager();
        assetManager.loadConceptConfiguration();

        FacetIndexModel indexModel = new ESFacetIndexModel();
        EndpointDataset dataset = assetManager.getDataset("dataset-local-npd");
        Set<ConceptConfiguration> configs = new HashSet<ConceptConfiguration>(
                assetManager.getConceptConfiguration().values());

        indexModel.constructFacetIndex(dataset, configs, null);
        Map<String, Set<String>> updatedFacetValues = indexModel
                .executeAbstractQuery(assetManager.getVQSQuery("npd-explorationwellbore-1-1"),
                        configs);

        LOGGER.info("Updated facet values:");

        updatedFacetValues.entrySet().forEach(entry -> {
            LOGGER.info(entry.getKey() + ": " + entry.getValue());
        });
    }
}
