package com.gururaj.pricetracker.data;

/**
 * Created by Gururaj on 5/11/2017.
 */

public class Tuple<X, Y> {
    public X x;
    public final Y y;

    public Tuple(X x, Y y) {
        this.x = x;
        this.y = y;
    }
}
