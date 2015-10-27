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
package com.revolsys.record.schema;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.revolsys.datatype.DataType;
import com.revolsys.geometry.model.GeometryFactoryProxy;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.code.CodeTable;

public interface RecordDefinition
  extends GeometryFactoryProxy, RecordStoreSchemaElement, MapSerializer {
  void addDefaultValue(String fieldName, Object defaultValue);

  Record createRecord();

  void delete(Record record);

  void destroy();

  CodeTable getCodeTableByFieldName(String fieldName);

  Object getDefaultValue(String fieldName);

  Map<String, Object> getDefaultValues();

  FieldDefinition getField(CharSequence name);

  FieldDefinition getField(int index);

  Class<?> getFieldClass(CharSequence name);

  Class<?> getFieldClass(int index);

  /**
   * Get the number of fields supported by the type.
   *
   * @return The number of fields.
   */
  int getFieldCount();

  /**
   * Get the index of the named field within the list of fields for the
   * type.
   *
   * @param name The field name.
   * @return The index.
   */
  int getFieldIndex(CharSequence name);

  /**
   * Get the maximum length of the field.
   *
   * @param index The field index.
   * @return The maximum length.
   */
  int getFieldLength(int index);

  /**
   * Get the name of the field at the specified index.
   *
   * @param index The field index.
   * @return The field name.
   */
  String getFieldName(int index);

  /**
   * Get the names of all the fields supported by the type.
   *
   * @return The field names.
   */
  List<String> getFieldNames();

  Set<String> getFieldNamesSet();

  List<FieldDefinition> getFields();

  /**
   * Get the maximum number of decimal places of the field
   *
   * @param index The field index.
   * @return The maximum number of decimal places.
   */
  int getFieldScale(int index);

  String getFieldTitle(String fieldName);

  List<String> getFieldTitles();

  DataType getFieldType(CharSequence name);

  /**
   * Get the type name of the field at the specified index.
   *
   * @param index The field index.
   * @return The field type name.
   */
  DataType getFieldType(int index);

  FieldDefinition getGeometryField();

  /**
   * Get the index of the primary Geometry field.
   *
   * @return The primary geometry index.
   */
  int getGeometryFieldIndex();

  /**
   * Get the index of all Geometry fields.
   *
   * @return The geometry indexes.
   */
  List<Integer> getGeometryFieldIndexes();

  /**
   * Get the name of the primary Geometry field.
   *
   * @return The primary geometry name.
   */
  String getGeometryFieldName();

  /**
   * Get the name of the all Geometry fields.
   *
   * @return The geometry names.
   */
  List<String> getGeometryFieldNames();

  FieldDefinition getIdField();

  /**
   * Get the index of the Unique identifier field.
   *
   * @return The unique id index.
   */
  int getIdFieldIndex();

  /**
   * Get the index of all ID fields.
   *
   * @return The ID indexes.
   */
  List<Integer> getIdFieldIndexes();

  /**
   * Get the name of the Unique identifier field.
   *
   * @return The unique id name.
   */
  String getIdFieldName();

  /**
   * Get the name of the all ID fields.
   *
   * @return The id names.
   */
  List<String> getIdFieldNames();

  List<FieldDefinition> getIdFields();

  int getInstanceId();

  RecordDefinitionFactory getRecordDefinitionFactory();

  RecordFactory getRecordFactory();

  /**
   * Check to see if the type has the specified field name.
   *
   * @param name The name of the field.
   * @return True id the type has the field, false otherwise.
   */
  boolean hasField(CharSequence name);

  boolean hasGeometryField();

  boolean isFieldRequired(CharSequence name);

  /**
   * Return true if a value for the field is required.
   *
   * @param index The field index.
   * @return True if the field is required, false otherwise.
   */
  boolean isFieldRequired(int index);

  boolean isInstanceOf(RecordDefinition classDefinition);

  void setDefaultValues(Map<String, ? extends Object> defaultValues);

  void setGeometryFactory(com.revolsys.geometry.model.GeometryFactory geometryFactory);
}
