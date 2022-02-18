package com.search.types;

import java.util.HashSet;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "packages", "indiciesToCreateAtStartup" })
public class Config {
    private List<Package> packages;
    private HashSet<String> indiciesToCreateAtStartup;

    @JsonCreator
    public Config(@JsonProperty(value = "packages", required = true) List<Package> packages,
            @JsonProperty(value = "indiciesToCreateAtStartup", required = true) HashSet<String> indiciesToCreateAtStartup) {
        this.packages = packages;
        this.indiciesToCreateAtStartup = indiciesToCreateAtStartup;
    }

    public List<Package> getPackages() {
        return packages;
    }

    public void setPackages(List<Package> packages) {
        this.packages = packages;
    }

    public HashSet<String> getIndiciesToCreateAtStartup() {
        return indiciesToCreateAtStartup;
    }

    public void setIndiciesToCreateAtStartup(HashSet<String> indiciesToCreateAtStartup) {
        this.indiciesToCreateAtStartup = indiciesToCreateAtStartup;
    }
}
