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

    public MyParser() {
        variables = new ArrayList<>();
        mapOfDomains = new HashMap<>();
        mapOfVariables = new HashMap<>();
        mapOfVariables = new HashMap<>();
        mapOfRelations = new HashMap<>();
        mapOfConstraints = new HashMap<>();
    }
    public MyParser(String filename) {
        this();
        InstanceParser parser = new InstanceParser();
        parser.loadInstance(filename);
        parser.parse(false);

        this.variables = new ArrayList<Variable>();

        System.out.println("Instance name: <Not currently parsed! Need to modify the InstanceParser()>");

        System.out.println("Variables");

        for (int i = 0; i < parser.getVariables().length; i++) {
            System.out.println("parser.getVariables()[i]" + parser.getVariables()[i].toString());
        }

        System.out.println("map relation " + parser.getMapOfRelations().toString());


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
        Map<String, PRelation> mapOfPRelation = parser.getMapOfRelations();
        for (Map.Entry<String, PRelation> entry : mapOfPRelation.entrySet()) {
            PRelation pRelation = entry.getValue();
            this.mapOfRelations.put(entry.getKey(),
                    new Relation(pRelation.getName(), pRelation.getArity(), pRelation.getSemantics(), pRelation.getTuples())
            );
        }

        /* Deserialize list of Constraint*/
        Map<String, PConstraint> mapOfPConstraint = parser.getMapOfConstraints();
        for (Map.Entry<String, PConstraint> entry : mapOfPConstraint.entrySet()) {
            PExtensionConstraint pConstraint = (PExtensionConstraint) entry.getValue();
            Relation relation = this.mapOfRelations.get(pConstraint.getRelation().getName());
            List<Variable> scope = new ArrayList<>();
            Arrays.stream(pConstraint.getScope()).forEach(
                    (pvar) -> scope.add(this.mapOfVariables.get(pvar.getName()))
            );
            ExtensionConstraint constraint = new ExtensionConstraint(pConstraint.getName(), pConstraint.getArity(), scope, relation);
            this.mapOfConstraints.put(entry.getKey(), constraint);
            // reference constraint to its variable
            constraint.scope.forEach(
                    (var) -> var.constraints.add(constraint)
            );
        }
    }

    public String toString() {
        return "variables: " + variables.toString() + "\n" +
                "mapOfDomains" + mapOfDomains.toString() + "\n" +
                "mapOfVariables" + mapOfVariables.toString() + "\n" +
                "mapOfRelations" + mapOfRelations.toString() + "\n" +
                "mapOfConstraints" + mapOfConstraints.toString() + "\n";

    }

    public static void main(String[] args) {

        MyParser parser = new MyParser("./tests/4queens-supports.xml");

        System.out.println(parser.toString());
    }
}
