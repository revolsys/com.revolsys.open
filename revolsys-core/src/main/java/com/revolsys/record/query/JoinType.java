package com.revolsys.record.query;

import org.jeometry.common.io.PathName;

import com.revolsys.record.schema.RecordDefinitionProxy;

public enum JoinType {

  CROSS_JOIN, INNER_JOIN, LEFT_OUTER_JOIN, RIGHT_OUTER_JOIN, FULL_OUTER_JOIN, COMMA(", ");

  static JoinType JOIN = INNER_JOIN;

  private String sql;

  private JoinType() {
    this.sql = name().replace('_', ' ');
  }

  private JoinType(final String sql) {
    this.sql = sql;
  }

  public Join build(final PathName tablePath) {
    return build().tablePath(tablePath);
  }

  public Join build() {
    return new Join(this);
  }

  public Join build(final RecordDefinitionProxy recordDefinition) {
    return build().recordDefinition(recordDefinition);
  }

  public Join build(final TableReference table) {
    return build().table(table);
  }

  public String getSql() {
    return this.sql;
  }

  @Override
  public String toString() {
    return this.sql;
  }
}
