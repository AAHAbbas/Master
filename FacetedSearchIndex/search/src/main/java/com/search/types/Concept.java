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

    private String root; // required field

    @JsonProperty("addAllMissingDatatypePropertiesToAllVariables")
    @JsonSetter(nulls = Nulls.SKIP)
    private Boolean addAllMissingDatatypePropertiesToAllVariables; // not required field, defaults to true

    @JsonProperty("addAllMissingDatatypePropertiesToVariable")
    @JsonSetter(nulls = Nulls.SKIP)
    private String addAllMissingDatatypePropertiesToVariable; // not required field, defaults to null

    @JsonProperty("addAllMissingObjectPropertiesToAllVariables")
    @JsonSetter(nulls = Nulls.SKIP)
    private Boolean addAllMissingObjectPropertiesToAllVariables; // not required field, defaults to false

    @JsonProperty("addAllMissingObjectPropertiesToVariable")
    @JsonSetter(nulls = Nulls.SKIP)
    private String addAllMissingObjectPropertiesToVariable; // not required field, defaults to null

    @JsonCreator
    public Concept(@JsonProperty(value = "variables", required = true) List<String> variables,
            @JsonProperty(value = "root", required = true) String root) {
        this.variables = variables;
        this.edges = new ArrayList<>();
        this.root = root;
        this.addAllMissingDatatypePropertiesToAllVariables = true;
        this.addAllMissingDatatypePropertiesToVariable = null;
        this.addAllMissingObjectPropertiesToAllVariables = false;
        this.addAllMissingObjectPropertiesToVariable = null;
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

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public Boolean getAddAllMissingDatatypePropertiesToAllVariables() {
        return addAllMissingDatatypePropertiesToAllVariables;
    }

    public void setAddAllMissingDatatypePropertiesToAllVariables(
            Boolean addAllMissingDatatypePropertiesToAllVariables) {
        this.addAllMissingDatatypePropertiesToAllVariables = addAllMissingDatatypePropertiesToAllVariables;
    }

    public String getAddAllMissingDatatypePropertiesToVariable() {
        return addAllMissingDatatypePropertiesToVariable;
    }

    public void setAddAllMissingDatatypePropertiesToVariable(String addAllMissingDatatypePropertiesToVariable) {
        this.addAllMissingDatatypePropertiesToVariable = addAllMissingDatatypePropertiesToVariable;
    }

    public Boolean getAddAllMissingObjectPropertiesToAllVariables() {
        return addAllMissingObjectPropertiesToAllVariables;
    }

    public void setAddAllMissingObjectPropertiesToAllVariables(Boolean addAllMissingObjectPropertiesToAllVariables) {
        this.addAllMissingObjectPropertiesToAllVariables = addAllMissingObjectPropertiesToAllVariables;
    }

    public String getAddAllMissingObjectPropertiesToVariable() {
        return addAllMissingObjectPropertiesToVariable;
    }

    public void setAddAllMissingObjectPropertiesToVariable(String addAllMissingObjectPropertiesToVariable) {
        this.addAllMissingObjectPropertiesToVariable = addAllMissingObjectPropertiesToVariable;
    }
}
