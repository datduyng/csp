package csp;

import java.util.Arrays;

public class Relation {
    String name;
    int arity;
    String semantics;
    int[][] tuples;

    public Relation(String name, int arity, String semantics, int[][] tuples) {
        this.name = name;
        this.arity = arity;
        this.semantics = semantics;
        this.tuples = tuples;
    }

    public String tuplesToString() {
        StringBuilder sb = new StringBuilder("{");
        Arrays.stream(tuples).forEach((tup) -> {
            sb.append("("+tup[0]+","+tup[1]+")");
        });
        return sb.append("}").toString();
    }

}
