package com.revolsys.record.query.functions;

import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.record.Record;
import com.revolsys.record.query.Condition;
import com.revolsys.record.query.Query;
import com.revolsys.record.query.QueryValue;
import com.revolsys.record.schema.RecordStore;

public class EnvelopeIntersects extends Condition {
  private QueryValue boundingBox1Value;

  private QueryValue boundingBox2Value;

  public EnvelopeIntersects(final QueryValue boundingBox1Value,
    final QueryValue boundingBox2Value) {
    this.boundingBox1Value = boundingBox1Value;
    this.boundingBox2Value = boundingBox2Value;
  }

  @Override
  public void appendDefaultSql(final Query query, final RecordStore recordStore,
    final StringBuilder buffer) {
    buffer.append("ST_INTERSECTS(");
    if (this.boundingBox1Value == null) {
      buffer.append("NULL");
    } else {
      this.boundingBox1Value.appendSql(query, recordStore, buffer);
    }
    buffer.append(", ");
    if (this.boundingBox2Value == null) {
      buffer.append("NULL");
    } else {
      this.boundingBox2Value.appendSql(query, recordStore, buffer);
    }
    buffer.append(")");
  }

  @Override
  public int appendParameters(int index, final PreparedStatement statement) {
    if (this.boundingBox1Value != null) {
      index = this.boundingBox1Value.appendParameters(index, statement);
    }
    if (this.boundingBox2Value != null) {
      index = this.boundingBox2Value.appendParameters(index, statement);
    }
    return index;
  }

  @Override
  public EnvelopeIntersects clone() {
    final EnvelopeIntersects clone = (EnvelopeIntersects)super.clone();
    if (this.boundingBox1Value != null) {
      clone.boundingBox1Value = this.boundingBox1Value.clone();
    }
    if (this.boundingBox2Value != null) {
      clone.boundingBox2Value = this.boundingBox2Value.clone();
    }
    return clone;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof EnvelopeIntersects) {
      final EnvelopeIntersects condition = (EnvelopeIntersects)obj;
      if (DataType.equal(condition.boundingBox1Value, this.boundingBox1Value)) {
        if (DataType.equal(condition.boundingBox2Value, this.boundingBox1Value)) {
          return true;
        }
      }
    }
    return false;
  }

  private BoundingBox getBoundingBox(final QueryValue queryValue, final Record record) {
    if (queryValue == null) {
      return null;
    } else {
      final Object value = queryValue.getValue(record);
      if (value instanceof BoundingBox) {
        return (BoundingBox)value;
      } else if (value instanceof Geometry) {
        final Geometry geometry = (Geometry)value;
        return geometry.getBoundingBox();
      } else {
        return null;
      }
    }
  }

  public QueryValue getBoundingBox1Value() {
    return this.boundingBox1Value;
  }

  public QueryValue getBoundingBox2Value() {
    return this.boundingBox2Value;
  }

  @Override
  public List<QueryValue> getQueryValues() {
    return Arrays.asList(this.boundingBox1Value, this.boundingBox2Value);
  }

  @Override
  public boolean test(final Record record) {
    final BoundingBox boundingBox1 = getBoundingBox(this.boundingBox1Value, record);
    final BoundingBox boundingBox2 = getBoundingBox(this.boundingBox2Value, record);
    if (boundingBox1 == null || boundingBox2 == null) {
      return false;
    } else {
      return boundingBox1.intersects(boundingBox2);
    }
  }

  @Override
  public String toString() {
    final Object value = this.boundingBox1Value;
    final Object value1 = this.boundingBox2Value;
    return "ST_INTERSECTS(" + DataTypes.toString(value) + "," + DataTypes.toString(value1) + ")";
  }

}
