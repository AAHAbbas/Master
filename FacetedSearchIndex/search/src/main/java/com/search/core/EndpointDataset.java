package com.search.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;

// Creates and connects to a SPARQLRepository and executes queries over the dataset by giving an endpoint URL and a SPARQL query
public class EndpointDataset {
    private String endpointURI;

    public EndpointDataset(String endpointURI) {
        this.endpointURI = endpointURI;
    }

    public List<BindingSet> runQuery(String query) {
        ArrayList<BindingSet> results = new ArrayList<BindingSet>();
        SPARQLRepository sparqlRepository = new SPARQLRepository(endpointURI);
        sparqlRepository.init();

        RepositoryConnection sparqlConnection = sparqlRepository.getConnection();

        TupleQuery tupleQuery = sparqlConnection.prepareTupleQuery(QueryLanguage.SPARQL, query);
        TupleQueryResult result = tupleQuery.evaluate();

        while (result.hasNext()) {
            BindingSet bindingSet = result.next();
            results.add(bindingSet);
        }

        result.close();
        sparqlConnection.close();

        return results;
    }
}
