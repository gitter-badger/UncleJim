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

package org.organicdesign.fp;

import org.junit.Test;
import org.organicdesign.fp.collections.ImList;
import org.organicdesign.fp.collections.ImMap;
import org.organicdesign.fp.collections.UnmodIterable;
import org.organicdesign.fp.function.Function1;
import org.organicdesign.fp.tuple.Tuple2;
import org.organicdesign.fp.tuple.Tuple3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.organicdesign.fp.StaticImports.tup;
import static org.organicdesign.fp.StaticImports.vec;
import static org.organicdesign.fp.UsageExampleTest.ColorVal.*;
import static org.organicdesign.fp.UsageExampleTest.EmailType.HOME;
import static org.organicdesign.fp.UsageExampleTest.EmailType.WORK;

// Usage examples are kept in this unit test to ensure they remain correct and current.
public class UsageExampleTest {

    // Define some field name constants with a standard Java Enum
    enum EmailType { HOME, WORK };

    // Part one of a 3-part example for defining data briefly and/or in a way that's easy to read.
    @Test public void dataDefinitionExample1() {
        // We start by creating a list of "people."  The tup(), vec(), set(), and map() methods are
        // imported from StaticImports and provide a mini data-definition language.  vec() creates
        // a vector (immutable list) of any length.  tup() creates a Tuple (some languages call this
        // a "record").  For now, you can think of Tuples as type-safe anonymous objects.
        //
        // This structured date is type-safe even though it looks a little like JSON.  Unlike JSON,
        // the compiler verifies the types! You can ignore the huge type signatures for now -
        // subsequent examples show different ways to simplify them.
        ImList<Tuple3<String,String,ImList<Tuple2<EmailType,String>>>> people =
                vec(tup("Jane", "Smith", vec(tup(HOME, "a@b.c"),
                                             tup(WORK, "b@c.d"))),
                    tup("Fred", "Tase", vec(tup(HOME, "c@d.e"),
                                            tup(WORK, "d@e.f"))));

        // Everything has build-in toString() methods.  Collections show the first 3-5 elements.
        assertEquals("PersistentVector(" +
                     "Tuple3(Jane,Smith,PersistentVector(Tuple2(HOME,a@b.c),Tuple2(WORK,b@c.d)))," +
                     "Tuple3(Fred,Tase,PersistentVector(Tuple2(HOME,c@d.e),Tuple2(WORK,d@e.f))))",
                     people.toString());

        // Let's look at Jane:
        Tuple3<String,String,ImList<Tuple2<EmailType,String>>> jane = people.get(0);

        assertEquals("Tuple3(Jane,Smith,PersistentVector(Tuple2(HOME,a@b.c),Tuple2(WORK,b@c.d)))",
                     jane.toString());

        // Let's make a map that we can use later to look up people by their email address.
        ImMap<String,Tuple3<String,String,ImList<Tuple2<EmailType,String>>>> peopleByEmail =

                // flatMap can be confusing the first time you see it.  We may need to produce
                // multiple entries in the resulting map (dictionary) for each person.  DBA's call
                // this a one-to-many relationship.  FlatMap is the way to produce zero or more
                // items for each input item.
                // Note: person._3() is the vector of email addresses
                people.flatMap(person -> person._3()
                                               // Map (dictionary) entries are key value pairs.
                                               // Tuple2 implements Map.Entry so we can set up
                                               // the pair right here.
                                               // Note: mail._2() is the address
                                               .map(mail -> tup(mail._2(), person)))
                        // Now convert the result into an immutable map.  This function takes
                        // another function which is normally used to convert data into key/value
                        // pairs, but we already have key/value pairs, so we just pass the identity
                        // function (which returns its argument unchanged)
                      .toImMap(Function1.identity());

        // Let's look at the map we just created
        assertEquals("PersistentHashMap(" +
                     "Tuple2(d@e.f," +
                     "Tuple3(Fred,Tase,PersistentVector(Tuple2(HOME,c@d.e),Tuple2(WORK,d@e.f))))," +
                     "Tuple2(a@b.c," +
                     "Tuple3(Jane,Smith,PersistentVector(Tuple2(HOME,a@b.c),Tuple2(WORK,b@c.d))))," +
                     "Tuple2(b@c.d," +
                     "Tuple3(Jane,Smith,PersistentVector(Tuple2(HOME,a@b.c),Tuple2(WORK,b@c.d))))," +
                     "Tuple2(c@d.e," +
                     "Tuple3(Fred,Tase,PersistentVector(Tuple2(HOME,c@d.e),Tuple2(WORK,d@e.f)))))",
                     peopleByEmail.toString());

        // Look up Jane by her address
        assertEquals(jane, peopleByEmail.get("b@c.d"));

        // Conclusion:
        // For the price of a few long type signatures, we can write very succinct Java code
        // without all the pre-work of defining types.  This is great for experiments, one-off
        // reports, or other low-investment high-value projects.
        //
        // Next we'll look at two different ways to eliminate or simplify those type signatures.
    }

    // Part 2 of 3
    @Test public void dataDefinitionExample2() {
        // Fluent interfaces can take maximum advantage of Java's type inferencing.  Here is
        // more-or-less the same code as the above, still type-checked by the compiler, but without
        // specifying any types explicitly:

        assertEquals("Jane",
                     // This is the "people" data structure from above
                     vec(tup("Jane", "Smith", vec(tup(HOME, "a@b.c"),
                                                  tup(WORK, "b@c.d"))),
                         tup("Fred", "Tase", vec(tup(HOME, "c@d.e"),
                                                 tup(WORK, "d@e.f"))))
                             // Create a map to look up people by their address
                             .flatMap(person -> person._3()
                                                      .map(mail -> tup(mail._2(), person)))
                             .toImMap(Function1.identity())
                             // Look up Jane by her address
                             .get("b@c.d")
                             // Get her first name
                             ._1());

        // Conclusion:
        // This kind of loose and free coding is great for trying out ideas, but it can be a little
        // hard for the person after you to read, especially with all the _1(), _2(), and _3()
        // methods on the tuples.  Naming your data types well can completely fix the legibility
        // problem.
    }

    // Part 3 of 3
    // The previous examples could have been cleaned up very easily with ML's type aliases.  Second
    // choice would be to case classes like Scala.  The best we can do in Java is to use objects,
    // but doing so has the useful side effect of letting us name the accessor methods. Extending
    // Tuples also give us immutable fields, equals(), hashCode(), and toString() methods, which
    // would otherwise be taxing to write and debug by hand!
    static class Email extends Tuple2<EmailType,String> {
        // Constructor delegates to Tuple2 constructor
        Email(EmailType t, String s) { super(t, s); }

        // Static factory method (for creating new instances) overrides Tuple2.of()
        public static Email of(EmailType t, String s) { return new Email(t, s); }

        // As long as we are making an object, we might as well give descriptive names to the
        // field getters
        public EmailType type() { return _1(); }
        public String address() { return _2(); }
    }

    // Notice in this type signature, we can replace Tuple2<EmailType,String> with Email
    static class Person extends Tuple3<String,String,ImList<Email>> {

        // Constructor delegates to Tuple3 constructor
        Person(String f, String l, ImList<Email> es) { super(f, l, es); }

        // Static factory method (for creating new instances) overrides Tuple3.of()
        public static Person of(String f, String l, ImList<Email> es) {
            return new Person(f, l, es);
        }

        // Give more descriptive names to the field getters
        public String first() { return _1(); }
        public String last() { return _2(); }
        public ImList<Email> emailAddrs() { return _3(); }
    }

    // Part 3 of 3 (continued)
    // Use the classes we made above to simplify the types and improve the toString implementations.
    @Test public void dataDefinitionExample3() {

        // The type signatures from the first example become very simple
        ImList<Person> people =
                vec(Person.of("Jane", "Smith", vec(Email.of(HOME, "a@b.c"),
                                                   Email.of(WORK, "b@c.d"))),
                    Person.of("Fred", "Tase", vec(Email.of(HOME, "c@d.e"),
                                                  Email.of(WORK, "d@e.f"))));

        // Notice that the tuples are smart enough to take their new names, Person and Email instead
        // of Tuple3 and Tuple2:
        assertEquals("PersistentVector(" +
                     "Person(Jane,Smith," +
                     "PersistentVector(Email(HOME,a@b.c),Email(WORK,b@c.d)))," +
                     "Person(Fred,Tase," +
                     "PersistentVector(Email(HOME,c@d.e),Email(WORK,d@e.f))))",
                     people.toString());

        // This type signature couldn't be simpler:
        Person jane = people.get(0);

        assertEquals("Person(Jane,Smith," +
                     "PersistentVector(Email(HOME,a@b.c),Email(WORK,b@c.d)))",
                     jane.toString());

        // Let's use our new, descriptive field getter methods:
        assertEquals("Jane", jane.first());
        assertEquals("Smith", jane.last());

        Email janesAddr = jane.emailAddrs().get(0);
        assertEquals(HOME, janesAddr.type());
        assertEquals("a@b.c", janesAddr.address());

        // Another simplified type signature.  Also notice that we are using descriptive method
        // names instead of _1(), _2(), and _3().
        ImMap<String,Person> peopleByEmail =
                people.flatMap(person -> person.emailAddrs()
                                               .map(mail -> tup(mail.address(), person)))
                      .toImMap(Function1.identity());

        assertEquals("PersistentHashMap(" +
                     "Tuple2(d@e.f," +
                     "Person(Fred,Tase,PersistentVector(Email(HOME,c@d.e),Email(WORK,d@e.f))))," +
                     "Tuple2(a@b.c," +
                     "Person(Jane,Smith,PersistentVector(Email(HOME,a@b.c),Email(WORK,b@c.d))))," +
                     "Tuple2(b@c.d," +
                     "Person(Jane,Smith,PersistentVector(Email(HOME,a@b.c),Email(WORK,b@c.d))))," +
                     "Tuple2(c@d.e," +
                     "Person(Fred,Tase,PersistentVector(Email(HOME,c@d.e),Email(WORK,d@e.f)))))",
                     peopleByEmail.toString());

        // Now look them up:
        assertEquals(jane, peopleByEmail.get("b@c.d"));

        // Conclusion:
        // Any Java shop should appreciate this kind of Immutable, Object-Oriented, Functional code
        // for it's legibility, reliability, and consistency.  Extending Tuples lets us accomplish
        // all this with less boilerplate than traditional Java coding.  Also, you can start with
        // a brief and dirty proof of concept, then retrofit step by step to the point where your
        // code is legible and easy to maintain.
        //
        // If you need to write out a complex type like the first example, then it's probably time
        // to define some classes in order to ensure quality code.
    }

    // Here is a fresh example to just focus on the Transormable/UnmodIterable interfaces.
    @Test public void transformTest() {
        // These transformations do not change the underlying data.  They build a new collection by
        // chaining together the specified operations, then applying them in a single pass.

        UnmodIterable<Integer> v1 = vec(4, 5);

        // Make a new vector with more numbers at the beginning.  "precat" is short for
        // "prepend version of concatenate" or "add-to-beginning".
        UnmodIterable<Integer> v2 = v1.precat(vec(1, 2, 3));

        // v2 now represents a bigger list of numbers
        assertEquals(vec(1, 2, 3, 4, 5), v2.toImList());

        // v1 is unchanged
        assertEquals(vec(4, 5), v1);

        // Instead of updating in place, each change returns a new data structure which is an
        // extremelly lightweight copy of the old because it shares as much as possible with the
        // previous structure.
        v2 = v2.concat(vec(6, 7, 8, 9));

        assertEquals(vec(1, 2, 3, 4, 5, 6, 7, 8, 9), v2.toImList());

        v2 = v2.filter(i -> i > 4);

        assertEquals(vec(5,6,7,8,9), v2.toImList());

        v2 = v2.map(i -> i - 2);

        assertEquals(vec(3,4,5,6,7), v2.toImList());

        // After a take, the subsequent items are not processed by the transformation.
        // If you had a billion items, this would only allow the first 5 to be processed.
        v2 = v2.take(4);

        assertEquals(vec(3,4,5,6), v2.toImList());

        v2 = v2.drop(2);

        assertEquals(vec(5,6), v2.toImList());

        // Let's see that again with the methods all chained together
        assertEquals(vec(5, 6),
                     vec(4, 5)                        //          4, 5
                             .precat(vec(1, 2, 3))    // 1, 2, 3, 4, 5
                             .concat(vec(6, 7, 8, 9)) // 1, 2, 3, 4, 5, 6, 7, 8, 9
                             .filter(i -> i > 4)      //             5, 6, 7, 8, 9
                             .map(i -> i - 2)         //       3, 4, 5, 6, 7
                             .take(4)                 //       3, 4, 5, 6
                             .drop(2)                 //             5, 6
                             .toImList());

        // Conclusion:
        // Once you get used to them, Transformations are easier to write and read than their
        // traditional Java looping counterparts (and almost as fast).  They are also much
        // easier to understand than Java 8 streams and handle and immutable destination
        // collections well.
    }

    // This is a new example that illustrates a common short-cut to use in Java code, but we
    // build on it in the next example.  This example converts from a character, byte, or integer
    // code to an enum value.
    enum ColorVal {
        // Standard enum declaration which defines a unique character code for each enum value.
        RED('R'), GREEN('G'), BLUE('B');
        private final Character ch;
        ColorVal(Character c) { ch = c; }
        public Character ch() { return ch; }

        // Convert the values() array of this enum to a map of key/value pairs.  This takes at least
        // 6 lines of code in a static initializer block or lambda to keep it private and
        // unmodifiable in traditional Java
        public static final ImMap<Character,ColorVal> charToColorMap =
                vec(values()).toImMap(v -> Tuple2.of(v.ch(), v));
    }

    // Let's use the above code to examine immutable maps in general.
    @Test public void enumCodeLookupTest() {
        assertNull(ColorVal.charToColorMap.get('1'));
        assertEquals(RED, ColorVal.charToColorMap.get('R'));
        assertEquals(GREEN, ColorVal.charToColorMap.get('G'));
        assertEquals(BLUE, ColorVal.charToColorMap.get('B'));

        // charToColorMap is "immutable" in a way that's safe to make extremely lightweight modified
        // copies of.  Someone else could build off that collection to refer to just the RED and
        // GREEN values, sometimes by number-characters instead of by letter-characters:
        ImMap<Character,ColorVal> secondMap = ColorVal.charToColorMap
                .assoc('1', RED)
                .assoc('2', GREEN)
                .without('B');

        assertEquals(RED, secondMap.get('1'));
        assertEquals(GREEN, secondMap.get('2'));
        assertEquals(RED, secondMap.get('R'));
        assertEquals(GREEN, secondMap.get('G'));
        assertNull(secondMap.get('B'));

        // Original map is unchanged by the intervening operations:
        assertNull(ColorVal.charToColorMap.get('1'));
        assertEquals(RED, ColorVal.charToColorMap.get('R'));
        assertEquals(GREEN, ColorVal.charToColorMap.get('G'));
        assertEquals(BLUE, ColorVal.charToColorMap.get('B'));
    }

    // TODO: Add an example with a set.
}
