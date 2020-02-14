package csp.old;

import java.util.*;
import java.util.stream.Collectors;

public class Variable {
    String name;
    Domain initialDomain;
    Domain currentDomain;
    List<Constraint> constraints;
    Set<Variable> neighbors;

    public Variable(String name, Domain initialDomain, Domain currentDomain) {
        this.name = name;
        this.initialDomain = initialDomain;
        this.currentDomain = currentDomain;
        this.constraints = new ArrayList<>();
        this.neighbors = new HashSet<>();
    }
    public String getName() { return this.name; }


    public static String varsToString(Set<Variable> vars) {
        String varsInStr = String.join(",",
                vars.stream().map(Variable::getName).collect(Collectors.toList()));
        return "{" + varsInStr + "}";
    }

    public String toListOfContraintNames() {
        return "{" + String.join(",",
                        constraints
                            .stream()
                            .map(c -> c.name)
                            .collect(Collectors.toList())) +  "}";
    }
    public String toString() {
        return "name: " + name +
                ", init-domain: " + initialDomain.toString() +
                ", constraints: " + toListOfContraintNames() +
                ", neighbors: " + varsToString(neighbors);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) { return false; }
        if (this == o) { return true; }

        // instanceof Check and actual value check
        if ((o instanceof Variable) && (((Variable) o).name.equals(this.name))) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
}
