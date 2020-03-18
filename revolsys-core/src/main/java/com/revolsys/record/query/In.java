package com.revolsys.record.query;

import java.util.Arrays;
import java.util.Collection;

import org.jeometry.common.data.type.DataTypes;

import com.revolsys.record.Record;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordStore;

public class In extends AbstractBinaryQueryValue implements Condition {

  public In(final FieldDefinition field, final Collection<? extends Object> values) {
    this(new Column(field), new CollectionValue(field, values));
  }

  public In(final QueryValue left, final CollectionValue values) {
    super(left, values);
    if (left instanceof Column) {
      final Column column = (Column)left;
      final FieldDefinition field = column.getFieldDefinition();
      if (field != null) {
        values.setFieldDefinition(field);
      }
    }
  }

  public In(final String name, final Collection<? extends Object> values) {
    this(new Column(name), new CollectionValue(values));
  }

  public In(final String name, final Object... values) {
    this(name, Arrays.asList(values));
  }

  @Override
  public void appendDefaultSql(final Query query, final RecordStore recordStore,
    final StringBuilder buffer) {
    super.appendLeft(buffer, query, recordStore);
    buffer.append("IN");
    super.appendRight(buffer, query, recordStore);
  }

  @Override
  public In clone() {
    final In clone = (In)super.clone();
    return clone;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof In) {
      final In in = (In)obj;
      return super.equals(in);
    }
    return false;
  }

  public CollectionValue getValues() {
    return (CollectionValue)getRight();
  }

  @Override
  public boolean isEmpty() {
    return getValues().isEmpty();
  }

  @Override
  public boolean test(final Record record) {
    final QueryValue left = getLeft();
    final Object value = left.getValue(record);

    final CollectionValue right = getValues();
    return right.containsValue(value);
  }

  @Override
  public String toString() {
    final Object value = getLeft();
    final Object value1 = getRight();
    return DataTypes.toString(value) + " IN " + DataTypes.toString(value1);
  }
}
