package com.search.model;

import java.util.Map;
import java.util.Set;

import com.search.core.EndpointDataset;
import com.search.core.RDFoxDataset;
import com.search.core.VQSQuery;

import com.search.core.ConceptConfiguration;

// A Facet index can be stored anywhere, and with any structure, but each facet index must have the functions listed below.
public abstract class FacetIndexModel {
        // Create the main cache, create the facet index
        public abstract void constructFacetIndex(EndpointDataset dataset,
                        Set<ConceptConfiguration> conceptConfigurations,
                        RDFoxDataset rdfoxDataset);

        // Executes an abstract query over the index. The list of updateAttributes are
        // the ones we want to find in the output data, the ones we want to get updated.
        public abstract Map<String, Set<String>> executeAbstractQuery(VQSQuery vqsQuery,
                        ConceptConfiguration conceptConfiguration);
}
