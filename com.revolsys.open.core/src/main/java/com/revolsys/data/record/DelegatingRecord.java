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
    return (DelegatingRecord)this.record.clone();
  }

  @Override
  public int compareTo(final Record other) {
    return this.record.compareTo(other);
  }

  @Override
  public void delete() {
    this.record.delete();
  }

  @Override
  public Set<Entry<String, Object>> entrySet() {
    return this.record.entrySet();
  }

  @Override
  public boolean equals(final Object o) {
    return this.record.equals(o);
  }

  @Override
  public Object get(final Object key) {
    return this.record.get(key);
  }

  @Override
  public Byte getByte(final CharSequence name) {
    return this.record.getByte(name);
  }

  @Override
  public Double getDouble(final CharSequence name) {
    return this.record.getDouble(name);
  }

  @Override
  public RecordFactory getFactory() {
    return this.record.getFactory();
  }

  @Override
  public String getFieldTitle(final String name) {
    return this.record.getFieldTitle(name);
  }

  @Override
  public Float getFloat(final CharSequence name) {
    return this.record.getFloat(name);
  }

  @Override
  public <T extends Geometry> T getGeometryValue() {
    return this.record.getGeometryValue();
  }

  @Override
  public Identifier getIdentifier() {
    return this.record.getIdentifier();
  }

  @Override
  public Identifier getIdentifier(final List<String> fieldNames) {
    return this.record.getIdentifier(fieldNames);
  }

  @Override
  public Identifier getIdentifier(final String... fieldNames) {
    return this.record.getIdentifier(fieldNames);
  }

  @Override
  public Integer getInteger(final CharSequence name) {
    return this.record.getInteger(name);
  }

  @Override
  public Long getLong(final CharSequence name) {
    return this.record.getLong(name);
  }

  public Record getRecord() {
    return this.record;
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return this.record.getRecordDefinition();
  }

  @Override
  public Short getShort(final CharSequence name) {
    return this.record.getShort(name);
  }

  @Override
  public RecordState getState() {
    return this.record.getState();
  }

  @Override
  public String getString(final CharSequence name) {
    return this.record.getString(name);
  }

  @Override
  public String getTypePath() {
    return this.record.getTypePath();
  }

  @Override
  public <T> T getValue(final CharSequence name) {
    return this.record.getValue(name);
  }

  @Override
  public <T extends Object> T getValue(final int index) {
    return this.record.getValue(index);
  }

  @Override
  public <T> T getValueByPath(final CharSequence path) {
    return this.record.getValueByPath(path);
  }

  @Override
  public Map<String, Object> getValueMap(final Collection<? extends CharSequence> fieldNames) {
    return this.record.getValueMap(fieldNames);
  }

  @Override
  public List<Object> getValues() {
    return this.record.getValues();
  }

  @Override
  public boolean hasField(final CharSequence name) {
    return this.record.hasField(name);
  }

  @Override
  public int hashCode() {
    return this.record.hashCode();
  }

  @Override
  public boolean hasValue(final CharSequence name) {
    return this.record.hasValue(name);
  }

  @Override
  public boolean hasValuesAny(final String... fieldNames) {
    return this.record.hasValuesAny(fieldNames);
  }

  @Override
  public boolean isModified() {
    return this.record.isModified();
  }

  @Override
  public boolean isValid(final int index) {
    return this.record.isValid(index);
  }

  @Override
  public boolean isValid(final String fieldName) {
    return this.record.isValid(fieldName);
  }

  @Override
  public Object put(final String key, final Object value) {
    return this.record.put(key, value);
  }

  @Override
  public void setGeometryValue(final Geometry geometry) {
    this.record.setGeometryValue(geometry);
  }

  @Override
  public void setIdentifier(final Identifier identifier) {
    this.record.setIdentifier(identifier);
  }

  @Override
  public void setIdValue(final Object id) {
    this.record.setIdValue(id);
  }

  @Override
  public void setState(final RecordState state) {
    this.record.setState(state);
  }

  @Override
  public void setValue(final CharSequence name, final Object value) {
    this.record.setValue(name, value);
  }

  @Override
  public void setValue(final int index, final Object value) {
    this.record.setValue(index, value);
  }

  @Override
  public void setValueByPath(final CharSequence path, final Object value) {
    this.record.setValueByPath(path, value);
  }

  @Override
  public <T> T setValueByPath(final CharSequence attributePath, final Record source,
    final String sourceAttributePath) {
    return this.record.setValueByPath(attributePath, source, sourceAttributePath);
  }

  @Override
  public void setValues(final Map<String, ? extends Object> values) {
    this.record.setValues(values);
  }

  @Override
  public void setValues(final Map<String, Object> record, final Collection<String> attributesNames) {
    this.record.setValues(record, attributesNames);
  }

  @Override
  public void setValues(final Map<String, Object> record, final String... fieldNames) {
    this.record.setValues(record, fieldNames);
  }

  @Override
  public void setValues(final Record object) {
    this.record.setValues(object);
  }

  @Override
  public void setValuesByPath(final Map<String, ? extends Object> values) {
    this.record.setValuesByPath(values);
  }

  @Override
  public String toString() {
    return this.record.toString();
  }
}
