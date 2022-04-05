package com.revolsys.record.query;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.exception.Exceptions;

import com.revolsys.jdbc.field.JdbcFieldDefinition;
import com.revolsys.jdbc.field.JdbcFieldDefinitions;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordStore;

// TODO accept (how?)
public class SqlCondition implements Condition {
  private List<FieldDefinition> parameterAttributes = new ArrayList<>();

  private List<Object> parameterValues = new ArrayList<>();

  private final String sql;

  public SqlCondition(final String sql) {
    this.sql = sql;
  }

  public SqlCondition(final String sql, final FieldDefinition parameterAttribute,
    final Object parameterValue) {
    this(sql, Arrays.asList(parameterAttribute), Arrays.asList(parameterValue));
  }

  public SqlCondition(final String sql, final List<FieldDefinition> parameterAttributes,
    final List<Object> parameterValues) {
    this.sql = sql;
    this.parameterValues = new ArrayList<>(parameterValues);
    this.parameterAttributes = new ArrayList<>(parameterAttributes);
  }

  public SqlCondition(final String sql, final Object... parameters) {
    this.sql = sql;
    addParameters(parameters);
  }

  public void addParameter(final Object value) {
    this.parameterValues.add(value);
    this.parameterAttributes.add(null);
  }

  public void addParameter(final Object value, final FieldDefinition attribute) {
    addParameter(value);
    this.parameterAttributes.set(this.parameterAttributes.size() - 1, attribute);
  }

  public void addParameters(final List<Object> parameters) {
    for (final Object parameter : parameters) {
      addParameter(parameter);
    }
  }

  public void addParameters(final Object... parameters) {
    addParameters(Arrays.asList(parameters));
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
  public int appendParameters(int index, final PreparedStatement statement) {
    for (int i = 0; i < this.parameterValues.size(); i++) {
      final Object value = this.parameterValues.get(i);
      JdbcFieldDefinition jdbcAttribute = null;
      if (i < this.parameterAttributes.size()) {
        final FieldDefinition attribute = this.parameterAttributes.get(i);
        if (attribute instanceof JdbcFieldDefinition) {
          jdbcAttribute = (JdbcFieldDefinition)attribute;

        }
      }

      if (jdbcAttribute == null) {
        jdbcAttribute = JdbcFieldDefinitions.newFieldDefinition(value);
      }
      try {
        index = jdbcAttribute.setPreparedStatementValue(statement, index, value);
      } catch (final SQLException e) {
        throw new RuntimeException("Unable to set value: " + value, e);
      }
    }
    return index;
  }

  @Override
  public SqlCondition clone() {
    return new SqlCondition(this.sql, this.parameterAttributes, this.parameterValues);
  }

  @Override
  public SqlCondition clone(final TableReference oldTable, final TableReference newTable) {
    return clone();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof SqlCondition) {
      final SqlCondition sqlCondition = (SqlCondition)obj;
      if (DataType.equal(sqlCondition.getSql(), getSql())) {
        if (DataType.equal(sqlCondition.getParameterValues(), getParameterValues())) {
          return true;
        }
      }
    }
    return false;
  }

  public List<Object> getParameterValues() {
    return this.parameterValues;
  }

  public String getSql() {
    return this.sql;
  }

  @Override
  public Object getValueFromResultSet(final RecordDefinition recordDefinition,
    final ResultSet resultSet, final ColumnIndexes indexes, final boolean internStrings)
    throws SQLException {
    return resultSet.getString(indexes.incrementAndGet());
  }

  @Override
  public String toString() {
    return getSql() + ": " + getParameterValues();
  }
}
