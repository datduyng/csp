package csp;

import java.util.LinkedHashMap;
import java.util.Map;

public class ProblemInstance {
    String name;
    Map<String, Variable> mapOfVariables;
    Map<String, Constraint> mapOfConstraints;

    public ProblemInstance() {
        mapOfVariables = new LinkedHashMap<>();
        mapOfConstraints = new LinkedHashMap<>();
    }
    public ProblemInstance(String name,
                           Map<String, Variable> mapOfVariables,
                           Map<String, Constraint> mapOfConstraints) {
        this();
        this.name = name;
        this.mapOfVariables.putAll(mapOfVariables);
        this.mapOfConstraints.putAll(mapOfConstraints);
    }
    public String mapOfVariablesToString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Variable> entry : mapOfVariables.entrySet()) {
            sb.append(entry.getValue().toString() + "\n");
        }
        return sb.toString();
    }

    public String mapOfConstraintToString() {
        StringBuilder sb = new StringBuilder();
        mapOfConstraints.entrySet().forEach((entry) -> {
            Constraint constraint = entry.getValue();
            sb.append(constraint.toFullString());
        });
        return sb.toString();
    }

    public String toString() {
        return  "Instance name: " + this.name + "\n" +
                "Variables: \n" + mapOfVariablesToString() +
                "Constraints: \n" + mapOfConstraintToString() + "\n";
    }
}
