package csp;


import java.util.Arrays;

public class Domain {
    String name;
    int[] values;

    public Domain(String name, int[] values) {
        this.name = name; this.values = values;
    }

    public String valuesToString() {
        StringBuilder sb = new StringBuilder("{");
        Arrays.stream(values).forEach((v) -> {
            sb.append(v+",");
        });
        return sb.append("}").toString();
    }
    public String toString() {
        return valuesToString();
    }
}
