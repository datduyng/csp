package csp.main;

import abscon.instance.components.PConstraint;
import abscon.instance.components.PVariable;
import utils.LOG;
import utils.Utils;

import java.util.*;
import java.util.stream.Collectors;

public class FC {
    public LOG log = new LOG(false);
    public String orderingHeuristic;
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
    public String generatedStat;
    public String csvReport;

    private List<PVariable> variables;
    private List<PConstraint> constraints;
    private Map<PVariable, Integer> variablesAssignment;
    private Map<PVariable, Reduction> reductionMap;
    private Map<PVariable, LinkedHashSet<PVariable>> futureFCMap;
    private Map<PVariable, LinkedHashSet<PVariable>> pastFCMap;
    private boolean consistent;

    public FC() {
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

        variablesAssignment = new HashMap<>();
        reductionMap = new HashMap<>();
        futureFCMap = new HashMap<>();
        pastFCMap = new HashMap<>();
        generatedStat = "";
        csvReport = "";
    }

    public FC(String problemName,
                     List<PVariable> variables, List<PConstraint> constraints) {
        this();
        this.problemName = problemName;
        this.variables = variables;
        this.constraints = constraints;
        for (int i=0; i<this.variables.size(); i++) {
            PVariable var = this.variables.get(i);
            reductionMap.put(var, new Reduction(var));
            futureFCMap.put(var, new LinkedHashSet<>());
            pastFCMap.put(var, new LinkedHashSet<>());
        }
    }

    String run(String reportType) {
        log.debug("Running Static FC .......");
        this.generatedStat += getInitReport();
        this.csvReport += this.problemName+","+"FC,"+this.orderingHeuristic+",";

        Long startTime = Utils.getCpuTimeInNano();
        consistent = true;
        BTStatus status = BTStatus.UNKNOWN;
        int index = 0;

        while (status == BTStatus.UNKNOWN) {
//            log.debug("index " + index);
            if (this.consistent) {
                index = fcLabel(index);
            } else {
                index = fcUnlabel(index);
                this.numBacktrack++;
            }
            if (index > this.variables.size() - 1) {
                this.numSolution++;
                if (this.numSolution == 1) {
                    this.cpu = Utils.getCpuTimeInNano() - startTime;
                    log.debug("First solution");
                    this.generatedStat += this.getFirstSolutionStat();
                    this.csvReport += this.getFirstSolutionForCsvReport();
                    break;
                }
                this.consistent = false;
                index = this.variables.size() - 1;
            } else if (index < 0 && numSolution > 0) {
                status = BTStatus.SOLUTION;
            } else if (index < 0) {
                status = BTStatus.IMPOSSIBLE;
            }
        }
        this.generatedStat += getAllSolutionStat();
        this.csvReport += joinStrs(new String[]{
                this.cc+"", this.numNodeVisited+"",
                this.numBacktrack+"", this.getCpuTimeInMillis()+"",
                this.numSolution+""
        });
        log.debug(status.toString());
        log.debug(this.generatedStat);
        log.debug("Number solution " + this.numSolution);
        if (reportType.equals("csv")) {
            return this.csvReport;
        }
        return this.generatedStat;
    }

    public String getFirstSolutionStat() {
        firstSolCc = this.cc;
        firstSolNumBacktrack = this.numBacktrack;
        firstSolNumNodeVisited = this.numNodeVisited;
        return "cc: " + this.cc + "\n" +
                "nv: " + this.numNodeVisited + "\n" +
                "bt: " + this.numBacktrack + "\n" +
                "cpu: " + this.getCpuTimeInMillis() + " ms\n";
    }

    public int fcLabel(int vi) {
        this.consistent = false;
        PVariable vari = this.variables.get(vi);
        List<Integer> currentDomainVals = new ArrayList<>(vari.currentDomain.currentVals);

        for (int i=0; i<currentDomainVals.size() && !this.consistent; i++) {
            this.consistent = true;
            this.numNodeVisited ++;
            int vali = currentDomainVals.get(i);
            variablesAssignment.put(vari, vali);
            for (int vh=vi+1; vh<this.variables.size() && this.consistent; vh++) {
                this.consistent = checkForward(vi, vali, vh);
            }
            if (!this.consistent) {
                vari.currentDomain.removeCurrentValByVal(vali);
                this.undoReduction(vi);
            }
        }
        if (this.consistent) { return vi+1; }
        return vi;
    }

    public int fcUnlabel(int vi) {
        int h = vi-1;
        if (h < 0) { return -1; }
        this.undoReduction(h);
        this.updatedCurrentDomain(vi);
        PVariable varh = this.variables.get(h);
        varh.currentDomain.removeCurrentValByVal(
                this.variablesAssignment.getOrDefault(varh, null)
        );
        this.consistent = !varh.currentDomain.currentVals.isEmpty();
        return h;

    }

    public boolean checkForward(int viIndex, int viVal, int vjIndex) {
        ArrayList<Integer> reduction = new ArrayList<>();
        PVariable vj = this.variables.get(vjIndex),
                  vi = this.variables.get(viIndex);
        for (Integer vjVal : vj.currentDomain.currentVals) {
            if (!(this.check(vi, viVal, vj, vjVal, true) &&
                    this.check(vj, viVal, vi, vjVal, false)) ) {
                reduction.add(vjVal);
            }
        }

        if (!reduction.isEmpty()) {
            for (Integer red : reduction) {
                vj.currentDomain.removeCurrentValByVal(red);
            }
            this.reductionMap.get(vj).pushReduction(reduction);
            this.futureFCMap.get(vi).add(vj);
            this.pastFCMap.get(vj).add(vi);
        }
        return !vj.currentDomain.currentVals.isEmpty();
    }

    public void undoReduction(int currentVarIndex) {
        PVariable currentVar = this.variables.get(currentVarIndex);
        if (!this.futureFCMap.get(currentVar).isEmpty()) {
            for (PVariable futureVar : this.futureFCMap.get(currentVar)) {
                this.reductionMap.get(futureVar).addReductionBack();
                this.pastFCMap.get(futureVar).remove(currentVar);
            }
        }
        futureFCMap.get(currentVar).clear();
    }

    public void updatedCurrentDomain(int currentVarIndex) {
        PVariable currentVar = this.variables.get(currentVarIndex);
        currentVar.resetCurrentDomain();
        int reductionSetsSize = reductionMap.get(currentVar).get().size();
        Stack<ArrayList<Integer>> temp = new Stack<>();
        for (int i=0; i<reductionSetsSize; i++) {
            for (int val : reductionMap.get(currentVar).get().peek()) {
                currentVar.currentDomain.removeCurrentValByVal(val);
            }
            temp.push(reductionMap.get(currentVar).get().pop());
        }
        for (int i=0; i<reductionSetsSize; i++) {
            reductionMap.get(currentVar).pushReduction(temp.pop());
        }
    }

    public boolean check(PVariable vari, Integer vali,
                         PVariable varj, Integer valj, boolean reverseVal) {
        List<PConstraint> cons = findConstraintByScope(
                new ArrayList<>(Arrays.asList(vari, varj)));
        if (cons == null) {
            return true;
        } //universal constraint
        this.cc ++;
        for (PConstraint con : cons) {
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

    public Double getCpuTimeInMillis() {
        return (double) this.cpu / 1000000.0;
    }

    public String getInitReport() {
        return "Instance name: " + this.problemName + "\n" ;
    }
    public String getAllSolutionStat() {
        return "all-sol cc: " + this.cc + "\n" +
                "all-sol nv: " + this.numNodeVisited + "\n" +
                "all-sol bt: " + this.numBacktrack + "\n" +
                "all-sol cpu: " + this.getCpuTimeInMillis() + "\n" +
                "Number of solutions: " + this.numSolution + "\n";
    }
    public String getFirstSolutionForCsvReport() {
        String[] parts = new String[]{
                this.cc+"", this.numNodeVisited+"",
                this.numBacktrack+"", this.getCpuTimeInMillis()+""
        };
        return joinStrs(parts)+",";
    }

    public String joinStrs(String[] parts) {
        return Arrays.stream(parts).collect(Collectors.joining(","));
    }
}
