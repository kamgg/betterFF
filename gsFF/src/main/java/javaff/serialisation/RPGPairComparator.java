package javaff.serialisation;

import java.util.Comparator;

import javaff.util.Pair;
import javaff.data.Fact;

/**
 * Comparator for use with a Priority Queue.
 * @Author Kamal Asmatpoor
 * @version 1.0
 */
class RPGPairComparatorDescending implements Comparator<Pair<Integer, Fact>> {
    /**
     * Compare two pairs and return an integer representation of their order in descending order.
     * @param fact1 fact to compare against.
     * @param fact1 fact to compare with.
     * @return Integer that is representative of the relative order of two pairs.
     */
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
/**
 * Comparator for use with a Priority Queue.
 * @Author Kamal Asmatpoor
 * @version 1.0
 */
class RPGPairComparatorAscending implements Comparator<Pair<Integer, Fact>> {
    /**
     * Compare two pairs and return an integer representation of their order in ascending order.
     * @param fact1 fact to compare against.
     * @param fact1 fact to compare with.
     * @return Integer that is representative of the relative order of two pairs.
     */
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