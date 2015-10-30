package com.revolsys.record.query;

import java.sql.PreparedStatement;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.equals.Equals;
import com.revolsys.record.Record;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordStore;

public class Column extends QueryValue {

  private FieldDefinition fieldDefinition;

  private final String name;

  public Column(final FieldDefinition fieldDefinition) {
    this.name = fieldDefinition.getName();
    this.fieldDefinition = fieldDefinition;
  }

  public Column(final String name) {
    this.name = name;
  }

  @Override
  public void appendDefaultSql(final Query query, final RecordStore recordStore,
    final StringBuilder buffer) {
    buffer.append(toString());
  }

  @Override
  public int appendParameters(final int index, final PreparedStatement statement) {
    return index;
  }

  @Override
  public Column clone() {
    return new Column(this.name);
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof Column) {
      final Column value = (Column)obj;
      return Equals.equal(value.getName(), this.getName());
    } else {
      return false;
    }
  }

  public FieldDefinition getFieldDefinition() {
    return this.fieldDefinition;
  }

  public String getName() {
    return this.name;
  }

  @Override
  public String getStringValue(final Record record) {
    final Object value = getValue(record);
    if (this.fieldDefinition == null) {
      return StringConverterRegistry.toString(value);
    } else {
      final Class<?> typeClass = this.fieldDefinition.getTypeClass();
      return StringConverterRegistry.toString(typeClass, value);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V> V getValue(final Record record) {
    final String name = getName();
    return (V)record.getValue(name);
  }

  @Override
  public void setRecordDefinition(final RecordDefinition recordDefinition) {
    this.fieldDefinition = recordDefinition.getField(getName());
  }

  @Override
  public String toString() {
    if (this.name.matches("([A-Z][_A-Z1-9]*\\.)?[A-Z][_A-Z1-9]*")) {
      return this.name;
    } else {
      return "\"" + this.name + "\"";
    }
  }
}
