package csp;

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

}
