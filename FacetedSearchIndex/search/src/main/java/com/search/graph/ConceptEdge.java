package com.search.graph;

public class ConceptEdge {
    private Variable source;
    private String property;
    private Variable target;

    public ConceptEdge(Variable source, String property, Variable target) {
        this.source = source;
        this.property = property;
        this.target = target;
    }

    public Variable getSource() {
        return source;
    }

    public void setSource(Variable source) {
        this.source = source;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public Variable getTarget() {
        return target;
    }

    public void setTarget(Variable target) {
        this.target = target;
    }
}
