package com.search;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.search.core.ConceptConfiguration;
import com.search.core.EndpointDataset;
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
    
2. More support for different value types:
    Probably not needed since coerce is enabled which allows quoted numbers into numeric fields (same for other types, such as boolean and date).
    So ES remembers the original form of string vs numeric, and returns values in their original form, but if coerce=false, then 
    strings would be rejected as invalid

3. Should I skip concept variables???
4. Only one config, for both? why?
5. Investigate why some field are UNDEF, probably just bad dataset
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
        assetManager = new AssetManager();
        indexModel = new ESFacetIndexModel();
        // EndpointDataset dataset = assetManager.getDataset("dataset-local-npd");
        RDFoxDataset rdfox = assetManager.getRDFoxDataset("rdfox-npd");
        // configs = new HashSet<ConceptConfiguration>(
        // assetManager.getConceptConfiguration().values());
        configs = new HashSet<ConceptConfiguration>();
        configs.add(assetManager.getConceptConfiguration("npd-expwellbore-2"));

        // indexModel.constructFacetIndex(dataset, configs, null);
        indexModel.constructFacetIndex(null, configs, rdfox);
        // rdfox.closeConnections();

        return indexModel;
    }

    private static void search() {
        VQSQuery query = assetManager.getVQSQuery("npd-explorationwellbore-1-4");

        if (query == null) {
            LOGGER.error("Failed to create an instance of VQSQuery");
        }

        Map<String, Set<String>> updatedFacetValues = indexModel.executeAbstractQuery(query, configs);

        if (query == null) {
            LOGGER.error("executeAbstractQuery failed");
        } else {
            LOGGER.info("Updated facet values:");

            updatedFacetValues.entrySet().forEach(entry -> {
                LOGGER.info(entry.getKey() + ": " + entry.getValue());
            });
        }
    }

    private static void closeConnection(ESFacetIndexModel model) {
        model.closeConnection();
    }
}
