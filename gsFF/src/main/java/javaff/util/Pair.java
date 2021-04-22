package javaff.util;

/**
 * Pair representation.
 * @author Kamal Asmatpoor
 * @version 1.0
 * 
 * @param <X> type of first item in pair.
 * @param <Y> type of second item in pair.
 */
public class Pair<X, Y> {
    public final X x;
    public final Y y;
    
    /**
     * Pair constructor.
     * @param x first item in pair of type X.
     * @param y second item in pair of type Y.
     */
    public Pair(X x, Y y) {
        this.x = x;
        this.y = y;
    }
}
