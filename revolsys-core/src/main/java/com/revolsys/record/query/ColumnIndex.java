package com.revolsys.record.query;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.revolsys.record.Record;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordStore;

public class ColumnIndex implements QueryValue {

  private final int index;

  public ColumnIndex(final int index) {
    this.index = index;
  }

  @Override
  public void appendDefaultSql(final Query query, final RecordStore recordStore,
    final StringBuilder sql) {
    sql.append(this.index);
  }

  @Override
  public int appendParameters(final int index, final PreparedStatement statement) {
    return index;
  }

  @Override
  public ColumnIndex clone() {
    try {
      return (ColumnIndex)super.clone();
    } catch (final CloneNotSupportedException e) {
      return null;
    }
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof ColumnIndex) {
      final ColumnIndex value = (ColumnIndex)obj;
      return value.index == this.index;
    } else {
      return false;
    }
  }

  @Override
  public <V> V getValue(final Record record) {
    return null;
  }

  @Override
  public Object getValueFromResultSet(final ResultSet resultSet, final ColumnIndexes indexes,
    final boolean internStrings) throws SQLException {
    return null;
  }

  @Override
  public void setRecordDefinition(final RecordDefinition recordDefinition) {
  }

  @Override
  public String toString() {
    return Integer.toString(this.index);
  }

}
