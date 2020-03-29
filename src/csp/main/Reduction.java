package csp.main;

import abscon.instance.components.PVariable;

import java.util.ArrayList;
import java.util.Stack;

public class Reduction {
    Stack<ArrayList<Integer>> sets;
    PVariable varRef;
    Reduction(PVariable varRef) {
        this.varRef = varRef;
        sets = new Stack<>();
    }
    Stack<ArrayList<Integer>> get() {
        return this.sets;
    }

    void pushReduction(ArrayList<Integer> reductionSet) {
        this.sets.push(reductionSet);
    }
    void reset() {
        this.sets.clear();
    }
    void addReductionBack() {
        if (!this.get().isEmpty()) {
            for (int val : this.get().pop()) {
                if (!this.varRef.currentDomain.currentVals.contains(val)) {
                    this.varRef.currentDomain.currentVals.add(val);
                } else {
                    throw new RuntimeException("Issue with restoring reduction");
                }
            }
        }
    }
}