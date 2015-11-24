package com.revolsys.record.query;

import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;

import com.revolsys.datatype.DataTypes;
import com.revolsys.equals.Equals;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordStore;

public class BinaryCondition extends Condition {

  private QueryValue left;

  private final String operator;

  private QueryValue right;

  public BinaryCondition(final QueryValue left, final String operator, final QueryValue right) {
    this.left = left;
    this.operator = operator;
    this.right = right;
  }

  public BinaryCondition(final String name, final String operator, final Object value) {
    this(new Column(name), operator, new Value(value));
  }

  @Override
  public void appendDefaultSql(final Query query, final RecordStore recordStore,
    final StringBuilder buffer) {
    if (this.left == null) {
      buffer.append("NULL");
    } else {
      this.left.appendSql(query, recordStore, buffer);
    }
    buffer.append(" ");
    buffer.append(this.operator);
    buffer.append(" ");
    if (this.right == null) {
      buffer.append("NULL");
    } else {
      this.right.appendSql(query, recordStore, buffer);
    }
  }

  @Override
  public int appendParameters(int index, final PreparedStatement statement) {
    FieldDefinition fieldDefinition = null;
    if (this.left != null) {
      index = this.left.appendParameters(index, statement);
      if (this.left instanceof Column) {
        fieldDefinition = ((Column)this.left).getFieldDefinition();
      }
    }
    if (this.right != null) {
      this.right.setFieldDefinition(fieldDefinition);
      index = this.right.appendParameters(index, statement);
    }
    return index;
  }

  @Override
  public BinaryCondition clone() {
    final BinaryCondition clone = (BinaryCondition)super.clone();
    clone.left = this.left.clone();
    clone.right = this.right.clone();
    return clone;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof BinaryCondition) {
      final BinaryCondition condition = (BinaryCondition)obj;
      if (Equals.equal(condition.getLeft(), this.getLeft())) {
        if (Equals.equal(condition.getRight(), this.getRight())) {
          if (Equals.equal(condition.getOperator(), this.getOperator())) {
            return true;
          }
        }
      }
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public <V extends QueryValue> V getLeft() {
    return (V)this.left;
  }

  public String getOperator() {
    return this.operator;
  }

  @Override
  public List<QueryValue> getQueryValues() {
    return Arrays.asList(this.left, this.right);
  }

  @SuppressWarnings("unchecked")
  public <V extends QueryValue> V getRight() {
    return (V)this.right;
  }

  @Override
  public String toString() {
    final Object value = this.left;
    final Object value1 = this.right;
    return DataTypes.toString(value) + " " + this.operator + " " + DataTypes.toString(value1);
  }
}
