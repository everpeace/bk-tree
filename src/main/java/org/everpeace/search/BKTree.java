package org.everpeace.search;

import java.util.*;

/**
 * implementation of BK-Tree.
 * provide effective approximation search in a given metric space.
 *
 * @param <E> E must be immutable object type, otherwise search function can't work correctly..
 *            User: everpeace _at_ gmail _dot_ com
 *            Date: 11/03/21
 *            Created by IntelliJ IDEA.
 */
public class BKTree<E> {
    // eval function of the tree
    private final Distance<E> distanceFunction;
    //root element of the tree
    private E r;
    // child trees, which must have the same distance function of the tree.
    private final Map<Double, BKTree<E>> children;

    /**
     * constructor.
     * eval function of the tree is converted from comparator:
     * dis(x,y) = |comparator.compare(x,y)|
     *
     * @param distanceFunction comparator
     * @param root             root element of the tree
     */
    public BKTree(final Comparator<E> distanceFunction, final E root) {
        this.distanceFunction = new Distance<E>() {
            public double eval(E o1, E o2) {
                return Math.abs(distanceFunction.compare(o1, o2));
            }
        };
        this.r = root;
        this.children = new HashMap<Double, BKTree<E>>();
    }

    /**
     * constructor.
     *
     * @param distanceFunction eval function.
     * @param root             root element of the tree
     */
    public BKTree(final Distance distanceFunction, final E root) {
        this.distanceFunction = distanceFunction;
        this.r = root;
        this.children = new HashMap<Double, BKTree<E>>();
    }

    /**
     * search elements on query distant within radius.
     *
     * @param query  -
     * @param radius -
     * @return -
     */
    public Set<E> searchWithin(E query, Double radius) {
        Double d = d(r, query);
        Set<E> result = new HashSet<E>();
        Double lo;
        if (d <= radius) {
            lo = 0d;
            result.add(r); // found
        } else {
            lo = d - radius;
        }
        Double hi = d + radius;

        // search children from eval-radius(>=0) to eval+radius.
        for (Double k : children.keySet()) {
            if (lo <= k && k <= hi) {
                result.addAll(children.get(k).searchWithin(query, radius));
            }
        }

        return result;
    }

    /**
     * search elements on query distant at distance
     *
     * @param query    -
     * @param distance -
     * @return -
     */
    public Set<E> searchAt(E query, Double distance) {
        Double d = d(r, query);
        Set<E> result = new HashSet<E>();
        Double lo;
        if (d < distance) {
            lo = 0d;
        } else if (d == distance) {
            lo = 0d;
            result.add(r); // found.
        } else {
            lo = d - distance;
        }
        Double hi = d + distance;

        // search children from eval-distance(>=0) to eval+distance.
        for (Double k : children.keySet()) {
            if (lo <= k && k <= hi) {
                result.addAll(children.get(k).searchAt(query, distance));
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
     * @return true: success to insert, false: e already exists, do not inserted.
     */
    synchronized public boolean insert(E e) {
        if (this.r == null) {
            this.r = e;
            return true;
        }

        Double d_re = d(r, e);
        if (d_re == 0d) return false; // e is already in the tree.

        if (children.containsKey(d_re)) {
            return children.get(d_re).insert(e);
        } else {
            BKTree<E> child = new BKTree<E>(this.distanceFunction, e);
            children.put(d_re, child);
            return true;
        }
    }

    /**
     * utility to calculate eval.
     *
     * @param e1 -
     * @param e2 -
     * @return -
     */

    private Double d(E e1, E e2) {
        return distanceFunction.eval(e1, e2);
    }

    /**
     * build BK-Tree from a collection of E using comparator as distance function.
     *
     * @param collection a collection of E
     * @param compar     comparator
     * @param <E>        type of elements
     * @return BK-Tree<E> containing elements of collection.
     */
    public static <E> BKTree<E> build(Collection<E> collection, Comparator<E> compar) {
        assert collection != null && compar != null;
        if (collection.size() > 0) {
            BKTree<E> tree = new BKTree<E>(compar, null);
            for (E e : collection) {
                tree.insert(e);
            }
            return tree;
        } else {
            throw new IllegalArgumentException("the size of collection is at least one.");
        }
    }

    /**
     * build BK-Tree from a collection of E and distance function.
     *
     * @param collection a collection of E
     * @param dist       distance function
     * @param <E>        type of elements
     * @return BK-Tree<E> containing elements of collection.
     */
    public static <E> BKTree<E> build(Collection<E> collection, Distance<E> dist) {
        assert collection != null && dist != null;
        if (collection.size() > 0) {
            BKTree<E> tree = new BKTree<E>(dist, null);
            for (E e : collection) {
                tree.insert(e);
            }
            return tree;
        } else {
            throw new IllegalArgumentException("the size of collection is at least one.");
        }
    }

    /**
     * return the height of tree.
     *
     * @return the height of tree
     */
    public Integer height() {
        if (numOfChildren() == 0) {
            return 1;
        } else {
            List<Integer> childHeights = new ArrayList<Integer>(children.keySet().size());
            for (Double d : children.keySet()) {
                childHeights.add(children.get(d).height());
            }
            return Collections.max(childHeights) + 1;
        }
    }

    /**
     * return the number of children.
     *
     * @return the number of children
     */
    public Integer numOfChildren() {
        return children.keySet().size();
    }

    @Override
    public String toString() {
        return "height:" + height() + "\n" + stringRepresentaiton(0);
    }

    private String stringRepresentaiton(int tab) {
        StringBuffer buf = new StringBuffer("[" + this.r);
        if (numOfChildren() > 0) {
            buf.append("\n");
        } else {
            buf.append("]");
        }
        Iterator<Double> iter = children.keySet().iterator();
        while (iter.hasNext()) {
            Double i = iter.next();
            buf.append(indent(tab + 1) + "(" + i + ")" + children.get(i).stringRepresentaiton(tab + 1));
            if (iter.hasNext()) {
                buf.append("\n");
            } else {
                if (tab > 0) {
                    buf.append("\n" + indent(tab) + space(i.toString().length() + 3) + "]");
                } else {
                    buf.append("\n]");
                }
            }
        }
        return buf.toString();
    }

    private static String indent(int tab) {
        String indent = "      "; // 6 whitespaces
        StringBuffer buf = new StringBuffer("");
        for (int i = 0; i < tab; i++) {
            buf.append(indent);
        }
        return buf.toString();
    }

    private static String space(int i) {
        StringBuffer buf = new StringBuffer();
        for (int j = 0; j < i; j++) {
            buf.append(" ");
        }
        return buf.toString();
    }
}
