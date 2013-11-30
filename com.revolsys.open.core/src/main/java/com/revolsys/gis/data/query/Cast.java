package com.revolsys.gis.data.query;

import java.sql.PreparedStatement;

import com.revolsys.gis.model.data.equals.EqualsRegistry;

public class Cast extends Condition {
  private final Condition condition;

  private final String dataType;

  public Cast(final Condition condition, final String dataType) {
    this.condition = condition;
    this.dataType = dataType;
  }

  public Cast(final String name, final String dataType) {
    this(new Column(name), dataType);
  }

  @Override
  public int appendParameters(final int index, final PreparedStatement statement) {
    return condition.appendParameters(index, statement);
  }

  @Override
  public void appendSql(final StringBuffer buffer) {
    buffer.append("CAST(");
    condition.appendSql(buffer);
    buffer.append(" AS ");
    buffer.append(dataType);
    buffer.append(")");
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof Cast) {
      final Cast condition = (Cast)obj;
      if (EqualsRegistry.equal(condition.getCondition(), this.getCondition())) {
        if (EqualsRegistry.equal(condition.getDataType(), this.getDataType())) {
          return true;
        }
      }
    }
    return false;
  }

  public Condition getCondition() {
    return condition;
  }

  public String getDataType() {
    return dataType;
  }

}
