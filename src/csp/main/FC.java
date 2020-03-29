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

    void run() {
        this.generatedStat += getInitReport();
        Long startTime = Utils.getCpuTimeInNano();
        consistent = true;
        BTStatus status = BTStatus.UNKNOWN;
        int index = 0;

        while (status == BTStatus.UNKNOWN) {
            if (this.consistent) {
                index = fclabel(index);
            } else {
                index = fcunlabel(index);
                this.numBacktrack++;
            }
            if (index > this.variables.size() - 1) {
                this.numSolution++;
                if (this.numSolution == 1) {
                    this.cpu = Utils.getCpuTimeInNano() - startTime;
                    log.info("First solution");
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

    public int fclabel(int vi) {
        return 0;
    }

    public int fcunlabel(int vi) {
        return 0;
    }

    public boolean checkForward(int vi, int viValue, int vj) {
        return false;
    }

    public void undoReduction(int vi) {

    }

    public void updatedCurrentDomain(int vi) {

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
}
