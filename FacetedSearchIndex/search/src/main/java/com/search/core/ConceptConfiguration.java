package com.search.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.search.graph.ConceptEdge;
import com.search.graph.ConceptVariable;
import com.search.graph.DatatypeVariable;
import com.search.graph.LabeledEdge;
import com.search.graph.Variable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.jgrapht.graph.DirectedAcyclicGraph;

// Contains the graph which consists of variables of certain types
// The variable index data structures are used to keep track of the ids (indicies) of the variables
public class ConceptConfiguration implements Comparable<ConceptConfiguration> {
    private static final Logger LOGGER = LogManager.getLogger(ConceptConfiguration.class);
    private String id; // The id of the concept configuration
    private Ontology ontology; // The ontology the config
    private int variableCounter; // Counter used to set the label of each variable in the concept configuration
    private Set<Variable> added;
    private Map<Variable, Integer> variableMapping; // When a configuration is built, one can assign an index to
                                                    // each variable. This map keeps this information
    private List<Variable> variables;
    private Map<Variable, String> dataPropertyVariables; // Given a local concept config variable, this
                                                         // gives the corresponding dataproperty URI
    private ConceptVariable root; // The root of the graph
    private DirectedAcyclicGraph<Variable, LabeledEdge> graph; // The directed graph defining the configuration

    public ConceptConfiguration(Ontology ontology, String id, ConceptVariable root, List<ConceptVariable> vars,
            List<ConceptEdge> edges, Boolean addAllDataType, Boolean addAllObject,
            ConceptVariable dataTypePropertySource, ConceptVariable objectPropertySource) {
        this.graph = new DirectedAcyclicGraph<>(LabeledEdge.class);
        this.id = id;
        this.ontology = ontology;
        this.variableCounter = 0;

        setVariableIds(vars);
        setRoot(root);
        addEdges(edges);

        if (addAllDataType) {
            addAllMissingDatatypePropertiesToAllVariables();
        } else if (dataTypePropertySource != null) {
            addAllMissingDatatypePropertiesToVariable(dataTypePropertySource);
        }

        if (addAllObject) {
            addAllMissingObjectPropertiesToAllVariables();
        } else if (objectPropertySource != null) {
            addAllMissingObjectPropertiesToVariable(objectPropertySource);
        }

        calculateVariableOrdering();
    }

    // Extend graph with triples
    // TODO: Maybe do something smart with labels of variables?
    private void addEdges(List<ConceptEdge> edges) {
        for (ConceptEdge edge : edges) {
            addEdge(edge);
        }
    }

    private void addEdge(ConceptEdge edge) {
        graph.addVertex(edge.getSource());
        graph.addVertex(edge.getTarget());
        graph.addEdge(edge.getSource(), edge.getTarget(), new LabeledEdge(edge.getProperty()));
    }

    // Calculate the variable index. The index is stored both as a map and as a
    // list. This function must be called after the config is fully constructed
    private void calculateVariableOrdering() {
        LOGGER.info("Calculate variable ordering for the config with id " + this.id);
        variables = new ArrayList<Variable>();
        variableMapping = new HashMap<Variable, Integer>();
        added = new HashSet<>();
        dataPropertyVariables = new HashMap<Variable, String>();

        addVariables(root);
        for (int i = 0; i < variables.size(); i++) {
            variableMapping.put(variables.get(i), i);
        }

        // Make map of local variables and their corresponding attribute URI
        Set<LabeledEdge> localEdges = graph.outgoingEdgesOf(root);
        for (LabeledEdge edge : localEdges) {
            Variable targetNode = graph.getEdgeTarget(edge);

            if (targetNode instanceof DatatypeVariable) {
                dataPropertyVariables.put(targetNode, edge.getLabel());
            }
        }
    }

    // Given a concept configuration, this calculates an index to each variable in
    // the graph. This index is used to identify the variable later, and is used as
    // column name in the index
    private void addVariables(Variable variable) {
        if (!added.contains(variable)) {
            variables.add(variable);
            added.add(variable);
        }

        ArrayList<LabeledEdge> edges = new ArrayList<LabeledEdge>(graph.outgoingEdgesOf(variable));

        for (LabeledEdge edge : edges) {
            addVariables(graph.getEdgeTarget(edge));
        }
    }

    // Given a variable in the concept configuration. Add all the missing data
    // properties given by the ontology
    private void addAllMissingDatatypePropertiesToAllVariables() {
        for (ConceptVariable variable : getConceptVariables()) {
            addAllMissingDatatypePropertiesToVariable(variable);
        }
    }

    // Given a variable in the concept configuration. Add all the missing data
    // properties defined in the ontology
    private void addAllMissingDatatypePropertiesToVariable(ConceptVariable source) {
        try {
            LOGGER.info("Add all missing data properties to variable " + source);
            Set<Entry<String, String>> dataProperties;
            dataProperties = ontology.getDataPropertiesWithType(source.getType());

            for (Entry<String, String> entry : dataProperties) {
                String property = entry.getKey();
                String type = entry.getValue();

                if (!containsObjectPropertyOutFromVariable(source, property, type)) {
                    addEdge(new ConceptEdge(source, property, new DatatypeVariable(generateVariableId(), type)));
                }
            }

            LOGGER.info("Done adding missing data properties to variable [" + source.getLabel() + "]");
            System.out.println(this);
        } catch (Exception e) {
            LOGGER.error("Exception occured when trying to add missing datatype properties to a variable");
            e.printStackTrace();
        }
    }

    private void addAllMissingObjectPropertiesToAllVariables() {
        for (ConceptVariable variable : getConceptVariables()) {
            addAllMissingObjectPropertiesToVariable(variable);
        }
    }

    // Given a variable in the concept configuration. Add all the missing object
    // properties defined in the ontology
    public void addAllMissingObjectPropertiesToVariable(ConceptVariable source) {
        LOGGER.info("Add all missing object properties to variable");

        Set<Entry<String, String>> objectProperties = ontology.getObjectPropertiesWithType(source.getType());

        for (Entry<String, String> entry : objectProperties) {
            String property = entry.getKey();
            String type = entry.getValue();

            if (!containsObjectPropertyOutFromVariable(source, property, type)) {
                addEdge(new ConceptEdge(source, property, new ConceptVariable(generateVariableId(), type)));
            }
        }
    }

    // Given a variable in the configuration, search for outgoing edges going to a
    // concept variable of a certain type
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
    // variable
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

    private void setVariableIds(List<ConceptVariable> vars) {
        for (ConceptVariable variable : vars) {
            variable.setLabel(generateVariableId());
        }
    }

    // This function returns a new unique variable id to use in the
    // conceptConfiguration
    private String generateVariableId() {
        boolean collision;
        String variableId;

        do {
            collision = false;
            variableCounter += 1;
            variableId = "x" + Integer.toString(variableCounter);

            for (Variable v : graph.vertexSet()) {
                if (v.getLabel().equals(variableId)) {
                    collision = true;
                }
            }
        } while (collision);

        return variableId;
    }

    private void setRoot(ConceptVariable root) {
        this.root = root;
        graph.addVertex(this.root);
    }

    // Return the set of datatype variables
    public Set<DatatypeVariable> getDatatypeVariables() {
        Set<DatatypeVariable> result = new HashSet<DatatypeVariable>();
        Set<Variable> variables = graph.vertexSet();

        for (Variable variable : variables) {
            if (variable instanceof DatatypeVariable)
                result.add((DatatypeVariable) variable);
        }

        return result;
    }

    // Return the set of concept variables
    public Set<ConceptVariable> getConceptVariables() {
        Set<ConceptVariable> result = new HashSet<ConceptVariable>();
        Set<Variable> variables = graph.vertexSet();

        for (Variable variable : variables) {
            if (variable instanceof ConceptVariable)
                result.add((ConceptVariable) variable);
        }

        return result;
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

    public List<Variable> getVariables() {
        return variables;
    }

    public int getVariableOrder(Variable variable) {
        return variableMapping.get(variable);
    }

    public Map<Variable, Integer> getVariableMapping() {
        return variableMapping;
    }

    // Returns a map from datatype variables to the datatype property between the
    // root node and the variable
    public Map<Variable, String> getDataPropertyVariables() {
        return dataPropertyVariables;
    }

    public String toString() {
        LOGGER.debug("CC (id=" + this.getId() + ") (size=" + graph.vertexSet().size() + "):");

        if (graph.vertexSet().size() > 1000) {
            return "Too large graph to print";
        }

        return "\n---Concept config---\n  ROOT: " + this.root + "\n  GRAPH: " + graph.toString() + "\n";
    }

    @Override
    public int compareTo(ConceptConfiguration o) {
        return this.id.compareTo(o.id);
    }
}
