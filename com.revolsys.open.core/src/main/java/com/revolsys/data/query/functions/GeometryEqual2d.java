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
import com.revolsys.jts.geom.Geometry;

public class GeometryEqual2d extends Condition {
  private QueryValue geometry1Value;

  private QueryValue geometry2Value;

  public GeometryEqual2d(final QueryValue geometry1Value,
    final QueryValue geometry2Value) {
    this.geometry1Value = geometry1Value;
    this.geometry2Value = geometry2Value;
  }

  @Override
  public boolean accept(final Map<String, Object> record) {
    final Geometry geometry1 = geometry1Value.getValue(record);
    final Geometry geometry2 = geometry2Value.getValue(record);
    if (geometry1 == null || geometry2 == null) {
      return false;
    } else {
      return geometry1.equals(2, geometry2);
    }
  }

  @Override
  public void appendDefaultSql(final Query query,
    final RecordStore recordStore, final StringBuffer buffer) {
    buffer.append("ST_EQUALS(");
    if (geometry1Value == null) {
      buffer.append("NULL");
    } else {
      geometry1Value.appendSql(query, recordStore, buffer);
    }
    buffer.append(", ");
    if (geometry1Value == null) {
      buffer.append("NULL");
    } else {
      geometry1Value.appendSql(query, recordStore, buffer);
    }
    buffer.append(")");
  }

  @Override
  public int appendParameters(int index, final PreparedStatement statement) {
    if (geometry1Value != null) {
      index = geometry1Value.appendParameters(index, statement);
    }
    if (geometry2Value != null) {
      index = geometry2Value.appendParameters(index, statement);
    }
    return index;
  }

  @Override
  public GeometryEqual2d clone() {
    final GeometryEqual2d clone = (GeometryEqual2d)super.clone();
    if (geometry1Value != null) {
      clone.geometry1Value = geometry1Value.clone();
    }
    if (geometry2Value != null) {
      clone.geometry2Value = geometry2Value.clone();
    }
    return clone;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof GeometryEqual2d) {
      final GeometryEqual2d condition = (GeometryEqual2d)obj;
      if (EqualsRegistry.equal(condition.geometry1Value, this.geometry1Value)) {
        if (EqualsRegistry.equal(condition.geometry2Value, geometry1Value)) {
          return true;
        }
      }
    }
    return false;
  }

  public QueryValue getGeometry1Value() {
    return geometry1Value;
  }

  public QueryValue getGeometry2Value() {
    return geometry2Value;
  }

  @Override
  public List<QueryValue> getQueryValues() {
    return Arrays.asList(geometry1Value, geometry2Value);
  }

  @Override
  public String toString() {
    return "ST_EQUALS(" + StringConverterRegistry.toString(geometry1Value)
      + "," + StringConverterRegistry.toString(geometry2Value) + ")";
  }

}
