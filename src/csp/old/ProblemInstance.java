package csp.old;

import java.util.*;

public class ProblemInstance {
    String name;
    Map<String, Variable> mapOfVariables;
    Map<String, Constraint> mapOfConstraints;
    Map<LinkedHashSet<Variable>, List<Constraint>> variableConstraintMap;

    public ProblemInstance() {
        mapOfVariables = new LinkedHashMap<>();
        mapOfConstraints = new LinkedHashMap<>();
        variableConstraintMap = new HashMap<>();
    }
    public ProblemInstance(String name,
                           Map<String, Variable> mapOfVariables,
                           Map<String, Constraint> mapOfConstraints,
                           Map<LinkedHashSet<Variable>, List<Constraint>> variableConstraintMap) {
        this();
        this.name = name;
        this.mapOfVariables.putAll(mapOfVariables);
        this.mapOfConstraints.putAll(mapOfConstraints);
        this.variableConstraintMap.putAll(variableConstraintMap);
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
