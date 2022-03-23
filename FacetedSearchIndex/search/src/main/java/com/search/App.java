package com.search;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.search.core.ConceptConfiguration;
import com.search.core.RDFoxDataset;
import com.search.model.ESFacetIndexModel;
import com.search.utils.AssetManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* TODOS:
test config and concepts
test rdfox and dataset
1. Handle regex filters: 
    What kind of regex filters do we need? 
    Full-text search or keyword (exact) search? 
    case sensitivity?
    other more advanced features? more advanced searching or just simple exact filtering

    Can use a text field with keyword tokenizer and special case sensitivity filter
    https://www.elastic.co/guide/en/elasticsearch/reference/6.8/analysis-keyword-analyzer.html

2. Should I skip concept variables???
3. Does it matter if field is boolean or string, as long as the string value is "true" or "false"
4. What does inUse field do: config.json
*/

public class App {
    private static final Logger LOGGER = LogManager.getLogger(App.class);
    private static AssetManager assetManager;
    private static ESFacetIndexModel indexModel;
    private static Set<ConceptConfiguration> configs;

    // File used to construct the facet index
    // To construct such an index, one needs a source endpoint, a model and a
    // configuration
    public static void main(String[] args) throws Exception {
        ESFacetIndexModel model = constructFacetIndex();
        // Thread.sleep(5000);
        // search();
        closeConnection(model);
    }

    private static ESFacetIndexModel constructFacetIndex() throws Exception {
        assetManager = new AssetManager("src/main/resources/config.json");
        indexModel = new ESFacetIndexModel();
        configs = new HashSet<ConceptConfiguration>(assetManager.getConfigsToUseAtStartup().values());

        // EndpointDataset dataset = assetManager.getDataset("dataset-local-npd");
        // indexModel.constructFacetIndex(dataset, configs, null);
        RDFoxDataset rdfox = assetManager.getRDFoxDataset("rdfox-npd");
        indexModel.constructFacetIndex(null, configs, rdfox);
        rdfox.closeConnections();

        return indexModel;
    }

    private static void search() {
        Map<String, Set<String>> updatedFacetValues = indexModel.executeAbstractQuery(
                assetManager.getVQSQuery("npd-wellbore-1-1", "ontology-npd"),
                assetManager.getConfig("npd-wellbore-3"));

        LOGGER.info("Updated facet values:");
        updatedFacetValues.entrySet().forEach(entry -> {
            LOGGER.info(entry.getKey() + ": " + entry.getValue());
        });
    }

    private static void closeConnection(ESFacetIndexModel model) {
        model.closeConnection();
    }
}
