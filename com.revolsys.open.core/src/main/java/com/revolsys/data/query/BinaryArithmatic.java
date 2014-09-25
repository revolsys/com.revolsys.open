package com.revolsys.data.query;

import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.equals.EqualsRegistry;
import com.revolsys.data.record.schema.RecordStore;

public abstract class BinaryArithmatic extends QueryValue {

  private QueryValue left;

  private final String operator;

  private QueryValue right;

  public BinaryArithmatic(final QueryValue left, final String operator,
    final QueryValue right) {
    this.left = left;
    this.operator = operator;
    this.right = right;
  }

  public BinaryArithmatic(final String name, final String operator,
    final Object value) {
    this(new Column(name), operator, new Value(value));
  }

  @Override
  public void appendDefaultSql(Query query,
    final RecordStore recordStore, final StringBuilder buffer) {
    if (left == null) {
      buffer.append("NULL");
    } else {
      left.appendSql(query, recordStore, buffer);
    }
    buffer.append(" ");
    buffer.append(operator);
    buffer.append(" ");
    if (right == null) {
      buffer.append("NULL");
    } else {
      right.appendSql(query, recordStore, buffer);
    }
  }

  @Override
  public int appendParameters(int index, final PreparedStatement statement) {
    if (left != null) {
      index = left.appendParameters(index, statement);
    }
    if (right != null) {
      index = right.appendParameters(index, statement);
    }
    return index;
  }

  @Override
  public BinaryArithmatic clone() {
    final BinaryArithmatic clone = (BinaryArithmatic)super.clone();
    clone.left = left.clone();
    clone.right = right.clone();
    return clone;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof BinaryArithmatic) {
      final BinaryArithmatic condition = (BinaryArithmatic)obj;
      if (EqualsRegistry.equal(condition.getLeft(), this.getLeft())) {
        if (EqualsRegistry.equal(condition.getRight(), this.getRight())) {
          if (EqualsRegistry.equal(condition.getOperator(), this.getOperator())) {
            return true;
          }
        }
      }
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public <V extends QueryValue> V getLeft() {
    return (V)left;
  }

  public String getOperator() {
    return operator;
  }

  @Override
  public List<QueryValue> getQueryValues() {
    return Arrays.asList(left, right);
  }

  @SuppressWarnings("unchecked")
  public <V extends QueryValue> V getRight() {
    return (V)right;
  }

  @Override
  public String toString() {
    return StringConverterRegistry.toString(left) + " " + operator + " "
      + StringConverterRegistry.toString(right);
  }
}
