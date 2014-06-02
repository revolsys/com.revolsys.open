package com.revolsys.gis.data.query;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.ctc.wstx.util.ExceptionUtil;
import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.jdbc.attribute.JdbcAttribute;
import com.revolsys.util.CollectionUtil;

public class CollectionValue extends QueryValue {
  private List<QueryValue> queryValues = new ArrayList<QueryValue>();

  private JdbcAttribute jdbcAttribute;

  private Attribute attribute;

  public CollectionValue(final Attribute attribute,
    final Collection<? extends Object> values) {
    setAttribute(attribute);
    for (final Object value : values) {
      QueryValue queryValue;
      if (value instanceof QueryValue) {
        queryValue = (QueryValue)value;
      } else {
        queryValue = new Value(value);
      }
      queryValues.add(queryValue);

    }
  }

  public CollectionValue(final Collection<? extends Object> values) {
    this(null, values);
  }

  @Override
  public void appendDefaultSql(Query query,
    final DataObjectStore dataStore, final StringBuffer buffer) {
    buffer.append('(');
    for (int i = 0; i < queryValues.size(); i++) {
      if (i > 0) {
        buffer.append(", ");
      }

      final QueryValue queryValue = queryValues.get(i);
      if (queryValue instanceof Value) {
        if (jdbcAttribute == null) {
          queryValue.appendSql(query, dataStore, buffer);
        } else {
          jdbcAttribute.addSelectStatementPlaceHolder(buffer);
        }
      } else {
        queryValue.appendSql(query, dataStore, buffer);
      }

    }
    buffer.append(')');
  }

  @Override
  public int appendParameters(int index, final PreparedStatement statement) {
    for (final QueryValue queryValue : queryValues) {
      JdbcAttribute jdbcAttribute = this.jdbcAttribute;
      if (queryValue instanceof Value) {
        final Value valueWrapper = (Value)queryValue;
        final Object value = valueWrapper.getQueryValue();
        if (jdbcAttribute == null) {
          jdbcAttribute = JdbcAttribute.createAttribute(value);
        }
        try {
          index = jdbcAttribute.setPreparedStatementValue(statement, index,
            value);
        } catch (final SQLException e) {
          ExceptionUtil.throwIfUnchecked(e);
        }
      } else {
        index = queryValue.appendParameters(index, statement);
      }
    }
    return index;
  }

  @Override
  public CollectionValue clone() {
    final CollectionValue clone = (CollectionValue)super.clone();
    clone.queryValues = cloneQueryValues(queryValues);
    return clone;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof CollectionValue) {
      final CollectionValue condition = (CollectionValue)obj;
      return EqualsRegistry.equal(condition.getQueryValues(),
        this.getQueryValues());
    } else {
      return false;
    }
  }

  public Attribute getAttribute() {
    return attribute;
  }

  @Override
  public List<QueryValue> getQueryValues() {
    return queryValues;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getValue(final Map<String, Object> record) {
    final List<Object> values = new ArrayList<Object>();
    for (final QueryValue queryValue : this.queryValues) {
      final Object value = queryValue.getValue(record);
      values.add(value);
    }
    return (V)values;
  }

  public List<Object> getValues() {
    CodeTable codeTable = null;
    if (attribute != null) {
      final DataObjectMetaData metaData = attribute.getMetaData();
      final String fieldName = attribute.getName();
      codeTable = metaData.getCodeTableByColumn(fieldName);
    }
    final List<Object> values = new ArrayList<Object>();
    for (final QueryValue queryValue : getQueryValues()) {
      Object value;
      if (queryValue instanceof Value) {
        final Value valueWrapper = (Value)queryValue;
        value = valueWrapper.getValue();
      } else {
        value = queryValue;
      }
      if (value != null) {
        if (codeTable != null) {
          value = codeTable.getId(value);
        }
        values.add(value);
      }
    }
    return values;
  }

  public void setAttribute(final Attribute attribute) {
    this.attribute = attribute;
    if (attribute == null) {
      this.jdbcAttribute = null;
    } else {
      if (attribute instanceof JdbcAttribute) {
        this.jdbcAttribute = (JdbcAttribute)attribute;
      } else {
        this.jdbcAttribute = null;
      }
      for (final QueryValue queryValue : queryValues) {
        if (queryValue instanceof Value) {
          final Value value = (Value)queryValue;
          value.setAttribute(attribute);
        }
      }
    }
  }

  @Override
  public String toString() {
    return "(" + CollectionUtil.toString(queryValues) + ")";
  }
}
