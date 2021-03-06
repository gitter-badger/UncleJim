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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Test;

import static org.junit.Assert.*;

public class UnmodCollectionTest {
    @Test public void contains() {
        Collection<String> c1 = Arrays.asList("Hello", "there", "world", "this", "is", "a", "test");
        assertFalse(UnmodCollection.contains(c1, "poodle"));
        assertFalse(UnmodCollection.contains(c1, null));
        assertTrue(UnmodCollection.contains(c1, "Hello"));
        assertTrue(UnmodCollection.contains(c1, "this"));
        assertTrue(UnmodCollection.contains(c1, "test"));

        assertTrue(UnmodCollection.containsAll(c1, c1));
        assertTrue(UnmodCollection.containsAll(c1, Arrays.asList("there", "this", "a")));
        assertTrue(UnmodCollection.containsAll(c1, Arrays.asList("Hello", "world", "is", "test")));
        assertFalse(UnmodCollection.containsAll(c1, Arrays.asList("Hello", "world", "is", "test", "noodle")));
        assertFalse(UnmodCollection.containsAll(c1, Arrays.asList("billboard", "Hello", "world", "is", "test")));
        assertFalse(UnmodCollection.containsAll(c1, Collections.singletonList("phone")));
        assertFalse(UnmodCollection.containsAll(c1, Collections.singletonList(null)));

        assertFalse(UnmodCollection.contains(UnmodCollection.empty(), "phone"));
        assertFalse(UnmodCollection.contains(UnmodCollection.empty(), null));
        assertFalse(UnmodCollection.containsAll(UnmodCollection.empty(), Collections.singletonList("phone")));
        assertFalse(UnmodCollection.containsAll(UnmodCollection.empty(), Collections.singletonList(null)));
    }

    @Test public void toArray() {
        Collection<String> c1 = Arrays.asList("Hello", "there", "world", "this", "is", "a", "test");
        assertArrayEquals(new String[]{"Hello", "there", "world", "this", "is", "a", "test"},
                          UnmodCollection.toArray(c1));
        assertArrayEquals(new String[]{"Hello", "there", "world", "this", "is", "a", "test"},
                          UnmodCollection.toArray(c1, new String[0]));
        assertArrayEquals(new String[]{"Hello", "there", "world", "this", "is", "a", "test"},
                          UnmodCollection.toArray(c1, new String[7]));
        assertArrayEquals(new String[]{"Hello", "there", "world", "this", "is", "a", "test"},
                          Arrays.copyOf(UnmodCollection.toArray(c1, new String[99]), 7));

        assertArrayEquals(new Object[0],
                          UnmodCollection.toArray(UnmodCollection.empty()));
        assertArrayEquals(new Object[0],
                          UnmodCollection.toArray(UnmodCollection.empty(), new Object[0]));
    }
}
