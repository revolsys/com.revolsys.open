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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.visitor.Visitor;

/**
 * The AbstracteReader is an implementation of the {@link Reader} interface,
 * which provides implementatons of {@link #read()} and {@link #visit(Visitor)}
 * which use the {@link Reader#iterator()} method which must be implemented by
 * subclasses.
 * 
 * @author Paul Austin
 * @param <T> The type of object being read.
 */
public abstract class AbstractReader<T> implements Reader<T> {
  private Map<QName,Object> properties = new HashMap<QName, Object>();
  /**
   * Get properties about the reader.
   * 
   * @return The properties.
   */
  public  Map<QName, Object> getProperties() {
    return properties;
  }

  /**
   * Get a property about the reader.
   * 
   * @param name The name of the property to get.
   * @return The property.
   */
  @SuppressWarnings("unchecked")
  public <C> C getProperty(
    final QName name) {
    return (C)getProperties().get(name);
  }

  public void setProperty(
    QName name,
    Object value) {
    getProperties().put(name, value);
  }
  /**
   * Read all items and return a List containing the items.
   * 
   * @return The list of items.
   */
  public List<T> read() {
    final List<T> items = new ArrayList<T>();
    for (final T item : this) {
      items.add(item);
    }
    return items;
  }

  /**
   * Visit each item returned from the reader until all items have been visited
   * or the visit method returns false.
   * 
   * @param visitor The visitor.
   */
  public void visit(
    final Visitor<T> visitor) {
    for (final T item : this) {
      if (!visitor.visit(item)) {
        return;
      }
    }
  }
}
