package com.search.types;

public enum FilterOperator {
    /** equal to */
    EQ("="),

    /** not equal to */
    NE("!="),

    /** lower than */
    LT("<"),

    /** lower than or equal to */
    LE("<="),

    /** greater than or equal to */
    GE(">="),

    /** greater than */
    GT(">"),

    /** regex operator */
    REG("regex");

    private String symbol;

    FilterOperator(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}
