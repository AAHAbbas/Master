package com.search;

import java.util.HashSet;

import com.search.core.ConceptConfiguration;
import com.search.core.EndpointDataset;
import com.search.core.RDFoxDataset;
import com.search.core.VQSQuery;
import com.search.model.ESFacetIndexModel;
import com.search.utils.AssetManager;

/* TODOS:

1. Should I skip concept variables???
2. Does it matter if field is boolean or string, as long as the string value is "true" or "false"
3. What does inUse field do: config.json

10298827 ~ 10 million triples
8904183  ~  9 million triples
*/

public class App {
    private static ESFacetIndexModel indexModel;
    private static AssetManager assetManager;

    public static void main(String[] args) throws Exception {
        constructFacetIndex();
        // Thread.sleep(5000);
        // search();
        indexModel.closeConnection();
    }

    private static void constructFacetIndex() {
        assetManager = new AssetManager("src/main/resources/config.json");
        indexModel = new ESFacetIndexModel();
        EndpointDataset dataset = assetManager.getDataset("blazegraph-npd");
        // RDFoxDataset rdfox = assetManager.getRDFoxDataset("rdfox-npd");

        indexModel.constructFacetIndex(dataset, new HashSet<>(assetManager.getConfigsToUseAtStartup().values()), null);
        // rdfox.closeConnections();
    }

    private static void search() {
        VQSQuery query = assetManager.getVQSQuery("wellbore-5.rq", "ontology-npd");
        ConceptConfiguration config = assetManager.getConfig("wellbore-1-5");

        indexModel.executeAbstractQuery(query, config);

        // LOGGER.info("Updated facet values:");
        // updatedFacetValues.entrySet().forEach(entry -> {
        // LOGGER.info(entry.getKey() + ": " + entry.getValue());
        // });
    }
}
