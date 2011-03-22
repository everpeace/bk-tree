package org.everpeace.search;

import java.util.*;

/**
 * implementation of BK-Tree.
 * provide effective approximation search in a given metric space.
 *
 * @param <E> E must be immutable object type, otherwise search function can't work correctly..
 * User: everpeace _at_ gmail _dot_ com
 * Date: 11/03/21
 * Created by IntelliJ IDEA.
 */
public class BKTree<E> {
     // eval function of the tree
    private final Distance<E> distanceFunction;
     //root element of the tree
    private final E r;
     // child trees, which must have the same distance function of the tree.
     // (TODO need to be concurrentMap?)
    private final Map<Integer, BKTree<E>> children;

    /**
     * constructor.
     * eval function of the tree is converted from comparator:
     * dis(x,y) = |comparator.compare(x,y)|
     *
     * @param distanceFunction comparator
     * @param root     root element of the tree
     */
    public BKTree(final Comparator<E> distanceFunction, final E root) {
        this.distanceFunction = new Distance<E>() {
            public int eval(E o1, E o2) {
                return Math.abs(distanceFunction.compare(o1, o2));
            }
        };
        this.r = root;
        this.children = new HashMap<Integer, BKTree<E>>();
    }

    /**
     * constructor.
     *
     * @param distanceFunction eval function.
     * @param root     root element of the tree
     */
    public BKTree(final Distance distanceFunction, final E root) {
        this.distanceFunction = distanceFunction;
        this.r = root;
        this.children = new HashMap<Integer, BKTree<E>>();
    }

    /**
     * search elements on query distant within radius.
     *
     * @param query -
     * @param radius -
     * @return -
     */
    public Set<E> searchWithin(E query, Integer radius) {
        Integer d = d(r, query);
        Set<E> result = new HashSet<E>();
        Integer lo;
        if (d <= radius) {
            lo = 0;
            result.add(r); // found
        } else {
            lo = d - radius;
        }
        Integer hi = d + radius;

        // search children from eval-radius(>=0) to eval+radius.
        for (int i = lo; i <= hi; i++) {
            if (children.containsKey(i)) {
                result.addAll(children.get(i).searchWithin(query, radius));
            }
        }
        return result;
    }

    /**
     * search elements on query distant at distance
     *
     * @param query -
     * @param distance -
     * @return -
     */
    public Set<E> searchAt(E query, Integer distance) {
        Integer d = d(r, query);
        Set<E> result = new HashSet<E>();
        Integer lo;
        if (d < distance) {
            lo = 0;
        } else if (d == distance) {
            lo = 0;
            result.add(r); // found.
        } else {
            lo = d - distance;
        }
        Integer hi = d + distance;

        // search children from eval-distance(>=0) to eval+distance.
        for (int i = lo; i <= hi; i++) {
            if (children.containsKey(i)) {
                result.addAll(children.get(i).searchAt(query, distance));
            }
        }
        return result;
    }

    //TODO
    // public Boolean isExistWithin(E query, Integer radius){
    // }
    // public Boolean isExistAt(E query, Integer distance){
    // }

    /**
     * insert an element the tree.
     * this method is synchronized.
     *
     * @param e an element to be inserted.
     */
    synchronized public void insert(E e) {
        int d_re = d(r, e);
        if (d_re == 0) return; // e is already in the tree.

        if (children.containsKey(d_re)) {
            children.get(d_re).insert(e);
        } else {
            BKTree<E> child = new BKTree<E>(this.distanceFunction, e);
            children.put(d_re, child);
        }
    }

    /**
     * utility to calculate eval.
     *
     * @param e1 -
     * @param e2 -
     * @return
     */
    private Integer d(E e1, E e2) {
        return distanceFunction.eval(e1, e2);
    }
}
