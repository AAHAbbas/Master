package com.search.types;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "ontology", "dataset", "indicies" })
public class Package {
    @JsonProperty("ontology")
    private Ontology ontology;

    @JsonProperty("dataset")
    private Dataset dataset;

    @JsonProperty("indicies")
    private List<Index> indicies = null;

    @JsonProperty("ontology")
    public Ontology getOntology() {
        return ontology;
    }

    @JsonProperty("ontology")
    public void setOntology(Ontology ontology) {
        this.ontology = ontology;
    }

    @JsonProperty("dataset")
    public Dataset getDataset() {
        return dataset;
    }

    @JsonProperty("dataset")
    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    @JsonProperty("indicies")
    public List<Index> getIndicies() {
        return indicies;
    }

    @JsonProperty("indicies")
    public void setIndicies(List<Index> indicies) {
        this.indicies = indicies;
    }
}
