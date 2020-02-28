package csp.main;

import java.util.*;
import java.util.stream.Collectors;

import abscon.instance.components.PConstraint;
import abscon.instance.components.PVariable;

import utils.LOG;
import utils.Utils;

public class BackTracking {
    LOG log = new LOG(false);

    public String problemName;
    public Double firstSolCpu;
    public Long firstSolCc;
    public Long firstSolNumNodeVisited;
    public Long firstSolNumBacktrack;

    public Long cpu;
    public Long cc;
    public Long numNodeVisited;//incremented in bt-label function
    public Long numBacktrack;
    public Long numSolution;
    public String variableOrderingHeuristic;
    public String variableStaticDynamic;
    public String valueOderingHeuristic;
    public String valueStaticDynamic;
    public String generatedStat;


    private Map<PVariable, Integer> variablesAssignment;

    private List<PConstraint> constraints;
    private List<PVariable> variables;

    public BackTracking() {
        problemName = "";
        firstSolCpu = 0.0;
        firstSolCc = 0L;
        firstSolNumBacktrack = 0L;
        firstSolNumNodeVisited = 0L;
        cpu = 0L;
        cc = 0L;
        numNodeVisited = 0L;
        numBacktrack = 0L;
        numSolution = 0L;
        variableOrderingHeuristic = "NA";
        variableStaticDynamic = "static";
        valueOderingHeuristic = "NA";
        valueStaticDynamic = "NA";
        variablesAssignment = new HashMap<>();
        generatedStat = "";
    }

    public BackTracking(String problemName,
                        List<PVariable> variables, List<PConstraint> constraints) {
        this();
        this.problemName = problemName;
        this.variables = variables;
        this.constraints = constraints;
    }

    enum BacktrackStatus {
        UNKNOWN,
        SOLUTION,
        IMPOSSIBLE
    }

    //bcssp: Binary constraint satisfaction “search” problem
    public void bcssp() {
        this.generatedStat += getInitReport();
        Long startTime = Utils.getCpuTimeInNano();
        this.consistent = true;
        BacktrackStatus status = BacktrackStatus.UNKNOWN;
        int index = 0;
        log.info("After applying order: " + this.varNameList());
        while (status == BacktrackStatus.UNKNOWN) {
//            log.info("index " + index);
            log.debug("index " + index);
            if (consistent) {
                log.debug("btLabel()");
                index = btLabel(index);
            } else {
                log.debug("Unlabel()");
                this.numBacktrack += 1;
                if (index == 0){
                    if (this.numSolution > 0) {
                        status = BacktrackStatus.SOLUTION;
                    }
                    break;
                }
                index = btUnlabel(index);
            }
            log.debug(varAssignment2String()+"\n");
            if (index > variables.size()-1) {

                this.numSolution+=1;
                if (this.numSolution == 1) {
                    this.cpu = Utils.getCpuTimeInNano() - startTime;
                    this.generatedStat += this.getFirstSolutionStat();
                }
                log.info("Solution:" + this.getSolutionInStr());
                index = this.variables.size() - 1; //this will later force label to be called on the last variable with next value
                this.consistent = this.variables.get(this.variables.size() - 1).currentDomain.currentVals.size() > 0;
                this.variables.get(this.variables.size() - 1).currentDomain.currentVals.remove(0);
            } else if (index < 0 && this.numSolution > 0) {
                status = BacktrackStatus.SOLUTION;
                break;
            } else if (index < 0) {
                status = BacktrackStatus.IMPOSSIBLE;
                break;
            }
        }
        this.cpu = Utils.getCpuTimeInNano() - startTime;
        this.generatedStat += this.getAllSolutionStat();
        log.info(status.toString());
        log.stdout(this.generatedStat);
        return;
    }
    private boolean consistent;
    private int btLabel(int varIndex) {
        PVariable vari = this.variables.get(varIndex);
        this.consistent = false;

        List<Integer> currentDomainVals = new ArrayList<>(vari.currentDomain.currentVals);

        for (int i=0; i<currentDomainVals.size() && !consistent; i++) {
            Integer currentDomainVal = currentDomainVals.get(i);
            variablesAssignment.put(vari, currentDomainVal);
            this.consistent = true;
            this.numNodeVisited += 1;
            for (int h=0; h<varIndex; h++) {
                if (!consistent) { break; }
                PVariable pastVar = variables.get(h);
                //TODO: Double check this. should this be an && or || ???
                consistent = check(vari, variablesAssignment.get(vari),
                                    pastVar, variablesAssignment.get(pastVar), false) &&
                                check(pastVar, variablesAssignment.get(vari),
                                    vari, variablesAssignment.get(pastVar), true);
                if (!consistent) {
                    vari.currentDomain.removeCurrentValByVal(currentDomainVal);
                }
            }
        }
        if (consistent) {
            return varIndex + 1;
        }
        return varIndex;
    }

    private Integer btUnlabel(int varIndex) {
        PVariable vari = this.variables.get(varIndex);
        vari.resetCurrentDomain();
        int h = varIndex - 1;
        PVariable pastVar = this.variables.get(h);
        pastVar.currentDomain.removeCurrentValByVal(variablesAssignment.getOrDefault(pastVar, null));
        this.consistent = (pastVar.currentDomain.currentVals.size() != 0);
        return h;

    }


    public boolean check(PVariable vari, Integer vali,
                         PVariable varj, Integer valj, boolean reverseVal) {
        List<PConstraint> cons = findConstraintByScope(
                new ArrayList<>(Arrays.asList(vari, varj)));
//        log.info("("+vari.getName()+","+varj.getName()+")"+"|"+
//                (cons != null ? cons.stream()
//                        .map(con -> con.getName())
//                        .collect(Collectors.joining(",")) : "null") );

        if (cons == null) {
            return true;
        } //universal constraint


        for (PConstraint con : cons) {
            this.cc ++;
            long val;
            if (!reverseVal) {
                val = con.computeCostOf(new int[]{vali, valj});
            } else {
                val = con.computeCostOf(new int[]{valj, vali});
            }
            if (val == 1) { return false; }
        }

        return true;
    }

    public List<PConstraint> findConstraintByScope(List<PVariable> scope) {
        List<PConstraint> res = new ArrayList<>();

        for (PConstraint con : this.constraints) {
            if (Utils.arr2List(con.getScope()).equals(scope) ) {
                res.add(con);
            }
        }
        if (res.size() == 0) { return null; }
        return res;
    }

    public void sortVariableDEG(Set<PVariable> visited) {
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

    public void sortVariableDD(Set<PVariable> visited) {
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
//            return degree2 - degree1;
            double ddr1 = (double)var1.currentDomain.currentVals.size()/(double)degree1;
            double ddr2 = (double)var2.currentDomain.currentVals.size()/(double)degree2;
            return ddr1 > ddr2 ? 1 : -1;
        });

    }

    public void preOrderVariableOrValue(String methods) {
        if (methods.equalsIgnoreCase("LX")) { //lexicographical ordering heuristic
            this.variableOrderingHeuristic = "id-var-st";
            Collections.sort(this.variables, (v1, v2) -> {
                return v1.getName().compareTo(v2.getName());
            });
        } else if (methods.equalsIgnoreCase("LD")) { // least domain ordering heuristic
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
        } else {
            LOG.error("Invalid preOrdering methods: " + methods);
        }
    }

    public void keepNodeConsistent() {
        for (PConstraint con : this.constraints) {
            if (con.getScope().length != 1) { continue; }

            List<Integer> domainVals = new ArrayList<>(
                    con.getScope()[0].currentDomain.currentVals);
            for (Integer vali : domainVals) {
                if (!con.isSupportedBy(new int[]{vali, vali})) {
                    con.getScope()[0].currentDomain.removeCurrentValByVal(vali);
                    con.getScope()[0].getDomain().removeCurrentValByVal(vali);
                }
            }
        }
    }

    public String getInitReport() {
        return "Instance name: " + this.problemName + "\n" +
                "variable-order-heuristic: " + this.variableOrderingHeuristic + "\n" +
                "var-static-dynamic: " + this.variableStaticDynamic + " \n" +
                "value-ordering-heuristic: " + this.valueOderingHeuristic + "\n" +
                "val-static-dynamic: " + this.valueOderingHeuristic + "\n";
    }

    public String getSolutionInStr() {
        return this.variables.stream()
                .map(var -> this.variablesAssignment.get(var).toString())
                .collect(Collectors.joining(","));
//        return this.variablesAssignment.values()
//                .stream().map(val -> val.toString())
//                .collect(Collectors.joining(","));
    }
    public String getFirstSolutionStat() {
        String firstSolution = this.getSolutionInStr();
        firstSolution += "\t(" +
                this.variablesAssignment.keySet().stream().map(var -> var.getName())
                        .collect(Collectors.joining(",")) + ")";
        firstSolCpu = this.getCpuTimeInMicro();
        firstSolCc = this.cc;
        firstSolNumBacktrack = this.numBacktrack;
        firstSolNumNodeVisited = this.numNodeVisited;
        return "cc: " + this.cc + "\n" +
                "nv: " + this.numNodeVisited + "\n" +
                "bt: " + this.numBacktrack + "\n" +
                "cpu: " + this.getCpuTimeInMicro() + "\n" +
                "First solution: " + firstSolution + "\n";
    }

    public String getAllSolutionStat() {
        return "all-sol cc: " + this.cc + "\n" +
                "all-sol nv: " + this.numNodeVisited + "\n" +
                "all-sol bt: " + this.numBacktrack + "\n" +
                "all-sol cpu: " + this.getCpuTimeInMicro() + "\n" +
                "Number of solutions: " + this.numSolution + "\n";
    }

    public Double getCpuTimeInMicro() {
        return (double) this.cpu / 100000.0;
    }

    public String varsCurrentDomain2Str() {
        String res = "";
        for (PVariable var : variables) {
            res += var.getName() + ": " + var.currentDomain.currentVals.toString() + "\n";
        }
        return res;
    }

    public String varAssignment2String() {
        return this.variablesAssignment.values()
                .stream().map(val -> val.toString())
                .collect(Collectors.joining(","));
    }

    public String varNameList() {
        return this.variables.stream()
                .map(var -> var.getName())
                .collect(Collectors.joining(", "));
    }
}
