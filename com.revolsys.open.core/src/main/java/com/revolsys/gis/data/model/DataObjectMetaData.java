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

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.io.ObjectWithProperties;

public interface DataObjectMetaData extends ObjectWithProperties,
  Comparable<DataObjectMetaData> {
  void addDefaultValue(String attributeName, Object defaultValue);

  DataObjectMetaData cloneCoordinates();

  DataObject createDataObject();

  void delete(DataObject dataObject);

  void destroy();

  Attribute getAttribute(CharSequence name);

  Attribute getAttribute(int index);

  Class<?> getAttributeClass(CharSequence name);

  Class<?> getAttributeClass(int index);

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
  int getAttributeIndex(CharSequence name);

  /**
   * Get the maximum length of the attribute.
   * 
   * @param index The attribute index.
   * @return The maximum length.
   */
  int getAttributeLength(int index);

  /**
   * Get the name of the attribute at the specified index.
   * 
   * @param index The attribute index.
   * @return The attribute name.
   */
  String getAttributeName(int index);

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
  int getAttributeScale(int index);

  DataType getAttributeType(CharSequence name);

  /**
   * Get the type name of the attribute at the specified index.
   * 
   * @param index The attribute index.
   * @return The attribute type name.
   */
  DataType getAttributeType(int index);

  CodeTable getCodeTableByColumn(String column);

  DataObjectFactory getDataObjectFactory();

  DataObjectMetaDataFactory getDataObjectMetaDataFactory();

  DataObjectStore getDataObjectStore();

  Object getDefaultValue(String attributeName);

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

  GeometryFactory getGeometryFactory();

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

  int getInstanceId();

  /**
   * Get the name of the object type. Names are described using a path (e.g.
   * /SCHEMA/TABLE).
   * 
   * @return The name.
   */
  String getPath();

  String getTypeName();

  /**
   * Check to see if the type has the specified attribute name.
   * 
   * @param name The name of the attribute.
   * @return True id the type has the attribute, false otherwise.
   */
  boolean hasAttribute(CharSequence name);

  /**
   * Return true if a value for the attribute is required.
   * 
   * @param index The attribute index.
   * @return True if the attribute is required, false otherwise.
   */
  boolean isAttributeRequired(int index);

  boolean isInstanceOf(DataObjectMetaData classDefinition);

  void setGeometryFactory(GeometryFactory geometryFactory);

  /**
   * Set the name of the object type. Names are described using a (e.g.
   * /SCHEMA/TABLE).
   * 
   * @param name The name.
   */
  void setName(String name);
}
