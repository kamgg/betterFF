package javaff.serialisation;

import java.util.Comparator;

import javaff.util.Pair;
import javaff.data.Fact;

import javaff.serialisation.*;


class RPGPairComparatorDescending implements Comparator<Pair<Integer, Fact>> {
    public int compare(Pair<Integer, Fact> fact1, Pair<Integer, Fact> fact2) {
        if (fact1.x < fact2.x) {
            return 1;
        } if (fact1.x > fact2.x) {
            return -1;
        } else {
            return 0;
        }
    }
}

class RPGPairComparatorAscending implements Comparator<Pair<Integer, Fact>> {
    public int compare(Pair<Integer, Fact> fact1, Pair<Integer, Fact> fact2) {
        if (fact1.x > fact2.x) {
            return 1;
        } if (fact1.x < fact2.x) {
            return -1;
        } else {
            return 0;
        }
    }
}