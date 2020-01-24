package csp;


import java.util.Arrays;

public class Domain {
    String name;
    int[] values;

    public Domain(String name, int[] values) {
        this.name = name; this.values = values;
    }

    public String toString() {
        return name;
    }
}
