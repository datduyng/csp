package csp.old;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    public static String arr2Str(int[] arr) {
        if (arr.length == 1) {
            return "( " + arr[0] + ")";
        }
        return "(" + arr[0] + "," + arr[1] + ")";
    }
    public String tuplesToString() {
        List<String> ss = Arrays.stream(tuples)
                .map(Relation::arr2Str)
                .collect(Collectors.toList());

        return "{" + String.join(",",ss) + "}";
    }

    public static boolean isTupleInTuples(int[] key, int[][] tuples) {
        for (int[] tup : tuples) {
            if (key[0] == tup[0] && key[1] == tup[1]) { return true; }
        }
        return false;
    }

}
