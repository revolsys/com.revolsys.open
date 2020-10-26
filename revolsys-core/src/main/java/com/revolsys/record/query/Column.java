package com.revolsys.record.query;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;

import com.revolsys.record.Record;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordStore;

public class Column implements QueryValue, ColumnReference {

  private FieldDefinition fieldDefinition;

  private final String name;

  private TableReference table;

  public Column(final CharSequence name) {
    this(name.toString());
  }

  public Column(final String name) {
    this.name = name;
  }

  public Column(final TableReference tableReference, final CharSequence name) {
    this.table = tableReference;
    this.name = name.toString();
  }

  @Override
  public void appendDefaultSql(final Query query, final RecordStore recordStore,
    final StringBuilder sql) {
    if (this.fieldDefinition == null) {
      sql.append(toString());
    } else {
      this.fieldDefinition.appendColumnName(sql, null);
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
      final ColumnReference value = (ColumnReference)obj;
      return DataType.equal(value.getName(), this.getName());
    } else {
      return false;
    }
  }

  @Override
  public FieldDefinition getFieldDefinition() {
    return this.fieldDefinition;
  }

  @Override
  public int getFieldIndex() {
    if (this.fieldDefinition == null) {
      return -1;
    } else {
      return this.fieldDefinition.getIndex();
    }
  }

  @Override
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
  public TableReference getTable() {
    return this.table;
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
  public Object getValueFromResultSet(final ResultSet resultSet, final ColumnIndexes indexes,
    final boolean internStrings) throws SQLException {
    if (this.fieldDefinition == null) {
      return null;
    } else {
      return this.fieldDefinition.getValueFromResultSet(resultSet, indexes, internStrings);
    }
  }

  @Override
  public void setRecordDefinition(final RecordDefinition recordDefinition) {
    final String getName = getName();
    this.fieldDefinition = recordDefinition.getField(getName);
  }

  @Override
  public <V> V toColumnTypeException(final Object value) {
    if (value == null) {
      return null;
    } else {
      if (this.fieldDefinition == null) {
        return (V)value;
      } else {
        return this.fieldDefinition.toColumnTypeException(value);
      }
    }
  }

  @Override
  public <V> V toFieldValueException(final Object value) {
    if (value == null) {
      return null;
    } else {
      if (this.fieldDefinition == null) {
        return (V)value;
      } else {
        return this.fieldDefinition.toFieldValueException(value);
      }
    }
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    if (this.table != null) {
      this.table.appendColumnPrefix(sb);
    }
    final String name = this.name;
    if ("*".equals(name) || name.indexOf('"') != -1 || name.indexOf('.') != -1
      || name.matches("([A-Z][_A-Z1-9]*\\.)?[A-Z][_A-Z1-9]*\\*")) {
      sb.append(name);
    } else {
      sb.append('"');
      sb.append(name);
      sb.append('"');
    }
    return sb.toString();
  }

  @Override
  public String toString(final Object value) {
    if (this.fieldDefinition == null) {
      return DataTypes.toString(value);
    } else {
      return this.fieldDefinition.toString(value);
    }
  }
}
