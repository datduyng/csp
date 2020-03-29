package csp.main;

import abscon.instance.components.PConstraint;
import abscon.instance.components.PVariable;
import utils.LOG;
import utils.Utils;

import java.util.*;
import java.util.stream.Collectors;

public class DynamicFC {
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
    private List<PVariable> unassignedVars;
    private Stack<PVariable> instantiatedVars;

    private boolean consistent;

    public DynamicFC() {
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
        unassignedVars = new ArrayList<>();
        instantiatedVars = new Stack<>();
    }

    public DynamicFC(String orderingHeuristic, String problemName,
                     List<PVariable> variables, List<PConstraint> constraints) {
        this();
        this.orderingHeuristic = (orderingHeuristic == null) ? "LX" : orderingHeuristic;
        this.problemName = problemName;
        this.variables = variables;
        this.constraints = constraints;
        for (int i=0; i<this.variables.size(); i++) {
            PVariable var = this.variables.get(i);
            reductionMap.put(var, new Reduction(var));
            futureFCMap.put(var, new LinkedHashSet<>());
            pastFCMap.put(var, new LinkedHashSet<>());
        }
        for (PVariable var : this.variables) {
            this.unassignedVars.add(var);
        }
    }

    boolean afterSolution = false;
    public void run() {
        this.sortUnclaimedVars();
        this.generatedStat += getInitReport();
        Long startTime = Utils.getCpuTimeInNano();
        this.consistent = true;
        BTStatus status = BTStatus.UNKNOWN;
        while (status == BTStatus.UNKNOWN) {
            printStatus();
            if (this.consistent) {
//                if (this.unassignedVars.isEmpty()) { break; }
                log.debug("fcLabel()");
                    this.sortUnclaimedVars();
                    fclabel();
            } else {
                log.debug("unlabel()");
                fcUnlabel();
                this.sortUnclaimedVars();
                numBacktrack++;
            }
//            if (this.unassignedVars.isEmpty()) {
//                LOG.info("!this.consisten && unassignedVars.isEmpty()");
//                break;
//            }
//            LOG.info("Solution num" + this.numSolution);
            if (consistent && this.unassignedVars.isEmpty()) {
                numSolution++;
                if (this.numSolution == 1) {
//                    LOG.info("First solution");
                    this.cpu = Utils.getCpuTimeInNano() - startTime;
                    this.generatedStat += this.getFirstSolutionStat();
                }
                log.debug("------SOLUTION----");
                printStatus();
                log.debug("<<<<<<<<<<>>>>>>>>");
                afterSolution = true;
            } else if (this.instantiatedVars.isEmpty() &&
                    this.numSolution > 0 &&
                    this.unassignedVars.get(this.unassignedVars.size()-1).currentDomain.currentVals.isEmpty()) {
                     status = BTStatus.SOLUTION;
            } else if (this.instantiatedVars.isEmpty() &&
                       this.unassignedVars.get(0).currentDomain.currentVals.isEmpty()) {
                status = BTStatus.IMPOSSIBLE;
            }
            log.debug("%%%%%%%%%%%%%%%%%%END ITER%%%%%%%%%%%%%%%%%%");
//            if (this.numSolution == 4) { break; }
        }
        this.generatedStat += getAllSolutionStat();
        LOG.info(status.toString());
        LOG.stdout(this.generatedStat);
        LOG.stdout("Number solution " + this.numSolution);
        return;
    }

    public void fclabel() {
        log.debug("before assignment");
        log.debug("instantiatedVars " +  this.instantiatedVars.stream().map(v -> v.getName()).collect(Collectors.toList()).toString());
        log.debug("unassignedVars " + this.unassignedVars.stream().map(v -> v.getName()).collect(Collectors.toList()).toString());
        this.consistent = false;
        int valIndex = 0;
        if (this.numSolution > 0 && afterSolution) {
            LOG.info("Solution? keep going");
            int value = this.instantiatedVars.peek().currentDomain.currentVals.get(0);
            this.instantiatedVars.peek().currentDomain.removeCurrentValByVal(value);
            this.consistent = !this.instantiatedVars.peek().currentDomain.currentVals.isEmpty();
            if (this.consistent) {
                this.unassignedVars.add(this.instantiatedVars.pop());
            }
            this.afterSolution = false;
            return;
        }

        while(!this.consistent && valIndex < this.unassignedVars.get(0).currentDomain.currentVals.size()) {
            int value = this.unassignedVars.get(0).currentDomain.currentVals.get(valIndex);
            this.numNodeVisited ++;
            this.consistent = true;
//            for (int h=1; h<this.unassignedVars.size() && this.consistent; h++) {
//                this.consistent = checkForward(this.unassignedVars.get(0), value, this.unassignedVars.get(h));
//            }
            int h = 1;
            while (this.consistent && h<this.unassignedVars.size()) {
                this.consistent = checkForward(this.unassignedVars.get(0), value, this.unassignedVars.get(h));
                log.debug("check: " + this.unassignedVars.get(0).getName() + " val : " + value + " varh " + this.unassignedVars.get(h).getName() + " result: " + this.consistent);

                h++;
            }

            if (!this.consistent) {
                this.unassignedVars.get(0).currentDomain.removeCurrentValByVal(value);
//                log.debug("before undoReduction");
//                printStatus();
                this.undoReduction(this.unassignedVars.get(0));
//                log.debug("----");
                valIndex--;
            }
            valIndex++;
        }
        this.instantiatedVars.push(this.unassignedVars.remove(0));
    }

    public void fcUnlabel() {
        if (this.instantiatedVars.size() == 1) {
            if (!this.instantiatedVars.peek().currentDomain.currentVals.isEmpty()) {
                int value = this.instantiatedVars.peek().currentDomain.currentVals.get(0);
                this.instantiatedVars.peek().currentDomain.removeCurrentValByVal(value);
            }
            if (this.instantiatedVars.peek().currentDomain.currentVals.isEmpty()) {
                this.unassignedVars.add(this.instantiatedVars.pop());
            } else {
                for (PVariable var : this.unassignedVars) {
                    var.resetCurrentDomain();
                    reductionMap.get(var).reset();
                    futureFCMap.get(var).clear();
                    pastFCMap.get(var).clear();
                }
                this.unassignedVars.add(this.instantiatedVars.pop());
                this.consistent = true;
            }
        } else {
            this.updateCurrentDomain(this.instantiatedVars.peek());
            this.unassignedVars.add(this.instantiatedVars.pop());
            this.undoReduction(this.instantiatedVars.peek());
            int value = this.instantiatedVars.peek().currentDomain.currentVals.get(0);
            this.instantiatedVars.peek().currentDomain.removeCurrentValByVal(value);
            this.consistent = !this.instantiatedVars.peek().currentDomain.currentVals.isEmpty();
            if (this.consistent) {
                this.unassignedVars.add(this.instantiatedVars.pop());
            }
        }
    }

    public boolean checkForward(PVariable vi, int viVal, PVariable vj) {
        ArrayList<Integer> reduction = new ArrayList<>();
        for (int vjVal : vj.currentDomain.currentVals) {
            if (!(this.check(vi, viVal, vj, vjVal, true) &&
                    this.check(vj, viVal, vi, vjVal, false)) ) {
                reduction.add(vjVal);
            }

        }

        if (!reduction.isEmpty()) {
            for (int red : reduction) {
                vj.currentDomain.removeCurrentValByVal(red);
            }
            reductionMap.get(vj).pushReduction(reduction);
            futureFCMap.get(vi).add(vj);
            pastFCMap.get(vj).add(vi);
        }

        return !vj.currentDomain.currentVals.isEmpty();
    }

    public void undoReduction(PVariable currentVar) {
        if (!futureFCMap.get(currentVar).isEmpty()) {
            for (PVariable futureVar : futureFCMap.get(currentVar)) {
                //add reduction back
                reductionMap.get(futureVar).addReductionBack();
                pastFCMap.get(futureVar).remove(currentVar);
            }
        }
        futureFCMap.get(currentVar).clear();
    }

    public void updateCurrentDomain(PVariable currentVar) {
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

    public void sortUnclaimedVars(){
        OrderingHeuristic orderingMachine = new OrderingHeuristic(this.unassignedVars);
        orderingMachine.run(this.orderingHeuristic);
        this.unassignedVars = orderingMachine.variables;
    }

    public void printStatus() {
        log.debug("Status");
        for (PVariable var : this.variables) {
            log.debug(var.getName() + " : " + var.currentDomain.currentVals.toString());
        }
        log.debug("-----");
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

    public String getInitReport() {
        return "Instance name: " + this.problemName + "\n" ;
    }
    public String getAllSolutionStat() {
        return "all-sol cc: " + this.cc + "\n" +
                "all-sol nv: " + this.numNodeVisited + "\n" +
                "all-sol bt: " + this.numBacktrack + "\n" +
                "all-sol cpu: " + this.getCpuTimeInMicro() + "\n" +
                "Number of solutions: " + this.numSolution + "\n";
    }
    public Double getCpuTimeInMicro() {
        return (double) this.cpu / 1000000.0;
    }

    public String getFirstSolutionStat() {
//        String firstSolution = this.getSolutionInStr();
//        firstSolution += "\t(" +
//                this.variablesAssignment.keySet().stream().map(var -> var.getName())
//                        .collect(Collectors.joining(",")) + ")";
        String firstSolution = "";
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

}
