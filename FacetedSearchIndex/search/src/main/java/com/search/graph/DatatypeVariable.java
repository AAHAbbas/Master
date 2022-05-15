package com.search.graph;

public class DatatypeVariable extends Variable {
    public DatatypeVariable(String label, String type) {
        super(label, type);
    }

    public DatatypeVariable(String type) {
        super("x", type);
    }
}
