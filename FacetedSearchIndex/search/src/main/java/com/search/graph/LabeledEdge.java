package com.search.graph;

import org.jgrapht.graph.DefaultEdge;

// Custom class to represent edges with a label.
// Used by jgrapht when representing edges in a query graph.
public class LabeledEdge extends DefaultEdge implements Comparable<LabeledEdge> {
    private String label;

    public LabeledEdge(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public String toString() {
        return label.substring(label.lastIndexOf("#") + 1);
    }

    public int compareTo(LabeledEdge otherEdge) {
        return label.compareTo(otherEdge.label);
    }
}
