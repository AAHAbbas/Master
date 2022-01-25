package com.search.graph;

public abstract class Variable {
    // The id/label of the variable. Just an id representing the variable. E.g v1,
    // v10, a1, a2
    private String label;

    // The type of the variable, i.e, the associated node in the navigation graph.
    // E.g string, decimal, integer, Person, Company
    private String type;

    public Variable(String label, String type) {
        this.label = label;
        this.type = type;
    }

    public String toString() {
        return "(" + label + "|" + type.substring(type.lastIndexOf("#") + 1) + ")";
    }

    public String getLabel() {
        return label;
    }

    public String getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return 31 * 1 + ((label == null) ? 0 : label.hashCode());
    }

    // Two variables are equal if they have the same label.
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if ((obj == null) || (getClass() != obj.getClass()))
            return false;

        Variable other = (Variable) obj;

        if (label == null) {
            if (other.label != null)
                return false;
        } else if (!label.equals(other.label))
            return false;

        return true;
    }
}
