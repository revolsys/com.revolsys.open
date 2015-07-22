package com.revolsys.data.query.functions;

import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.equals.Equals;
import com.revolsys.data.query.Condition;
import com.revolsys.data.query.Query;
import com.revolsys.data.query.QueryValue;
import com.revolsys.data.record.schema.RecordStore;
import com.revolsys.jts.geom.Geometry;

public class GeometryEqual2d extends Condition {
  private QueryValue geometry1Value;

  private QueryValue geometry2Value;

  public GeometryEqual2d(final QueryValue geometry1Value, final QueryValue geometry2Value) {
    this.geometry1Value = geometry1Value;
    this.geometry2Value = geometry2Value;
  }

  @Override
  public void appendDefaultSql(final Query query, final RecordStore recordStore,
    final StringBuilder buffer) {
    buffer.append("ST_EQUALS(");
    if (this.geometry1Value == null) {
      buffer.append("NULL");
    } else {
      this.geometry1Value.appendSql(query, recordStore, buffer);
    }
    buffer.append(", ");
    if (this.geometry1Value == null) {
      buffer.append("NULL");
    } else {
      this.geometry1Value.appendSql(query, recordStore, buffer);
    }
    buffer.append(")");
  }

  @Override
  public int appendParameters(int index, final PreparedStatement statement) {
    if (this.geometry1Value != null) {
      index = this.geometry1Value.appendParameters(index, statement);
    }
    if (this.geometry2Value != null) {
      index = this.geometry2Value.appendParameters(index, statement);
    }
    return index;
  }

  @Override
  public GeometryEqual2d clone() {
    final GeometryEqual2d clone = (GeometryEqual2d)super.clone();
    if (this.geometry1Value != null) {
      clone.geometry1Value = this.geometry1Value.clone();
    }
    if (this.geometry2Value != null) {
      clone.geometry2Value = this.geometry2Value.clone();
    }
    return clone;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof GeometryEqual2d) {
      final GeometryEqual2d condition = (GeometryEqual2d)obj;
      if (Equals.equal(condition.geometry1Value, this.geometry1Value)) {
        if (Equals.equal(condition.geometry2Value, this.geometry1Value)) {
          return true;
        }
      }
    }
    return false;
  }

  public QueryValue getGeometry1Value() {
    return this.geometry1Value;
  }

  public QueryValue getGeometry2Value() {
    return this.geometry2Value;
  }

  @Override
  public List<QueryValue> getQueryValues() {
    return Arrays.asList(this.geometry1Value, this.geometry2Value);
  }

  @Override
  public boolean test(final Map<String, Object> record) {
    final Geometry geometry1 = this.geometry1Value.getValue(record);
    final Geometry geometry2 = this.geometry2Value.getValue(record);
    if (geometry1 == null || geometry2 == null) {
      return false;
    } else {
      return geometry1.equals(2, geometry2);
    }
  }

  @Override
  public String toString() {
    return "ST_EQUALS(" + StringConverterRegistry.toString(this.geometry1Value) + ","
      + StringConverterRegistry.toString(this.geometry2Value) + ")";
  }

}
