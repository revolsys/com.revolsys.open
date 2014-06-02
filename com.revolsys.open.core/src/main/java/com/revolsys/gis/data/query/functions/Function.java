package com.revolsys.gis.data.query.functions;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.query.Query;
import com.revolsys.gis.data.query.QueryValue;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.util.CollectionUtil;

public class Function extends QueryValue {

  private final String name;

  private List<QueryValue> parameters;

  public Function(final String name, final List<QueryValue> parameters) {
    this.name = name;
    this.parameters = new ArrayList<QueryValue>(parameters);
  }

  public Function(final String name, final QueryValue... parameters) {
    this(name, Arrays.asList(parameters));
  }

  public void add(final QueryValue value) {
    this.parameters.add(value);
  }

  @Override
  public void appendDefaultSql(Query query,
    final DataObjectStore dataStore, final StringBuffer buffer) {
    buffer.append(name);
    buffer.append("(");
    boolean first = true;
    for (final QueryValue parameter : getParameters()) {
      if (first) {
        first = false;
      } else {
        buffer.append(", ");
      }
      parameter.appendSql(query, dataStore, buffer);
    }
    buffer.append(")");
  }

  @Override
  public int appendParameters(int index, final PreparedStatement statement) {
    for (final QueryValue value : getParameters()) {
      index = value.appendParameters(index, statement);
    }
    return index;
  }

  public void clear() {
    parameters.clear();
  }

  @Override
  public Function clone() {
    final Function clone = (Function)super.clone();
    clone.parameters = cloneQueryValues(this.parameters);
    return clone;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof Function) {
      final Function value = (Function)obj;
      if (EqualsRegistry.equal(getName(), value.getName())) {
        final List<QueryValue> parameters1 = getParameters();
        final List<QueryValue> parameters2 = value.getParameters();
        if (parameters1.size() == parameters2.size()) {
          for (int i = 0; i < parameters1.size(); i++) {
            final QueryValue value1 = parameters1.get(i);
            final QueryValue value2 = parameters2.get(i);
            if (!EqualsRegistry.equal(value1, value2)) {
              return false;
            }
          }
          return true;
        }
      }
    }
    return false;
  }

  public String getName() {
    return name;
  }

  public QueryValue getParameter(final int index) {
    final List<QueryValue> parameters = getParameters();
    if (index >= 0 && index < parameters.size()) {
      return parameters.get(index);
    } else {
      return null;
    }
  }

  public List<QueryValue> getParameters() {
    return Collections.unmodifiableList(parameters);
  }

  public String getParameterStringValue(final int index,
    final Map<String, Object> record) {
    final QueryValue parameter = getParameter(index);
    if (parameter == null) {
      return null;
    } else {
      return parameter.getStringValue(record);
    }
  }

  public <V> V getParameterValue(final int index,
    final Map<String, Object> record) {
    final QueryValue parameter = getParameter(index);
    if (parameter == null) {
      return null;
    } else {
      final V value = parameter.getValue(record);
      return value;
    }
  }

  @Override
  public List<QueryValue> getQueryValues() {
    return Collections.<QueryValue> unmodifiableList(parameters);

  }

  @Override
  public <V> V getValue(final Map<String, Object> record) {
    throw new UnsupportedOperationException("Function is not supported"
      + getName());
  }

  @Override
  public String toString() {
    final List<QueryValue> parameters = getParameters();
    return getName() + "(" + CollectionUtil.toString(", ", parameters) + ")";
  }
}
