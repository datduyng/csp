package csp.main;

import java.util.*;
import java.util.stream.Collectors;

import abscon.instance.components.PConstraint;
import abscon.instance.components.PVariable;

import utils.LOG;
import utils.Utils;

public class BackTracking {
    LOG log = new LOG(true);

    public String problemName;
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
        cpu = 0L;
        cc = 0L;
        numNodeVisited = 0L;
        numBacktrack = 0L;
        numSolution = 0L;
        variableOrderingHeuristic = "NA";
        variableStaticDynamic = "NA";
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
        this.generatedStat += getInitReport();
    }

    enum BacktrackStatus {
        UNKNOWN,
        SOLUTION,
        IMPOSSIBLE
    }

    public String varsCurrentDomain2Str() {
        String res = "";
        for (PVariable var : variables) {
            res += var.getName() + ": " + var.currentDomain.currentVals.toString() + "\n";
        }
        return res;
    }
    //bcssp: Binary constraint satisfaction “search” problem
    public void bcssp() {
        Long startTime = Utils.getCpuTimeInNano();
        this.consistent = true;
        BacktrackStatus status = BacktrackStatus.UNKNOWN;
        int index = 0;
        while (status == BacktrackStatus.UNKNOWN) {
            log.debug("index " + index);
            if (consistent) {
                log.debug("btLabel()");
                index = btLabel(index);
            } else {
                this.numBacktrack += 1;
                if (index == 0){
                    if (this.numSolution > 0) {
                        status = BacktrackStatus.SOLUTION;
                    }
                    break;
                }
                index = btUnlabel(index);
            }
            log.debug(varsCurrentDomain2Str());
            if (index > variables.size()-1) {
                this.numSolution+=1;
                if (this.numSolution == 1) {
                    this.cpu = Utils.getCpuTimeInNano() - startTime;
                    this.generatedStat += this.getFirstSolutionStat();
                }

                index = index - 1; //this will later force label to be called on the last variable with next value
                this.variables.get(index).currentDomain.currentVals.remove(0);
                this.consistent = this.variables.get(index).currentDomain.currentVals.size() > 0;

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
        int h = varIndex - 1;
        vari.resetCurrentDomain();

        PVariable pastVar = this.variables.get(h);
        pastVar.currentDomain.removeCurrentValByVal(variablesAssignment.getOrDefault(pastVar, null));
        this.consistent = (pastVar.currentDomain.currentVals.size() != 0);
        return h;

    }


    public boolean check(PVariable vari, Integer vali,
                         PVariable varj, Integer valj, boolean reverseVal) {
        List<PConstraint> constraints = findConstraintByScope(
                new ArrayList<>(Arrays.asList(vari, varj)));
        if (constraints == null) {
//            log.debug("(" + vari.getName() + ", " + varj.getName() + ") | (" + vali +","+valj+")"+ ": " + "true (universal constraint)");
            return true;
        } //universal constraint

        this.cc ++;
        boolean res = false;
        for (PConstraint con : constraints) {
            long val;
            if (!reverseVal) {
                val = con.computeCostOf(new int[]{vali, valj});
            } else {
                val = con.computeCostOf(new int[]{valj, vali});
            }
            res = res || (val == 0);
        }

        String valInStr = !reverseVal ? "(" + vali +","+valj+")" : "(" + valj +","+vali+")";
//        log.debug("(" + vari.getName() + ", " + varj.getName() + ") | " + valInStr + ": " + res);
        return res;
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

    public String getInitReport() {
        return "Instance name: " + this.problemName + "\n" +
                "variable-order-heuristic: " + this.variableOrderingHeuristic + "\n" +
                "var-static-dynamic: " + this.variableStaticDynamic + " \n" +
                "value-ordering-heuristic: " + this.valueOderingHeuristic + "\n" +
                "val-static-dynamic: " + this.valueOderingHeuristic + "\n";
    }
    public String getFirstSolutionStat() {
        String firstSolution = this.variablesAssignment.values()
                .stream().map(val -> val.toString())
                .collect(Collectors.joining(","));
        firstSolution += "\t(" +
                this.variablesAssignment.keySet().stream().map(var -> var.getName())
                        .collect(Collectors.joining(",")) + ")";
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

}
