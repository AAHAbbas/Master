package com.search.graph;

public class ConceptVariable extends Variable {
    public ConceptVariable(String label, String type) {
        super(label, type);
    }

    public ConceptVariable(String type) {
        super("x", type);
    }
}
