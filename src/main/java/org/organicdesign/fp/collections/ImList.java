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

/**
 Holds Immutable "modification" methods that return a new ImList reflecting the modification while
 sharing as much data structure with the previous ImList as possible (for performance).
 */
public interface ImList<E> extends UnmodList<E> {
    /** {@inheritDoc} */
    @Override default UnmodSortedIterator<E> iterator() { return listIterator(0); }

// Inherited correctly - there is no ImIterator.
// UnmodListIterator<E> listIterator(int index) {

// Inherited correctly and need to be implemented by the implementing class
// int size() {
// boolean equals(Object o) {
// int hashCode() {
// E get(int index) {

    /**
     This default implementation is at least O(n) slow.
     Inserts a new item at the specified index, shifting that item and subsequent items up/right.
     @param i the zero-based index to insert at
     @param val the value to insert
     @return a new ImList with the additional item.
     */
    default ImList<E> insert(int i, E val) {
        if (i == size()) { return append(val); }

        if ( (i > size()) || (i < 0) ) {
            throw new IllegalArgumentException("Can't insert outside the possible bounds");
        }

        UnmodIterator<E> iter = iterator();
        ImList<E> v = PersistentVector.empty();
        int j = 0;
        for (; j < i; j++) {
            v = v.append(iter.next());
        }
        v = v.append(val);
        for (; j < size(); j++) {
            v = v.append(iter.next());
        }
        return v;
    }

    /**
     Adds one item to the end of the ImList.

     @param e the value to insert
     @return a new ImList with the additional item at the end.
     */
    ImList<E> append(E e);

    /**
     Adds multiple items to the end of the ImList.

     @param es the values to insert
     @return a new ImList with the additional items at the end.
     */
    @SuppressWarnings("unchecked")
    @Override default ImList<E> concat(Iterable<? extends E> es) {
        ImList<E> result = this;
        for (E e : es) {
            result = result.append(e);
        }
        return result;
    };

//    /** {@inheritDoc} */
//    @Override ImList<E> concat(Sequence<E> other);


//    /**
//     The first item in this sequence.  This was originally called first() but that conflicted with
//     SortedSet.first() which did not return an Option and threw an exception when the set was
//     empty.
//     */
//    @Override default Option<E> head() {
//        return size() > 0 ? Option.of(get(0)) : Option.none();
//    }
//
//    /**
//     The rest of this sequnce (all the items after its head).  This was originally called rest(),
//     but when I renamed first() to head(), I renamed rest() to tail() so that it wouldn't mix
//     metaphors.
//     */
//    @Override default Sequence<E> tail() {
//        return Sequence.ofIter(this).tail();
//    }
//
    /**
     * This method goes against Josh Bloch's Item 25: "Prefer Lists to Arrays", but is provided for
     * backwards compatibility in some performance-critical situations.
     * {@inheritDoc}
     */
    @Override default Object[] toArray() { return UnmodCollection.toArray(this); }

// I don't know if this is a good idea or not and I don't want to have to support it if not.
//    /**
//     * Returns the item at this index, but takes any Number as an argument.
//     * @param n the zero-based index to get from the vector.
//     * @return the value at that index.
//     */
//    default E get(Number n) { return get(n.intValue()); }

    /**
     * Returns the item at this index.
     * @param i the zero-based index to get from the vector.
     * @param notFound the value to return if the index is out of bounds.
     * @return the value at that index, or the notFound value.
     */
    default E get(int i, E notFound) {
        if (i >= 0 && i < size())
            return get(i);
        return notFound;
    }

    /**
     Replace the item at the given index.  Note: i.replace(i.size(), o) used to be equivalent to
     i.concat(o), but it probably won't be for the RRB tree implementation, so this will change too.

     @param idx the index where the value should be stored.
     @param e the value to store
     @return a new ImList with the replaced item
     */
    // TODO: Don't make i.replace(i.size(), o) equivalent to i.concat(o)
    ImList<E> replace(int idx, E e);

    // ====================================== STATIC METHODS ======================================
//    static <T> ImList<T> empty() { return PersistentVector.empty(); }

//    /**
//     * Inserts a new item at the specified index.
//     * @param i the zero-based index to insert at (pushes current item and all subsequent items
//     *        up)
//     * @param val the value to insert
//     * @return a new ImList with the additional item.
//     */
//    static <E> ImList<E> insert(ImList<E> list, int i, E val) {
//        if (i == list.size()) { return list.concat(val); }
//
//        if ( (i > list.size()) || (i < 0) ) {
//            throw new IllegalArgumentException("Can't insert outside the possible bounds");
//        }
//
//        UnmodIterator<E> uli = list.iterator();
//        ImList<E> v = PersistentVector.empty();
//        for (int j = 0; j < i; j++) {
//            v = v.concat(uli.next());
//        }
//        v = v.concat(val);
//        for (int j = i; j < list.size(); j++) {
//            v = v.concat(uli.next());
//        }
//        return v;
//    }
//
//    /**
//     * Adds items to the end of the ImList.
//     * @param es the values to insert
//     * @return a new ImList with the additional items at the end.
//     */
//    static <E> ImList<E> concat(ImList<E> l, E e) {
//        return l.insert(l.size() - 1, e);
//    }
//
//    /**
//     * Adds items to the end of the ImList.
//     * @param es the values to insert
//     * @return a new ImList with the additional items at the end.
//     */
//    static <E> ImList<E> appendSkipNull(ImList<E> l, E e) {
//        if (e == null) { return l; }
//        return l.concat(e);
//    }
//
//    /**
//     * Inserts items at the beginning of the ImList.
//     * @param es the values to insert
//     * @return a new ImList beginning with the additional items.
//     */
//    static <E> ImList<E> precat(ImList<E> l, E e) {
//        return l.insert(0, e);
//    }
//
//    /**
//     * Inserts items at the beginning of the ImList.
//     * @param es the values to insert
//     * @return a new ImList beginning with the additional items.
//     */
//    static <E> ImList<E> prependSkipNull(ImList<E> l, E e) {
//        if (e == null) { return l; }
//        return l.insert(0, e);
//    }


}
