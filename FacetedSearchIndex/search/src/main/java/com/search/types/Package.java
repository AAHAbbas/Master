package com.search.types;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "ontology", "dataset", "indices" })
public class Package {
    private Ontology ontology;
    private Dataset dataset;
    private List<Index> indices = null;

    @JsonCreator
    public Package(@JsonProperty(value = "ontology", required = true) Ontology ontology,
            @JsonProperty(value = "dataset", required = true) Dataset dataset,
            @JsonProperty(value = "indices", required = true) List<Index> indices) {
        this.ontology = ontology;
        this.dataset = dataset;
        this.indices = indices;
    }

    public Ontology getOntology() {
        return ontology;
    }

    public void setOntology(Ontology ontology) {
        this.ontology = ontology;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    public List<Index> getIndices() {
        return indices;
    }

    public void setIndices(List<Index> indicies) {
        this.indices = indicies;
    }
}
