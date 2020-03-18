package com.revolsys.record.query;

import org.jeometry.common.data.type.DataType;

import com.revolsys.record.Record;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.util.Property;

public class ILike extends BinaryCondition {

  public ILike(final QueryValue left, final QueryValue right) {
    super(left, "LIKE", right);
  }

  @Override
  public void appendDefaultSql(final Query query, final RecordStore recordStore,
    final StringBuilder buffer) {
    final QueryValue left = getLeft();
    final QueryValue right = getRight();

    buffer.append("UPPER(CAST(");
    if (left == null) {
      buffer.append("NULL");
    } else {
      left.appendSql(query, recordStore, buffer);
    }
    buffer.append(" AS VARCHAR(4000))) LIKE UPPER(");
    if (right == null) {
      buffer.append("NULL");
    } else {
      right.appendSql(query, recordStore, buffer);
    }
    buffer.append(")");
  }

  @Override
  public ILike clone() {
    return (ILike)super.clone();
  }

  @Override
  public boolean test(final Record record) {
    final QueryValue left = getLeft();
    String value1 = left.getStringValue(record);

    final QueryValue right = getRight();
    String value2 = right.getStringValue(record);

    if (Property.hasValue(value1)) {
      if (Property.hasValue(value2)) {
        value1 = value1.toUpperCase();
        value2 = value2.toUpperCase();
        if (value2.contains("%")) {
          value2 = Like.toPattern(value2);
          if (value1.matches(value2)) {
            return true;
          } else {
            return false;
          }
        } else {
          return DataType.equal(value1, value2);
        }
      } else {
        return false;
      }
    } else {
      return !Property.hasValue(value2);
    }
  }

}
