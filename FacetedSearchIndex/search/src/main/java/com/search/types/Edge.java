package com.search.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "source", "property", "target" })
public class Edge {
    private int source;
    private String property;
    private int target;

    @JsonCreator
    public Edge(@JsonProperty(value = "source", required = true) int source,
            @JsonProperty(value = "property", required = true) String property,
            @JsonProperty(value = "target", required = true) int target) {
        this.source = source;
        this.property = property;
        this.target = target;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = target;
    }
}
