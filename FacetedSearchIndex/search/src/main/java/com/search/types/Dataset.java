package com.search.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "type", "endpoint" })
public class Dataset {
    private String name;
    private DatasetType type;
    private String endpoint;

    @JsonCreator
    public Dataset(@JsonProperty(value = "name", required = true) String name,
            @JsonProperty(value = "type", required = true) DatasetType type,
            @JsonProperty(value = "endpoint", required = true) String endpoint) {
        this.name = name;
        this.type = type;
        this.endpoint = endpoint;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DatasetType getType() {
        return type;
    }

    public void setType(DatasetType type) {
        System.out.println(type);
        this.type = type;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
