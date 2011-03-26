package org.everpeace.search;

/**
 * an interface representing distance function on E.
 * distance function must satisfy three axiom:
 * 1) d(x,y) = 0  <=> x==y      (anti-symmetry)
 * 2) d(x,y) = d(y,x)           (symmetry)
 * 3) d(x,z) <= d(x,y) + d(y,z) (triangle inequality)
 *
 * User: everpeace _at_ gmail _dot_ com
 * Date: 11/03/21
 * Created by IntelliJ IDEA.
 */
public interface Distance<E> {
    /**
     * calculate distance between e1 and e2.
     *
     * @param e1 an element.
     * @param e2 an element.
     * @return distance between e1 and e2.
     */
    double eval(E e1, E e2);
}
