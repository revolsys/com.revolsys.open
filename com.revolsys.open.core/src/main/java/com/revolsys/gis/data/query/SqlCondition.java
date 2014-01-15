package com.revolsys.gis.data.query;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.jdbc.attribute.JdbcAttribute;

// TODO accept (how?)
public class SqlCondition extends Condition {
  private List<Attribute> parameterAttributes = new ArrayList<Attribute>();

  private List<Object> parameterValues = new ArrayList<Object>();

  private final String sql;

  public SqlCondition(final String sql) {
    this.sql = sql;
  }

  public SqlCondition(final String sql, final Attribute parameterAttribute,
    final Object parameterValue) {
    this(sql, Arrays.asList(parameterAttribute), Arrays.asList(parameterValue));
  }

  public SqlCondition(final String sql,
    final List<Attribute> parameterAttributes,
    final List<Object> parameterValues) {
    this.sql = sql;
    this.parameterValues = new ArrayList<Object>(parameterValues);
    this.parameterAttributes = new ArrayList<Attribute>(parameterAttributes);
  }

  public SqlCondition(final String sql, final Object... parameters) {
    this.sql = sql;
    addParameters(parameters);
  }

  public void addParameter(final Object value) {
    parameterValues.add(value);
    parameterAttributes.add(null);
  }

  public void addParameter(final Object value, final Attribute attribute) {
    addParameter(value);
    parameterAttributes.set(parameterAttributes.size() - 1, attribute);
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
  public int appendParameters(int index, final PreparedStatement statement) {
    for (int i = 0; i < parameterValues.size(); i++) {
      final Object value = parameterValues.get(i);
      JdbcAttribute jdbcAttribute = null;
      if (i < parameterAttributes.size()) {
        final Attribute attribute = parameterAttributes.get(i);
        if (attribute instanceof JdbcAttribute) {
          jdbcAttribute = (JdbcAttribute)attribute;

        }
      }

      if (jdbcAttribute == null) {
        jdbcAttribute = JdbcAttribute.createAttribute(value);
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
  public void appendSql(final StringBuffer buffer) {
    buffer.append(sql);
  }

  @Override
  public SqlCondition clone() {
    return new SqlCondition(sql, parameterAttributes, parameterValues);
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof SqlCondition) {
      final SqlCondition sqlCondition = (SqlCondition)obj;
      if (EqualsRegistry.equal(sqlCondition.getSql(), this.getSql())) {
        if (EqualsRegistry.equal(sqlCondition.getParameterValues(),
          this.getParameterValues())) {
          return true;
        }
      }
    }
    return false;
  }

  public List<Object> getParameterValues() {
    return parameterValues;
  }

  public String getSql() {
    return sql;
  }

  @Override
  public String toString() {
    return getSql() + ": " + getParameterValues();
  }
}
