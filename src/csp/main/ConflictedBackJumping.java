package csp.main;

import abscon.instance.components.PConstraint;
import abscon.instance.components.PVariable;

import java.util.*;
import java.util.stream.Collectors;

import utils.LOG;
import utils.Utils;

public class ConflictedBackJumping {


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

    private List<PVariable> variables;
    private List<PConstraint> constraints;
    private List<Integer>[] conf_set;
    private Map<PVariable, Integer> variablesAssignment;
    private Integer[] cbf;
    private boolean consistent;
    private String generatedStat;

    public ConflictedBackJumping() {
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
        generatedStat = "";
    }

    public ConflictedBackJumping(String problemName,
                        List<PVariable> variables, List<PConstraint> constraints) {
        this();
        this.problemName = problemName;
        this.variables = variables;
        this.constraints = constraints;
        this.conf_set = new ArrayList[this.variables.size()];
        for (int i=0; i<conf_set.length; i++) {
            conf_set[i] = new ArrayList<>(Arrays.asList(-1));
        }
        this.cbf = new Integer[this.variables.size()];
        Arrays.fill(this.cbf, 0);
    }


    public void run() {
        this.generatedStat += getInitReport();
        Long startTime = Utils.getCpuTimeInNano();
        consistent = true;
        int index = 0;
        BTStatus status = BTStatus.UNKNOWN;
        while (status == BTStatus.UNKNOWN) {
            if (this.consistent) {
                index = cbj_label(index);
            } else {
                this.numBacktrack += 1;
                index = cbj_unlabel(index);
            }
            if (index > this.variables.size() - 1) {
                this.numSolution++;
                for (int i = 0; i < cbf.length; i ++) {
                    cbf[i] = 1;
                }
                if (this.numSolution == 1) {
                    this.cpu = Utils.getCpuTimeInNano() - startTime;
                    this.generatedStat += this.getFirstSolutionStat();
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
        LOG.info(status.toString());
        LOG.stdout(this.generatedStat);
        LOG.stdout("Number solution " + this.numSolution);
        return;
    }

    private Integer cbj_label(Integer varIndex) {
        PVariable vari = this.variables.get(varIndex);
        this.consistent = false;

        List<Integer> currentDomainVals = new ArrayList<>(vari.currentDomain.currentVals);

        for (int i=0; i<currentDomainVals.size() && !consistent; i++) {
            this.consistent = true;
            Integer currentDomainVal = currentDomainVals.get(i);
            this.variablesAssignment.put(vari, currentDomainVal);
            this.numNodeVisited += 1;
            for (int h=0; h<varIndex; h++) {
                if (!this.consistent) { break; }
                PVariable pastVar = variables.get(h);
                this.consistent = check(vari, variablesAssignment.get(vari),
                        pastVar, variablesAssignment.get(pastVar), false) &&
                        check(pastVar, variablesAssignment.get(vari),
                                vari, variablesAssignment.get(pastVar), true);
                if (!this.consistent) {
                    this.conf_set[varIndex].add(h);
                    vari.currentDomain.removeCurrentValByVal(currentDomainVal);
                }
            }
        }

        if (this.consistent) {
            return varIndex + 1;
        }
        return varIndex;
    }

    private Integer cbj_unlabel(int varIndex) {
        if (varIndex == 0) { return -1; }
        Integer h = 0;
        if (cbf[varIndex] == 1) {
            h = varIndex - 1;
            cbf[varIndex] = 0;
        } else {
            h = Collections.max(conf_set[varIndex]);
        }
        conf_set[h] = Utils.listUnion(conf_set[h], conf_set[varIndex]);
        Integer finalH = h;
        conf_set[h].removeIf(v -> v.equals(finalH));

        for (int j=h+1; j<=varIndex; j++) {
            conf_set[j].clear();
            this.variables.get(j).resetCurrentDomain();
        }
        PVariable pastVar = this.variables.get(h);
        pastVar.currentDomain.removeCurrentValByVal(this.variablesAssignment.getOrDefault(pastVar, null));
        this.consistent = (pastVar.currentDomain.currentVals.size() != 0);
        return h;
    }


    public boolean check(PVariable vari, Integer vali,
                         PVariable varj, Integer valj, boolean reverseVal) {
        List<PConstraint> cons = findConstraintByScope(
                new ArrayList<>(Arrays.asList(vari, varj)));
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
        return "Instance name: " + this.problemName + "\n" ;
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

    public String getSolutionInStr() {
        return this.variables.stream()
                .map(var -> this.variablesAssignment.get(var).toString())
                .collect(Collectors.joining(","));
    }

}
