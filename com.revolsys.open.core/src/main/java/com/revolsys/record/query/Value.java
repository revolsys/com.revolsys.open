package com.revolsys.record.query;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.identifier.TypedIdentifier;
import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.date.Dates;

import com.revolsys.jdbc.field.JdbcFieldDefinition;
import com.revolsys.jdbc.field.JdbcFieldDefinitions;
import com.revolsys.record.Record;
import com.revolsys.record.code.CodeTable;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.util.Property;
import com.revolsys.util.Strings;

public class Value implements QueryValue {
  public static Object getValue(final Object value) {
    if (value instanceof TypedIdentifier) {
      final Identifier identifier = (Identifier)value;
      return identifier;
    } else if (value instanceof Identifier) {
      final Identifier identifier = (Identifier)value;
      return identifier.toSingleValue();
    } else {
      return value;
    }
  }

  private FieldDefinition fieldDefinition;

  private Object displayValue;

  private JdbcFieldDefinition jdbcField;

  private Object queryValue;

  public Value(final FieldDefinition field, final Object value) {
    this.fieldDefinition = field;
    setQueryValue(value);
    this.displayValue = this.queryValue;
    setFieldDefinition(field);
  }

  public Value(final Object value) {
    this(JdbcFieldDefinitions.newFieldDefinition(value), value);
  }

  @Override
  public void appendDefaultSql(final Query query, final RecordStore recordStore,
    final StringBuilder buffer) {
    if (this.jdbcField == null) {
      buffer.append('?');
    } else {
      this.jdbcField.addSelectStatementPlaceHolder(buffer);
    }
  }

  @Override
  public int appendParameters(final int index, final PreparedStatement statement) {
    try {
      try {
        return this.jdbcField.setPreparedStatementValue(statement, index, this.queryValue);
      } catch (final IllegalArgumentException e) {
        return this.jdbcField.setPreparedStatementValue(statement, index, null);
      }
    } catch (final SQLException e) {
      throw new RuntimeException("Unable to set value: " + this.queryValue, e);
    }
  }

  @Override
  public Value clone() {
    try {
      return (Value)super.clone();
    } catch (final CloneNotSupportedException e) {
      return null;
    }
  }

  public void convert(final DataType dataType) {
    if (this.queryValue != null) {
      final Object value = this.queryValue;
      final Object newValue = dataType.toObject(value);
      final Class<?> typeClass = dataType.getJavaClass();
      if (newValue == null || !typeClass.isAssignableFrom(newValue.getClass())) {
        throw new IllegalArgumentException(
          "'" + this.queryValue + "' is not a valid " + dataType.getValidationName());
      } else {
        setQueryValue(newValue);
      }
    }
  }

  public void convert(final FieldDefinition field) {
    if (field instanceof JdbcFieldDefinition) {
      this.jdbcField = (JdbcFieldDefinition)field;
    }
    convert(field.getDataType());
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof Value) {
      final Value value = (Value)obj;
      return DataType.equal(value.getValue(), this.getValue());
    } else {
      return false;
    }
  }

  public Object getDisplayValue() {
    return this.displayValue;
  }

  public JdbcFieldDefinition getJdbcField() {
    return this.jdbcField;
  }

  public Object getQueryValue() {
    return this.queryValue;
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

  public Object getValue() {
    return this.queryValue;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getValue(final Record record) {
    return (V)this.queryValue;
  }

  @Override
  public void setFieldDefinition(final FieldDefinition field) {
    if (field != null) {
      this.fieldDefinition = field;
      if (field instanceof JdbcFieldDefinition) {
        this.jdbcField = (JdbcFieldDefinition)field;
      } else {
        this.jdbcField = JdbcFieldDefinitions.newFieldDefinition(this.queryValue);
      }

      CodeTable codeTable = null;
      if (field != null) {
        final RecordDefinition recordDefinition = field.getRecordDefinition();
        if (recordDefinition != null) {
          final String fieldName = field.getName();
          codeTable = recordDefinition.getCodeTableByFieldName(fieldName);
          if (codeTable instanceof RecordDefinitionProxy) {
            final RecordDefinitionProxy proxy = (RecordDefinitionProxy)codeTable;
            if (proxy.getRecordDefinition() == recordDefinition) {
              codeTable = null;
            }
          }
          if (codeTable != null) {
            final Identifier id = codeTable.getIdentifier(this.queryValue);
            if (id == null) {
              this.displayValue = this.queryValue;
            } else {
              setQueryValue(id);
              final List<Object> values = codeTable.getValues(id);
              if (values.size() == 1) {
                this.displayValue = values.get(0);
              } else {
                this.displayValue = Strings.toString(":", values);
              }
            }
          }
        }
      }
    }
  }

  protected void setQueryValue(final Object value) {
    this.queryValue = getValue(value);
  }

  @Override
  public void setRecordDefinition(final RecordDefinition recordDefinition) {
    final String fieldName = this.fieldDefinition.getName();
    if (Property.hasValue(fieldName)) {
      final FieldDefinition field = recordDefinition.getField(fieldName);
      setFieldDefinition(field);
    }
  }

  public void setValue(Object value) {
    value = getValue(value);
    if (this.fieldDefinition.getName() == JdbcFieldDefinitions.UNKNOWN) {
      this.fieldDefinition = JdbcFieldDefinitions.newFieldDefinition(value);
    }
    setQueryValue(value);
  }

  @Override
  public String toFormattedString() {
    return toString();
  }

  @Override
  public String toString() {
    if (this.displayValue instanceof Number) {
      final Object value = this.displayValue;
      return DataTypes.toString(value);
    } else if (this.displayValue instanceof Date) {
      final Date date = (Date)this.displayValue;
      final String stringValue = Dates.format("yyyy-MM-dd", date);
      return "{d '" + stringValue + "'}";
    } else if (this.displayValue instanceof Time) {
      final Time time = (Time)this.displayValue;
      final String stringValue = Dates.format("HH:mm:ss", time);
      return "{t '" + stringValue + "'}";
    } else if (this.displayValue instanceof Timestamp) {
      final Timestamp time = (Timestamp)this.displayValue;
      final String stringValue = Dates.format("yyyy-MM-dd HH:mm:ss.S", time);
      return "{ts '" + stringValue + "'}";
    } else if (this.displayValue instanceof java.util.Date) {
      final java.util.Date time = (java.util.Date)this.displayValue;
      final String stringValue = Dates.format("yyyy-MM-dd HH:mm:ss.S", time);
      return "{ts '" + stringValue + "'}";
    } else {
      final Object value = this.displayValue;
      final String string = DataTypes.toString(value);
      return "'" + string.replaceAll("'", "''") + "'";
    }
  }
}
