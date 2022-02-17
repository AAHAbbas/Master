package com.search.types;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "packages", "indiciesToCreateAtStartup" })
public class Config {
    @JsonProperty("packages")
    private List<Package> packages = null;

    @JsonProperty("indiciesToCreateAtStartup")
    private List<String> indiciesToCreateAtStartup = null;

    @JsonProperty("packages")
    public List<Package> getPackages() {
        return packages;
    }

    @JsonProperty("packages")
    public void setPackages(List<Package> packages) {
        this.packages = packages;
    }

    @JsonProperty("indiciesToCreateAtStartup")
    public List<String> getIndiciesToCreateAtStartup() {
        return indiciesToCreateAtStartup;
    }

    @JsonProperty("indiciesToCreateAtStartup")
    public void setIndiciesToCreateAtStartup(List<String> indiciesToCreateAtStartup) {
        this.indiciesToCreateAtStartup = indiciesToCreateAtStartup;
    }
}
