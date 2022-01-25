package com.search.model;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import uk.ac.ox.cs.JRDFox.JRDFoxException;
import uk.ac.ox.cs.JRDFox.store.DataStore;

import com.search.core.EndpointDataset;
import com.search.core.VqsQuery;
import com.search.core.ConceptConfiguration;

// Abstract Facet index main class. A Facet index can be stored anywhere, and with any structure, but each facet index must have the functions listed below.
public abstract class FacetIndexModel {

    // This method should create the main cache. It is given a dataset, and a set of
    // classes with corresponding properties, a.k.a configuration.
    public abstract int constructFacetIndex(EndpointDataset dataset, Set<ConceptConfiguration> conceptConfigurations,
            DataStore store) throws SQLException, IOException, JRDFoxException;

    // This method executes an abstract query over the index. It returns an
    // OutputData.
    // The list of updateAttributes are the ones we want to find in the output data,
    // the ones we want to get updated.
    public abstract Map<String, Set<String>> executeAbstractQuery(VqsQuery vqsQuery,
            Set<ConceptConfiguration> conceptConfiguration)
            throws SQLException, IOException, IllegalArgumentException, Exception;

}
