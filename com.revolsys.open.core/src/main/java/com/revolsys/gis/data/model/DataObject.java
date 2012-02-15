/*
 * $URL: https://secure.revolsys.com/svn/open.revolsys.com/rs-gis-core/trunk/src/main/java/com/revolsys/gis/data/model/DataObject.java $
 * $Author: paul.austin@revolsys.com $
 * $Date: 2008-12-24 09:59:17 -0800 (Wed, 24 Dec 2008) $
 * $Revision: 1451 $

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
package com.revolsys.gis.data.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import com.vividsolutions.jts.geom.Geometry;

public interface DataObject extends Map<String, Object> {
  /**
   * Create a clone of the data object.
   * 
   * @return The data object.
   */
  DataObject clone();

  void delete();

  /**
   * Get the factory which created the instance.
   * 
   * @return The factory.
   */
  DataObjectFactory getFactory();

  /**
   * Get the value of the primary geometry attribute.
   * 
   * @return The primary geometry.
   */
  <T extends Geometry> T getGeometryValue();

  /**
   * Get the value of the unique identifier attribute.
   * 
   * @return The unique identifier.
   */
  <T extends Object> T getIdValue();

  /**
   * Get the meta data describing the DataObject and it's attributes.
   * 
   * @return The meta data.
   */
  DataObjectMetaData getMetaData();

  DataObjectState getState();

  QName getTypeName();

  /**
   * Get the value of the attribute with the specified name.
   * 
   * @param name The name of the attribute.
   * @return The attribute value.
   */
  <T extends Object> T getValue(CharSequence name);

  /**
   * Get the value of the attribute with the specified index.
   * 
   * @param index The index of the attribute.
   * @return The attribute value.
   */
  <T extends Object> T getValue(int index);

  <T> T getValueByPath(CharSequence attributePath);

  Map<String, Object> getValueMap(
    final Collection<? extends CharSequence> attributeNames);

  /**
   * Get the values of all attributes.
   * 
   * @return The attribute value.
   */
  List<Object> getValues();

  /**
   * Checks to see if the metadata for this DataObject has an attribute with the
   * specified name.
   * 
   * @param name The name of the attribute.
   * @return True if the DataObject has an attribute with the specified name.
   */
  boolean hasAttribute(CharSequence name);

  /**
   * Set the value of the primary geometry attribute.
   * 
   * @param geometry The primary geometry.
   */
  void setGeometryValue(Geometry geometry);

  /**
   * Set the value of the unique identifier attribute.
   * 
   * @param id The unique identifier.
   */
  void setIdValue(Object id);

  void setState(final DataObjectState state);

  /**
   * Set the value of the attribute with the specified name.
   * 
   * @param name The name of the attribute. param value The attribute value.
   * @param value The new value;
   */
  void setValue(CharSequence name, Object value);

  /**
   * Set the value of the attribute with the specified name.
   * 
   * @param index The index of the attribute. param value The attribute value.
   * @param value The new value;
   */
  void setValue(int index, Object value);

  <T> T setValueByPath(
    CharSequence attributePath,
    DataObject source,
    String sourceAttributePath);

  void setValueByPath(CharSequence attributePath, Object value);

  void setValues(final DataObject object);

  /**
   * Set the values on the object based on the values in the map.
   * 
   * @param values The values to set.
   */
  void setValues(Map<String, ? extends Object> values);
}
