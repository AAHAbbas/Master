package com.search.utils;

import com.search.types.FilterOperator;

import org.eclipse.rdf4j.model.Value;

public class Filter {
    private FilterOperator operator; // The operator between the variable and the value
    private Value value; // The value to compare to

    public Filter(FilterOperator operator, Value value) {
        this.operator = operator;
        this.value = value;
    }

    public FilterOperator getOperator() {
        return operator;
    }

    public Value getValue() {
        return value;
    }

    public String toString() {
        return operator.toString() + value.toString();
    }
}
