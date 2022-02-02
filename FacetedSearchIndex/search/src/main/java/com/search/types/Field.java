package com.search.types;

public class Field {
    public String name;
    public DataType type;

    public Field(int id, DataType type) {
        this.name = Constants.FIELD_PREFIX + id;
        this.type = type;
    }
}
