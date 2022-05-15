package com.search.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.search.graph.Variable;
import com.search.types.FilterOperator;
import com.search.utils.Filter;
import com.search.utils.SparqlQueryVisitor;
import com.search.graph.ConceptVariable;
import com.search.graph.DatatypeVariable;
import com.search.graph.LabeledEdge;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.algebra.Compare;
import org.eclipse.rdf4j.query.algebra.Regex;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.eclipse.rdf4j.query.algebra.ValueConstant;
import org.eclipse.rdf4j.query.algebra.ValueExpr;
import org.eclipse.rdf4j.query.algebra.Var;
import org.jgrapht.graph.DirectedAcyclicGraph;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// An VQS query is a java representation of a VQS query - a SPARQL query that can be constructed in OtiqueVQS.
// TODO maybe unify this and the concept config?
public class VQSQuery {
    private static final Logger LOGGER = LogManager.getLogger(VQSQuery.class);
    private Variable root; // The root variable of the graph
    private String SPARQLQuery; // The query given in SPARQL
    private Ontology ontology;

    // The directed graph defining the query. 2019 Vidar: Actually this is not
    // strong enough. This allows queries that are not tree shaped when direction is
    // ignored
    private DirectedAcyclicGraph<Variable, LabeledEdge> graph;
    private Map<Variable, Set<Filter>> filters; // Map from each datatype variable to a set of filters

    public VQSQuery(Ontology ontology, String SPARQLQuery, String rootName) {
        this.SPARQLQuery = SPARQLQuery;
        this.ontology = ontology;
        this.graph = new DirectedAcyclicGraph<>(LabeledEdge.class);
        this.filters = new HashMap<>();

        SparqlQueryVisitor queryVisitor;
        try {
            queryVisitor = new SparqlQueryVisitor(SPARQLQuery);
        } catch (Exception e) {
            LOGGER.error("Failed to parse the SPARQL query and visit the nodes");
            e.printStackTrace();
            return;
        }
        // Collect the objects found by the visitor
        // Maps to keep an overview of all the variables we create
        Map<String, Variable> variables = new HashMap<>();

        // Find all concept variables by looking for rdf:type, store them in a map
        for (Iterator<StatementPattern> it = queryVisitor.statementPatterns.iterator(); it.hasNext();) {
            StatementPattern sp = it.next();

            if (sp.getPredicateVar().getValue().toString().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
                String subject = sp.getSubjectVar().getName();
                String object = sp.getObjectVar().getValue().toString();
                variables.put(subject, new ConceptVariable(subject, object));
                it.remove();
            }
        }

        // Find all other variables. They must be datatype variables
        for (Iterator<StatementPattern> it = queryVisitor.statementPatterns.iterator(); it.hasNext();) {
            StatementPattern sp = it.next();

            // If both the subject and the object are variables
            if (sp.getSubjectVar().getValue() == null && sp.getObjectVar().getValue() == null) {
                String subject = sp.getSubjectVar().getName();
                String predicate = sp.getPredicateVar().getValue().stringValue();
                String object = sp.getObjectVar().getName();

                // Both are object variable
                if (variables.containsKey(subject) && variables.containsKey(object)) {
                    LOGGER.info("Both concepts are object variable (" + subject + " - " + object + ")");
                }

                if (variables.containsKey(subject) && !variables.containsKey(object)) {
                    String type = ontology.getPropertyTargetType(variables.get(subject).getType(), predicate);
                    DatatypeVariable objectVariable = new DatatypeVariable(object, type);
                    variables.put(object, objectVariable);
                }
            }
        }

        Variable root = variables.get(rootName);
        graph.addVertex(root);
        this.root = root;

        // Doing a breath first search to populate the graph with nodes.
        // Variable names we want and will include in the query, but which has not been
        // searched for in the statements yet
        Set<String> variablesToSearchFor = new HashSet<>();
        variablesToSearchFor.add(rootName);

        while (!variablesToSearchFor.isEmpty()) {
            HashSet<String> round = new HashSet<>();

            for (Iterator<StatementPattern> it = queryVisitor.statementPatterns.iterator(); it.hasNext();) {
                StatementPattern sp = it.next();

                // If both the subject and the object are variables
                if (sp.getSubjectVar().getValue() == null && sp.getObjectVar().getValue() == null) {
                    String subject = sp.getSubjectVar().getName();
                    String predicate = sp.getPredicateVar().getValue().stringValue();
                    String object = sp.getObjectVar().getName();

                    // Get variables for the subject and object
                    Variable subjectVariable = variables.get(subject);
                    Variable objectVariable = variables.get(object);

                    if (variablesToSearchFor.contains(subject)) {
                        graph.addVertex(subjectVariable);
                        graph.addVertex(objectVariable);
                        graph.addEdge(subjectVariable, objectVariable, new LabeledEdge(predicate));
                        round.add(object);
                        it.remove();

                    }

                    if (variablesToSearchFor.contains(object)) {
                        graph.addVertex(subjectVariable);
                        graph.addVertex(objectVariable);
                        graph.addEdge(objectVariable, subjectVariable, new LabeledEdge(predicate + "_inverseProp"));
                        round.add(subject);
                        it.remove();
                    }
                }
            }

            variablesToSearchFor = round;
        }

        // Find all compare filters
        for (ValueExpr filter : queryVisitor.compareFilters) {
            Compare compare = (Compare) filter;
            String datatype = ((Var) compare.getLeftArg()).getName();
            Variable datatypeVariable = variables.get(datatype);
            Value value = ((ValueConstant) compare.getRightArg()).getValue();

            // Associate datatypeVariable with the filter by putting it in the filter map
            if (filters.get(datatypeVariable) == null)
                filters.put(datatypeVariable, new HashSet<Filter>());

            filters.get(datatypeVariable).add(new Filter(FilterOperator.valueOf(compare.getOperator().name()), value));
        }

        // Find all regex filters
        for (ValueExpr filter : queryVisitor.regexFilters) {
            Regex regex = (Regex) filter;
            String datatype = ((Var) regex.getLeftArg()).getName();
            Variable datatypeVariable = variables.get(datatype);
            Value value = ((ValueConstant) regex.getRightArg()).getValue();

            // Associate datatypeVariable with the filter by putting it in the filter map
            if (filters.get(datatypeVariable) == null)
                filters.put(datatypeVariable, new HashSet<Filter>());

            filters.get(datatypeVariable).add(new Filter(FilterOperator.REG, value));
        }
    }

    public Ontology getOntology() {
        return this.ontology;
    }

    public String getSparqlRepresentation() {
        return this.SPARQLQuery;
    }

    public String toString() {
        return "\n    ---VQS query---\n    ROOT: " + this.root + "\n    GRAPH" + graph.toString() + "\n    FILTERS:"
                + filters + "\n";
    }

    public Variable getRoot() {
        return root;
    }

    public DirectedAcyclicGraph<Variable, LabeledEdge> getGraph() {
        return graph;
    }

    public Set<Filter> getFiltersForVariable(Variable var) {
        if (filters.get(var) == null)
            return new HashSet<Filter>();

        return filters.get(var);
    }
}
