package com.revolsys.record.query;

import com.revolsys.record.Record;
import com.revolsys.record.schema.RecordStore;

public class ParenthesisCondition extends AbstractUnaryQueryValue implements Condition {

  public ParenthesisCondition(final Condition condition) {
    super(condition);
  }

  @Override
  public void appendDefaultSql(final Query query, final RecordStore recordStore,
    final StringBuilder buffer) {
    buffer.append("(");
    super.appendDefaultSql(query, recordStore, buffer);
    buffer.append(")");
  }

  @Override
  public ParenthesisCondition clone() {
    final ParenthesisCondition clone = (ParenthesisCondition)super.clone();
    return clone;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof ParenthesisCondition) {
      final ParenthesisCondition condition = (ParenthesisCondition)obj;
      return super.equals(condition);
    }
    return false;
  }

  public Condition getCondition() {
    return getValue();
  }

  @Override
  public boolean test(final Record record) {
    final Condition condition = getCondition();
    return condition.test(record);
  }

  @Override
  public String toString() {
    return "(" + super.toString() + ")";
  }
}
