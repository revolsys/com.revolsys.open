package com.revolsys.jdbc.field;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

import org.springframework.asm.Type;

import com.revolsys.datatype.DataType;
import com.revolsys.datatype.DataTypes;
import com.revolsys.record.Record;
import com.revolsys.record.schema.FieldDefinition;

public class JdbcFieldDefinition extends FieldDefinition {
  public static JdbcFieldDefinition newFieldDefinition(final Object value) {
    if (value == null) {
      return new JdbcFieldDefinition(null, null, DataTypes.OBJECT, Types.OTHER, 0, 0, false, null,
        null);
    } else if (value instanceof CharSequence) {
      return new JdbcStringFieldDefinition(null, null, Types.CHAR, -1, false, null, null);
    } else if (value instanceof BigInteger) {
      return new JdbcLongFieldDefinition(null, null, Types.BIGINT, -1, false, null, null);
    } else if (value instanceof Long) {
      return new JdbcLongFieldDefinition(null, null, Types.BIGINT, -1, false, null, null);
    } else if (value instanceof Integer) {
      return new JdbcIntegerFieldDefinition(null, null, Types.INTEGER, -1, false, null, null);
    } else if (value instanceof Short) {
      return new JdbcShortFieldDefinition(null, null, Types.SMALLINT, -1, false, null, null);
    } else if (value instanceof Byte) {
      return new JdbcByteFieldDefinition(null, null, Types.TINYINT, -1, false, null, null);
    } else if (value instanceof Double) {
      return new JdbcDoubleFieldDefinition(null, null, Type.DOUBLE, -1, false, null, null);
    } else if (value instanceof Float) {
      return new JdbcFloatFieldDefinition(null, null, Types.FLOAT, -1, false, null, null);
    } else if (value instanceof BigDecimal) {
      return new JdbcBigDecimalFieldDefinition(null, null, Types.NUMERIC, -1, -1, false, null,
        null);
    } else if (value instanceof Date) {
      return new JdbcDateFieldDefinition(null, null, -1, false, null, null);
    } else if (value instanceof java.util.Date) {
      return new JdbcTimestampFieldDefinition(null, null, -1, false, null, null);
    } else if (value instanceof Boolean) {
      return new JdbcBooleanFieldDefinition(null, null, Types.BIT, -1, false, null, null);
    } else {
      return new JdbcFieldDefinition();
    }
  }

  private String dbName;

  private boolean quoteName = false;

  private int sqlType;

  private JdbcFieldDefinition() {
  }

  public JdbcFieldDefinition(final String dbName, final String name, final DataType type,
    final int sqlType, final int length, final int scale, final boolean required,
    final String description, final Map<String, Object> properties) {
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
    if (this.quoteName) {
      sql.append('"');
    }
    sql.append(dbName);
    if (this.quoteName) {
      sql.append('"');
    }
  }

  public void addInsertStatementPlaceHolder(final StringBuilder sql, final boolean generateKeys) {
    addStatementPlaceHolder(sql);
  }

  public void addSelectStatementPlaceHolder(final StringBuilder sql) {
    addStatementPlaceHolder(sql);
  }

  public void addStatementPlaceHolder(final StringBuilder sql) {
    sql.append('?');
  }

  @Override
  public JdbcFieldDefinition clone() {
    return new JdbcFieldDefinition(this.dbName, getName(), getDataType(), getSqlType(), getLength(),
      getScale(), isRequired(), getDescription(), getProperties());
  }

  public String getDbName() {
    return this.dbName;
  }

  public int getSqlType() {
    return this.sqlType;
  }

  public boolean isQuoteName() {
    return this.quoteName;
  }

  public int setFieldValueFromResultSet(final ResultSet resultSet, final int columnIndex,
    final Record record) throws SQLException {
    final Object value = resultSet.getObject(columnIndex);
    setValue(record, value);
    return columnIndex + 1;
  }

  public int setInsertPreparedStatementValue(final PreparedStatement statement,
    final int parameterIndex, final Record record) throws SQLException {
    final String name = getName();
    final Object value = record.getValue(name);
    return setPreparedStatementValue(statement, parameterIndex, value);
  }

  public int setPreparedStatementValue(final PreparedStatement statement, final int parameterIndex,
    final Object value) throws SQLException {
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
