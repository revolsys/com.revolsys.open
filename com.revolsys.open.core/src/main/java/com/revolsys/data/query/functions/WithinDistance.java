package com.revolsys.data.query.functions;

import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.equals.EqualsRegistry;
import com.revolsys.data.io.DataObjectStore;
import com.revolsys.data.query.Condition;
import com.revolsys.data.query.Query;
import com.revolsys.data.query.QueryValue;
import com.revolsys.jts.geom.Geometry;

public class WithinDistance extends Condition {
  private QueryValue geometry1Value;

  private QueryValue geometry2Value;

  private QueryValue distanceValue;

  public WithinDistance(final QueryValue geometry1Value,
    final QueryValue geometry2Value, final QueryValue distanceValue) {
    this.geometry1Value = geometry1Value;
    this.geometry2Value = geometry2Value;
    this.distanceValue = distanceValue;
  }

  @Override
  public boolean accept(final Map<String, Object> record) {
    if (geometry1Value == null || geometry2Value == null
      || distanceValue == null) {
      return false;
    } else {
      final Geometry geometry1 = geometry1Value.getValue(record);
      final Geometry geometry2 = geometry2Value.getValue(record);
      ;
      final Number acceptDistance = distanceValue.getValue(record);
      if (acceptDistance == null || geometry1 == null || geometry2 == null) {
        return false;
      } else {
        final double distance = geometry1.distance(geometry2);
        return distance <= acceptDistance.doubleValue();
      }
    }
  }

  @Override
  public void appendDefaultSql(final Query query,
    final DataObjectStore dataStore, final StringBuffer sql) {
    sql.append("ST_DWithin(");
    if (geometry1Value == null) {
      sql.append("NULL");
    } else {
      geometry1Value.appendSql(query, dataStore, sql);
    }
    sql.append(", ");
    if (geometry2Value == null) {
      sql.append("NULL");
    } else {
      geometry2Value.appendSql(query, dataStore, sql);
    }
    sql.append(", ");
    if (distanceValue == null) {
      sql.append("0");
    } else {
      distanceValue.appendSql(query, dataStore, sql);
    }
    sql.append(")");
  }

  @Override
  public int appendParameters(int index, final PreparedStatement statement) {
    if (geometry1Value != null) {
      index = geometry1Value.appendParameters(index, statement);
    }
    if (geometry2Value != null) {
      index = geometry2Value.appendParameters(index, statement);
    }
    if (distanceValue != null) {
      index = distanceValue.appendParameters(index, statement);
    }
    return index;
  }

  @Override
  public WithinDistance clone() {
    final WithinDistance clone = (WithinDistance)super.clone();
    if (geometry1Value != null) {
      clone.geometry1Value = geometry1Value.clone();
    }
    if (geometry2Value != null) {
      clone.geometry2Value = geometry2Value.clone();
    }
    if (distanceValue != null) {
      clone.distanceValue = distanceValue.clone();
    }
    return clone;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof WithinDistance) {
      final WithinDistance condition = (WithinDistance)obj;
      if (EqualsRegistry.equal(condition.geometry1Value, this.geometry1Value)) {
        if (EqualsRegistry.equal(condition.geometry2Value, geometry1Value)) {
          if (EqualsRegistry.equal(condition.distanceValue, this.distanceValue)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public QueryValue getDistanceValue() {
    return distanceValue;
  }

  public QueryValue getGeometry1Value() {
    return geometry1Value;
  }

  public QueryValue getGeometry2Value() {
    return geometry2Value;
  }

  @Override
  public List<QueryValue> getQueryValues() {
    return Arrays.asList(geometry1Value, geometry2Value, distanceValue);
  }

  @Override
  public String toString() {
    return "DWithin(" + StringConverterRegistry.toString(geometry1Value) + ","
      + StringConverterRegistry.toString(geometry2Value) + ","
      + StringConverterRegistry.toString(distanceValue) + ")";
  }

}
