package com.revolsys.data.record;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.revolsys.data.identifier.Identifiable;
import com.revolsys.data.identifier.Identifier;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.jts.geom.Geometry;

public interface Record extends Map<String, Object>, Comparable<Record>, Identifiable {
  /**
   * Create a clone of the data object.
   *
   * @return The data object.
   */
  Record clone();

  void delete();

  Byte getByte(final CharSequence name);

  Double getDouble(final CharSequence name);

  /**
   * Get the factory which created the instance.
   *
   * @return The factory.
   */
  RecordFactory getFactory();

  String getFieldTitle(String fieldName);

  Float getFloat(final CharSequence name);

  /**
   * Get the value of the primary geometry attribute.
   *
   * @return The primary geometry.
   */
  <T extends Geometry> T getGeometryValue();

  Identifier getIdentifier(List<String> fieldNames);

  Identifier getIdentifier(String... fieldNames);

  /**
   * Get the value of the unique identifier attribute.
   *
   * @return The unique identifier.
   */
  <T extends Object> T getIdValue();

  Integer getInteger(CharSequence name);

  Long getLong(final CharSequence name);

  /**
   * Get the meta data describing the Record and it's attributes.
   *
   * @return The meta data.
   */
  RecordDefinition getRecordDefinition();

  Short getShort(final CharSequence name);

  RecordState getState();

  String getString(final CharSequence name);

  String getTypePath();

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

  Map<String, Object> getValueMap(final Collection<? extends CharSequence> fieldNames);

  /**
   * Get the values of all attributes.
   *
   * @return The attribute value.
   */
  List<Object> getValues();

  /**
   * Checks to see if the definition for this Record has an attribute with the
   * specified name.
   *
   * @param name The name of the attribute.
   * @return True if the Record has an attribute with the specified name.
   */
  boolean hasField(CharSequence name);

  boolean hasValue(CharSequence name);

  /**
   * Check if any of the fields have a value.
   * @param fieldNames
   * @return True if any of the fields have a value, false otherwise.
   */
  boolean hasValuesAny(String... fieldNames);

  boolean isModified();

  boolean isValid(int index);

  boolean isValid(String fieldName);

  /**
   * Set the value of the primary geometry attribute.
   *
   * @param geometry The primary geometry.
   */
  void setGeometryValue(Geometry geometry);

  void setIdentifier(Identifier identifier);

  /**
   * Set the value of the unique identifier attribute.
   *
   * @param id The unique identifier.
   */
  void setIdValue(Object id);

  void setState(final RecordState state);

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

  void setValueByPath(CharSequence attributePath, Object value);

  <T> T setValueByPath(CharSequence attributePath, Record source, String sourceAttributePath);

  void setValues(Map<String, ? extends Object> values);

  void setValues(Map<String, Object> record, Collection<String> fieldNames);

  void setValues(Map<String, Object> record, String... fieldNames);

  void setValues(final Record object);

  /**
   * Set the values on the object based on the values in the map.
   *
   * @param values The values to set.
   */
  void setValuesByPath(Map<String, ? extends Object> values);

}
