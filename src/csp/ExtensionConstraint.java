package csp;

import java.util.HashSet;
import java.util.List;

public class ExtensionConstraint extends Constraint {
    Relation relation;
    public ExtensionConstraint(String name, int arity, List<Variable> scope, Relation relation) {
        super(name, arity, scope);
        this.relation = relation;
    }
    public String toFullString() {
        return "Name: " + name +
                ", variables: " + Variable.varsToString(new HashSet<>(scope)) +
                ", definition: " + relation.semantics + " " + relation.tuplesToString() + "\n";
    }

    @Override
    public boolean isSupportedBy(int[] vals) {
        for (int[] tup : this.relation.tuples) {
            if (tup[0] == vals[0] && tup[1] == vals[1]) {
                if (this.relation.semantics.equals("conflicts")) {
                    return false;
                }
                return true;
            }
        }
        if (this.relation.semantics.equals("conflicts")) {
            return true;
        }
        return false;
    }
}
