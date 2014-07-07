package com.revolsys.data.query.functions;

import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.equals.EqualsRegistry;
import com.revolsys.data.io.RecordStore;
import com.revolsys.data.query.Condition;
import com.revolsys.data.query.Query;
import com.revolsys.data.query.QueryValue;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;

public class EnvelopeIntersects extends Condition {
  private QueryValue boundingBox1Value;

  private QueryValue boundingBox2Value;

  public EnvelopeIntersects(final QueryValue boundingBox1Value,
    final QueryValue boundingBox2Value) {
    this.boundingBox1Value = boundingBox1Value;
    this.boundingBox2Value = boundingBox2Value;
  }

  @Override
  public boolean accept(final Map<String, Object> record) {
    final BoundingBox boundingBox1 = getBoundingBox(boundingBox1Value, record);
    final BoundingBox boundingBox2 = getBoundingBox(boundingBox2Value, record);
    if (boundingBox1 == null || boundingBox2 == null) {
      return false;
    } else {
      return boundingBox1.intersects(boundingBox2);
    }
  }

  @Override
  public void appendDefaultSql(final Query query,
    final RecordStore recordStore, final StringBuffer buffer) {
    buffer.append("ST_INTERSECTS(");
    if (boundingBox1Value == null) {
      buffer.append("NULL");
    } else {
      boundingBox1Value.appendSql(query, recordStore, buffer);
    }
    buffer.append(", ");
    if (boundingBox1Value == null) {
      buffer.append("NULL");
    } else {
      boundingBox1Value.appendSql(query, recordStore, buffer);
    }
    buffer.append(")");
  }

  @Override
  public int appendParameters(int index, final PreparedStatement statement) {
    if (boundingBox1Value != null) {
      index = boundingBox1Value.appendParameters(index, statement);
    }
    if (boundingBox2Value != null) {
      index = boundingBox2Value.appendParameters(index, statement);
    }
    return index;
  }

  @Override
  public EnvelopeIntersects clone() {
    final EnvelopeIntersects clone = (EnvelopeIntersects)super.clone();
    if (boundingBox1Value != null) {
      clone.boundingBox1Value = boundingBox1Value.clone();
    }
    if (boundingBox2Value != null) {
      clone.boundingBox2Value = boundingBox2Value.clone();
    }
    return clone;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof EnvelopeIntersects) {
      final EnvelopeIntersects condition = (EnvelopeIntersects)obj;
      if (EqualsRegistry.equal(condition.boundingBox1Value,
        this.boundingBox1Value)) {
        if (EqualsRegistry.equal(condition.boundingBox2Value, boundingBox1Value)) {
          return true;
        }
      }
    }
    return false;
  }

  private BoundingBox getBoundingBox(final QueryValue queryValue,
    final Map<String, Object> record) {
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
    return boundingBox1Value;
  }

  public QueryValue getBoundingBox2Value() {
    return boundingBox2Value;
  }

  @Override
  public List<QueryValue> getQueryValues() {
    return Arrays.asList(boundingBox1Value, boundingBox2Value);
  }

  @Override
  public String toString() {
    return "ST_INTERSECTS("
      + StringConverterRegistry.toString(boundingBox1Value) + ","
      + StringConverterRegistry.toString(boundingBox2Value) + ")";
  }

}
