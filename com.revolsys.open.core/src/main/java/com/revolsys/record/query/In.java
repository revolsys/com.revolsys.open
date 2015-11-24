package com.revolsys.record.query;

import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.revolsys.datatype.DataTypes;
import com.revolsys.equals.Equals;
import com.revolsys.record.Record;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordStore;

public class In extends Condition {

  private QueryValue left;

  private CollectionValue values;

  public In(final FieldDefinition field, final Collection<? extends Object> values) {
    this(field.getName(), new CollectionValue(field, values));
  }

  public In(final QueryValue left, final CollectionValue values) {
    this.left = left;
    if (left instanceof Column) {
      final Column column = (Column)left;
      final FieldDefinition field = column.getFieldDefinition();
      if (field != null) {
        values.setFieldDefinition(field);
      }
    }
    this.values = values;
  }

  public In(final String name, final Collection<? extends Object> values) {
    this(new Column(name), new CollectionValue(values));
  }

  public In(final String name, final CollectionValue values) {
    this(new Column(name), values);
  }

  public In(final String name, final Object... values) {
    this(name, Arrays.asList(values));
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
    buffer.append(" IN ");
    this.values.appendSql(query, recordStore, buffer);
  }

  @Override
  public int appendParameters(int index, final PreparedStatement statement) {
    if (this.left != null) {
      index = this.left.appendParameters(index, statement);
    }
    if (this.values != null) {
      index = this.values.appendParameters(index, statement);
    }
    return index;
  }

  @Override
  public In clone() {
    final In clone = (In)super.clone();
    clone.left = this.left.clone();
    clone.values = this.values.clone();
    return clone;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof In) {
      final In in = (In)obj;
      if (Equals.equal(in.getLeft(), this.getLeft())) {
        if (Equals.equal(in.getValues(), this.getValues())) {
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
    return Arrays.asList(this.left, this.values);
  }

  public CollectionValue getValues() {
    return this.values;
  }

  @Override
  public boolean test(final Record record) {
    final QueryValue left = getLeft();
    final Object value = left.getValue(record);

    final CollectionValue right = getValues();
    final List<Object> allowedValues = right.getValues();

    for (final Object allowedValue : allowedValues) {
      if (Equals.equal(value, allowedValue)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    final Object value = this.left;
    final Object value1 = this.values;
    return DataTypes.toString(value) + " IN " + DataTypes.toString(value1);
  }
}
