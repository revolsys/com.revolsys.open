/*
 * $URL$
 * $Author$
 * $Date$
 * $Revision$

 * Copyright 2004-2005 Revolution Systems Inc.
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

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.io.ObjectWithProperties;

public interface DataObjectMetaData extends ObjectWithProperties {

  /** The namespaceUri for standard properties. */
  String NS_URI = "http://revolsys.com/gis";

  void addDefaultValue(
    String attributeName,
    Object defaultValue);

  DataObject createDataObject();

  Attribute getAttribute(
    CharSequence name);

  Attribute getAttribute(
    int index);

  /**
   * Get the number of attributes supported by the type.
   * 
   * @return The number of attributes.
   */
  int getAttributeCount();

  /**
   * Get the index of the named attribute within the list of attributes for the
   * type.
   * 
   * @param name The attribute name.
   * @return The index.
   */
  int getAttributeIndex(
    CharSequence name);

  /**
   * Get the maximum length of the attribute.
   * 
   * @param index The attribute index.
   * @return The maximum length.
   */
  int getAttributeLength(
    int index);

  /**
   * Get the name of the attribute at the specified index.
   * 
   * @param index The attribute index.
   * @return The attribute name.
   */
  String getAttributeName(
    int index);

  /**
   * Get the names of all the attributes supported by the type.
   * 
   * @return The attribute names.
   */
  List<String> getAttributeNames();

  List<Attribute> getAttributes();

  /**
   * Get the maximum number of decimal places of the attribute
   * 
   * @param index The attribute index.
   * @return The maximum number of decimal places.
   */
  int getAttributeScale(
    int index);

  /**
   * Get the type name of the attribute at the specified index.
   * 
   * @param index The attribute index.
   * @return The attribute type name.
   */
  DataType getAttributeType(
    int index);

  DataType getAttributeType(
    CharSequence name);

  DataObjectFactory getDataObjectFactory();

  DataObjectMetaDataFactory getDataObjectMetaDataFactory();

  DataObjectStore getDataObjectStore();

  Object getDefaultValue(
    String attributeName);

  Map<String, Object> getDefaultValues();

  Attribute getGeometryAttribute();

  /**
   * Get the index of the primary Geometry attribute.
   * 
   * @return The primary geometry index.
   */
  int getGeometryAttributeIndex();

  /**
   * Get the index of all Geometry attributes.
   * 
   * @return The geometry indexes.
   */
  List<Integer> getGeometryAttributeIndexes();

  /**
   * Get the name of the primary Geometry attribute.
   * 
   * @return The primary geometry name.
   */
  String getGeometryAttributeName();

  /**
   * Get the name of the all Geometry attributes.
   * 
   * @return The geometry names.
   */
  List<String> getGeometryAttributeNames();

  Attribute getIdAttribute();

  /**
   * Get the index of the Unique identifier attribute.
   * 
   * @return The unique id index.
   */
  int getIdAttributeIndex();

  /**
   * Get the name of the Unique identifier attribute.
   * 
   * @return The unique id name.
   */
  String getIdAttributeName();

  /**
   * Get the name of the object type. Names are described using a {@link QName}
   * consisting of a namespaceUri and a name.
   * 
   * @return The name.
   */
  QName getName();

  /**
   * Check to see if the type has the specified attribute name.
   * 
   * @param name The name of the attribute.
   * @return True id the type has the attribute, false otherwise.
   */
  boolean hasAttribute(
    CharSequence name);

  /**
   * Return true if a value for the attribute is required.
   * 
   * @param index The attribute index.
   * @return True if the attribute is required, false otherwise.
   */
  boolean isAttributeRequired(
    int index);

  boolean isInstanceOf(
    DataObjectMetaData classDefinition);

  /**
   * Set the name of the object type. Names are described using a {@link QName}
   * consisting of a namespaceUri and a name.
   * 
   * @param name The name.
   */
  void setName(
    QName name);
}
