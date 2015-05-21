package com.revolsys.data.record;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.revolsys.data.identifier.Identifier;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.jts.geom.Geometry;

public class DelegatingRecord extends AbstractRecord {
  private final Record record;

  public DelegatingRecord(final Record record) {
    this.record = record;
  }

  @Override
  public DelegatingRecord clone() {
    return (DelegatingRecord)record.clone();
  }

  @Override
  public int compareTo(final Record other) {
    return record.compareTo(other);
  }

  @Override
  public void delete() {
    record.delete();
  }

  @Override
  public Set<Entry<String, Object>> entrySet() {
    return record.entrySet();
  }

  @Override
  public boolean equals(final Object o) {
    return record.equals(o);
  }

  @Override
  public Object get(final Object key) {
    return record.get(key);
  }

  @Override
  public Byte getByte(final CharSequence name) {
    return record.getByte(name);
  }

  @Override
  public Double getDouble(final CharSequence name) {
    return record.getDouble(name);
  }

  @Override
  public RecordFactory getFactory() {
    return record.getFactory();
  }

  @Override
  public String getFieldTitle(final String name) {
    return record.getFieldTitle(name);
  }

  @Override
  public Float getFloat(final CharSequence name) {
    return record.getFloat(name);
  }

  @Override
  public <T extends Geometry> T getGeometryValue() {
    return record.getGeometryValue();
  }

  @Override
  public Identifier getIdentifier() {
    return record.getIdentifier();
  }

  @Override
  public Identifier getIdentifier(final List<String> fieldNames) {
    return record.getIdentifier(fieldNames);
  }

  @Override
  public Identifier getIdentifier(final String... fieldNames) {
    return record.getIdentifier(fieldNames);
  }

  @Override
  public Integer getInteger(final CharSequence name) {
    return record.getInteger(name);
  }

  @Override
  public Long getLong(final CharSequence name) {
    return record.getLong(name);
  }

  public Record getRecord() {
    return record;
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return record.getRecordDefinition();
  }

  @Override
  public Short getShort(final CharSequence name) {
    return record.getShort(name);
  }

  @Override
  public RecordState getState() {
    return record.getState();
  }

  @Override
  public String getString(final CharSequence name) {
    return record.getString(name);
  }

  @Override
  public String getTypePath() {
    return record.getTypePath();
  }

  @Override
  public <T> T getValue(final CharSequence name) {
    return record.getValue(name);
  }

  @Override
  public <T extends Object> T getValue(final int index) {
    return record.getValue(index);
  }

  @Override
  public <T> T getValueByPath(final CharSequence path) {
    return record.getValueByPath(path);
  }

  @Override
  public Map<String, Object> getValueMap(
    final Collection<? extends CharSequence> fieldNames) {
    return record.getValueMap(fieldNames);
  }

  @Override
  public List<Object> getValues() {
    return record.getValues();
  }

  @Override
  public boolean hasField(final CharSequence name) {
    return record.hasField(name);
  }

  @Override
  public int hashCode() {
    return record.hashCode();
  }

  @Override
  public boolean hasValue(final CharSequence name) {
    return record.hasValue(name);
  }

  @Override
  public boolean hasValuesAny(final String... fieldNames) {
    return record.hasValuesAny(fieldNames);
  }

  @Override
  public boolean isModified() {
    return record.isModified();
  }

  @Override
  public boolean isValid(final int index) {
    return record.isValid(index);
  }

  @Override
  public boolean isValid(final String fieldName) {
    return record.isValid(fieldName);
  }

  @Override
  public Object put(final String key, final Object value) {
    return record.put(key, value);
  }

  @Override
  public void setGeometryValue(final Geometry geometry) {
    record.setGeometryValue(geometry);
  }

  @Override
  public void setIdentifier(final Identifier identifier) {
    record.setIdentifier(identifier);
  }

  @Override
  public void setIdValue(final Object id) {
    record.setIdValue(id);
  }

  @Override
  public void setState(final RecordState state) {
    record.setState(state);
  }

  @Override
  public boolean setValue(final CharSequence name, final Object value) {
    return record.setValue(name, value);
  }

  @Override
  public boolean setValue(final int index, final Object value) {
    return record.setValue(index, value);
  }

  @Override
  public boolean setValueByPath(final CharSequence path, final Object value) {
    return record.setValueByPath(path, value);
  }

  @Override
  public <T> T setValueByPath(final CharSequence attributePath,
    final Record source, final String sourceAttributePath) {
    return record.setValueByPath(attributePath, source, sourceAttributePath);
  }

  @Override
  public void setValues(final Map<String, ? extends Object> values) {
    record.setValues(values);
  }

  @Override
  public void setValues(final Map<String, Object> record,
    final Collection<String> attributesNames) {
    this.record.setValues(record, attributesNames);
  }

  @Override
  public void setValues(final Map<String, Object> record,
    final String... fieldNames) {
    this.record.setValues(record, fieldNames);
  }

  @Override
  public void setValues(final Record object) {
    record.setValues(object);
  }

  @Override
  public void setValuesByPath(final Map<String, ? extends Object> values) {
    record.setValuesByPath(values);
  }

  @Override
  public String toString() {
    return record.toString();
  }
}
