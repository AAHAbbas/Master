package com.search.core;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import tech.oxfordsemantic.jrdfox.Prefixes;
import tech.oxfordsemantic.jrdfox.client.ConnectionFactory;
import tech.oxfordsemantic.jrdfox.client.Cursor;
import tech.oxfordsemantic.jrdfox.client.DataStoreConnection;
import tech.oxfordsemantic.jrdfox.client.ServerConnection;
import tech.oxfordsemantic.jrdfox.client.UpdateType;
import tech.oxfordsemantic.jrdfox.exceptions.JRDFoxException;

// Connect to RDFox
public class RDFoxDataset {
    DataStoreConnection store;

    public RDFoxDataset(String fileName) {
        try (ServerConnection connection = ConnectionFactory.newServerConnection("rdfox:local", "", "")) {
            connection.createDataStore("Abbas", "par-complex-nn", Map.of("import.invalid-literal-policy",
                    "as-string-silent"));

            store = connection.newDataStoreConnection("Abbas");
            store.importData(UpdateType.ADDITION, Prefixes.s_emptyPrefixes, new File(fileName));
        } catch (JRDFoxException e) {
            e.printStackTrace();
        }
    }

    // Run a query over the dataset
    public Cursor runQuery(String query) {
        try {
            return store.createCursor(Prefixes.s_defaultPrefixes, query, new HashMap<String, String>());
        } catch (JRDFoxException e) {
            e.printStackTrace();
        }

        return null;
    }
}
