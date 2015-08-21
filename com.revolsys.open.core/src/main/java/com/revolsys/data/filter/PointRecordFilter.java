package com.revolsys.data.filter;

import java.util.function.Predicate;

import com.revolsys.data.record.Record;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.Point;

public class PointRecordFilter implements Predicate<Record> {
  public static final PointRecordFilter FILTER = new PointRecordFilter();

  private PointRecordFilter() {
  }

  @Override
  public boolean test(final Record object) {
    final Geometry geometry = object.getGeometry();
    return geometry instanceof Point;
  }

  @Override
  public String toString() {
    return "Point";
  }

}
