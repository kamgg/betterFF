package javaff.util;

/**
 * Triplet representation.
 * @author Kamal Asmatpoor
 * @version 1.0
 * 
 * @param <X> type of first item in triplet.
 * @param <Y> type of second item in triplet.
 * @param <Z> type of third item in triplet.
 */
public class Triplet<X, Y, Z> {
    public final X x;
    public final Y y;
    public final Z z;
    
    /**
     * Triplet constructor.
     * @param x first item in triplet of type X.
     * @param y second item in triplet of type Y.
     * @param z third item in triplet of type Y.
     */
    public Triplet(X x, Y y, Z z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
