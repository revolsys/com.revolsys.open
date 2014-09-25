package com.revolsys.data.query;

import java.sql.PreparedStatement;
import java.util.Map;

import com.revolsys.data.equals.EqualsRegistry;
import com.revolsys.data.record.schema.RecordStore;

public class Cast extends QueryValue {
  private final QueryValue value;

  private final String dataType;

  public Cast(final QueryValue queryValue, final String dataType) {
    this.value = queryValue;
    this.dataType = dataType;
  }

  public Cast(final String name, final String dataType) {
    this(new Column(name), dataType);
  }

  @Override
  public int appendParameters(final int index, final PreparedStatement statement) {
    return value.appendParameters(index, statement);
  }

  @Override
  public void appendDefaultSql(Query query, RecordStore recordStore, final StringBuilder buffer) {
    buffer.append("CAST(");
    value.appendSql(query, recordStore, buffer);
    buffer.append(" AS ");
    buffer.append(dataType);
    buffer.append(")");
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof Cast) {
      final Cast condition = (Cast)obj;
      if (EqualsRegistry.equal(condition.getValue(), this.getValue())) {
        if (EqualsRegistry.equal(condition.getDataType(), this.getDataType())) {
          return true;
        }
      }
    }
    return false;
  }

  public String getDataType() {
    return dataType;
  }

  @Override
  public String getStringValue(final Map<String, Object> record) {
    return value.getStringValue(record);
  }

  public QueryValue getValue() {
    return value;
  }

  @Override
  public <V> V getValue(final Map<String, Object> record) {
    return value.getValue(record);
  }

  @Override
  public String toString() {
    final StringBuilder buffer = new StringBuilder();
    buffer.append("CAST(");
    buffer.append(value);
    buffer.append(" AS ");
    buffer.append(dataType);
    buffer.append(")");
    return buffer.toString();
  }
}
