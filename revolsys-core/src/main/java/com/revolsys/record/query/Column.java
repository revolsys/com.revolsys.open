package com.revolsys.record.query;

import java.sql.PreparedStatement;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;

import com.revolsys.record.Record;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordStore;

public class Column implements QueryValue {

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
    if (this.fieldDefinition == null) {
      buffer.append(toString());
    } else {
      this.fieldDefinition.appendColumnName(buffer, null);
    }
  }

  @Override
  public int appendParameters(final int index, final PreparedStatement statement) {
    return index;
  }

  @Override
  public Column clone() {
    try {
      return (Column)super.clone();
    } catch (final CloneNotSupportedException e) {
      return null;
    }
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof Column) {
      final Column value = (Column)obj;
      return DataType.equal(value.getName(), this.getName());
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
      return DataTypes.toString(value);
    } else {
      return this.fieldDefinition.toString(value);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V> V getValue(final Record record) {
    if (record == null) {
      return null;
    } else {
      final String name = getName();
      return (V)record.getValue(name);
    }
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
