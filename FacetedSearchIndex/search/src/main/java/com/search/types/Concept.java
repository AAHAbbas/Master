package com.search.types;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "variables",
        "edges",
        "root",
        "addAllMissingDatatypePropertiesToAllVariables",
        "addAllMissingDatatypePropertiesToVariable",
        "addAllMissingObjectPropertiesToAllVariables",
        "addAllMissingObjectPropertiesToVariable"
})
public class Concept {
    private List<String> variables; // required field

    @JsonProperty("edges")
    @JsonSetter(nulls = Nulls.SKIP)
    private List<Edge> edges; // not required field, defaults to an empty list

    private int root; // required field

    @JsonProperty("addAllMissingDatatypePropertiesToAllVariables")
    @JsonSetter(nulls = Nulls.SKIP)
    private Boolean addAllMissingDatatypePropertiesToAllVariables; // not required field, defaults to true

    @JsonProperty("addAllMissingDatatypePropertiesToVariable")
    @JsonSetter(nulls = Nulls.SKIP)
    private int addAllMissingDatatypePropertiesToVariable; // not required field, defaults to null

    @JsonProperty("addAllMissingObjectPropertiesToAllVariables")
    @JsonSetter(nulls = Nulls.SKIP)
    private Boolean addAllMissingObjectPropertiesToAllVariables; // not required field, defaults to false

    @JsonProperty("addAllMissingObjectPropertiesToVariable")
    @JsonSetter(nulls = Nulls.SKIP)
    private int addAllMissingObjectPropertiesToVariable; // not required field, defaults to null

    @JsonCreator
    public Concept(@JsonProperty(value = "variables", required = true) List<String> variables,
            @JsonProperty(value = "root", required = true) int root) {
        this.variables = variables;
        this.edges = new ArrayList<>();
        this.root = root;
        this.addAllMissingDatatypePropertiesToAllVariables = true;
        this.addAllMissingDatatypePropertiesToVariable = -1;
        this.addAllMissingObjectPropertiesToAllVariables = false;
        this.addAllMissingObjectPropertiesToVariable = -1;
    }

    public List<String> getVariables() {
        return variables;
    }

    public void setVariables(List<String> variables) {
        this.variables = variables;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public void setEdges(List<Edge> edges) {
        this.edges = edges;
    }

    public int getRoot() {
        return root;
    }

    public void setRoot(int root) {
        this.root = root;
    }

    public Boolean getAddAllMissingDatatypePropertiesToAllVariables() {
        return addAllMissingDatatypePropertiesToAllVariables;
    }

    public void setAddAllMissingDatatypePropertiesToAllVariables(
            Boolean addAllMissingDatatypePropertiesToAllVariables) {
        this.addAllMissingDatatypePropertiesToAllVariables = addAllMissingDatatypePropertiesToAllVariables;
    }

    public int getAddAllMissingDatatypePropertiesToVariable() {
        return addAllMissingDatatypePropertiesToVariable;
    }

    public void setAddAllMissingDatatypePropertiesToVariable(int addAllMissingDatatypePropertiesToVariable) {
        this.addAllMissingDatatypePropertiesToVariable = addAllMissingDatatypePropertiesToVariable;
    }

    public Boolean getAddAllMissingObjectPropertiesToAllVariables() {
        return addAllMissingObjectPropertiesToAllVariables;
    }

    public void setAddAllMissingObjectPropertiesToAllVariables(Boolean addAllMissingObjectPropertiesToAllVariables) {
        this.addAllMissingObjectPropertiesToAllVariables = addAllMissingObjectPropertiesToAllVariables;
    }

    public int getAddAllMissingObjectPropertiesToVariable() {
        return addAllMissingObjectPropertiesToVariable;
    }

    public void setAddAllMissingObjectPropertiesToVariable(int addAllMissingObjectPropertiesToVariable) {
        this.addAllMissingObjectPropertiesToVariable = addAllMissingObjectPropertiesToVariable;
    }
}
