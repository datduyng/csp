package csp.main;

import abscon.instance.components.PConstraint;
import abscon.instance.components.PVariable;
import csp.old.ProblemInstance;
import csp.old.Variable;
import utils.LOG;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ArcConsistency {
    List<PVariable> variables;
    List<PConstraint> constraints;
    String problemName;
    Long timeSetup;
    Long cpu;
    Long cc;
    Long fval;

    Double iSize;
    Double fSize;


    public ArcConsistency() {
        this.timeSetup = 0L;
        this.cpu = 0L;
        this.cc = 0L;
        this.fval = 0L;
        this.iSize = 0.0;
        this.fSize = 0.0;
    }

    public ArcConsistency(String problemName, List<PVariable> variables, List<PConstraint> constraints) {
        this();
        this.problemName = problemName;
        this.variables = variables;
        this.constraints = constraints;
    }

    public boolean arcConsistencyOne() {
        this.iSize = getCSPSize(this.variables);
        keepNodeConsistent();

        long tic = getCpuTimeInNano();

        boolean change = false;
        boolean wipedOut = false;
        do {
            change = false;
            for (PConstraint con : constraints) {
                if (con.getArity() == 1) { continue; }
                boolean updated = checkSupportedAndRevise(
                        con.getScope()[0], con.getScope()[1]);
                if (con.getScope()[0].getDomain().currentVals.size() == 0) {
                    this.fSize = null;
                    this.cpu = getCpuTimeInNano() - tic;
                    return false;
                }
                boolean updatedReverse = checkSupportedAndRevise(
                        con.getScope()[1], con.getScope()[0]);
                if (con.getScope()[1].getDomain().currentVals.size() == 0) {
                    this.fSize = null;
                    this.cpu = getCpuTimeInNano() - tic;
                    return false;
                }
                change = updated || updatedReverse || change;
            }
        } while (change);

        this.fSize = getCSPSize(this.variables);
        this.cpu = getCpuTimeInNano() - tic;
        return true;
    }

    public void keepNodeConsistent() {
        for (PConstraint con : this.constraints) {
            if (con.getScope().length != 1) { continue; }

            List<Integer> domainVals = new ArrayList<>(
                    con.getScope()[0].getDomain().currentVals);
            for (Integer vali : domainVals) {
                if (!con.isSupportedBy(new int[]{vali, vali})) {
                    con.getScope()[0].getDomain().removeCurrentValByVal(vali);
                    this.fval++;
                }
            }
        }
    }

    public boolean checkSupportedAndRevise(PVariable vi, PVariable vj) {
        List<Integer> viVals = new ArrayList<>(vi.getDomain().currentVals);
        boolean revised = false;
        for (Integer viVal : viVals) {
            if (supported(vi, viVal, vj)) {
                revised = true;
                this.fval+=1;
                vi.getDomain().removeCurrentValByVal(viVal);
            }
        }
        return revised;
    }

    public boolean supported(PVariable vi, Integer viVal, PVariable vj) {
        for (Integer vjVal : vj.getDomain().currentVals) {
            boolean ch = check(vi, viVal, vj, vjVal);
            LOG.stdout("check " +vi.getName() + ","+vj.getName() + " Val: "+ "(" +viVal + "," +vjVal+")" +" checked res: "+ ch);
            if (ch) {
                return true;
            }
        }
        return false;
    }

    public boolean check(PVariable vi, Integer viVal, PVariable vj, Integer vjVal) {
        PConstraint con = findConstraintByScope(
                new ArrayList<>(Arrays.asList(vi, vj)));
        if (con == null) { return true; } //universal constraint

        this.cc ++;
        long val = con.computeCostOf(new int[]{viVal, vjVal});
        return val == 0;
    }

    public PConstraint findConstraintByScope(List<PVariable> scope) {
        for (PConstraint con : this.constraints) {
            if (Arr2List(con.getScope()).equals(scope)) {
                return con;
            }
        }
        return null;
    }


    public String getReport() {
        return "Instance name: " + this.problemName + "\n" +
                "cc: " + this.cc + "\n" +
                "cpu: " + this.cpu + "\n" +
                "fval: " + this.fval + "\n" +
                "iSize: " + this.iSize + "\n" +
                "fSize: " + (this.fSize == null ? "FALSE" : this.fSize) + "\n" +
                "fEffect: " + (this.getfEffect() == null ? "FALSE" : this.getfEffect()) + "\n";
    }

    public Double getfEffect() {
        if (iSize == null || fSize == null) { return null; }
        return this.iSize - this.fSize;
    }


    public String getVarsDomain() {
        String res = "Variables domains: \n";
        for (PVariable var : this.variables) {
            res += var.getName() + ": " + var.getDomain().currentVals.toString() + "\n";
        }
        return res;
    }
    public static <G> List<G> Arr2List(G[] vars) {
        return Arrays.stream(vars).collect(Collectors.toList());
    }


    public static double getCSPSize(List<PVariable> vars) {
        double sumLn = 0.;
        for (PVariable var : vars) {
            sumLn += Math.log(var.getDomain().currentVals.size() + 0.);
        }
        return sumLn;
    }
    public static long getCpuTimeInNano() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        return bean.isCurrentThreadCpuTimeSupported( ) ?
                bean.getCurrentThreadCpuTime() : 0L;
    }


}
