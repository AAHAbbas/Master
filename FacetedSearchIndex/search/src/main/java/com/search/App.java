package com.search;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.search.core.ConceptConfiguration;
import com.search.core.RDFoxDataset;
import com.search.core.VQSQuery;
import com.search.model.ESFacetIndexModel;
import com.search.utils.AssetManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* TODOS:
1. Handle regex filters: 
    What kind of regex filters do we need? 
    Full-text search or keyword (exact) search? 
    case sensitivity?
    other more advanced features? more advanced searching or just simple exact filtering

    Can use a text field with keyword tokenizer and special case sensitivity filter
    https://www.elastic.co/guide/en/elasticsearch/reference/6.8/analysis-keyword-analyzer.html

3. Should I skip concept variables???
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
        Thread.sleep(5000);
        search();
        closeConnection(model);
    }

    private static ESFacetIndexModel constructFacetIndex() throws Exception {
        assetManager = new AssetManager("src/main/resources/config.json");
        indexModel = new ESFacetIndexModel();
        configs = new HashSet<ConceptConfiguration>(Arrays.asList(
                assetManager.getConceptConfiguration("npd-wellbore-3")));

        // EndpointDataset dataset = assetManager.getDataset("dataset-local-npd");
        // indexModel.constructFacetIndex(dataset, configs, null);
        RDFoxDataset rdfox = assetManager.getRDFoxDataset("rdfox-npd");
        indexModel.constructFacetIndex(null, configs, rdfox);
        rdfox.closeConnections();

        return indexModel;
    }

    private static void search() throws Exception {
        VQSQuery query = assetManager.getVQSQuery("npd-wellbore-1-1", "ontology-npd");
        Map<String, Set<String>> updatedFacetValues = indexModel.executeAbstractQuery(query,
                assetManager.getConceptConfiguration("npd-wellbore-3"));

        LOGGER.info("Updated facet values:");
        updatedFacetValues.entrySet().forEach(entry -> {
            LOGGER.info(entry.getKey() + ": " + entry.getValue());
        });
    }

    private static void closeConnection(ESFacetIndexModel model) {
        model.closeConnection();
    }
}
