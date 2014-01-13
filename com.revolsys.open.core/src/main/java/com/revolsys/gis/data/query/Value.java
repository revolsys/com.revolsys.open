package com.revolsys.gis.data.query;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.gis.data.model.codes.CodeTableProperty;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.jdbc.attribute.JdbcAttribute;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.DateUtil;

public class Value extends QueryValue {
  private JdbcAttribute jdbcAttribute;

  private Object queryValue;

  private Object displayValue;

  private Attribute attribute;

  public Value(final Attribute attribute, final Object value) {
    this.queryValue = value;
    this.displayValue = value;
    setAttribute(attribute);

  }

  public Value(final Object value) {
    this(JdbcAttribute.createAttribute(value), value);
  }

  @Override
  public int appendParameters(final int index, final PreparedStatement statement) {
    try {
      return jdbcAttribute.setPreparedStatementValue(statement, index,
        this.queryValue);
    } catch (final SQLException e) {
      throw new RuntimeException("Unable to set value: " + this.queryValue, e);
    }
  }

  @Override
  public void appendSql(final StringBuffer buffer) {
    buffer.append('?');
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
        this.queryValue = newValue;
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
    return displayValue;
  }

  public JdbcAttribute getJdbcAttribute() {
    return jdbcAttribute;
  }

  public Object getQueryValue() {
    return queryValue;
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
    if (attribute == null) {

    } else {
      if (attribute instanceof JdbcAttribute) {
        jdbcAttribute = (JdbcAttribute)attribute;
      } else {
        jdbcAttribute = JdbcAttribute.createAttribute(this.queryValue);
      }

      CodeTable codeTable = null;
      if (attribute != null) {
        final DataObjectMetaData metaData = attribute.getMetaData();
        if (metaData != null) {
          final String fieldName = attribute.getName();
          codeTable = metaData.getCodeTableByColumn(fieldName);
          if (codeTable instanceof CodeTableProperty) {
            final CodeTableProperty codeTableProperty = (CodeTableProperty)codeTable;
            if (codeTableProperty.getMetaData() == metaData) {
              codeTable = null;
            }
          }
          if (codeTable != null) {
            final Object id = codeTable.getId(this.queryValue);
            if (id == null) {
              this.displayValue = this.queryValue;
            } else {
              this.queryValue = id;
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

  public void setValue(final Object value) {
    this.queryValue = value;
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
      final String stringValue = DateUtil.format("yyyy-MM-dd ", date);
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
