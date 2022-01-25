package com.search.core;

import java.io.File;

import uk.ac.ox.cs.JRDFox.JRDFoxException;
import uk.ac.ox.cs.JRDFox.store.DataStore;
import uk.ac.ox.cs.JRDFox.store.TupleIterator;

// Connect to RDFox
public class RDFoxDataset {
    DataStore store;

    public RDFoxDataset(String fileName) throws JRDFoxException {
        store = new DataStore(DataStore.StoreType.ParallelSimpleNN);
        store.importFiles(new File[] { new File(fileName) });
    }

    // Run a query over the dataset
    public TupleIterator runQuery(String query) throws JRDFoxException {
        return store.compileQuery(query);
    }
}
