package csp;

import java.util.ArrayList;
import java.util.List;

public class Variable {
    String name;
    Domain initialDomain;
    Domain currentDomain;
    List<Constraint> constraints;
    List<Variable> neighbors;

    public Variable(String name, Domain initialDomain, Domain currentDomain) {
        this.name = name;
        this.initialDomain = initialDomain;
        this.currentDomain = currentDomain;
        this.constraints = new ArrayList<>();
        this.neighbors = new ArrayList<>();
    }

    public String varsToString(List<Variable> vars) {
        String res = "{";
        vars.forEach((v) -> {
            res.concat(", " + v.name);
        });
        return res + "}";
    }
    public String toString() {
        return "name: " + name +
                ", init-domain: " + initialDomain.toString() +
                ", currentDomain:" + currentDomain.toString() +
                ", constraints: " + constraints.toString() +
                ", neighbors: " + varsToString(neighbors);
    }
}
