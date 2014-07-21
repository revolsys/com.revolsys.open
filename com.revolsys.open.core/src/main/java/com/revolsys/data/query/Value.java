package com.revolsys.data.query;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.codes.CodeTable;
import com.revolsys.data.codes.CodeTableProperty;
import com.revolsys.data.equals.EqualsRegistry;
import com.revolsys.data.identifier.Identifier;
import com.revolsys.data.io.RecordStore;
import com.revolsys.data.record.schema.Attribute;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.types.DataType;
import com.revolsys.jdbc.attribute.JdbcAttribute;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.DateUtil;
import com.revolsys.util.Property;

public class Value extends QueryValue {
  public static Object getValue(final Object value) {
    Object newValue;
    if (value instanceof Identifier) {
      final Identifier identifier = (Identifier)value;
      final List<Object> values = identifier.getValues();
      if (values.size() == 0) {
        newValue = null;
      } else if (values.size() == 1) {
        newValue = values.get(0);
      } else {
        throw new IllegalArgumentException(
          "Cannot create value for identifier with multiple parts " + value);
      }
    } else {
      newValue = value;
    }
    return newValue;
  }

  private JdbcAttribute jdbcAttribute;

  private Object queryValue;

  private Object displayValue;

  private Attribute attribute;

  public Value(final Attribute attribute, final Object value) {
    setQueryValue(value);
    this.displayValue = this.queryValue;
    setAttribute(attribute);
  }

  public Value(final Object value) {
    this(JdbcAttribute.createAttribute(value), value);
  }

  @Override
  public void appendDefaultSql(final Query query,
    final RecordStore recordStore, final StringBuffer buffer) {
    if (this.jdbcAttribute == null) {
      buffer.append('?');
    } else {
      this.jdbcAttribute.addSelectStatementPlaceHolder(buffer);
    }
  }

  @Override
  public int appendParameters(final int index, final PreparedStatement statement) {
    try {
      return this.jdbcAttribute.setPreparedStatementValue(statement, index,
        this.queryValue);
    } catch (final SQLException e) {
      throw new RuntimeException("Unable to set value: " + this.queryValue, e);
    }
  }

  @Override
  public Value clone() {
    return (Value)super.clone();
  }

  public void convert(final Attribute attribute) {
    if (attribute instanceof JdbcAttribute) {
      this.jdbcAttribute = (JdbcAttribute)attribute;
    }
    convert(attribute.getType());
  }

  public void convert(final DataType dataType) {
    if (this.queryValue != null) {
      final Object newValue = StringConverterRegistry.toObject(dataType,
        this.queryValue);
      final Class<?> typeClass = dataType.getJavaClass();
      if (newValue == null || !typeClass.isAssignableFrom(newValue.getClass())) {
        throw new IllegalArgumentException(this.queryValue + " is not a valid "
            + typeClass);
      } else {
        setQueryValue(newValue);
      }
    }
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof Value) {
      final Value value = (Value)obj;
      return EqualsRegistry.equal(value.getValue(), this.getValue());
    } else {
      return false;
    }
  }

  public Object getDisplayValue() {
    return this.displayValue;
  }

  public JdbcAttribute getJdbcAttribute() {
    return this.jdbcAttribute;
  }

  public Object getQueryValue() {
    return this.queryValue;
  }

  @Override
  public String getStringValue(final Map<String, Object> record) {
    final Object value = getValue(record);
    if (this.attribute == null) {
      return StringConverterRegistry.toString(value);
    } else {
      final Class<?> typeClass = this.attribute.getTypeClass();
      return StringConverterRegistry.toString(typeClass, value);
    }
  }

  public Object getValue() {
    return this.queryValue;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getValue(final Map<String, Object> record) {
    return (V)this.queryValue;
  }

  public void setAttribute(final Attribute attribute) {
    this.attribute = attribute;
    if (attribute != null) {
      if (attribute instanceof JdbcAttribute) {
        this.jdbcAttribute = (JdbcAttribute)attribute;
      } else {
        this.jdbcAttribute = JdbcAttribute.createAttribute(this.queryValue);
      }

      CodeTable codeTable = null;
      if (attribute != null) {
        final RecordDefinition recordDefinition = attribute.getRecordDefinition();
        if (recordDefinition != null) {
          final String fieldName = attribute.getName();
          codeTable = recordDefinition.getCodeTableByColumn(fieldName);
          if (codeTable instanceof CodeTableProperty) {
            final CodeTableProperty codeTableProperty = (CodeTableProperty)codeTable;
            if (codeTableProperty.getRecordDefinition() == recordDefinition) {
              codeTable = null;
            }
          }
          if (codeTable != null) {
            final Identifier id = codeTable.getId(this.queryValue);
            if (id == null) {
              this.displayValue = this.queryValue;
            } else {
              setQueryValue(id);
              final List<Object> values = codeTable.getValues(id);
              if (values.size() == 1) {
                this.displayValue = values.get(0);
              } else {
                this.displayValue = CollectionUtil.toString(":", values);
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
    final String attributeName = this.attribute.getName();
    if (Property.hasValue(attributeName)) {
      final Attribute attribute = recordDefinition.getAttribute(attributeName);
      setAttribute(attribute);
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
