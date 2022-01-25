package com.search.utils;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.algebra.Compare.CompareOp;

// A filter, usually related to a specific query variable.
public class Filter {
    // The operator between the variable and the value.
    private CompareOp operator;
    // The value to compare to
    private Value value;

    public Filter(CompareOp operator, Value value) {
        this.operator = operator;
        this.value = value;
    }

    public CompareOp getOperator() {
        return operator;
    }

    public Value getValue() {
        return value;
    }

    public String toString() {
        return operator.toString() + value.toString();

    }
}
