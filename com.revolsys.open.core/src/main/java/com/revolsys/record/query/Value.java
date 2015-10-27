package com.revolsys.record.query;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.datatype.DataType;
import com.revolsys.equals.Equals;
import com.revolsys.identifier.Identifier;
import com.revolsys.jdbc.field.JdbcFieldDefinition;
import com.revolsys.record.Record;
import com.revolsys.record.code.CodeTable;
import com.revolsys.record.code.CodeTableProperty;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.util.DateUtil;
import com.revolsys.util.Property;
import com.revolsys.util.Strings;

public class Value extends QueryValue {
  public static Object getValue(final Object value) {
    if (value instanceof Identifier) {
      final Identifier identifier = (Identifier)value;
      return identifier.toSingleValue();
    } else {
      return value;
    }
  }

  private FieldDefinition field;

  private Object displayValue;

  private JdbcFieldDefinition jdbcField;

  private Object queryValue;

  public Value(final FieldDefinition field, final Object value) {
    setQueryValue(value);
    this.displayValue = this.queryValue;
    setField(field);
  }

  public Value(final Object value) {
    this(JdbcFieldDefinition.newFieldDefinition(value), value);
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
      return this.jdbcField.setPreparedStatementValue(statement, index, this.queryValue);
    } catch (final SQLException e) {
      throw new RuntimeException("Unable to set value: " + this.queryValue, e);
    }
  }

  @Override
  public Value clone() {
    return (Value)super.clone();
  }

  public void convert(final DataType dataType) {
    if (this.queryValue != null) {
      final Object newValue = StringConverterRegistry.toObject(dataType, this.queryValue);
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
    convert(field.getType());
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof Value) {
      final Value value = (Value)obj;
      return Equals.equal(value.getValue(), this.getValue());
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
    if (this.field == null) {
      return StringConverterRegistry.toString(value);
    } else {
      final Class<?> typeClass = this.field.getTypeClass();
      return StringConverterRegistry.toString(typeClass, value);
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

  public void setField(final FieldDefinition field) {
    this.field = field;
    if (field != null) {
      if (field instanceof JdbcFieldDefinition) {
        this.jdbcField = (JdbcFieldDefinition)field;
      } else {
        this.jdbcField = JdbcFieldDefinition.newFieldDefinition(this.queryValue);
      }

      CodeTable codeTable = null;
      if (field != null) {
        final RecordDefinition recordDefinition = field.getRecordDefinition();
        if (recordDefinition != null) {
          final String fieldName = field.getName();
          codeTable = recordDefinition.getCodeTableByFieldName(fieldName);
          if (codeTable instanceof CodeTableProperty) {
            final CodeTableProperty codeTableProperty = (CodeTableProperty)codeTable;
            if (codeTableProperty.getRecordDefinition() == recordDefinition) {
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
    final String fieldName = this.field.getName();
    if (Property.hasValue(fieldName)) {
      final FieldDefinition field = recordDefinition.getField(fieldName);
      setField(field);
    }
  }

  public void setValue(final Object value) {
    setQueryValue(value);
  }

  @Override
  public String toFormattedString() {
    return toString();
  }

  @Override
  public String toString() {
    if (this.displayValue instanceof Number) {
      return StringConverterRegistry.toString(this.displayValue);
    } else if (this.displayValue instanceof Date) {
      final Date date = (Date)this.displayValue;
      final String stringValue = DateUtil.format("yyyy-MM-dd", date);
      return "{d '" + stringValue + "'}";
    } else if (this.displayValue instanceof Time) {
      final Time time = (Time)this.displayValue;
      final String stringValue = DateUtil.format("HH:mm:ss", time);
      return "{t '" + stringValue + "'}";
    } else if (this.displayValue instanceof Timestamp) {
      final Timestamp time = (Timestamp)this.displayValue;
      final String stringValue = DateUtil.format("yyyy-MM-dd HH:mm:ss.S", time);
      return "{ts '" + stringValue + "'}";
    } else if (this.displayValue instanceof java.util.Date) {
      final java.util.Date time = (java.util.Date)this.displayValue;
      final String stringValue = DateUtil.format("yyyy-MM-dd HH:mm:ss.S", time);
      return "{ts '" + stringValue + "'}";
    } else {
      final String string = StringConverterRegistry.toString(this.displayValue);
      return "'" + string.replaceAll("'", "''") + "'";
    }
  }

}
