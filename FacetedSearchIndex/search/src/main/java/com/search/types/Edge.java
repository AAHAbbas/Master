package com.search.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "source", "property", "target" })
public class Edge {
    private String source;
    private String property;
    private String target;

    @JsonCreator
    public Edge(@JsonProperty(value = "source", required = true) String source,
            @JsonProperty(value = "property", required = true) String property,
            @JsonProperty(value = "target", required = true) String target) {
        this.source = source;
        this.property = property;
        this.target = target;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
}
