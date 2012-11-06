package com.revolsys.jdbc.attribute;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

import org.springframework.asm.Type;

import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;

public class JdbcAttribute extends Attribute {

  public static JdbcAttribute createAttribute(final Object value) {
    if (value == null) {
      return new JdbcAttribute(null, DataTypes.OBJECT, Types.OTHER, 0, 0,
        false, null);
    } else if (value instanceof CharSequence) {
      return new JdbcStringAttribute(null, Types.CHAR, -1, false, null);
    } else if (value instanceof BigInteger) {
      return new JdbcLongAttribute(null, Types.BIGINT, -1, false, null);
    } else if (value instanceof Long) {
      return new JdbcLongAttribute(null, Types.BIGINT, -1, false, null);
    } else if (value instanceof Integer) {
      return new JdbcIntegerAttribute(null, Types.INTEGER, -1, false, null);
    } else if (value instanceof Short) {
      return new JdbcShortAttribute(null, Types.SMALLINT, -1, false, null);
    } else if (value instanceof Byte) {
      return new JdbcByteAttribute(null, Types.TINYINT, -1, false, null);
    } else if (value instanceof Double) {
      return new JdbcDoubleAttribute(null, Type.DOUBLE, -1, false, null);
    } else if (value instanceof Float) {
      return new JdbcFloatAttribute(null, Types.FLOAT, -1, false, null);
    } else if (value instanceof BigDecimal) {
      return new JdbcBigDecimalAttribute(null, Types.NUMERIC, -1, -1, false,
        null);
    } else if (value instanceof Date) {
      return new JdbcDateAttribute(null, -1, false, null);
    } else if (value instanceof java.util.Date) {
      return new JdbcTimestampAttribute(null, -1, false, null);
    } else if (value instanceof Boolean) {
      return new JdbcBooleanAttribute(null, Types.BIT, -1, false, null);
    } else {
      return new JdbcAttribute();
    }
  }

  private int sqlType;

  private JdbcAttribute() {
  }

  public JdbcAttribute(final String name, final DataType type,
    final int sqlType, final int length, final int scale,
    final boolean required, final Map<String, Object> properties) {
    super(name, type, length, scale, required, properties);
    this.sqlType = sqlType;
  }

  public void addColumnName(final StringBuffer sql, final String tablePrefix) {
    if (tablePrefix != null) {
      sql.append(tablePrefix);
      sql.append(".");
    }
    sql.append(getName());
  }

  public void addInsertStatementPlaceHolder(final StringBuffer sql,
    final boolean generateKeys) {
    addStatementPlaceHolder(sql);
  }

  public void addSelectStatementPlaceHolder(final StringBuffer sql) {
    addStatementPlaceHolder(sql);
  }

  public void addStatementPlaceHolder(final StringBuffer sql) {
    sql.append('?');
  }

  @Override
  public JdbcAttribute clone() {
    return new JdbcAttribute(getName(), getType(), getSqlType(), getLength(),
      getScale(), isRequired(), getProperties());
  }

  public int getSqlType() {
    return sqlType;
  }

  public int setAttributeValueFromResultSet(final ResultSet resultSet,
    final int columnIndex, final DataObject object) throws SQLException {
    final Object value = resultSet.getObject(columnIndex);
    object.setValue(getIndex(), value);
    return columnIndex + 1;
  }

  public int setInsertPreparedStatementValue(final PreparedStatement statement,
    final int parameterIndex, final DataObject object) throws SQLException {
    final String name = getName();
    final Object value = object.getValue(name);
    return setPreparedStatementValue(statement, parameterIndex, value);
  }

  public int setPreparedStatementValue(final PreparedStatement statement,
    final int parameterIndex, final Object value) throws SQLException {
    statement.setObject(parameterIndex, value);
    return parameterIndex + 1;
  }

  public void setSqlType(final int sqlType) {
    this.sqlType = sqlType;
  }
}
