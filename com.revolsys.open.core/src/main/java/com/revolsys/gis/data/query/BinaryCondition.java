package com.revolsys.gis.data.query;

import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.model.data.equals.EqualsRegistry;

public class BinaryCondition extends Condition {

  private QueryValue left;

  private final String operator;

  private QueryValue right;

  public BinaryCondition(final QueryValue left, final String operator,
    final QueryValue right) {
    this.left = left;
    this.operator = operator;
    this.right = right;
  }

  public BinaryCondition(final String name, final String operator,
    final Object value) {
    this(new Column(name), operator, new Value(value));
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
  public void appendSql(final StringBuffer buffer) {
    if (left == null) {
      buffer.append("NULL");
    } else {
      left.appendSql(buffer);
    }
    buffer.append(" ");
    buffer.append(operator);
    buffer.append(" ");
    if (right == null) {
      buffer.append("NULL");
    } else {
      right.appendSql(buffer);
    }
  }

  @Override
  public BinaryCondition clone() {
    final BinaryCondition clone = (BinaryCondition)super.clone();
    clone.left = left.clone();
    clone.right = right.clone();
    return clone;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof BinaryCondition) {
      final BinaryCondition condition = (BinaryCondition)obj;
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

  public QueryValue getLeft() {
    return left;
  }

  public String getOperator() {
    return operator;
  }

  @Override
  public List<QueryValue> getQueryValues() {
    return Arrays.asList(left, right);
  }

  public QueryValue getRight() {
    return right;
  }

  @Override
  public String toString() {
    return StringConverterRegistry.toString(left) + " " + operator + " "
      + StringConverterRegistry.toString(right);
  }
}
