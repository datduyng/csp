package csp;

import abscon.instance.components.*;
import abscon.instance.tools.InstanceParser;

import java.util.*;

public class MyParser {
    private List<Variable> variables;
    private Map<String, Domain> mapOfDomains;
    private Map<String, Variable> mapOfVariables;
    private Map<String, Relation> mapOfRelations;
    private Map<String, Constraint> mapOfConstraints;
    InstanceParser parser;
    public MyParser() {
        variables = new ArrayList<>();
        mapOfDomains = new LinkedHashMap<>();
        mapOfVariables = new LinkedHashMap<>();
        mapOfRelations = new LinkedHashMap<>();
        mapOfConstraints = new LinkedHashMap<>();
        parser = new InstanceParser();
    }
    public MyParser(String filename) {
        this();
        parser.loadInstance(filename);
        parser.parse(false);
    }

    public ProblemInstance parse() {
        this.variables = new ArrayList<Variable>();

        /* Deserialize list of Domain */
        Map<String, PDomain> mapOfPDomains = parser.getMapOfDomains();

        for (Map.Entry<String, PDomain> entry : mapOfPDomains.entrySet()) {
            this.mapOfDomains.put(entry.getKey(),
                    new Domain(entry.getValue().getName(), entry.getValue().getValues()));
        }

        /* Deserialize List of Variable */
        for (PVariable pvar : parser.getVariables()) {
            String varDomain = pvar.getDomain().getName();
            this.mapOfVariables.put(pvar.getName(),
                    new Variable(pvar.getName(), this.mapOfDomains.get(varDomain), this.mapOfDomains.get(varDomain))
            );
        }

        /* Deserialize list of relation */
        Map<String, PRelation> mapOfPRelation = sortHashMapByKey(parser.getMapOfRelations());
        for (Map.Entry<String, PRelation> entry : mapOfPRelation.entrySet()) {
            PRelation pRelation = entry.getValue();
            this.mapOfRelations.put(entry.getKey(),
                    new Relation(pRelation.getName(), pRelation.getArity(), pRelation.getSemantics(), pRelation.getTuples())
            );
        }

        /* Deserialize list of Constraint*/
        Map<String, PConstraint> mapOfPConstraint = sortHashMapByKey(parser.getMapOfConstraints());
        for (Map.Entry<String, PConstraint> entry : mapOfPConstraint.entrySet()) {
            Constraint constraint = null;
            List<Variable> scope = new ArrayList<>();
            if (entry.getValue() instanceof PExtensionConstraint) {
                PExtensionConstraint pExConstraint  = (PExtensionConstraint) entry.getValue();
                Relation relation = this.mapOfRelations.get(pExConstraint.getRelation().getName());

                Arrays.stream(pExConstraint.getScope()).forEach(
                        (pvar) -> scope.add(this.mapOfVariables.get(pvar.getName()))
                );

                for (int i=0; i<scope.size(); i++) {
                    Set<Variable> t = addNeighbor(scope.get(i), scope);
                    scope.get(i).neighbors.addAll(t);
                }

                constraint = new ExtensionConstraint(
                        pExConstraint.getName(), pExConstraint.getArity(), scope, relation
                );
            } else {
                PIntensionConstraint pInConstraint = (PIntensionConstraint) entry.getValue();

                Arrays.stream(pInConstraint.getScope()).forEach(
                        (pvar) -> scope.add(this.mapOfVariables.get(pvar.getName()))
                );

                for (int i=0; i<scope.size(); i++) {
                    Set<Variable> t = addNeighbor(scope.get(i), scope);
                    scope.get(i).neighbors.addAll(t);
                }

                constraint = new IntensionConstraint(
                        pInConstraint.getName(), pInConstraint.getArity(), scope,
                        pInConstraint.getEffectiveParametersExpression(),
                        pInConstraint.getFunction(), pInConstraint.getUniversalPostfixExpression()
                );

            }

            this.mapOfConstraints.put(entry.getKey(), constraint);
            // reference constraint to its variable
            Constraint finalConstraint = constraint;
            constraint.scope.forEach(
                    (var) -> var.constraints.add(finalConstraint)
            );
        }

        return new ProblemInstance(parser.getPresentationName(), this.mapOfVariables, this.mapOfConstraints);
    }

    public <V> Map<String, V> sortHashMapByKey(Map<String, V> hmap) {
        List<Map.Entry<String, V>> list = new ArrayList<>(hmap.entrySet());
        Collections.sort(list, (e1, e2) -> {
            return e1.getKey().compareTo(e2.getKey());
        });
        Map<String, V> hm = new LinkedHashMap<String, V>();
        list.forEach((e) -> {
            hm.put(e.getKey(), e.getValue());
        });
        return hm;
    }

    public Set<Variable> addNeighbor(Variable var, List<Variable> vars) {
        vars.forEach((v) -> {
            if (!v.equals(var)) { var.neighbors.add(v); }
        });
        return var.neighbors;
    }

    public String mapOfVariablesToString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Variable> entry : mapOfVariables.entrySet()) {
            sb.append(entry.getValue().toString() + "\n");
        }
        return sb.append("\n").toString();
    }

    public String mapOfConstraintToString() {
        StringBuilder sb = new StringBuilder();
        mapOfConstraints.entrySet().forEach((entry) -> {
            ExtensionConstraint constraint = (ExtensionConstraint) entry.getValue();
            sb.append(constraint.toFullString());
        });
        return sb.toString();
    }
    public String toString() {
        return  "mapOfVariables: \n" + mapOfVariablesToString() + "" +
                "mapOfConstraints \n" + mapOfConstraintToString() + "\n";
    }

    public static void main(String[] args) {
        if (args.length == 2 && args[0].equals("-f")) {
            MyParser parser = new MyParser(args[1]);
            ProblemInstance pi = parser.parse();
            System.out.println(pi.toString());
        } else {
            System.out.println("Usage: -f <filename>");
        }

    }
}
