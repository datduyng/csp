package csp.old;

import java.util.HashSet;
import java.util.Set;

public class Domain {
    String name;
    Set<Integer> values;

    public Domain(String name, Set<Integer> values) {
        this.name = name;
        this.values = new HashSet<>(values);
    }

    public String valuesToString() {
        String valInStr = values.toString();
        return "{" + valInStr + "}";
    }
    public String toString() {
        return valuesToString();
    }

    public static Domain deepCopy(Domain other) {
        return new Domain(other.name, other.values);
    }
}
