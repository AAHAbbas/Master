package com.search.types;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "type", "endpoint" })
public class Dataset {
    @JsonProperty("name")
    private String name;

    @JsonProperty("type")
    private DatasetType type;

    @JsonProperty("endpoint")
    private String endpoint;

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("type")
    public DatasetType getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(DatasetType type) {
        System.out.println(type);
        this.type = type;
    }

    @JsonProperty("endpoint")
    public String getEndpoint() {
        return endpoint;
    }

    @JsonProperty("endpoint")
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
