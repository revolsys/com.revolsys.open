package com.revolsys.data.query.functions;

import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.equals.EqualsRegistry;
import com.revolsys.data.query.Condition;
import com.revolsys.data.query.Query;
import com.revolsys.data.query.QueryValue;
import com.revolsys.data.record.schema.RecordStore;
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
    if (this.geometry1Value == null || this.geometry2Value == null
        || this.distanceValue == null) {
      return false;
    } else {
      final Geometry geometry1 = this.geometry1Value.getValue(record);
      final Geometry geometry2 = this.geometry2Value.getValue(record);
      ;
      final Number acceptDistance = this.distanceValue.getValue(record);
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
    final RecordStore recordStore, final StringBuilder sql) {
    if (this.geometry1Value == null || this.geometry2Value == null
        || this.distanceValue == null) {
      sql.append("1 = 0");
    } else {
      sql.append("ST_DWithin(");
      this.geometry1Value.appendSql(query, recordStore, sql);
      sql.append(", ");
      this.geometry2Value.appendSql(query, recordStore, sql);
      sql.append(", ");
      this.distanceValue.appendSql(query, recordStore, sql);
      sql.append(")");
    }
  }

  @Override
  public int appendParameters(int index, final PreparedStatement statement) {
    if (this.geometry1Value != null) {
      index = this.geometry1Value.appendParameters(index, statement);
    }
    if (this.geometry2Value != null) {
      index = this.geometry2Value.appendParameters(index, statement);
    }
    if (this.distanceValue != null) {
      index = this.distanceValue.appendParameters(index, statement);
    }
    return index;
  }

  @Override
  public WithinDistance clone() {
    final WithinDistance clone = (WithinDistance)super.clone();
    if (this.geometry1Value != null) {
      clone.geometry1Value = this.geometry1Value.clone();
    }
    if (this.geometry2Value != null) {
      clone.geometry2Value = this.geometry2Value.clone();
    }
    if (this.distanceValue != null) {
      clone.distanceValue = this.distanceValue.clone();
    }
    return clone;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof WithinDistance) {
      final WithinDistance condition = (WithinDistance)obj;
      if (EqualsRegistry.equal(condition.geometry1Value, this.geometry1Value)) {
        if (EqualsRegistry.equal(condition.geometry2Value, this.geometry1Value)) {
          if (EqualsRegistry.equal(condition.distanceValue, this.distanceValue)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public QueryValue getDistanceValue() {
    return this.distanceValue;
  }

  public QueryValue getGeometry1Value() {
    return this.geometry1Value;
  }

  public QueryValue getGeometry2Value() {
    return this.geometry2Value;
  }

  @Override
  public List<QueryValue> getQueryValues() {
    return Arrays.asList(this.geometry1Value, this.geometry2Value,
      this.distanceValue);
  }

  @Override
  public String toString() {
    return "DWithin(" + StringConverterRegistry.toString(this.geometry1Value)
        + "," + StringConverterRegistry.toString(this.geometry2Value) + ","
        + StringConverterRegistry.toString(this.distanceValue) + ")";
  }

}
