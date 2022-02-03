package com.search.model;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import uk.ac.ox.cs.JRDFox.JRDFoxException;
import uk.ac.ox.cs.JRDFox.store.DataStore;

import com.search.core.EndpointDataset;
import com.search.core.VQSQuery;
import com.search.core.ConceptConfiguration;

// A Facet index can be stored anywhere, and with any structure, but each facet index must have the functions listed below.
public abstract class FacetIndexModel {
        // Create the main cache, create the facet index
        public abstract void constructFacetIndex(EndpointDataset dataset,
                        Set<ConceptConfiguration> conceptConfigurations,
                        DataStore store) throws SQLException, IOException, JRDFoxException;

        // Executes an abstract query over the index. The list of updateAttributes are
        // the ones we want to find in the output data, the ones we want to get updated.
        public abstract Map<String, Set<String>> executeAbstractQuery(VQSQuery vqsQuery,
                        Set<ConceptConfiguration> conceptConfiguration)
                        throws SQLException, IOException, IllegalArgumentException, Exception;
}
