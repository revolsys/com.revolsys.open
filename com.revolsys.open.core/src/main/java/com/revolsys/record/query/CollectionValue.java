package com.revolsys.record.query;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.ctc.wstx.util.ExceptionUtil;
import com.revolsys.equals.Equals;
import com.revolsys.jdbc.field.JdbcFieldDefinition;
import com.revolsys.record.Record;
import com.revolsys.record.code.CodeTable;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.util.Strings;

public class CollectionValue extends QueryValue {
  private FieldDefinition field;

  private JdbcFieldDefinition jdbcField;

  private List<QueryValue> queryValues = new ArrayList<QueryValue>();

  public CollectionValue(final Collection<? extends Object> values) {
    this(null, values);
  }

  public CollectionValue(final FieldDefinition field, final Collection<? extends Object> values) {
    setField(field);
    for (final Object value : values) {
      QueryValue queryValue;
      if (value instanceof QueryValue) {
        queryValue = (QueryValue)value;
      } else {
        queryValue = new Value(value);
      }
      this.queryValues.add(queryValue);

    }
  }

  @Override
  public void appendDefaultSql(final Query query, final RecordStore recordStore,
    final StringBuilder buffer) {
    buffer.append('(');
    for (int i = 0; i < this.queryValues.size(); i++) {
      if (i > 0) {
        buffer.append(", ");
      }

      final QueryValue queryValue = this.queryValues.get(i);
      if (queryValue instanceof Value) {
        if (this.jdbcField == null) {
          queryValue.appendSql(query, recordStore, buffer);
        } else {
          this.jdbcField.addSelectStatementPlaceHolder(buffer);
        }
      } else {
        queryValue.appendSql(query, recordStore, buffer);
      }

    }
    buffer.append(')');
  }

  @Override
  public int appendParameters(int index, final PreparedStatement statement) {
    for (final QueryValue queryValue : this.queryValues) {
      JdbcFieldDefinition jdbcField = this.jdbcField;
      if (queryValue instanceof Value) {
        final Value valueWrapper = (Value)queryValue;
        final Object value = valueWrapper.getQueryValue();
        if (jdbcField == null) {
          jdbcField = JdbcFieldDefinition.newFieldDefinition(value);
        }
        try {
          index = jdbcField.setPreparedStatementValue(statement, index, value);
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
    clone.queryValues = cloneQueryValues(this.queryValues);
    return clone;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof CollectionValue) {
      final CollectionValue condition = (CollectionValue)obj;
      return Equals.equal(condition.getQueryValues(), this.getQueryValues());
    } else {
      return false;
    }
  }

  public FieldDefinition getField() {
    return this.field;
  }

  @Override
  public List<QueryValue> getQueryValues() {
    return this.queryValues;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getValue(final Record record) {
    final List<Object> values = new ArrayList<Object>();
    for (final QueryValue queryValue : this.queryValues) {
      final Object value = queryValue.getValue(record);
      values.add(value);
    }
    return (V)values;
  }

  public List<Object> getValues() {
    CodeTable codeTable = null;
    if (this.field != null) {
      final RecordDefinition recordDefinition = this.field.getRecordDefinition();
      final String fieldName = this.field.getName();
      codeTable = recordDefinition.getCodeTableByFieldName(fieldName);
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
          value = codeTable.getIdentifier(value);
        }
        value = Value.getValue(value);
        values.add(value);
      }
    }
    return values;
  }

  public void setField(final FieldDefinition field) {
    this.field = field;
    if (field == null) {
      this.jdbcField = null;
    } else {
      if (field instanceof JdbcFieldDefinition) {
        this.jdbcField = (JdbcFieldDefinition)field;
      } else {
        this.jdbcField = null;
      }
      for (final QueryValue queryValue : this.queryValues) {
        if (queryValue instanceof Value) {
          final Value value = (Value)queryValue;
          value.setField(field);
        }
      }
    }
  }

  @Override
  public String toString() {
    return "(" + Strings.toString(this.queryValues) + ")";
  }
}
