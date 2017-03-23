package com.revolsys.jdbc.field;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

import com.revolsys.datatype.DataType;
import com.revolsys.datatype.DataTypes;
import com.revolsys.identifier.Identifier;
import com.revolsys.identifier.TypedIdentifier;
import com.revolsys.record.Record;
import com.revolsys.record.schema.FieldDefinition;

public class JdbcFieldDefinition extends FieldDefinition {
  public static final String UNKNOWN = "UNKNOWN";

  private static final JdbcFieldDefinition FIELD_UNKNOWN = new JdbcFieldDefinition();

  private static final JdbcBooleanFieldDefinition FIELD_BOOLEAN = new JdbcBooleanFieldDefinition(
    UNKNOWN, UNKNOWN, Types.BIT, -1, false, null, null);

  private static final JdbcTimestampFieldDefinition FIELD_TIMESTAMP = new JdbcTimestampFieldDefinition(
    UNKNOWN, UNKNOWN, -1, false, null, null);

  private static final JdbcDateFieldDefinition FIELD_DATE = new JdbcDateFieldDefinition(UNKNOWN,
    UNKNOWN, -1, false, null, null);

  private static final JdbcBigDecimalFieldDefinition FIELD_BIG_DECIMAL = new JdbcBigDecimalFieldDefinition(
    UNKNOWN, UNKNOWN, Types.NUMERIC, -1, -1, false, null, null);

  private static final JdbcFloatFieldDefinition FIELD_FLOAT = new JdbcFloatFieldDefinition(UNKNOWN,
    UNKNOWN, Types.FLOAT, false, null, null);

  private static final JdbcDoubleFieldDefinition FIELD_DOUBLE = new JdbcDoubleFieldDefinition(
    UNKNOWN, UNKNOWN, Types.DOUBLE, false, null, null);

  private static final JdbcByteFieldDefinition FIELD_BYTE = new JdbcByteFieldDefinition(UNKNOWN,
    UNKNOWN, Types.TINYINT, false, null, null);

  private static final JdbcShortFieldDefinition FIELD_SHORT = new JdbcShortFieldDefinition(UNKNOWN,
    UNKNOWN, Types.SMALLINT, false, null, null);

  private static final JdbcIntegerFieldDefinition FIELD_INTEGER = new JdbcIntegerFieldDefinition(
    UNKNOWN, UNKNOWN, Types.INTEGER, false, null, null);

  private static final JdbcLongFieldDefinition FIELD_LONG = new JdbcLongFieldDefinition(UNKNOWN,
    UNKNOWN, Types.BIGINT, false, null, null);

  private static final JdbcStringFieldDefinition FIELD_STRING = new JdbcStringFieldDefinition(
    UNKNOWN, UNKNOWN, Types.CHAR, -1, false, null, null);

  private static final JdbcFieldDefinition FIELD_OBJECT = new JdbcFieldDefinition(UNKNOWN, UNKNOWN,
    DataTypes.OBJECT, Types.OTHER, 0, 0, false, null, null);

  public static JdbcFieldDefinition newFieldDefinition(Object value) {
    if (value instanceof TypedIdentifier) {
      return FIELD_STRING;
    } else if (value instanceof Identifier) {
      final Identifier identifier = (Identifier)value;
      value = identifier.toSingleValue();
    }
    if (value == null) {
      return FIELD_OBJECT;
    } else if (value instanceof CharSequence) {
      return FIELD_STRING;
    } else if (value instanceof BigInteger) {
      return FIELD_LONG;
    } else if (value instanceof Long) {
      return FIELD_LONG;
    } else if (value instanceof Integer) {
      return FIELD_INTEGER;
    } else if (value instanceof Short) {
      return FIELD_SHORT;
    } else if (value instanceof Byte) {
      return FIELD_BYTE;
    } else if (value instanceof Double) {
      return FIELD_DOUBLE;
    } else if (value instanceof Float) {
      return FIELD_FLOAT;
    } else if (value instanceof BigDecimal) {
      return FIELD_BIG_DECIMAL;
    } else if (value instanceof Date) {
      return FIELD_DATE;
    } else if (value instanceof java.util.Date) {
      return FIELD_TIMESTAMP;
    } else if (value instanceof Boolean) {
      return FIELD_BOOLEAN;
    } else {
      return FIELD_UNKNOWN;
    }
  }

  private String dbName;

  private boolean quoteName = false;

  private int sqlType;

  private JdbcFieldDefinition() {
    setName(UNKNOWN);
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
    if (value == null) {
      statement.setNull(parameterIndex, this.sqlType);
    } else {
      statement.setObject(parameterIndex, value);
    }
    return parameterIndex + 1;
  }

  public void setQuoteName(final boolean quoteName) {
    this.quoteName = quoteName;
  }

  public void setSqlType(final int sqlType) {
    this.sqlType = sqlType;
  }
}
