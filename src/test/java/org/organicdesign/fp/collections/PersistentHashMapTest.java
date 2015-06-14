// Copyright 2015 PlanBase Inc. & Glen Peterson
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.organicdesign.fp.collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.organicdesign.fp.Option;
import org.organicdesign.fp.StaticImports;
import org.organicdesign.fp.StaticImportsTest;
import org.organicdesign.fp.function.Function2;
import org.organicdesign.fp.permanent.Sequence;
import org.organicdesign.fp.tuple.Tuple2;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.organicdesign.fp.FunctionUtils.ordinal;
import static org.organicdesign.fp.testUtils.EqualsContract.equalsDistinctHashCode;
import static org.organicdesign.fp.testUtils.EqualsContract.equalsSameHashCode;

@RunWith(JUnit4.class)
public class PersistentHashMapTest {
    @Test public void assocAndGet() {
        PersistentHashMap<String,Integer> m1 = PersistentHashMap.empty();
        PersistentHashMap<String,Integer> m2 = m1.assoc("one", 1);

        // Prove m1 unchanged
        assertEquals(0, m1.size());
        assertNull(m1.get("one"));

        // Show m2 correct.
        assertEquals(1, m2.size());
        assertEquals(Integer.valueOf(1), m2.get("one"));
        assertNull(m1.get("two"));

        Integer twoInt = Integer.valueOf(2);
        PersistentHashMap<String,Integer> m3 = m2.assoc("two", twoInt);

        // Prove m1 unchanged
        assertEquals(0, m1.size());
        assertNull(m1.get("one"));

        // Show m2 unchanged
        assertEquals(1, m2.size());
        assertEquals(Integer.valueOf(1), m2.get("one"));

        // Show m3 correct
        assertEquals(2, m3.size());
        assertEquals(Integer.valueOf(1), m3.get("one"));
        assertEquals(Integer.valueOf(2), m3.get("two"));
        assertNull(m3.get("three"));

//        System.out.println("m3: " + m3);
//        PersistentHashMap<String,Integer> m4 = m3.assoc("two", twoInt);
//        System.out.println("m4: " + m4);

        // Check that inserting the same key/value pair returns the same collection.
        assertTrue(m3 == m3.assoc("two", twoInt));

        // Check that it uses the == test and not the .equals() test.
        assertFalse(m3 == m3.assoc("two", new Integer(2)));
    }

    @Test public void seq3() {
        PersistentHashMap<String,Integer> m1 = PersistentHashMap.of("c", 1);
        assertEquals(Option.of(Tuple2.of("c", 1)),
                     m1.seq().head());

        PersistentHashMap<String,Integer> m2 = PersistentHashMap.of(
                "c", 1,
                "b", 2,
                "a", 3);

        Set<Option<Tuple2<String,Integer>>> s = new HashSet<>(Arrays.asList(Option.of(Tuple2.of("c", 1)),
                                                                            Option.of(Tuple2.of("b", 2)),
                                                                            Option.of(Tuple2.of("a", 3))));

        Sequence<UnmodMap.UnEntry<String,Integer>> seq = m2.seq();
        Option o = seq.head();
        assertTrue(s.contains(o));
        s.remove(o);

        seq = seq.tail();
        o = seq.head();
        assertTrue(s.contains(o));
        s.remove(o);

        seq = seq.tail();
        o = seq.head();
        assertTrue(s.contains(o));
        s.remove(o);

        seq = seq.tail();
        o = seq.head();
        assertEquals(Option.none(), o);
    }

    @Test public void seqMore() {
        PersistentHashMap<String,Integer> m1 = PersistentHashMap.of("g", 1, "f", 2, "e", 3, "d", 4, "c", 5,
                                                                    "b", 6, "a", 7);
        // System.out.println("m1.toString(): " + m1.toString());

        Set<UnmodMap.UnEntry<String,Integer>> s1 = new HashSet<>(Arrays.asList(Tuple2.of("g", 1),
                                                                           Tuple2.of("f", 2),
                                                                           Tuple2.of("e", 3),
                                                                           Tuple2.of("d", 4),
                                                                           Tuple2.of("c", 5),
                                                                           Tuple2.of("b", 6),
                                                                           Tuple2.of("a", 7)));

        // System.out.println("s1: " + s1);

        Sequence<UnmodMap.UnEntry<String,Integer>> seq1 = m1.seq();
        Option<UnmodMap.UnEntry<String,Integer>> o1 = seq1.head();
        while (o1.isSome()) {
            UnmodMap.UnEntry<String,Integer> entry = o1.get();
            // System.out.println("entry: " + entry);
            assertTrue(s1.contains(entry));
            s1.remove(entry);
            seq1 = seq1.tail();
            o1 = seq1.head();
        }
        assertEquals(0, s1.size());
        assertTrue(s1.isEmpty());


        Set<String> s2 = new HashSet<>(Arrays.asList("g", "f", "e", "d", "c", "b", "a"));
        // System.out.println("s2: " + s2);

        Sequence<String> seq2 = m1.seq().map(e -> e.getKey());
        Option<String> o2 = seq2.head();
        while (o2.isSome()) {
            String str = o2.get();
            // System.out.println("str: " + str);
            assertTrue(s2.contains(str));
            s2.remove(str);
            seq2 = seq2.tail();
            o2 = seq2.head();
        }
        assertEquals(0, s2.size());
        assertTrue(s2.isEmpty());
    }

    // This is the root cause of issues.
    @Test public void seqMore2() {
        PersistentHashMap<String,String> s1 = PersistentHashMap.empty();
        s1 = s1.assoc("one", "one");
        assertEquals(1, s1.size());
        assertTrue(s1.containsKey("one"));
        assertFalse(s1.containsKey("two"));

        showSeq(s1.seq());
        // System.out.println("One: " + s1);

        s1 = s1.assoc("two", "two");
        assertEquals(2, s1.size());
        assertTrue(s1.containsKey("one"));
        assertTrue(s1.containsKey("two"));
        assertFalse(s1.containsKey("three"));

        showSeq(s1.seq());
        // System.out.println("Two: " + s1);

        s1 = s1.assoc("three", "three");
        assertEquals(3, s1.size());
        assertTrue(s1.containsKey("one"));
        assertTrue(s1.containsKey("two"));
        assertTrue(s1.containsKey("three"));
        assertFalse(s1.containsKey("four"));

        showSeq(s1.seq());
        // System.out.println("Three: " + s1);

        s1 = s1.assoc("four", "four");
        assertEquals(4, s1.size());
        assertTrue(s1.containsKey("one"));
        assertTrue(s1.containsKey("two"));
        assertTrue(s1.containsKey("three"));
        assertTrue(s1.containsKey("four"));
        assertFalse(s1.containsKey("five"));

        // TODO: Right here!
        showSeq(s1.seq());
        // System.out.println("Four: " + s1);

//        System.out.println("s1.seq().toMutableList()" + s1.seq().toMutableList());

    }

    void showSeq(Sequence<UnmodMap.UnEntry<String,String>> seq) {
        // System.out.println("seq");
        Option<UnmodMap.UnEntry<String,String>> opt = seq.head();
        while (opt.isSome()) {
            // System.out.println("\topt.get(): " + opt.get());
            seq = seq.tail();
            opt = seq.head();
        }
    }

    public static void println(Object s) { System.out.println(String.valueOf(s)); }

    @Test public void longerSeq() {
        // This is an assumed to work mutable set - the "control" for this test.
        Set<UnmodMap.UnEntry<String,Integer>> set = new HashSet<>();
        // This is the map being tested.
        ImMap<String,Integer> accum = PersistentHashMap.empty();

        int MAX = 1000;

        for (int i = 0; i < MAX; i++) {
            String s = "Str" + i;
            set.add(Tuple2.of(s, i));
            accum = accum.assoc(s, accum.getOrElse(s, 0) + i);
//            println("accum.size(): " + accum.size());
//            println("accum: " + accum);

            // This will blow up with an obvious non-seq so we know what size causes the real trouble.
            Option<UnmodMap.UnEntry<String,Integer>> o = accum.seq().head();
            assertTrue(o.isSome());
            //noinspection ConstantConditions
            assertTrue(o.get().getKey() instanceof String);
            //noinspection ConstantConditions
            assertTrue(o.get().getValue() instanceof Integer);
        }
        for (int i = 0; i < MAX; i++) {
            assertEquals(Integer.valueOf(i), accum.get("Str" + i));
        }

        Sequence<UnmodMap.UnEntry<String,Integer>> seq = accum.seq();
        for (int i = 0; i < MAX; i++) {
            Option<UnmodMap.UnEntry<String,Integer>> o = seq.head();

            assertTrue(set.contains(o.get()));
            set.remove(o.get());

            seq = seq.tail();
        }
//        System.out.println("seq: " + seq);
        assertFalse(seq.head().isSome());
        assertTrue(set.isEmpty());

//        println("accum: " + accum);
    }

    @Test public void unorderedOps() {
        PersistentHashMap<String,Integer> m1 = PersistentHashMap.of(
                "c", 1,
                "b", 2,
                "a", 3);

        // Prove m1 unchanged
        assertEquals(3, m1.size());
        assertEquals(Integer.valueOf(1), m1.get("c"));
        assertEquals(Integer.valueOf(2), m1.get("b"));
        assertEquals(Integer.valueOf(3), m1.get("a"));
        assertNull(m1.get("d"));

//        // System.out.println(m1.keySet().toString());

        // Values are an unsorted set as well...
        assertEquals(new HashSet<>(Arrays.asList(3, 2, 1)),
                     m1.values());

        assertEquals(new HashSet<>(Arrays.asList("a", "b", "c")),
                     PersistentHashMap.of("a", 3,
                                          "b", 2,
                                          "c", 1).keySet());


        PersistentHashMap<String,Integer> m2 = PersistentHashMap.of("c", 3)
                        .assoc("b", 2)
                        .assoc("a", 1);
        UnmodIterator<UnmodMap.UnEntry<String,Integer>> iter = m2.iterator();
        UnmodMap.UnEntry<String,Integer> next = iter.next();
        assertEquals("a", next.getKey());
        assertEquals(Integer.valueOf(1), next.getValue());

        next = iter.next();
        assertEquals("b", next.getKey());
        assertEquals(Integer.valueOf(2), next.getValue());

        next = iter.next();
        assertEquals("c", next.getKey());
        assertEquals(Integer.valueOf(3), next.getValue());
    }

    @Test public void hashCodeAndEquals() {
        ImMap<String,Integer> a=PersistentHashMap.ofSkipNull(Tuple2.of("one", 1), Tuple2.of("two", 2), Tuple2.of("three", 3));
        ImMap<String,Integer> b=PersistentHashMap.ofSkipNull(Tuple2.of("one", 1), Tuple2.of("two", 2), Tuple2.of("three", 3));

        assertEquals(a.hashCode(), b.hashCode());
        assertFalse(a == b);
        assertEquals(a.size(), b.size());

//        System.out.println("a.entrySet(): " + a.entrySet());
//        System.out.println("b.entrySet(): " + b.entrySet());

        assertEquals(a, b);
        assertEquals(b, a);

        equalsDistinctHashCode(PersistentHashMap.of("one", 1).assoc("two", 2).assoc("three", 3),
                               PersistentHashMap.of("three", 3).assoc("two", 2).assoc("one", 1),
                               PersistentHashMap.of("two", 2, "three", 3, "one", 1),
                               PersistentHashMap.of("two", 2, "three", 3, "four", 4));

        Map<String,Integer> m = new HashMap<>();
        m.put("one", 1);
        m.put("two", 2);
        m.put("three", 3);

        equalsDistinctHashCode(PersistentHashMap.of("one", 1, "two", 2, "three", 3),
                               m,
                               StaticImports.unmod(m),
                               PersistentHashMap.of("two", 2, "three", 3, "four", 4));

        equalsDistinctHashCode(PersistentHashMap.of("one", 1).assoc("two", 2).assoc("three", 3),
                               PersistentHashMap.of("three", 3).assoc("two", 2).assoc("one", 1),
                               PersistentHashMap.of("two", 2, "three", 3, "one", 1),
                               PersistentHashMap.of("zne", 1, "two", 2, "three", 3));

        equalsDistinctHashCode(PersistentHashMap.of("one", 1).assoc("two", 2).assoc("three", 3),
                               PersistentHashMap.of("three", 3).assoc("two", 2).assoc("one", 1),
                               PersistentHashMap.of("two", 2, "three", 3, "one", 1),
                               PersistentHashMap.of("one", 1, "two", 2, "three", 2));

        equalsSameHashCode(PersistentHashMap.of("one", 1).assoc("two", 2).assoc("three", 3),
                           PersistentHashMap.of("three", 3).assoc("two", 2).assoc("one", 1),
                           PersistentHashMap.of("two", 2, "three", 3, "one", 1),
                           PersistentHashMap.of(1, "one", 2, "two", 3, "three"));
    }

    public void friendlierArrayEq(Object[] a1, Object[] a2) {
        if (a1 == null) {
            assertNull(a2);
            return;
        } else if (a2 == null) {
            assertNull(a1);
            return;
        }
        assertEquals(a1.length, a2.length);
        for (int i = 0; i < a1.length; i++) {
            assertTrue(a1[i].equals(a2[i]));
        }
    }

    @Test public void testToString() {
        assertEquals("PersistentHashMap()",
                     PersistentHashMap.empty().toString());
        assertEquals("PersistentHashMap(Tuple2(1,one))",
                     PersistentHashMap.of(1, "one").toString());
    }

    @Test public void without() {
        PersistentHashMap<Integer,String> m = PersistentHashMap.of(1, "one").assoc(2, "two").assoc(3, "three");

        assertEquals(m, m.without(0));

        assertEquals(PersistentHashMap.of(2, "two").assoc(3, "three"),
                     m.without(1));

        assertEquals(PersistentHashMap.of(1, "one").assoc(3, "three"),
                     m.without(2));

        assertEquals(PersistentHashMap.of(1, "one").assoc(2, "two"),
                     m.without(3));

        assertEquals(m, m.without(4));

        assertEquals(PersistentHashMap.of(3, "three"),
                     m.without(1).without(2));

        assertEquals(PersistentHashMap.of(1, "one").assoc(3, "three"),
                     m.without(2));

        assertEquals(PersistentHashMap.of(1, "one").assoc(2, "two"),
                     m.without(3));

        assertEquals(PersistentHashMap.EMPTY, PersistentHashMap.empty().without(4));
    }

    @Test public void without2() {
        Set<Integer> control = new HashSet<>();
        PersistentHashMap<Integer,String> m = PersistentHashMap.empty();
        int MAX = 20000;
        for (int i = 0; i < MAX; i++) {
            m = m.assoc(i, ordinal(i));
            control.add(i);
        }
        assertEquals(control.size(), m.size());

        while (control.size() > 0) {
            assertEquals(control.size(), m.size());

            // This yields a somewhat random integer from those that are left.
            int r = control.iterator().next();

            // Make sure we get out what we put in.
            assertEquals(ordinal(r), m.get(r));

            // Remove r from each.
            control.remove(r);
            m = m.without(r);
        }
        assertEquals(0, m.size());
    }

    @Test public void largerMap() {
        PersistentHashMap<Integer,String> m =
                PersistentHashMap.of(1, "one").assoc(2, "two").assoc(3, "three").assoc(4, "four").assoc(5, "five")
                        .assoc(6, "six").assoc(7, "seven").assoc(8, "eight").assoc(9, "nine").assoc(10, "ten")
                        .assoc(11, "eleven").assoc(12, "twelve").assoc(13, "thirteen").assoc(14, "fourteen")
                        .assoc(15, "fifteen").assoc(16, "sixteen").assoc(17, "seventeen").assoc(18, "eighteen")
                        .assoc(19, "nineteen").assoc(20, "twenty");
        m = m.without(10);
        m = m.without(9);
        m = m.without(11);
        m = m.assoc(11, "eleven again");
        m = m.assoc(10, "ten again");
        m = m.assoc(9, "nine again");
        m = m.without(1);
        m = m.assoc(1, "one again");
        m = m.assoc(21, "twenty one");
        m = m.without(20);
        m = m.assoc(20, "twenty again");

        System.out.println("m.keySet(): " + m.keySet());

        assertEquals(new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21)),
                     m.keySet());
        assertEquals(new HashSet<>(Arrays.asList("one again", "two", "three", "four", "five", "six", "seven", "eight",
                                                 "nine again", "ten again", "eleven again", "twelve", "thirteen",
                                                 "fourteen",
                                                 "fifteen", "sixteen", "seventeen", "eighteen", "nineteen", "twenty again",
                                                 "twenty one")),
                     m.values());
    }

    @Test public void entrySet() {
        PersistentHashMap<Integer,String> m =
                PersistentHashMap.of(1, "one").assoc(2, "two").assoc(3, "three").assoc(4, "four").assoc(5, "five");
        Set<Map.Entry<Integer,String>> s = new HashSet<>();
        s.add(UnmodMap.UnEntry.of(3, "three"));
        s.add(UnmodMap.UnEntry.of(5, "five"));
        s.add(UnmodMap.UnEntry.of(2, "two"));
        s.add(UnmodMap.UnEntry.of(1, "one"));
        s.add(UnmodMap.UnEntry.of(4, "four"));
        assertEquals(s, m.entrySet());
    }

    @Test public void values() {
        PersistentHashMap<Integer,String> m =
                PersistentHashMap.of(4, "four").assoc(5, "five").assoc(2, "two").assoc(3, "three").assoc(1, "one");
        Set<String> s = new HashSet<>(Arrays.asList("four", "one", "five", "two", "three"));

        // System.out.println("m: " + m);
        // System.out.println("m.hasNull(): " + m.hasNull());
        // System.out.println("m.seq(): " + m.seq());
        // System.out.println("m.seq().map(e -> e.getValue()).toMutableList(): " + m.seq().map(e -> e.getValue()).toMutableList());
        // System.out.println("m.seq().map(e -> e.getValue()).toMutableSet(): " + m.seq().map(e -> e.getValue()).toMutableSet());
        // System.out.println("m.seq().map(e -> e.getValue()).toImSortedSet(): " + m.seq().map(e -> e.getValue()).toImSortedSet(String.CASE_INSENSITIVE_ORDER));

        Sequence<String> seq = m.seq().map(e -> e.getValue());
        PersistentHashSet<String> u = PersistentHashSet.empty();
        // System.out.println("Initial u: " + u);
//        Function2<PersistentHashSet<String>,? super String,PersistentHashSet<String>> fun = (accum, t) -> accum.put(t);
        // System.out.println("seq: " + seq);
        // System.out.println("===>item: " + item);
        Option<String> item = seq.head();
        while (item.isSome()) {
            // System.out.println("item.get(): " + item.get());
            // u = fun.apply(u, item.get());
            u = u.put(item.get());
            // System.out.println("u: " + u);
            // repeat with next element
            seq = seq.tail();
            item = seq.head();
        }
        // System.out.println("Final u: " + u);
        // System.out.println("m.seq().map(e -> e.getValue()).foldLeft(): " + m.seq().map(e -> e.getValue()).foldLeft(PersistentHashSet.empty(), (accum, t) -> accum.put(t)));
        // System.out.println("m.seq().map(e -> e.getValue()).toImSet(): " + m.seq().map(e -> e.getValue()).toImSet());
        // System.out.println("m.values(): " + m.values());

        assertEquals(s, m.values());

        equalsDistinctHashCode(m.values(),
                               PersistentHashMap.of(4, "four").assoc(2, "two").assoc(5, "five").assoc(1, "one").assoc(3, "three").values(),
                               s,
                               new HashSet<>(Arrays.asList("four", "one", "zippy", "two", "three")));

//        assertTrue(m.values().equals(Arrays.asList("one", "two", "three", "four", "five")));
        assertNotEquals(0, m.values().hashCode());
        assertNotEquals(m.values().hashCode(), PersistentHashMap.of(4, "four").assoc(5, "five").hashCode());
        assertEquals(m.values().hashCode(),
                     PersistentHashMap.of(4, "four").assoc(2, "two").assoc(5, "five").assoc(1, "one").assoc(3, "three")
                                      .values()
                                      .hashCode());

    }

    public static class Z {
        public final LocalDateTime date;
        public final Integer integer;
        private Z(LocalDateTime d, Integer i) { date = d; integer = i; }
        public static Z of(LocalDateTime d, Integer i) { return new Z(d, i); }
    }

    public static Equator.ComparisonContext<Z> BY_DATE = new Equator.ComparisonContext<Z>() {
        @Override public int hash(Z z) { return z.date.hashCode(); }
        @Override public int compare(Z z1, Z z2) { return z1.date.compareTo(z2.date); }
    };

    public static Equator.ComparisonContext<Z> BY_INT = new Equator.ComparisonContext<Z>() {
        @Override public int hash(Z z) { return z.integer.hashCode(); }
        @Override public int compare(Z z1, Z z2) {
            return (z1.integer > z2.integer) ? 1 :
                   (z1.integer < z2.integer) ? -1 :
                    0;
        }
    };

    @Test public void testEquator() {
        Z z1 = Z.of(LocalDateTime.of(2015, 6, 13, 18, 38), 6);
        Z z2 = Z.of(LocalDateTime.of(2015, 6, 13, 18, 39), 5);
        Z z3 = Z.of(LocalDateTime.of(2015, 6, 13, 19, 38), 4);
        Z z4 = Z.of(LocalDateTime.of(2015, 6, 14, 18, 38), 3);
        Z z5 = Z.of(LocalDateTime.of(2015, 7, 13, 18, 38), 2);
        Z z6 = Z.of(LocalDateTime.of(2016, 6, 13, 18, 38), 1);

        ImMap<Z,String> a = PersistentHashMap.ofEq(
                BY_DATE,
                z1, ordinal(z1.integer),
                z3, ordinal(z3.integer),
                z5, ordinal(z5.integer),
                z6, ordinal(z6.integer));

        assertTrue(a.containsKey(z1));
        assertFalse(a.containsKey(z2));
        assertTrue(a.containsKey(z3));
        assertFalse(a.containsKey(z4));
        assertTrue(a.containsKey(z5));
        assertTrue(a.containsKey(z6));

        assertEquals(a, a.assoc(z1, ordinal(z1.integer)));
        assertEquals(a, a.assoc(z3, ordinal(z3.integer)));
        assertEquals(a, a.assoc(z5, ordinal(z5.integer)));
        assertEquals(a, a.assoc(z6, ordinal(z6.integer)));
        assertEquals(4, a.size());

        assertNotEquals(a, a.assoc(z1, "replaced"));
        assertEquals(4, a.size());
        assertNotEquals(a, a.assoc(z3, "replaced"));
        assertEquals(4, a.size());
        assertNotEquals(a, a.assoc(z5, "replaced"));
        assertEquals(4, a.size());
        assertNotEquals(a, a.assoc(z6, "replaced"));
        assertEquals(4, a.size());

        assertEquals(a, a.assoc(Z.of(z1.date, Integer.MAX_VALUE), ordinal(z1.integer)));
        assertEquals(4, a.size());
        assertEquals(a, a.assoc(Z.of(z3.date, Integer.MIN_VALUE), ordinal(z3.integer)));
        assertEquals(4, a.size());
        assertEquals(a, a.assoc(Z.of(z5.date, 0), ordinal(z5.integer)));
        assertEquals(4, a.size());
        assertEquals(a, a.assoc(Z.of(z6.date, 99999), ordinal(z6.integer)));
        assertEquals(4, a.size());

        a = a.assoc(z2, "added later");
        assertEquals(5, a.size());

        ImMap<Z,String> b = PersistentHashMap.ofEq(
                BY_INT,
                z2, ordinal(z2.integer),
                z4, ordinal(z4.integer),
                z6, ordinal(z6.integer));

        assertFalse(b.containsKey(z1));
        assertTrue(b.containsKey(z2));
        assertFalse(b.containsKey(z3));
        assertTrue(b.containsKey(z4));
        assertFalse(b.containsKey(z5));
        assertTrue(b.containsKey(z6));

        assertEquals(b, b.assoc(z2, ordinal(z2.integer)));
        assertEquals(b, b.assoc(z4, ordinal(z4.integer)));
        assertEquals(b, b.assoc(z6, ordinal(z6.integer)));
        assertEquals(3, b.size());

        assertNotEquals(b, b.assoc(z2, "replaced"));
        assertEquals(3, b.size());
        assertNotEquals(b, b.assoc(z4, "replaced"));
        assertEquals(3, b.size());
        assertNotEquals(b, b.assoc(z6, "replaced"));
        assertEquals(3, b.size());

        assertEquals(b, b.assoc(Z.of(LocalDateTime.MAX, z2.integer), ordinal(z2.integer)));
        assertEquals(3, b.size());
        assertEquals(b, b.assoc(Z.of(LocalDateTime.MIN, z4.integer), ordinal(z4.integer)));
        assertEquals(3, b.size());
        assertEquals(b, b.assoc(Z.of(LocalDateTime.now(), z6.integer), ordinal(z6.integer)));
        assertEquals(3, b.size());

        b = b.assoc(z3, "added later");
        assertEquals(4, b.size());

    }

    @Test public void testImMap10() {
        int max = 10;
        Map<Integer,String> a = PersistentHashMap.of(1, "One", 2, "Two", 3, "Three", 4, "Four", 5, "Five", 6, "Six",
                                                     7, "Seven", 8, "Eight", 9, "Nine", 10, "Ten");
        StaticImportsTest.mapHelper(a, max);
        Map<Integer,String> b = PersistentHashMap.ofSkipNull(
                Tuple2.of(1, "One"), Tuple2.of(2, "Two"), Tuple2.of(3, "Three"),
                Tuple2.of(4, "Four"), Tuple2.of(5, "Five"), Tuple2.of(6, "Six"),
                Tuple2.of(7, "Seven"), Tuple2.of(8, "Eight"), Tuple2.of(9, "Nine"),
                Tuple2.of(10, "Ten"));
        StaticImportsTest.mapHelper(b, max);

        Map<Integer,String> c = PersistentHashMap.ofEq(Equator.defaultEquator(),
                                                         1, "One", 2, "Two", 3, "Three", 4, "Four", 5, "Five", 6, "Six",
                                                         7, "Seven", 8, "Eight", 9, "Nine", 10, "Ten");
        assertEquals(a, b);
        assertEquals(b, a);
        assertEquals(a, c);
        assertEquals(c, a);
        assertEquals(b, c);
        assertEquals(c, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertEquals(a.hashCode(), c.hashCode());
        StaticImportsTest.mapHelperOdd(PersistentHashMap.ofSkipNull(
                Tuple2.of(1, "One"), null, Tuple2.of(3, "Three"), null, Tuple2.of(5, "Five"), null,
                Tuple2.of(7, "Seven"), null, Tuple2.of(9, "Nine"), null), max);
        StaticImportsTest.mapHelperEven(PersistentHashMap.ofSkipNull(
                null, Tuple2.of(2, "Two"), null, Tuple2.of(4, "Four"), null,
                Tuple2.of(6, "Six"), null, Tuple2.of(8, "Eight"), null, Tuple2.of(10, "Ten")), max);
    }

    @Test public void testImMap9() {
        int max = 9;
        Map<Integer,String> a = PersistentHashMap.of(1, "One", 2, "Two", 3, "Three", 4, "Four", 5, "Five", 6, "Six",
                                                     7, "Seven", 8, "Eight", 9, "Nine");
        StaticImportsTest.mapHelper(a, max);
        Map<Integer,String> b = PersistentHashMap.ofSkipNull(
                Tuple2.of(1, "One"), Tuple2.of(2, "Two"), Tuple2.of(3, "Three"),
                Tuple2.of(4, "Four"), Tuple2.of(5, "Five"), Tuple2.of(6, "Six"),
                Tuple2.of(7, "Seven"), Tuple2.of(8, "Eight"), Tuple2.of(9, "Nine"));
        StaticImportsTest.mapHelper(b, max);
        Map<Integer,String> c = PersistentHashMap.ofEq(Equator.defaultEquator(),
                                                         1, "One", 2, "Two", 3, "Three", 4, "Four", 5, "Five", 6, "Six",
                                                         7, "Seven", 8, "Eight", 9, "Nine");
        assertEquals(a, b);
        assertEquals(b, a);
        assertEquals(a, c);
        assertEquals(c, a);
        assertEquals(b, c);
        assertEquals(c, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertEquals(a.hashCode(), c.hashCode());
        StaticImportsTest.mapHelperOdd(PersistentHashMap.ofSkipNull(
                Tuple2.of(1, "One"), null, Tuple2.of(3, "Three"), null, Tuple2.of(5, "Five"), null,
                Tuple2.of(7, "Seven"), null, Tuple2.of(9, "Nine")), max);
        StaticImportsTest.mapHelperEven(PersistentHashMap.ofSkipNull(
                null, Tuple2.of(2, "Two"), null, Tuple2.of(4, "Four"), null,
                Tuple2.of(6, "Six"), null, Tuple2.of(8, "Eight"), null), max);
    }

    @Test public void testImMap8() {
        int max = 8;
        Map<Integer,String> a = PersistentHashMap.of(1, "One", 2, "Two", 3, "Three", 4, "Four", 5, "Five", 6, "Six",
                                                     7, "Seven", 8, "Eight");
        StaticImportsTest.mapHelper(a, max);
        Map<Integer,String> b = PersistentHashMap.ofSkipNull(
                Tuple2.of(1, "One"), Tuple2.of(2, "Two"), Tuple2.of(3, "Three"),
                Tuple2.of(4, "Four"), Tuple2.of(5, "Five"), Tuple2.of(6, "Six"),
                Tuple2.of(7, "Seven"), Tuple2.of(8, "Eight"));
        StaticImportsTest.mapHelper(b, max);
        Map<Integer,String> c = PersistentHashMap.ofEq(Equator.defaultEquator(),
                                                         1, "One", 2, "Two", 3, "Three", 4, "Four", 5, "Five", 6, "Six",
                                                         7, "Seven", 8, "Eight");
        assertEquals(a, b);
        assertEquals(b, a);
        assertEquals(a, c);
        assertEquals(c, a);
        assertEquals(b, c);
        assertEquals(c, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertEquals(a.hashCode(), c.hashCode());
        StaticImportsTest.mapHelperOdd(PersistentHashMap.ofSkipNull(
                Tuple2.of(1, "One"), null, Tuple2.of(3, "Three"), null, Tuple2.of(5, "Five"), null,
                Tuple2.of(7, "Seven"), null), max);
        StaticImportsTest.mapHelperEven(PersistentHashMap.ofSkipNull(
                null, Tuple2.of(2, "Two"), null, Tuple2.of(4, "Four"), null,
                Tuple2.of(6, "Six"), null, Tuple2.of(8, "Eight")), max);
    }

    @Test public void testImMap7() {
        int max = 7;
        Map<Integer,String> a = PersistentHashMap.of(1, "One", 2, "Two", 3, "Three", 4, "Four", 5, "Five", 6, "Six",
                                                     7, "Seven");
        StaticImportsTest.mapHelper(a, max);
        Map<Integer,String> b = PersistentHashMap.ofSkipNull(
                Tuple2.of(1, "One"), Tuple2.of(2, "Two"), Tuple2.of(3, "Three"),
                Tuple2.of(4, "Four"), Tuple2.of(5, "Five"), Tuple2.of(6, "Six"),
                Tuple2.of(7, "Seven"));
        StaticImportsTest.mapHelper(b, max);
        Map<Integer,String> c = PersistentHashMap.ofEq(Equator.defaultEquator(),
                                                         1, "One", 2, "Two", 3, "Three", 4, "Four", 5, "Five", 6, "Six",
                                                         7, "Seven");
        assertEquals(a, b);
        assertEquals(b, a);
        assertEquals(a, c);
        assertEquals(c, a);
        assertEquals(b, c);
        assertEquals(c, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertEquals(a.hashCode(), c.hashCode());
        StaticImportsTest.mapHelperOdd(PersistentHashMap.ofSkipNull(
                Tuple2.of(1, "One"), null, Tuple2.of(3, "Three"), null, Tuple2.of(5, "Five"), null,
                Tuple2.of(7, "Seven")), max);
        StaticImportsTest.mapHelperEven(PersistentHashMap.ofSkipNull(
                null, Tuple2.of(2, "Two"), null, Tuple2.of(4, "Four"), null,
                Tuple2.of(6, "Six"), null), max);
    }

    @Test public void testImMap6() {
        int max = 6;
        Map<Integer,String> a = PersistentHashMap.of(1, "One", 2, "Two", 3, "Three", 4, "Four", 5, "Five", 6, "Six");
        StaticImportsTest.mapHelper(a, max);
        Map<Integer,String> b = PersistentHashMap.ofSkipNull(
                Tuple2.of(1, "One"), Tuple2.of(2, "Two"), Tuple2.of(3, "Three"),
                Tuple2.of(4, "Four"), Tuple2.of(5, "Five"), Tuple2.of(6, "Six"));
        StaticImportsTest.mapHelper(b, max);
        Map<Integer,String> c = PersistentHashMap.ofEq(Equator.defaultEquator(),
                                                         1, "One", 2, "Two", 3, "Three", 4, "Four", 5, "Five",
                                                         6, "Six");
        assertEquals(a, b);
        assertEquals(b, a);
        assertEquals(a, c);
        assertEquals(c, a);
        assertEquals(b, c);
        assertEquals(c, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertEquals(a.hashCode(), c.hashCode());
        StaticImportsTest.mapHelperOdd(PersistentHashMap.ofSkipNull(
                Tuple2.of(1, "One"), null, Tuple2.of(3, "Three"), null, Tuple2.of(5, "Five"), null), max);
        StaticImportsTest.mapHelperEven(PersistentHashMap.ofSkipNull(
                null, Tuple2.of(2, "Two"), null, Tuple2.of(4, "Four"), null,
                Tuple2.of(6, "Six")), max);
    }

    @Test public void testImMap5() {
        int max = 5;
        Map<Integer,String> a = PersistentHashMap.of(1, "One", 2, "Two", 3, "Three", 4, "Four", 5, "Five");
        StaticImportsTest.mapHelper(a, max);
        Map<Integer,String> b = PersistentHashMap.ofSkipNull(
                Tuple2.of(1, "One"), Tuple2.of(2, "Two"), Tuple2.of(3, "Three"),
                Tuple2.of(4, "Four"), Tuple2.of(5, "Five"));
        StaticImportsTest.mapHelper(b, max);
        Map<Integer,String> c = PersistentHashMap.ofEq(Equator.defaultEquator(),
                                                         1, "One", 2, "Two", 3, "Three", 4, "Four", 5, "Five");
        assertEquals(a, b);
        assertEquals(b, a);
        assertEquals(a, c);
        assertEquals(c, a);
        assertEquals(b, c);
        assertEquals(c, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertEquals(a.hashCode(), c.hashCode());
        StaticImportsTest.mapHelperOdd(PersistentHashMap.ofSkipNull(
                Tuple2.of(1, "One"), null, Tuple2.of(3, "Three"), null, Tuple2.of(5, "Five")), max);
        StaticImportsTest.mapHelperEven(PersistentHashMap.ofSkipNull(
                null, Tuple2.of(2, "Two"), null, Tuple2.of(4, "Four"), null), max);
    }

    @Test public void testImMap4() {
        int max = 4;
        Map<Integer,String> a = PersistentHashMap.of(1, "One", 2, "Two", 3, "Three", 4, "Four");
        StaticImportsTest.mapHelper(a, max);
        Map<Integer,String> b = PersistentHashMap.ofSkipNull(
                Tuple2.of(1, "One"), Tuple2.of(2, "Two"), Tuple2.of(3, "Three"),
                Tuple2.of(4, "Four"));
        StaticImportsTest.mapHelper(b, max);
        Map<Integer,String> c = PersistentHashMap.ofEq(Equator.defaultEquator(),
                                                         1, "One", 2, "Two", 3, "Three", 4, "Four");
        assertEquals(a, b);
        assertEquals(b, a);
        assertEquals(a, c);
        assertEquals(c, a);
        assertEquals(b, c);
        assertEquals(c, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertEquals(a.hashCode(), c.hashCode());
        StaticImportsTest.mapHelperOdd(PersistentHashMap.ofSkipNull(
                Tuple2.of(1, "One"), null, Tuple2.of(3, "Three"), null), max);
        StaticImportsTest.mapHelperEven(PersistentHashMap.ofSkipNull(
                null, Tuple2.of(2, "Two"), null, Tuple2.of(4, "Four")), max);
    }

    @Test public void testImMap3() {
        int max = 3;
        Map<Integer,String> a = PersistentHashMap.of(1, "One", 2, "Two", 3, "Three");
        StaticImportsTest.mapHelper(a, max);
        Map<Integer,String> b = PersistentHashMap.ofSkipNull(
                Tuple2.of(1, "One"), Tuple2.of(2, "Two"), Tuple2.of(3, "Three"));
        StaticImportsTest.mapHelper(b, max);
        Map<Integer,String> c = PersistentHashMap.ofEq(Equator.defaultEquator(),
                                                         1, "One", 2, "Two", 3, "Three");
        assertEquals(a, b);
        assertEquals(b, a);
        assertEquals(a, c);
        assertEquals(c, a);
        assertEquals(b, c);
        assertEquals(c, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertEquals(a.hashCode(), c.hashCode());
        StaticImportsTest.mapHelperOdd(PersistentHashMap.ofSkipNull(
                Tuple2.of(1, "One"), null, Tuple2.of(3, "Three")), max);
        StaticImportsTest.mapHelperEven(PersistentHashMap.ofSkipNull(
                null, Tuple2.of(2, "Two"), null), max);
    }

    @Test public void testImMap2() {
        int max = 2;
        Map<Integer,String> a = PersistentHashMap.of(1, "One", 2, "Two");
        StaticImportsTest.mapHelper(a, max);
        Map<Integer,String> b = PersistentHashMap.ofSkipNull(
                Tuple2.of(1, "One"), Tuple2.of(2, "Two"));
        StaticImportsTest.mapHelper(b, max);
        Map<Integer,String> c = PersistentHashMap.ofEq(Equator.defaultEquator(),
                                                         1, "One", 2, "Two");
        assertEquals(a, b);
        assertEquals(b, a);
        assertEquals(a, c);
        assertEquals(c, a);
        assertEquals(b, c);
        assertEquals(c, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertEquals(a.hashCode(), c.hashCode());
        StaticImportsTest.mapHelperOdd(PersistentHashMap.ofSkipNull(
                Tuple2.of(1, "One"), null), max);
        StaticImportsTest.mapHelperEven(PersistentHashMap.ofSkipNull(
                null, Tuple2.of(2, "Two")), max);
    }

    @Test public void testImMap1() {
        int max = 1;
        Map<Integer,String> a = PersistentHashMap.of(1, "One");
        StaticImportsTest.mapHelper(a, max);
        Map<Integer,String> b = PersistentHashMap.ofSkipNull(
                Tuple2.of(1, "One"));
        StaticImportsTest.mapHelper(b, max);
        Map<Integer,String> c = PersistentHashMap.ofEq(Equator.defaultEquator(),
                                                         1, "One");
        assertEquals(a, b);
        assertEquals(b, a);
        assertEquals(a, c);
        assertEquals(c, a);
        assertEquals(b, c);
        assertEquals(c, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertEquals(a.hashCode(), c.hashCode());
        StaticImportsTest.mapHelperOdd(PersistentHashMap.ofSkipNull(
                Tuple2.of(1, "One")), max);
        StaticImportsTest.mapHelperEven(PersistentHashMap.ofSkipNull((Map.Entry<Integer,String>) null), max);
    }

    @Test public void testImMap0() {
        int max = 0;
        Map<Integer,String> b = PersistentHashMap.ofSkipNull();
        StaticImportsTest.mapHelper(b, max);
        Map<Integer,String> c = PersistentHashMap.empty(Equator.defaultEquator());
        assertEquals(b, c);
        assertEquals(c, b);
        assertEquals(b.hashCode(), c.hashCode());
        StaticImportsTest.mapHelperOdd(PersistentHashMap.ofSkipNull(), max);
        StaticImportsTest.mapHelperEven(PersistentHashMap.ofSkipNull(), max);
    }

    public static class Result<A,B> {
        List<Tuple2<A,B>> goodies;
        List<A> baddies;
        boolean hasNull;
    }

    public static <A,B> void verify(Result<A,B> result, PersistentHashMap<A,B> m) {
        assertEquals(result.hasNull, m.hasNull());
        assertEquals(result.hasNull, m.containsKey(null));

        assertEquals(result.goodies.size(), m.size());

        for (Tuple2<A,B> t : result.goodies) {
            assertTrue(m.containsKey(t.getKey()));
            assertEquals(t.getValue(), m.get(t.getKey()));
            assertTrue(m.entry(t.getKey()).isSome());
            assertEquals(t, m.entry(t.getKey()).get());
        }

        for (A a : result.baddies) {
            assertFalse(m.containsKey(a));
            assertNull(m.get(a));
            assertFalse(m.entry(a).isSome());
        }

        int s = m.size();
        for (Tuple2<A,B> t : result.goodies) {
            --s;
            m = m.without(t.getKey());
            assertFalse(m.containsKey(t.getKey()));
            assertFalse(m.entry(t.getKey()).isSome());
            assertEquals(null, m.get(t.getKey()));
            assertEquals(s, m.size());
        }
        assertEquals(0, m.size());

        for (Tuple2<A,B> t : result.goodies) {
            assertFalse(m.containsKey(t.getKey()));
            assertNull(m.get(t.getKey()));
        }
    }

    @Test public void testSkipNull() {
        Result<Integer,String> result = new Result<>();
        result.goodies = Arrays.asList(Tuple2.of(1, "one"), Tuple2.of(2, "two"), Tuple2.of(3, "three"),
                                       Tuple2.of(4, "four"));
        result.baddies = Arrays.asList(0, 5, Integer.MAX_VALUE, Integer.MIN_VALUE, null);
        result.hasNull = false;

        verify(result, PersistentHashMap.ofSkipNull(
                Tuple2.of(1, "one"),
                null,
                Tuple2.of(2, "two"),
                null,
                Tuple2.of(3, "three"),
                null,
                Tuple2.of(4, "four")));

        verify(result, PersistentHashMap.ofSkipNull(
                null,
                Tuple2.of(1, "one"),
                null,
                Tuple2.of(2, "two"),
                null,
                Tuple2.of(3, "three"),
                null,
                Tuple2.of(4, "four"),
                null));

        verify(result, PersistentHashMap.ofEqSkipNull(
                Equator.defaultEquator(),
                null,
                Tuple2.of(1, "one"),
                null,
                Tuple2.of(2, "two"),
                null,
                Tuple2.of(3, "three"),
                null,
                Tuple2.of(4, "four"),
                null));

        verify(result, PersistentHashMap.ofEqSkipNull(
                Equator.defaultEquator(),
                Tuple2.of(1, "one"),
                null,
                Tuple2.of(2, "two"),
                null,
                Tuple2.of(3, "three"),
                null,
                Tuple2.of(4, "four")));
    }

    @Test public void withNull() {
        Result<Integer,String> result = new Result<>();
        result.goodies = Arrays.asList(Tuple2.of(null, "nada"), Tuple2.of(2, "two"), Tuple2.of(1, "one"));
        result.baddies = Arrays.asList(0, 3, Integer.MAX_VALUE, Integer.MIN_VALUE);
        result.hasNull = true;

        verify(result, PersistentHashMap.of(null, "nada", 1, "one", 2, "two"));


        verify(result, PersistentHashMap.of(1, "one", 2, "two", null, "nada"));

        verify(result, PersistentHashMap.<Integer,String>empty(Equator.defaultEquator())
                .assoc(1, "one").assoc(null, "nada").assoc(2, "two"));
    }
}
