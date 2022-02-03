package com.search.utils;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.algebra.Compare.CompareOp;

public class Filter {
    private CompareOp operator; // The operator between the variable and the value
    private Value value; // The value to compare to

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
