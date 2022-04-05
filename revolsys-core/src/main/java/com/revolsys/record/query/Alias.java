package com.revolsys.record.query;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.exception.Exceptions;

import com.revolsys.collection.map.MapEx;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordStore;

public class Alias implements QueryValue {

  private final String alias;

  private final QueryValue value;

  public Alias(final QueryValue values, final CharSequence alias) {
    this.value = values;
    this.alias = alias.toString();
  }

  protected void appendAlias(final Appendable sql) {
    try {
      sql.append('"');
      sql.append(this.alias);
      sql.append('"');
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public void appendDefaultSelect(final Query query, final RecordStore recordStore,
    final Appendable sql) {
    this.value.appendDefaultSelect(query, recordStore, sql);
    try {
      sql.append(" as ");
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
    appendAlias(sql);
  }

  @Override
  public void appendDefaultSql(final Query query, final RecordStore recordStore,
    final Appendable sql) {
    try {
      sql.append(this.alias);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public int appendParameters(final int index, final PreparedStatement statement) {
    return index;
  }

  @Override
  public Alias clone() {
    try {
      return (Alias)super.clone();
    } catch (final CloneNotSupportedException e) {
      return null;
    }
  }

  @Override
  public Alias clone(final TableReference oldTable, final TableReference newTable) {
    if (oldTable != newTable) {
      final QueryValue clonedValue = this.value.clone(oldTable, newTable);
      return new Alias(clonedValue, this.alias);
    }
    return clone();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof Alias) {
      final Alias alias = (Alias)obj;
      if (this.value.equals(alias.value)) {
        return DataType.equal(alias.alias, alias);
      }
    }
    return false;
  }

  @Override
  public int getFieldIndex() {
    return this.value.getFieldIndex();
  }

  @Override
  public String getStringValue(final MapEx record) {
    return this.value.getStringValue(record);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V> V getValue(final MapEx record) {
    if (record == null) {
      return null;
    } else {
      return (V)this.value.getValue(record);
    }
  }

  @Override
  public Object getValueFromResultSet(final RecordDefinition recordDefinition,
    final ResultSet resultSet, final ColumnIndexes indexes, final boolean internStrings)
    throws SQLException {
    return this.value.getValueFromResultSet(recordDefinition, resultSet, indexes, internStrings);
  }

  @Override
  public String toString() {
    final StringBuilder sql = new StringBuilder();
    this.value.appendDefaultSelect(null, null, sql);
    sql.append(" as ");
    appendAlias(sql);
    return sql.toString();
  }

}
