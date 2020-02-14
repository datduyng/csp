package csp;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;
import java.util.stream.Collectors;

public class CSPSolver {
    private ProblemInstance problemInstance;
    private long timeSetup;
    private double cpuTime;
    private long cc;
    private long fval;

    private double fEffect;
    private double iSize;
    private Double fSize;

    public CSPSolver(ProblemInstance problemInstance) {
        this.setProblemInstance(problemInstance);
        this.setTimeSetup(0);
        this.setCpuTime(0);
        this.setCc(0);
        this.setFval(0);
        this.setiSize(0.);
        this.setfSize(0.);
        this.setfEffect(0.);
    }
    /*
     * Verifies where a constraint exist between Vi and Vj. or
     * If (vali, valj) in R_{Vi, Vj} if support
     */
    public boolean check(Variable vi, int vali , Variable vj, int valj) {
        if (!problemInstance.variableConstraintMap.containsKey(
                new LinkedHashSet<>(Arrays.asList(vi, vj)) )) {
            //constraint does not exist;
            return true; // universal constraint
        }
        this.cc += 1;
        if (vi.neighbors.contains(vj)) {
            Constraint constraint = problemInstance.variableConstraintMap
                    .get(new LinkedHashSet<>(Arrays.asList(vi, vj)));
            return constraint.isSupportedBy(new int[]{vali, valj});
        }
        return true;
    }

    /*
     * Verifies that ⟨Vi,a⟩ has at least one support in C_{Vi,Vj}
     */
    public boolean supported(Variable vi, int vali, Variable vj) {
        List<Integer> vals = new ArrayList<>(vj.currentDomain.values);
        for (int valj : vals) {
            if (check(vi, vali, vj, valj)) { return true; }
        }

        return false;
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

//        boolean revised = false;
//        List<Integer> originalDomainVals = new ArrayList<>(vi.currentDomain.values);
//        for (Integer viVal : originalDomainVals) {
//            if (supported(vi, viVal, vj)) {
//                revised = true;
//                this.fval += 1;
//                vi.currentDomain.values.remove(viVal);
//            }
//        }
//        return revised;
    }

    public boolean arcConsistency3() {
        this.setiSize(CSPSolver.getCSPSize(this.problemInstance));
        checkNodeConsistency();

        long tic = this.getCpuTimeInNano();
        Queue<List<Variable>> queue = new LinkedList<>();
        for (LinkedHashSet<Variable> vpair : this.problemInstance.variableConstraintMap.keySet()) {
            List<Variable> _vpair = vpair.stream().collect(Collectors.toList());
            if (vpair.size() == 1) { continue; }
            queue.offer(new ArrayList<>(Arrays.asList(
                    _vpair.get(0), _vpair.get(1)
            )));
            queue.offer(new ArrayList<>(Arrays.asList(
                    _vpair.get(1), _vpair.get(0)
            )));
        }

        while (!queue.isEmpty()) {
            List<Variable> vpair = queue.poll();
            if (vpair.size() == 1) { continue; }
            boolean updated = revise(vpair.get(0), vpair.get(1));

            if (updated) {
                for (Variable neigh : vpair.get(0).neighbors) {
                    if (neigh.equals(vpair.get(1))) { continue; }
                    queue.offer(new ArrayList<>(Arrays.asList(
                            neigh, vpair.get(0)
                    )));
                }
            }

            //check for domain wipe out
            if (vpair.get(0).currentDomain.values.size() == 0 ||
                vpair.get(1).currentDomain.values.size() == 0) {
                this.setfSize(null);
                long toc = this.getCpuTimeInNano();
                this.setCpuTime((double) (toc - tic));
                return false;
            }
        }
        long toc = this.getCpuTimeInNano();
        this.setCpuTime((double) (toc - tic) );
        this.setfSize(CSPSolver.getCSPSize(this.problemInstance));
        return true;
    }

    public boolean arcConsistency1() {
        this.setiSize(CSPSolver.getCSPSize(this.problemInstance));

        checkNodeConsistency();
        long tic = this.getCpuTimeInNano();
        Set<LinkedHashSet<Variable>> directedArcs = this.problemInstance.variableConstraintMap.keySet();
        boolean change = false;
        do {
            change = false;
            for (LinkedHashSet<Variable> arc : directedArcs) {
                List<Variable> _arc = arc.stream().collect(Collectors.toList());
                if (_arc.size() == 1) { continue; }
                boolean updated1 = revise(_arc.get(0), _arc.get(1));
                if (_arc.get(0).currentDomain.values.size() == 0) {
                    this.setfSize(null);
                    this.setCpuTime((double) (this.getCpuTimeInNano() - tic));
                    return false;
                }

                boolean updated2 = revise(_arc.get(1), _arc.get(0));
                if (_arc.get(1).currentDomain.values.size() == 0) {
                    this.setfSize(null);
                    this.setCpuTime((double) (this.getCpuTimeInNano() - tic));
                    return false;
                }

                change = updated1 || updated2 || change;

            }
        } while (change);
        this.setCpuTime((double) (this.getCpuTimeInNano() - tic));
        this.setfSize(CSPSolver.getCSPSize(this.problemInstance));
        return true;
    }

    public void checkNodeConsistency() {
        for (Constraint constraint : this.problemInstance.mapOfConstraints.values()) {
            if (constraint.scope.size() == 1) {
                List<Integer> temp = new ArrayList<>(constraint.scope.get(0).currentDomain.values);

                for (Integer i : temp) {
                    if (!constraint.isSupportedBy(new int[]{i.intValue(), i.intValue()})) {
                        constraint.scope.get(0).currentDomain.values.remove(i);
                        fval += 1;
                    }
                }
            }
        }
    }
    /** Get CPU time in nanoseconds. */
    public long getCpuTimeInNano() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        return bean.isCurrentThreadCpuTimeSupported( ) ?
                bean.getCurrentThreadCpuTime() : 0L;
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
                "cpu: " + this.getCpuTime() + "ms\n" +
                "fval: " + this.getFval() + "\n" +
                "iSize: " + this.getiSize() + "\n" +
                "fSize: " + ((this.getfSize() == null) ? "false" : this.getfSize())  + "\n" +
                "fEffect: " + ((this.getfSize() == null) ? "false" : this.getfEffect()) + "\n";
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

    public double getCpuTime() {
        return this.cpuTime/1000000.0;
    }

    public void setCpuTime(double cpuTime) {
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

    public Double getfSize() {
        return fSize;
    }

    public void setfSize(Double fSize) {
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
