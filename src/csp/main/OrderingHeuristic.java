package csp.main;

import abscon.instance.components.PVariable;
import utils.LOG;

import java.util.*;
import java.util.stream.Collectors;

public class OrderingHeuristic {
    String variableOrderingHeuristic;
    List<PVariable> variables;

    public OrderingHeuristic(List<PVariable> variables) {
        this.variables = variables;
    }

    public void run(String methods) {
        if (methods == null || methods.equals("")) { return; }

        if (methods.equalsIgnoreCase("LX") ||
                methods.equalsIgnoreCase("dLX")) { //lexicographical ordering heuristic
            this.variableOrderingHeuristic = "id-var-st";
            Collections.sort(this.variables, (v1, v2) -> {
                return v1.getName().compareTo(v2.getName());
            });
        } else if (methods.equalsIgnoreCase("LD") ||
                methods.equalsIgnoreCase("dLD")) { // least domain ordering heuristic
            this.variableOrderingHeuristic = "ld-var-st";
            Collections.sort(this.variables, (v1, v2) -> {
                int dom1Size = v1.currentDomain.currentVals.size();
                int dom2Size = v2.currentDomain.currentVals.size();
                if (dom1Size == dom2Size) {
                    return v1.getName().compareTo(v2.getName());
                }
                return dom1Size - dom2Size;
            });

            /*
            TO order the variable. We start at the node with largest degree. Then remove
            The edges from other node.
             */
        } else if (methods.equalsIgnoreCase("DEG")) {//degree domain ordering heuristic
            this.variableOrderingHeuristic = "deg-var-st";
            Set<PVariable> visited = new HashSet<>();
            List<PVariable> ordered = new ArrayList<>();
            int numOfVar = this.variables.size();
            for (int i=0; i<numOfVar; i++) {
                sortVariableDEG(visited);
                visited.add(this.variables.get(0));
                ordered.add(this.variables.get(0));
                this.variables.remove(0);
            }
            this.variables = ordered;
        } else if (methods.equalsIgnoreCase("DD")) {// domain degree domain ordering heuristic
            this.variableOrderingHeuristic = "ddr-var-st";
            Set<PVariable> visited = new HashSet<>();
            List<PVariable> ordered = new ArrayList<>();
            int numOfVar = this.variables.size();
            for (int i=0; i<numOfVar; i++) {
                sortVariableDD(visited);
                visited.add(this.variables.get(0));
                ordered.add(this.variables.get(0));
                this.variables.remove(0);
            }
            this.variables = ordered;
        } else if (methods.equalsIgnoreCase("dDEG")) {
            Collections.sort(this.variables, (var1, var2) -> {
                return var1.neighbors.size() == var2.neighbors.size() ?
                        var1.getName().compareTo(var2.getName()) :
                        var2.neighbors.size() - var1.neighbors.size();
            });
        } else if (methods.equalsIgnoreCase("dDD")) {
            Collections.sort(this.variables, (var1, var2) -> {
                if (var1.neighbors.isEmpty()) { return 1; }
                if (var2.neighbors.isEmpty()) { return -1; }
                return var1.currentDomain.currentVals.size()/var1.neighbors.size() == var2.currentDomain.currentVals.size()/var2.neighbors.size() ?
                        var1.getName().compareTo(var2.getName()) :
                        var1.currentDomain.currentVals.size()/var1.neighbors.size() - var2.currentDomain.currentVals.size()/var2.neighbors.size();
            });
        } else {
            LOG.error("Invalid preOrdering methods: " + methods);
        }
    }

    private void sortVariableDEG(Set<PVariable> visited) {
        Collections.sort(this.variables, (var1, var2) -> {
            int degree1 = 0;
            for (PVariable var : var1.neighbors) {
                if (!visited.contains(var)) { degree1++; }
            }
            int degree2 = 0;
            for (PVariable var : var2.neighbors) {
                if (!visited.contains(var)) { degree2++; }
            }
            if (degree1 == degree2) {
                return var1.getName().compareTo(var2.getName());
            }
            return degree2 - degree1;
        });
    }

    private void sortVariableDD(Set<PVariable> visited) {
        Collections.sort(this.variables, (var1, var2) -> {
            int degree1 = 0;
            for (PVariable var : var1.neighbors) {
                if (!visited.contains(var)) { degree1++; }
            }
            int degree2 = 0;
            for (PVariable var : var2.neighbors) {
                if (!visited.contains(var)) { degree2++; }
            }
            if (degree1 == degree2 && var1.currentDomain.currentVals.size() == var2.currentDomain.currentVals.size()) {
                return var1.getName().compareTo(var2.getName());
            }
            double ddr1 = (double)var1.currentDomain.currentVals.size()/(double)degree1;
            double ddr2 = (double)var2.currentDomain.currentVals.size()/(double)degree2;
            return ddr1 > ddr2 ? 1 : -1;
        });
    }

    public void printOrdering() {
        LOG.stdout(varNameList());
    }
    private String varNameList() {
        return this.variables.stream()
                .map(var -> var.getName())
                .collect(Collectors.joining(", "));
    }
}
