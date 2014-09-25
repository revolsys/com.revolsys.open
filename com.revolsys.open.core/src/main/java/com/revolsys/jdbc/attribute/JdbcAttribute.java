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

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.Attribute;
import com.revolsys.data.types.DataType;
import com.revolsys.data.types.DataTypes;

public class JdbcAttribute extends Attribute {
  public static JdbcAttribute createAttribute(final Object value) {
    if (value == null) {
      return new JdbcAttribute(null, null, DataTypes.OBJECT, Types.OTHER, 0, 0,
        false, null, null);
    } else if (value instanceof CharSequence) {
      return new JdbcStringAttribute(null, null, Types.CHAR, -1, false, null,
        null);
    } else if (value instanceof BigInteger) {
      return new JdbcLongAttribute(null, null, Types.BIGINT, -1, false, null,
        null);
    } else if (value instanceof Long) {
      return new JdbcLongAttribute(null, null, Types.BIGINT, -1, false, null,
        null);
    } else if (value instanceof Integer) {
      return new JdbcIntegerAttribute(null, null, Types.INTEGER, -1, false,
        null, null);
    } else if (value instanceof Short) {
      return new JdbcShortAttribute(null, null, Types.SMALLINT, -1, false,
        null, null);
    } else if (value instanceof Byte) {
      return new JdbcByteAttribute(null, null, Types.TINYINT, -1, false, null,
        null);
    } else if (value instanceof Double) {
      return new JdbcDoubleAttribute(null, null, Type.DOUBLE, -1, false, null,
        null);
    } else if (value instanceof Float) {
      return new JdbcFloatAttribute(null, null, Types.FLOAT, -1, false, null,
        null);
    } else if (value instanceof BigDecimal) {
      return new JdbcBigDecimalAttribute(null, null, Types.NUMERIC, -1, -1,
        false, null, null);
    } else if (value instanceof Date) {
      return new JdbcDateAttribute(null, null, -1, false, null, null);
    } else if (value instanceof java.util.Date) {
      return new JdbcTimestampAttribute(null, null, -1, false, null, null);
    } else if (value instanceof Boolean) {
      return new JdbcBooleanAttribute(null, null, Types.BIT, -1, false, null,
        null);
    } else {
      return new JdbcAttribute();
    }
  }

  private boolean quoteName = false;

  private int sqlType;

  private String dbName;

  private JdbcAttribute() {
  }

  public JdbcAttribute(final String dbName, final String name,
    final DataType type, final int sqlType, final int length, final int scale,
    final boolean required, final String description,
    final Map<String, Object> properties) {
    super(name, type, length, scale, required, description, properties);
    this.dbName = dbName;
    this.sqlType = sqlType;
  }

  public void addColumnName(final StringBuilder sql, final String tablePrefix) {
    if (tablePrefix != null) {
      sql.append(tablePrefix);
      sql.append(".");
    }
    final String dbName = getDbName();
    if (quoteName) {
      sql.append('"');
    }
    sql.append(dbName);
    if (quoteName) {
      sql.append('"');
    }
  }

  public void addInsertStatementPlaceHolder(final StringBuilder sql,
    final boolean generateKeys) {
    addStatementPlaceHolder(sql);
  }

  public void addSelectStatementPlaceHolder(final StringBuilder sql) {
    addStatementPlaceHolder(sql);
  }

  public void addStatementPlaceHolder(final StringBuilder sql) {
    sql.append('?');
  }

  @Override
  public JdbcAttribute clone() {
    return new JdbcAttribute(dbName, getName(), getType(), getSqlType(),
      getLength(), getScale(), isRequired(), getDescription(), getProperties());
  }

  public String getDbName() {
    return dbName;
  }

  public int getSqlType() {
    return sqlType;
  }

  public boolean isQuoteName() {
    return quoteName;
  }

  public int setAttributeValueFromResultSet(final ResultSet resultSet,
    final int columnIndex, final Record object) throws SQLException {
    final Object value = resultSet.getObject(columnIndex);
    setValue(object, value);
    return columnIndex + 1;
  }

  public int setInsertPreparedStatementValue(final PreparedStatement statement,
    final int parameterIndex, final Record object) throws SQLException {
    final String name = getName();
    final Object value = object.getValue(name);
    return setPreparedStatementValue(statement, parameterIndex, value);
  }

  public int setPreparedStatementValue(final PreparedStatement statement,
    final int parameterIndex, final Object value) throws SQLException {
    statement.setObject(parameterIndex, value);
    return parameterIndex + 1;
  }

  public void setQuoteName(final boolean quoteName) {
    this.quoteName = quoteName;
  }

  public void setSqlType(final int sqlType) {
    this.sqlType = sqlType;
  }
}
