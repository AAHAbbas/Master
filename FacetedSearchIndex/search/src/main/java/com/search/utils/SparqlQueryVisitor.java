package com.search.utils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.search.graph.ConceptVariable;
import com.search.graph.LabeledEdge;
import com.search.graph.Variable;

import org.eclipse.rdf4j.query.algebra.Compare;
import org.eclipse.rdf4j.query.algebra.Regex;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.eclipse.rdf4j.query.algebra.ValueExpr;
import org.eclipse.rdf4j.query.algebra.helpers.AbstractQueryModelVisitor;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.sparql.SPARQLParser;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedMultigraph;

// Class used to parse a SPARQL query construct all the parts needed to construct e.g an abstract query.
public class SparqlQueryVisitor extends AbstractQueryModelVisitor<Exception> {
    public Set<ValueExpr> regexFilters = new HashSet<ValueExpr>();
    public Set<ValueExpr> compareFilters = new HashSet<ValueExpr>();
    public Set<StatementPattern> statementPatterns = new HashSet<StatementPattern>();

    Graph<Variable, LabeledEdge> directedGraph = new DirectedMultigraph<>(LabeledEdge.class);
    ConceptVariable root;
    Map<Variable, Set<Filter>> filters;

    // Constructor. Takes a partial query, parses it and saves it as a variable
    public SparqlQueryVisitor(String SPARQLQuery) throws Exception {
        // Make parser and parse query
        SPARQLParser parser = new SPARQLParser();
        ParsedQuery parsedQuery = parser.parseQuery(SPARQLQuery, null);

        // This stores all the useful information in regexFilters, compareFilters and
        // statementPatterns.
        parsedQuery.getTupleExpr().visit(this);
    }

    public void meet(StatementPattern sp) {
        statementPatterns.add(sp);
    }

    public void meet(Regex regex) {
        regexFilters.add(regex);
    }

    public void meet(Compare compare) {
        compareFilters.add(compare);
    }
}
