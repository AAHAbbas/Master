package com.search.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.search.graph.Variable;
import com.search.utils.Filter;
import com.search.utils.SparqlQueryVisitor;
import com.search.graph.ConceptVariable;
import com.search.graph.DatatypeVariable;
import com.search.graph.LabeledEdge;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.algebra.Compare;
import org.eclipse.rdf4j.query.algebra.Compare.CompareOp;
import org.eclipse.rdf4j.query.algebra.Regex;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.eclipse.rdf4j.query.algebra.ValueConstant;
import org.eclipse.rdf4j.query.algebra.ValueExpr;
import org.eclipse.rdf4j.query.algebra.Var;
import org.jgrapht.graph.DirectedAcyclicGraph;

// An VQS query is a java representation of a VQS query - a SPARQL query that can be constructed in OtiqueVQS.
// TODO maybe unify this and the concept config?
public class VqsQuery {
    private Variable root; // The root variable of the graph
    private String sparqlRepresentation; // The query given in SPARQL

    private Ontology ontology; // The ontology the query is over.

    // The directed graph defining the query. 2019 Vidar: Actually this is not
    // strong enough. This allows queries that are not tree shaped when direction is
    // ignored.
    private DirectedAcyclicGraph<Variable, LabeledEdge> graph;

    // Map from each datatype variable to a set of filters.
    private Map<Variable, Set<Filter>> filters;

    // Constructor. 2019: Tom wrote somehting better.
    public VqsQuery(Ontology ontology, String sparqlRepresentation, String rootVariableName) throws Exception {
        this.sparqlRepresentation = sparqlRepresentation;
        this.ontology = ontology;
        this.graph = new DirectedAcyclicGraph<>(LabeledEdge.class);
        this.filters = new HashMap<Variable, Set<Filter>>();

        // Construct a visitor and visit.
        SparqlQueryVisitor partialQueryVisitor = new SparqlQueryVisitor(sparqlRepresentation);
        // Collect the objects found by the visitor
        // Maps to keep an overview of all the variables we create
        Map<String, Variable> variables = new HashMap<String, Variable>();

        // Prepare the statementpatterns
        Set<StatementPattern> remainingStatementPatterns = new HashSet<StatementPattern>();
        // Add each statementpattern from the visitor
        for (StatementPattern i : partialQueryVisitor.statementPatterns)
            remainingStatementPatterns.add(i);

        // Find all concept variables by looking for rdf:type, store them in a map.
        for (Iterator<StatementPattern> it = remainingStatementPatterns.iterator(); it.hasNext();) {
            StatementPattern sp = it.next();

            if (sp.getPredicateVar().getValue().toString().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
                String subjectLabel = sp.getSubjectVar().getName();
                String objectLabel = sp.getObjectVar().getValue().toString();
                variables.put(subjectLabel, new ConceptVariable(subjectLabel, objectLabel));
                it.remove();
            }
        }

        // Find all other variables. They must be datatype variables
        for (Iterator<StatementPattern> it = remainingStatementPatterns.iterator(); it.hasNext();) {
            StatementPattern sp = it.next();

            // If both the subject and the object are variables
            if (sp.getSubjectVar().getValue() == null && sp.getObjectVar().getValue() == null) {
                String subjectName = sp.getSubjectVar().getName();
                String predicate = sp.getPredicateVar().getValue().stringValue();
                String objectName = sp.getObjectVar().getName();

                // Both are object vars
                if (variables.containsKey(subjectName) && variables.containsKey(objectName)) {
                    System.out.println("Two concepts. Do nothing for now");
                }

                if (!variables.containsKey(subjectName) && variables.containsKey(objectName)) {
                    System.out.println(
                            "WARNInG!!! I do not think this ever happens in a vqs-formatted query, so I have not implemenetd it..");
                    System.in.read();
                }

                if (variables.containsKey(subjectName) && !variables.containsKey(objectName)) {
                    String dpType = ontology.getPropertyTargetType(variables.get(subjectName).getType(), predicate);
                    DatatypeVariable objectVariable = new DatatypeVariable(objectName, dpType);
                    variables.put(objectName, objectVariable);
                }
            }
        }

        // Setting the root. This assumes that the root variable already exists in
        // variables
        Variable root = variables.get(rootVariableName);
        graph.addVertex(root);
        this.root = root;

        // Doing a breath first search to populate the graph with more nodes.
        // Variable names we want and will include in the query, but which has not been
        // searched for in the statements yet.
        Set<String> variableNamesToSearchFor = new HashSet<String>();
        variableNamesToSearchFor.add(rootVariableName);

        while (!variableNamesToSearchFor.isEmpty()) {
            HashSet<String> nextRound = new HashSet<String>();

            for (Iterator<StatementPattern> spIt = remainingStatementPatterns.iterator(); spIt.hasNext();) {
                StatementPattern sp = spIt.next();

                // If both the subject and the object are variables
                if (sp.getSubjectVar().getValue() == null && sp.getObjectVar().getValue() == null) {
                    String subjectName = sp.getSubjectVar().getName();
                    String predicate = sp.getPredicateVar().getValue().stringValue();
                    String objectName = sp.getObjectVar().getName();

                    // Get variables for the subject and object
                    Variable subjectVariable = variables.get(subjectName);
                    Variable objectVariable = variables.get(objectName);

                    if (variableNamesToSearchFor.contains(subjectName)) {
                        graph.addVertex(subjectVariable);
                        graph.addVertex(objectVariable);
                        graph.addEdge(subjectVariable, objectVariable, new LabeledEdge(predicate));
                        nextRound.add(objectName);
                        spIt.remove();

                    }

                    if (variableNamesToSearchFor.contains(objectName)) {
                        graph.addVertex(subjectVariable);
                        graph.addVertex(objectVariable);
                        graph.addEdge(objectVariable, subjectVariable, new LabeledEdge(predicate + "_inverseProp"));
                        nextRound.add(subjectName);
                        spIt.remove();
                    }
                }
            }

            variableNamesToSearchFor = nextRound;
        }

        // Loop over to find all compare filters
        for (ValueExpr filter : partialQueryVisitor.compareFilters) {
            Compare f = (Compare) filter;
            String datatypeVariableName = ((Var) f.getLeftArg()).getName();
            Variable datatypeVariable = variables.get(datatypeVariableName);
            Value value = ((ValueConstant) f.getRightArg()).getValue();

            // Associate datatypeVariable with the filter by putting it in the filter map
            if (filters.get(datatypeVariable) == null)
                filters.put(datatypeVariable, new HashSet<Filter>());

            filters.get(datatypeVariable).add(new Filter(f.getOperator(), value));
        }

        // Loop over to find all regex filters
        for (ValueExpr filter : partialQueryVisitor.regexFilters) {
            Regex f = (Regex) filter;
            String datatypeVariableName = ((Var) f.getLeftArg()).getName();
            Variable datatypeVariable = variables.get(datatypeVariableName);
            Value value = ((ValueConstant) f.getRightArg()).getValue();

            // Associate datatypeVariable with the filter by putting it in the filter map
            if (filters.get(datatypeVariable) == null)
                filters.put(datatypeVariable, new HashSet<Filter>());

            filters.get(datatypeVariable).add(new Filter(CompareOp.EQ, value));
        }

    }

    public Ontology getOntology() {
        return this.ontology;
    }

    public String getSparqlRepresentation() {
        return this.sparqlRepresentation;
    }

    // This displays the abstract query as a string. Used for output.
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
