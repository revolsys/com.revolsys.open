package com.revolsys.data.query.functions;

import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.List;

import com.revolsys.data.equals.EqualsRegistry;
import com.revolsys.data.io.DataObjectStore;
import com.revolsys.data.query.Query;
import com.revolsys.data.query.QueryValue;

public abstract class UnaryFunction extends QueryValue {

  private final String name;

  private final QueryValue parameter;

  public UnaryFunction(final String name, final QueryValue parameter) {
    this.name = name;
    this.parameter = parameter;
  }

  @Override
  public void appendDefaultSql(Query query,
    final DataObjectStore dataStore, final StringBuffer buffer) {
    buffer.append(getName());
    buffer.append("(");
    final QueryValue parameter = getParameter();
    parameter.appendSql(query, dataStore, buffer);
    buffer.append(")");
  }

  @Override
  public int appendParameters(final int index, final PreparedStatement statement) {
    final QueryValue parameter = getParameter();
    return parameter.appendParameters(index, statement);
  }

  @Override
  public UnaryFunction clone() {

    return (UnaryFunction)super.clone();
  }

  @Override
  public boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (other instanceof UnaryFunction) {
      final UnaryFunction function = (UnaryFunction)other;
      if (EqualsRegistry.equal(function.getName(), getName())) {
        if (EqualsRegistry.equal(function.getParameter(), getParameter())) {
          return true;
        }
      }
    }
    return false;
  }

  public String getName() {
    return name;
  }

  public QueryValue getParameter() {
    return parameter;
  }

  @Override
  public List<QueryValue> getQueryValues() {
    return Collections.singletonList(parameter);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((parameter == null) ? 0 : parameter.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return getName() + "(" + getParameter() + ")";
  }
}
