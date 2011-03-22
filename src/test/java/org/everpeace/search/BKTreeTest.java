package org.everpeace.search;

import org.junit.Test;

import java.util.*;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * test for {@link BKTree}
 * User: everpeace _at_ gmail _dot_ com
 * Date: 11/03/22
 * Created by IntelliJ IDEA.
 */
public class BKTreeTest {
    /**
     * search test for BK-Tree on Integer.
     * distance function is abs..
     * <p/>
     * 1. BK-Tree with [-dataRadius, dataRadius]
     * 2. perform num searches with random query([-(dataRadius+maxRadius),dataRadius-maxRadius]) and random radius([0,dataRadius/100])
     * 3. print average time and divergence.
     */
    @Test
    public void testBKTreeInt() {
        int dataRadius = 100000;
        int num = 100;
        int maxRadius = (int) (dataRadius / 1000);
        List<Integer> data = new ArrayList<Integer>();
        for (int i = -1 * dataRadius; i <= dataRadius; i++) {
            data.add(i);
        }
        Collections.shuffle(data);
        //System.out.println(data);
        long start = System.currentTimeMillis();
        BKTree<Integer> bkTree = BKTree.build(data, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1 - o2;
            }
        });
        long end = System.currentTimeMillis();
        System.out.println("constuction time:" + (end - start) + "[ms] (" + (dataRadius * 2 + 1) + "integers, height=" + bkTree.height() + ")");

        //System.out.println(bkTree)
        double sum = 0;
        long[] times = new long[num];
        System.out.println("search max radius:" + maxRadius);
        for (int n = 1; n <= num; n++) {
            // random point [-(dataRadius-maxRadius) ... (dataRadius-maxRadius)]
            int query = (int) (Math.random() * (dataRadius - maxRadius));
            if ((int) (Math.random() * dataRadius) % 2 == 1) {
                query *= -1;
            }
            // random point [0 .. (dataRadius/2)]
            int radius = (int) (Math.random() * maxRadius);
            start = System.currentTimeMillis();
            Set<Integer> result = bkTree.searchWithin(query, radius);// query-radius ... query+radius
            end = System.currentTimeMillis();
            //System.out.println("result:" + result);
            assertThat(result.size(), is(2 * radius + 1));
            for (int i = query - radius; i <= query + radius; i++) {
                assertTrue(result.contains(i));
            }
            System.out.print("#" + n + " query:" + query + ", radius:" + radius);
            System.out.println(" time:" + (end - start) + "[ms]");
            sum += (end - start);
            times[n - 1] = (end - start);
        }
        double ave = sum / num;
        double div = 0;
        for (int i = 0; i < num; i++) {
            div += (ave - times[i]) * (ave - times[i]);
        }
        div /= num;
        div = Math.sqrt(div);
        System.out.println("average time(" + num + " trials): " + (sum / num) + "[ms]  divergence:" + div);
    }

    /**
     * search test for BK-Tree on String.
     * distance function is levenshtein distance.
     * <p/>
     * 1. construct BK-Tree with numOfData random Strings(max length is lengthLimit).
     * 2. perfom num searches with random string query and radius.
     * 3. print average search time and divergence.
     */
    @Test
    public void testBKTreeString() {
        TreeSet<String> strings = new TreeSet<String>();
        int lengthLimit = 20;
        int numOfData = 50000;
        int num = 100;
        int radius = 3;

        for (int i = 0; i < numOfData; i++) {
            String st = randomString(1 + (int) (Math.random() * (lengthLimit - 1)));
            if (!strings.contains(st)) {
                strings.add(st);
            } else {
                i -= 1;//retry
            }
        }

        Long start = System.currentTimeMillis();
        Distance<String> d = new Levenshtein();
        BKTree<String> bkTree = BKTree.build(strings, d);
        Long end = System.currentTimeMillis();
        if (numOfData <= 100) System.out.println(bkTree);
        System.out.println("construction time:" + (end - start) + "[ms] (" + numOfData + " strings, height=" + bkTree.height() + ")");

        double sum = 0;
        long[] times = new long[num];
        for (int n = 1; n <= num; n++) {
            String query = randomString(1 + (int) (Math.random() * (lengthLimit - 1)));

            // build answer.
            Set<String> answer = new TreeSet<String>();
            for (String s : strings) {
                if (d.eval(query, s) <= radius) {
                    answer.add(s);
                }
            }
            start = System.currentTimeMillis();
            Set<String> result = bkTree.searchWithin(query, radius);
            end = System.currentTimeMillis();
            System.out.print("#" + n + " query:" + query + " radius:" + radius);

            assertThat(answer, is(result));
            if (answer.size() <= 10) System.out.print(" answer:" + result);
            else System.out.print(" answer:" + answer.size() + " strings");
            System.out.println("  time:" + (end - start) + "[ms]");
            sum += (end - start);
            times[n - 1] = (end - start);
        }
        double div = 0;
        double ave = sum / num;
        for (int i = 0; i < num; i++) {
            div += (ave - times[i]) * (ave - times[i]);
        }
        div /= num;
        div = Math.sqrt(div);
        System.out.println("average time(" + num + " trials): " + ave + "[ms]  divergence: " + div);
    }

    public static class Levenshtein implements Distance<String> {
        @Override
        public int eval(String x, String y) {
            int len_x = x.length(), len_y = y.length();
            int[][] row = new int[len_x + 1][len_y + 1];
            int i, j;
            int result;

            for (i = 0; i < len_x + 1; i++)
                row[i][0] = i;
            for (i = 0; i < len_y + 1; i++)
                row[0][i] = i;
            for (i = 1; i <= len_x; ++i) {
                for (j = 1; j <= len_y; ++j) {
                    int cost = 0;
                    if (x.substring(i - 1, i).equals(y.substring(j - 1, j))) {
                        cost = 0;
                    } else {
                        cost = 1;
                    }
                    int replace = row[i - 1][j - 1] + cost;// replace
                    int delete = row[i][j - 1] + 1;// deletion
                    int insert = row[i - 1][j] + 1;// insertion
                    row[i][j] = Math.min(Math.min(replace, delete), insert);
                }
            }
            result = (Integer) (row[len_x][len_y]);

            return result;
        }
    }

    private static String randomString(int length) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < length; i++)
            buf.append(randomAlpha());
        return buf.toString();
    }


    private static char randomAlpha() {
        int i = (int) (Math.random() * 26);
        return (char) (i + 65);
    }
}
