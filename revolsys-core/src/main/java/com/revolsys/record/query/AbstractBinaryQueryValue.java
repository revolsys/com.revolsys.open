package com.revolsys.record.query;

import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.jeometry.common.data.type.DataType;

import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordStore;

public abstract class AbstractBinaryQueryValue implements QueryValue {

  private QueryValue left;

  private QueryValue right;

  public AbstractBinaryQueryValue(final QueryValue left, final QueryValue right) {
    this.left = left;
    this.right = right;
  }

  protected void appendLeft(final StringBuilder buffer, final Query query,
    final RecordStore recordStore) {
    if (this.left == null) {
      buffer.append("NULL");
    } else {
      this.left.appendSql(query, recordStore, buffer);
    }
    buffer.append(" ");
  }

  @Override
  public int appendParameters(int index, final PreparedStatement statement) {
    if (this.left != null) {
      if (this.right instanceof Column && !(this.left instanceof Column)) {
        final FieldDefinition rightFieldDefinition = ((Column)this.right).getFieldDefinition();
        this.left.setFieldDefinition(rightFieldDefinition);
      }
      index = this.left.appendParameters(index, statement);
    }
    if (this.right != null) {
      if (this.left instanceof Column && !(this.right instanceof Column)) {
        final FieldDefinition rightFieldDefinition = ((Column)this.left).getFieldDefinition();
        this.right.setFieldDefinition(rightFieldDefinition);
      }
      index = this.right.appendParameters(index, statement);
    }
    return index;
  }

  protected void appendRight(final StringBuilder buffer, final Query query,
    final RecordStore recordStore) {
    buffer.append(" ");
    if (this.right == null) {
      buffer.append("NULL");
    } else {
      this.right.appendSql(query, recordStore, buffer);
    }
  }

  @Override
  public AbstractBinaryQueryValue clone() {
    try {
      final AbstractBinaryQueryValue clone = (AbstractBinaryQueryValue)super.clone();
      clone.left = this.left.clone();
      clone.right = this.right.clone();
      return clone;
    } catch (final CloneNotSupportedException e) {
      return null;
    }
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof AbstractBinaryQueryValue) {
      final AbstractBinaryQueryValue binary = (AbstractBinaryQueryValue)obj;
      if (DataType.equal(binary.getLeft(), this.getLeft())) {
        if (DataType.equal(binary.getRight(), this.getRight())) {
          return true;
        }
      }
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public <V extends QueryValue> V getLeft() {
    return (V)this.left;
  }

  @Override
  public List<QueryValue> getQueryValues() {
    return Arrays.asList(this.left, this.right);
  }

  @SuppressWarnings("unchecked")
  public <V extends QueryValue> V getRight() {
    return (V)this.right;
  }

  public void setLeft(final QueryValue left) {
    this.left = left;
  }

  public void setRight(final QueryValue right) {
    this.right = right;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <QV extends QueryValue> QV updateQueryValues(
    final Function<QueryValue, QueryValue> valueHandler) {
    final QueryValue left = valueHandler.apply(this.left);
    final QueryValue right = valueHandler.apply(this.right);
    if (left == this.left && right == this.right) {
      return (QV)this;
    } else {
      final AbstractBinaryQueryValue clone = clone();
      clone.left = left;
      clone.right = right;
      return (QV)clone;
    }
  }
}
