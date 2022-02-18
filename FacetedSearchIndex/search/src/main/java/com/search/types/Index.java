package com.search.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "path", "inUse" })
public class Index {
    private String name;
    private String path;
    private Boolean inUse;

    @JsonCreator
    public Index(@JsonProperty(value = "name", required = true) String name,
            @JsonProperty(value = "path", required = true) String path, @JsonProperty("inUse") Boolean inUse) {
        this.name = name;
        this.path = path;
        this.inUse = inUse;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Boolean getInUse() {
        return inUse;
    }

    public void setInUse(Boolean inUse) {
        this.inUse = inUse;
    }
}
