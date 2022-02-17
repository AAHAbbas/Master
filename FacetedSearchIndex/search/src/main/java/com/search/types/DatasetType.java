package com.search.types;

public enum DatasetType {
    RDFOX("RDFOX"),
    TRIPLESTORE("TRIPLESTORE");

    private String type;

    private DatasetType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
