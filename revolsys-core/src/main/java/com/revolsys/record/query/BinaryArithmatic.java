package com.revolsys.record.query;

import java.io.IOException;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.exception.Exceptions;

import com.revolsys.record.schema.RecordStore;

public abstract class BinaryArithmatic extends AbstractBinaryQueryValue {

  private final String operator;

  public BinaryArithmatic(final QueryValue left, final String operator, final QueryValue right) {
    super(left, right);
    this.operator = operator;
  }

  public BinaryArithmatic(final String name, final String operator, final Object value) {
    this(new Column(name), operator, Value.newValue(value));
  }

  @Override
  public void appendDefaultSql(final Query query, final RecordStore recordStore,
    final Appendable buffer) {
    try {
      appendLeft(buffer, query, recordStore);
      buffer.append(this.operator);
      appendRight(buffer, query, recordStore);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public BinaryArithmatic clone() {
    return (BinaryArithmatic)super.clone();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof BinaryArithmatic) {
      final BinaryArithmatic condition = (BinaryArithmatic)obj;
      if (DataType.equal(condition.getOperator(), getOperator())) {
        return super.equals(condition);
      }
    }
    return false;
  }

  public String getOperator() {
    return this.operator;
  }

  @Override
  public String toString() {
    final Object left = getLeft();
    final Object right = getRight();
    return DataTypes.toString(left) + " " + this.operator + " " + DataTypes.toString(right);
  }

}
