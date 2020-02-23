package csp.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import abscon.instance.components.PConstraint;
import abscon.instance.components.PVariable;

import utils.LOG;
import utils.Utils;



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

        long tic = Utils.getCpuTimeInNano();

        boolean change = false;
        do {
            change = false;
            for (PConstraint con : constraints) {
                if (con.getArity() == 1) { continue; }
                boolean updated = checkSupportedAndRevise(
                        con.getScope()[0], con.getScope()[1], false);
                boolean updatedReverse = checkSupportedAndRevise(
                        con.getScope()[1], con.getScope()[0], true);

                if (con.getScope()[0].currentDomain.currentVals.size() == 0) {
                    this.fSize = null;
                    this.cpu = Utils.getCpuTimeInNano() - tic;
                    return false;
                }

                change = updated || updatedReverse || change;
            }
        } while (change);

        this.fSize = getCSPSize(this.variables);
        this.cpu = Utils.getCpuTimeInNano() - tic;
        return true;
    }

    public void keepNodeConsistent() {
        for (PConstraint con : this.constraints) {
            if (con.getScope().length != 1) { continue; }

            List<Integer> domainVals = new ArrayList<>(
                    con.getScope()[0].currentDomain.currentVals);
            for (Integer vali : domainVals) {
                if (!con.isSupportedBy(new int[]{vali, vali})) {
                    con.getScope()[0].currentDomain.removeCurrentValByVal(vali);
                    this.fval++;
                }
            }
        }
    }

    public boolean checkSupportedAndRevise(PVariable vi, PVariable vj, boolean reversed) {
        List<Integer> viVals = new ArrayList<>(vi.currentDomain.currentVals);
        boolean revised = false;
        for (Integer viVal : viVals) {
            if (!supported(vi, viVal, vj, reversed)) {
                revised = true;
                this.fval+=1;
                vi.currentDomain.removeCurrentValByVal(viVal);
            }
        }
        return revised;
    }

    public boolean supported(PVariable vi, Integer viVal, PVariable vj, boolean reversed) {
        List<Integer> vjVals = new ArrayList<>(vj.currentDomain.currentVals);
        for (Integer vjVal : vjVals) {
            if (check(vi, viVal, vj, vjVal, reversed)) {
                return true;
            }
        }
        return false;
    }

    public boolean check(PVariable vi, Integer viVal, PVariable vj, Integer vjVal, boolean reversed) {
        PConstraint con;
        if (reversed) {
            con = findConstraintByScope(
                    new ArrayList<>(Arrays.asList(vj, vi)));
        } else {
            con = findConstraintByScope(
                    new ArrayList<>(Arrays.asList(vi, vj)));
        }
        if (con == null) { return true; } //universal constraint


        this.cc ++;
        return reversed ?
                con.computeCostOf(new int[]{vjVal, viVal}) == 0 :
                con.computeCostOf(new int[]{viVal, vjVal}) == 0;
    }

    public PConstraint findConstraintByScope(List<PVariable> scope) {
        for (PConstraint con : this.constraints) {
            if (Utils.arr2List(con.getScope()).equals(scope)) { return con; }
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
            res += var.getName() + ": " + var.currentDomain.currentVals.toString() + "\n";
        }
        return res;
    }

    public static double getCSPSize(List<PVariable> vars) {
        double sumLn = 0.;
        for (PVariable var : vars) {
            sumLn += Math.log(var.currentDomain.currentVals.size() + 0.);
        }
        return sumLn;
    }

    public void printEndingInfo() {
        LOG.stdout("=========");
        for (PVariable var : this.variables) {
            LOG.stdout(var.toString());
        }
    }
}
