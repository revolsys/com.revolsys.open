package com.revolsys.record.query;

import java.sql.PreparedStatement;

import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.io.PathName;

import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.record.Record;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.record.schema.RecordStore;

public class Join implements QueryValue {

  private final JoinType joinType;

  private PathName tablePath;

  private String qualifiedTableName;

  private String alias;

  private RecordDefinition recordDefinition;

  private Condition condition;

  public Join(final JoinType joinType) {
    this.joinType = joinType;
  }

  public Join alias(final String alias) {
    this.alias = alias;
    return this;
  }

  @Override
  public void appendDefaultSql(final Query query, final RecordStore recordStore,
    final StringBuilder sql) {
    sql.append(' ');
    sql.append(this.joinType);
    sql.append(' ');
    sql.append(this.qualifiedTableName);
    if (this.alias != null) {
      sql.append(" as \"");
      sql.append(this.alias);
      sql.append('"');
    }
    if (this.condition != null) {
      sql.append(" ON ");
      this.condition.appendSql(query, recordStore, sql);
    }
  }

  @Override
  public int appendParameters(final int index, final PreparedStatement statement) {
    return this.condition.appendParameters(index, statement);
  }

  public void appendSql(final StringBuilder sql) {
    sql.append(' ');
    sql.append(this.joinType);
    sql.append(' ');
    sql.append(this.qualifiedTableName);
    if (this.alias != null) {
      sql.append(" as \"");
      sql.append(this.alias);
      sql.append('"');
    }
    if (this.condition != null) {
      sql.append(" ON ");
      sql.append(this.condition);
    }
  }

  @Override
  public QueryValue clone() {
    try {
      final Join join = (Join)super.clone();
      join.condition = this.condition.clone();
      return join;
    } catch (final CloneNotSupportedException e) {
      throw Exceptions.wrap(e);
    }
  }

  public Join condition(final Condition condition) {
    this.condition = condition;
    return this;
  }

  public Condition getCondition() {
    return this.condition;
  }

  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  public PathName getTableName() {
    return this.tablePath;
  }

  @Override
  public <V> V getValue(final Record record) {
    return null;
  }

  public Join recordDefinition(final RecordDefinitionProxy recordDefinition) {
    this.recordDefinition = recordDefinition.getRecordDefinition();
    this.tablePath = this.recordDefinition.getPathName();
    this.qualifiedTableName = this.recordDefinition.getQualifiedTableName();
    return this;
  }

  public Join tablePath(final PathName tableName) {
    this.tablePath = tableName;
    this.qualifiedTableName = JdbcUtils.getQualifiedTableName(this.tablePath);
    return this;
  }

  public String toSql() {
    final StringBuilder string = new StringBuilder();
    appendSql(string);
    return string.toString();
  }

  @Override
  public String toString() {
    return toSql();
  }
}
