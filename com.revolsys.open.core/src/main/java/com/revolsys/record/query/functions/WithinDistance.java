package com.revolsys.record.query.functions;

import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;

import com.revolsys.converter.string.StringConverter;
import com.revolsys.equals.Equals;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.record.Record;
import com.revolsys.record.query.Condition;
import com.revolsys.record.query.Query;
import com.revolsys.record.query.QueryValue;
import com.revolsys.record.schema.RecordStore;

public class WithinDistance extends Condition {
  private QueryValue distanceValue;

  private QueryValue geometry1Value;

  private QueryValue geometry2Value;

  public WithinDistance(final QueryValue geometry1Value, final QueryValue geometry2Value,
    final QueryValue distanceValue) {
    this.geometry1Value = geometry1Value;
    this.geometry2Value = geometry2Value;
    this.distanceValue = distanceValue;
  }

  @Override
  public void appendDefaultSql(final Query query, final RecordStore recordStore,
    final StringBuilder sql) {
    if (this.geometry1Value == null || this.geometry2Value == null || this.distanceValue == null) {
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
      if (Equals.equal(condition.geometry1Value, this.geometry1Value)) {
        if (Equals.equal(condition.geometry2Value, this.geometry1Value)) {
          if (Equals.equal(condition.distanceValue, this.distanceValue)) {
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
    return Arrays.asList(this.geometry1Value, this.geometry2Value, this.distanceValue);
  }

  @Override
  public boolean test(final Record record) {
    if (this.geometry1Value == null || this.geometry2Value == null || this.distanceValue == null) {
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
  public String toString() {
    return "DWithin(" + StringConverter.toString(this.geometry1Value) + ","
      + StringConverter.toString(this.geometry2Value) + ","
      + StringConverter.toString(this.distanceValue) + ")";
  }

}
