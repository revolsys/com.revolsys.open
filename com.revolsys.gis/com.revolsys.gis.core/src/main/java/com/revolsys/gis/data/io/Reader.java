/*
 * $URL$
 * $Author$
 * $Date$
 * $Revision$

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
package com.revolsys.gis.data.io;

import java.util.Iterator;
import java.util.List;

import com.revolsys.gis.data.visitor.Visitor;
import com.revolsys.io.ObjectWithProperties;

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
public interface Reader<T> extends Iterable<T>, ObjectWithProperties{
  /**
   * Close the reader and all resources associated with it.
   */
  void close();

  /**
   * Open the reader so that it is ready to be read from.
   */
  void open();
  
  /**
   * Return a new iterator for type T at the first item to read. Subsequent
   * calls to this method must return a new iterator. Implementors of this are
   * responsible for cleaning up the iterator when the hasNext or next method on
   * iterator reaches the end of the items to read.
   * 
   * @return The iterator.
   */
  Iterator<T> iterator();

  /**
   * Read all items and return a List containing the items.
   * 
   * @return The list of items.
   */
  List<T> read();

  /**
   * Visit each item returned from the reader until all items have been visited
   * or the visit method returns false.
   * 
   * @param visitor The visitor.
   */
  void visit(
    Visitor<T> visitor);
}
