package com.search.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.search.graph.ConceptVariable;
import com.search.graph.DatatypeVariable;
import com.search.graph.LabeledEdge;
import com.search.graph.Variable;

import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.graph.ClassBasedEdgeFactory;

// The concept configuration. Right now this contains the graph, which contains variables of certain types.
// The variable index data structures are used to keep track of the ids (indicies) of the variables.
//TODO make a constructor which takes everything, and then calculates the variable index.
public class ConceptConfiguration {

    private String id; // The id of the concept configuration

    private Ontology ontology; // The ontology the config is over.

    private int variableCounter; // Counter used to set the label of each variable in the concept configuration.

    private boolean variableOrderingHasBeenCalculated; // This is set to false when the variables has been ordered and
                                                       // given an index/number.
    private Map<Variable, Integer> variableOrderingMap; // When a configuration is built, one can assign an index to
                                                        // each variable. This map keeps this information.
    private List<Variable> variableOrderingList; // Same data as above, just given as a list.
    private Map<Variable, String> localVariablesToDatatypePropertyURI; // Given a local concept config variable, this
                                                                       // gives the corresponding dataproperty URI.

    private ConceptVariable root; // The root of the graph.
    private DirectedAcyclicGraph<Variable, LabeledEdge> graph; // The directed graph defining the configuration.

    // The only constructor
    public ConceptConfiguration(Ontology ontology, String id) {
        this.graph = new DirectedAcyclicGraph<Variable, LabeledEdge>(
                new ClassBasedEdgeFactory<Variable, LabeledEdge>(LabeledEdge.class));
        this.id = id;
        this.ontology = ontology;
        this.variableOrderingHasBeenCalculated = false;
        this.variableCounter = 0;
    }

    // Extend graph with triple. TODO: Maybe do something smart with labels of
    // variables?
    public void addEdge(Variable sourceVariable, String propertyURI, Variable targetVariable) throws IOException {
        // Extends the graph, but only if variable index is not calculated yet.
        if (!variableOrderingHasBeenCalculated) {
            graph.addVertex(sourceVariable);
            graph.addVertex(targetVariable);
            graph.addEdge(sourceVariable, targetVariable, new LabeledEdge(propertyURI));
        } else {
            System.out.println("WARNING! Cannot add edge because ordering of variables has been calculated.");
        }
    }

    // Set the graph after the config has been created
    public void setGraph(DirectedAcyclicGraph<Variable, LabeledEdge> g) {
        if (!variableOrderingHasBeenCalculated) {
            this.graph = g;
        }
    }

    // Can only set the root if variable index is not set.
    public void setRoot(ConceptVariable rootVariable) {
        if (!variableOrderingHasBeenCalculated) {
            this.root = rootVariable;
            graph.addVertex(this.root);
        }
    }

    // Return the id of the concept configuration
    public String getId() {
        return id;
    }

    public ConceptVariable getRoot() {
        return root;
    }

    public DirectedAcyclicGraph<Variable, LabeledEdge> getGraph() {
        return graph;
    }

    public String toString() {
        System.out.println("CC (id=" + this.getId() + ") (size=" + graph.vertexSet().size() + "):");

        if (graph.vertexSet().size() > 1000) {
            return "Too large graph to print";
        }

        return "\n---Concept config---\n  ROOT: " + this.root + "\n  GRAPH: " + graph.toString() + "\n";
    }

    public List<Variable> getVariableOrderingList() {
        // Calculate the variable index if not calculated yet
        if (!variableOrderingHasBeenCalculated)
            calculateVariableOrdering();

        return variableOrderingList;
    }

    public int getVariableOrdering(Variable v) {
        // Calculate the variable index if not calculated yet
        if (!variableOrderingHasBeenCalculated)
            calculateVariableOrdering();

        return variableOrderingMap.get(v);
    }

    public Map<Variable, Integer> getVariableOrderingMap() {
        if (!variableOrderingHasBeenCalculated)
            calculateVariableOrdering();

        return variableOrderingMap;
    }

    // Returns a map from datatype variables to the datatype property between the
    // root node and the variable.
    public Map<Variable, String> getLocalVariables() {
        if (!variableOrderingHasBeenCalculated)
            calculateVariableOrdering();

        return localVariablesToDatatypePropertyURI;
    }

    // Calculate the variable index. The index is stored both as a map and as a
    // list. This function must be called after the config is fully constructed.
    // TODO: Maybe we can add this to the constructor somehow? E.g have a
    // constructor where a whole tree is given, and then the variable index is
    // constructed after it is given.
    private void calculateVariableOrdering() {
        System.out.println("Calculate variable ordering for the cc with id " + this.id);
        variableOrderingList = new ArrayList<Variable>();
        variableOrderingMap = new HashMap<Variable, Integer>();
        localVariablesToDatatypePropertyURI = new HashMap<Variable, String>();

        recursiveAddVariablesToList(root);
        for (int i = 0; i < variableOrderingList.size(); i++) {
            variableOrderingMap.put(variableOrderingList.get(i), i);
        }

        // Make map of local variables and their corresponding attribute URI.
        Set<LabeledEdge> localEdges = graph.outgoingEdgesOf(root);
        for (LabeledEdge edge : localEdges) {
            Variable targetNode = graph.getEdgeTarget(edge);

            if (targetNode instanceof DatatypeVariable) {
                localVariablesToDatatypePropertyURI.put(targetNode, edge.getLabel());
            }

        }

        variableOrderingHasBeenCalculated = true;
    }

    // Given a concept configuration, this calculates an index to each variable in
    // the graph. This index is used to identify the variable later, and is used as
    // column name in the index.
    private void recursiveAddVariablesToList(Variable v) {
        variableOrderingList.add(v);

        ArrayList<LabeledEdge> edgeList = new ArrayList<LabeledEdge>(graph.outgoingEdgesOf(v));
        Collections.sort(edgeList);

        for (LabeledEdge e : edgeList) {
            recursiveAddVariablesToList(graph.getEdgeTarget(e));
        }
    }

    // Return the set of concept variables.
    public Set<ConceptVariable> getConceptVariables() {
        Set<ConceptVariable> conceptVariables = new HashSet<ConceptVariable>();
        Set<Variable> allVariables = graph.vertexSet();

        for (Variable variable : allVariables) {
            if (variable instanceof ConceptVariable)
                conceptVariables.add((ConceptVariable) variable);
        }

        return conceptVariables;
    }

    // Return the set of datatype variables.
    public Set<DatatypeVariable> getDatatypeVariables() {
        Set<DatatypeVariable> datatypeVariables = new HashSet<DatatypeVariable>();
        Set<Variable> allVariables = graph.vertexSet();

        for (Variable variable : allVariables) {
            if (variable instanceof DatatypeVariable)
                datatypeVariables.add((DatatypeVariable) variable);
        }

        return datatypeVariables;
    }

    // Given a variable in the configuration, search for outgoing edges going to a
    // concept variable of a certain type.
    public boolean containsTripleOutFromVariable(ConceptVariable sourceVariable, String propertyURI,
            String targetConceptURI) {
        Set<LabeledEdge> edges = graph.outgoingEdgesOf(sourceVariable);

        for (LabeledEdge edge : edges) {
            if (propertyURI.equals(edge.getLabel()) && graph.getEdgeTarget(edge).getType().equals(targetConceptURI))
                return true;
        }

        return false;
    }

    // Return true if the graph contains the given data property out from the given
    // variable.
    public boolean containsObjectPropertyOutFromVariable(ConceptVariable sourceVariable, String propertyURI,
            String targetType) {
        Set<LabeledEdge> edges = graph.outgoingEdgesOf(sourceVariable);

        for (LabeledEdge edge : edges) {
            if (propertyURI.equals(edge.getLabel()) && targetType.equals(graph.getEdgeTarget(edge).getType())) {
                return true;
            }
        }

        return false;
    }

    // This function returns a new unique variable id to use in the
    // conceptConfiguration
    public String genVarId() {
        boolean collision;
        String candidateVariableId;

        do {
            collision = false;
            variableCounter += 1;
            candidateVariableId = "x" + Integer.toString(variableCounter);

            for (Variable v : graph.vertexSet()) {
                if (v.getLabel().equals(candidateVariableId)) {
                    collision = true;
                }
            }
        } while (collision);

        return candidateVariableId;
    }

    // Given a variable in the concept configuration. Add all the missing object
    // properties defined in the ontology.
    public void addAllMissingObjectPropertiesToVariable(ConceptVariable conceptVariable) throws Exception {
        System.out.println("Add all missing obj properties to variable");
        if (!variableOrderingHasBeenCalculated) {
            System.out.println("Calc");

            Set<Entry<String, String>> objectProperties = ontology
                    .getObjectPropertiesWithType(conceptVariable.getType());

            for (Entry<String, String> entry : objectProperties) {
                String objectPropertyURI = entry.getKey();
                String concept = entry.getValue();

                if (!containsObjectPropertyOutFromVariable(conceptVariable, objectPropertyURI, concept)) {
                    addEdge(conceptVariable, objectPropertyURI, new ConceptVariable(genVarId(), concept));
                }
            }
        } else {
            System.out.println("Warning. variable Ordering was not calculated");
        }
    }

    // Given a variable in the concept configuration. Add all the missing data
    // properties defined in the ontology.
    public void addAllMissingDatatypePropertiesToVariable(ConceptVariable conceptVariable) throws Exception {
        System.out.println("Add all missing data properties to variable");
        if (!variableOrderingHasBeenCalculated) {
            System.out.println("Calc");
            System.out.println(ontology);
            System.out.println(conceptVariable);
            Set<Entry<String, String>> dataProperties = ontology.getDataPropertiesWithType(conceptVariable.getType());

            for (Entry<String, String> entry : dataProperties) {
                String propertyURI = entry.getKey();
                String type = entry.getValue();

                if (!containsObjectPropertyOutFromVariable(conceptVariable, propertyURI, type)) {
                    addEdge(conceptVariable, propertyURI, new DatatypeVariable(genVarId(), type));
                }
            }
        }

        System.out.println("Done adding missing data properties to some variable");
        System.out.println(this);
    }

    // Given a variable in the concept configuration. Add all the missing data
    // properties given by the ontology.
    public void addAllMissingDatatypePropertiesToAllVariables() throws Exception {
        if (!variableOrderingHasBeenCalculated) {
            for (ConceptVariable cv : getConceptVariables()) {
                addAllMissingDatatypePropertiesToVariable(cv);
            }
        }
    }

    public void addAllMissingObjectPropertiesToAllVariables() throws Exception {
        if (!variableOrderingHasBeenCalculated) {
            for (ConceptVariable cv : getConceptVariables()) {
                addAllMissingObjectPropertiesToVariable(cv);
            }
        }
    }
}
