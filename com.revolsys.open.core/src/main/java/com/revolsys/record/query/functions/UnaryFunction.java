package com.revolsys.record.query.functions;

import org.jeometry.common.data.type.DataType;

import com.revolsys.record.query.AbstractUnaryQueryValue;
import com.revolsys.record.query.Query;
import com.revolsys.record.query.QueryValue;
import com.revolsys.record.schema.RecordStore;

public abstract class UnaryFunction extends AbstractUnaryQueryValue {

  private final String name;

  public UnaryFunction(final String name, final QueryValue parameter) {
    super(parameter);
    this.name = name;
  }

  @Override
  public void appendDefaultSql(final Query query, final RecordStore recordStore,
    final StringBuilder buffer) {
    buffer.append(getName());
    buffer.append("(");
    super.appendDefaultSql(query, recordStore, buffer);
    buffer.append(")");
  }

  @Override
  public UnaryFunction clone() {
    return (UnaryFunction)super.clone();
  }

  @Override
  public boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (other instanceof UnaryFunction) {
      final UnaryFunction function = (UnaryFunction)other;
      if (DataType.equal(function.getName(), getName())) {
        return super.equals(function);
      }
    }
    return false;
  }

  public String getName() {
    return this.name;
  }

  public QueryValue getParameter() {
    return super.getValue();
  }

  @Override
  public String toString() {
    return getName() + "(" + super.toString() + ")";
  }
}
