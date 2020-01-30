package csp;


import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Domain {
    String name;
    int[] values;

    public Domain(String name, int[] values) {
        this.name = name; this.values = values;
    }

    public String valuesToString() {
        List<String> ss = Arrays.stream(values)
                .mapToObj(n -> String.valueOf(n))
                .collect(Collectors.toList());
        String valInStr = String.join(",", ss);
        return "{" + valInStr + "}";
    }
    public String toString() {
        return valuesToString();
    }
}
