package csp;

import java.util.*;
import java.util.stream.Collectors;

public class CSPSolver {
    private ProblemInstance problemInstance;
    private long timeSetup;
    private long cpuTime;
    private long cc;
    private long fval;
    private double fSize;
    private double fEffect;
    private double iSize;

    public CSPSolver(ProblemInstance problemInstance) {
        this.problemInstance = problemInstance;
    }
    /*
     * Verifies where a constraint exist between Vi and Vj. or
     * If (vali, valj) in R_{Vi, Vj} if support
     */
    public boolean check(Variable vi, int vali , Variable vj, int valj) {
        if (!problemInstance.variableConstraintMap.containsKey(
                new HashSet<>(Arrays.asList(vi, vj))
        )) {
            //constraint does not exist;
            return true; // universal constraint
        }
        this.cc += 1;
        Constraint constraint = problemInstance.variableConstraintMap
                    .get(new HashSet<>(Arrays.asList(vi, vj)));
        if (constraint instanceof ExtensionConstraint) {
            ExtensionConstraint exCon = (ExtensionConstraint) constraint;
            boolean isIn = exCon.computeCostOf(new int[]{vali, valj});
//            boolean isIn = Relation.isTupleInTuples(new int[]{vali, valj},
//                    ((ExtensionConstraint) constraint).relation.tuples);
            if (exCon.relation.semantics.equals("conflicts")) {
                return !isIn;
            } else {
                return isIn;
            }
        } else {

        }
        return false;
    }

    /*
     * Verifies that ⟨Vi,a⟩ has at least one support in C_{Vi,Vj}
     */
    public boolean supported(Variable vi, int vali, Variable vj) {
        boolean support = false;
        for (int valj : vj.currentDomain.values) {
            if (check(vi, vali, vj, valj)) {
                support = true;
                return support;
            }
        }

        return support; // false. most likely
    }


    /*
     * Update D_{vi} given the constraint C_{vi, vj}
     * if D_{v_i} is modified -> return true, else false
     */
    public boolean revise(Variable vi, Variable vj) {

        boolean revised = false;

        Iterator valIterator = vi.currentDomain.values.iterator();
        while (valIterator.hasNext()) {
            Integer valx = (Integer) valIterator.next();
            boolean found = supported(vi, valx.intValue(), vj);
            if (!found) {
                revised = true;
                this.fval += 1;
                valIterator.remove(); //D_{vi} <- D_{vi} \ {x}
            }
        }
        return revised;
    }

    public boolean arcConsistency1() {
        this.setiSize(CSPSolver.getCSPSize(this.problemInstance));

        nodeConsistency();

        Set<Set<Variable>> directedArcs = this.problemInstance.variableConstraintMap.keySet();
        boolean change = false;
        do {
            change = false;
            for (Set<Variable> arc : directedArcs) {
                assert arc.size() == 2;
                List<Variable> arcArr = arc.stream().collect(Collectors.toList());
                boolean updated = revise(arcArr.get(0), arcArr.get(1));
                if (arcArr.get(0).currentDomain.values.size() == 0) {
                    return false;
                } else {
                    change = updated || change;
                }
            }
        } while (change);

        this.setfSize(CSPSolver.getCSPSize(this.problemInstance));
        return true;
    }

    public void nodeConsistency() {

    }

    public static double getCSPSize(ProblemInstance problemInstance) {
        List<Variable> vars = new ArrayList<>(problemInstance.mapOfVariables.values());
        double sumLn = 0.;
        for (Variable var : vars) {
            sumLn += Math.log(var.currentDomain.values.size() + 0.);
        }
        return sumLn;
    }


    public String solverReport() {
        return "Instance name: " + problemInstance.name + "\n" +
                "cc: " + this.getCc() + " \n" +
                "cpu: " + this.getCpuTime() + "\n" +
                "fval: " + this.getFval() + "\n" +
                "iSize: " + this.getiSize() + "\n" +
                "fSize: " + this.getfSize() + "\n" +
                "fEffect: " + this.getfEffect() + "\n";

    }

    public ProblemInstance getProblemInstance() {
        return problemInstance;
    }

    public void setProblemInstance(ProblemInstance problemInstance) {
        this.problemInstance = problemInstance;
    }

    public long getTimeSetup() {
        return timeSetup;
    }

    public void setTimeSetup(long timeSetup) {
        this.timeSetup = timeSetup;
    }

    public long getCpuTime() {
        return cpuTime;
    }

    public void setCpuTime(long cpuTime) {
        this.cpuTime = cpuTime;
    }

    public long getCc() {
        return cc;
    }

    public void setCc(long cc) {
        this.cc = cc;
    }

    public long getFval() {
        return fval;
    }

    public void setFval(long fval) {
        this.fval = fval;
    }

    public double getfSize() {
        return fSize;
    }

    public void setfSize(double fSize) {
        this.fSize = fSize;
    }

    public double getfEffect() {
        return this.iSize - this.fSize;
    }

    public void setfEffect(double fEffect) {
        this.fEffect = fEffect;
    }

    public double getiSize() {
        return iSize;
    }

    public void setiSize(double iSize) {
        this.iSize = iSize;
    }
}
