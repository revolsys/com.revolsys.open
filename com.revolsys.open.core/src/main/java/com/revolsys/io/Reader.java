/*
 * $URL: https://secure.revolsys.com/svn/open.revolsys.com/com.revolsys/trunk/com.revolsys.gis/com.revolsys.gis.core/src/main/java/com/revolsys/gis/data/io/Reader.java $
 * $Author: paul.austin@revolsys.com $
 * $Date: 2010-11-04 20:52:56 -0700 (Thu, 04 Nov 2010) $
 * $Revision: 2602 $

 * Copyright 2004-2007 Revolution Systems Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.io;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.revolsys.collection.Visitor;
import com.revolsys.properties.ObjectWithProperties;

/**
 * <p>
 * The Reader interface defines methods for reading objects of type T. Objects
 * can either by read as a {@link List} or using an {@link Iterator} or visited
 * using a {@link Visitor}.
 * </p>
 * <p>
 * The simplest and most effecient way to loop through all objects in the reader
 * is to use the following loop.
 * </p>
 *
 * <pre>
 * Reader&lt;T&gt; reader = ...
 * for (T object : reader) {
 *   // Do something with the object.
 * }
 * </pre>
 *
 * @author Paul Austin
 * @param <T> The type of the item to read.
 */
public interface Reader<T> extends Iterable<T>, ObjectWithProperties, AutoCloseable {
  Reader<?> EMPTY = new ListReader<>();

  @SuppressWarnings("unchecked")
  static <V> Reader<V> empty() {
    return (Reader<V>)EMPTY;
  }

  /**
   * Close the reader and all resources associated with it.
   */
  @Override
  default void close() {
  }

  /**
   * Return a new iterator for type T at the first item to read. Subsequent
   * calls to this method must return a new iterator. Implementors of this are
   * responsible for cleaning up the iterator when the hasNext or next method on
   * iterator reaches the end of the items to read.
   *
   * @return The iterator.
   */
  @Override
  Iterator<T> iterator();

  /**
   * Open the reader so that it is ready to be read from.
   */
  default void open() {
  }

  /**
   * Read all items and return a List containing the items.
   *
   * @return The list of items.
   */
  default List<T> read() {
    final List<T> items = new ArrayList<T>();
    if (iterator() != null) {
      for (final T item : this) {
        items.add(item);
      }
    }
    return items;
  }

  /**
   * Visit each item returned from the reader until all items have been visited
   * or the visit method returns false.
   *
   * @param visitor The visitor.
   */
  default void visit(final Visitor<T> visitor) {
    if (iterator() != null) {
      for (final T item : this) {
        if (!visitor.visit(item)) {
          return;
        }
      }
    }
  }
}
