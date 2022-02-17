package com.search.types;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

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
    @JsonProperty("variables")
    private List<String> variables = null;

    @JsonProperty("edges")
    private List<Edge> edges = null;

    @JsonProperty("root")
    private String root = null;

    @JsonProperty("addAllMissingDatatypePropertiesToAllVariables")
    private Boolean addAllMissingDatatypePropertiesToAllVariables;

    @JsonProperty("addAllMissingDatatypePropertiesToVariable")
    private String addAllMissingDatatypePropertiesToVariable;

    @JsonProperty("addAllMissingObjectPropertiesToAllVariables")
    private Boolean addAllMissingObjectPropertiesToAllVariables;

    @JsonProperty("addAllMissingObjectPropertiesToVariable")
    private String addAllMissingObjectPropertiesToVariable;

    @JsonProperty("variables")
    public List<String> getVariables() {
        return variables;
    }

    @JsonProperty("variables")
    public void setVariables(List<String> variables) {
        this.variables = variables;
    }

    @JsonProperty("edges")
    public List<Edge> getEdges() {
        return edges;
    }

    @JsonProperty("edges")
    public void setEdges(List<Edge> edges) {
        this.edges = edges;
    }

    @JsonProperty("root")
    public String getRoot() {
        return root;
    }

    @JsonProperty("root")
    public void setRoot(String root) {
        this.root = root;
    }

    @JsonProperty("addAllMissingDatatypePropertiesToAllVariables")
    public Boolean getAddAllMissingDatatypePropertiesToAllVariables() {
        return addAllMissingDatatypePropertiesToAllVariables;
    }

    @JsonProperty("addAllMissingDatatypePropertiesToAllVariables")
    public void setAddAllMissingDatatypePropertiesToAllVariables(
            Boolean addAllMissingDatatypePropertiesToAllVariables) {
        this.addAllMissingDatatypePropertiesToAllVariables = addAllMissingDatatypePropertiesToAllVariables;
    }

    @JsonProperty("addAllMissingDatatypePropertiesToVariable")
    public String getAddAllMissingDatatypePropertiesToVariable() {
        return addAllMissingDatatypePropertiesToVariable;
    }

    @JsonProperty("addAllMissingDatatypePropertiesToVariable")
    public void setAddAllMissingDatatypePropertiesToVariable(String addAllMissingDatatypePropertiesToVariable) {
        this.addAllMissingDatatypePropertiesToVariable = addAllMissingDatatypePropertiesToVariable;
    }

    @JsonProperty("addAllMissingObjectPropertiesToAllVariables")
    public Boolean getAddAllMissingObjectPropertiesToAllVariables() {
        return addAllMissingObjectPropertiesToAllVariables;
    }

    @JsonProperty("addAllMissingObjectPropertiesToAllVariables")
    public void setAddAllMissingObjectPropertiesToAllVariables(Boolean addAllMissingObjectPropertiesToAllVariables) {
        this.addAllMissingObjectPropertiesToAllVariables = addAllMissingObjectPropertiesToAllVariables;
    }

    @JsonProperty("addAllMissingObjectPropertiesToVariable")
    public String getAddAllMissingObjectPropertiesToVariable() {
        return addAllMissingObjectPropertiesToVariable;
    }

    @JsonProperty("addAllMissingObjectPropertiesToVariable")
    public void setAddAllMissingObjectPropertiesToVariable(String addAllMissingObjectPropertiesToVariable) {
        this.addAllMissingObjectPropertiesToVariable = addAllMissingObjectPropertiesToVariable;
    }
}
