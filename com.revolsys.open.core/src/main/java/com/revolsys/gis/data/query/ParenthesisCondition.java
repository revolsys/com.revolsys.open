package com.revolsys.gis.data.query;

import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.model.data.equals.EqualsRegistry;

public class ParenthesisCondition extends Condition {

  private Condition condition;

  public ParenthesisCondition(final Condition condition) {
    this.condition = condition;
  }

  @Override
  public boolean accept(final Map<String, Object> record) {
    return condition.accept(record);
  }

  @Override
  public void appendDefaultSql(Query query,
    final DataObjectStore dataStore, final StringBuffer buffer) {
    buffer.append("(");
    condition.appendSql(query, dataStore, buffer);
    buffer.append(")");
  }

  @Override
  public int appendParameters(final int index, final PreparedStatement statement) {
    return condition.appendParameters(index, statement);
  }

  @Override
  public ParenthesisCondition clone() {
    final ParenthesisCondition clone = (ParenthesisCondition)super.clone();
    clone.condition = condition.clone();
    return clone;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof ParenthesisCondition) {
      final ParenthesisCondition condition = (ParenthesisCondition)obj;
      if (EqualsRegistry.equal(condition.getCondition(), this.getCondition())) {
        return true;
      }
    }
    return false;
  }

  public Condition getCondition() {
    return condition;
  }

  @Override
  public List<QueryValue> getQueryValues() {
    return Collections.<QueryValue> singletonList(condition);
  }

  @Override
  public <V> V getValue(final Map<String, Object> record) {
    return condition.getValue(record);
  }

  @Override
  public String toString() {
    return "(" + getCondition() + ")";
  }
}
