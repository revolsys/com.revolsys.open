package com.revolsys.record.query;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.exception.Exceptions;

import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordStore;

public class SqlExpression implements Condition {

  private final String sql;

  private final DataType dataType;

  public SqlExpression(final String sql, final DataType dataType) {
    this.sql = sql;
    this.dataType = dataType;
  }

  @Override
  public void appendDefaultSql(final Query query, final RecordStore recordStore,
    final Appendable buffer) {
    try {
      buffer.append(this.sql);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public int appendParameters(final int index, final PreparedStatement statement) {
    return index;
  }

  @Override
  public SqlExpression clone() {
    return new SqlExpression(this.sql, this.dataType);
  }

  @Override
  public SqlExpression clone(final TableReference oldTable, final TableReference newTable) {
    return clone();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof SqlExpression) {
      final SqlExpression sqlCondition = (SqlExpression)obj;
      if (DataType.equal(sqlCondition.getSql(), getSql())) {
        if (sqlCondition.dataType == this.dataType) {
          return true;
        }
      }
    }
    return false;
  }

  public String getSql() {
    return this.sql;
  }

  @Override
  public Object getValueFromResultSet(final RecordDefinition recordDefinition,
    final ResultSet resultSet, final ColumnIndexes indexes, final boolean internStrings)
    throws SQLException {
    final int index = indexes.incrementAndGet();
    final Object value = resultSet.getObject(index);
    if (resultSet.wasNull()) {
      return null;
    } else {
      return this.dataType.toObject(value);
    }
  }

  @Override
  public String toString() {
    return getSql();
  }
}
