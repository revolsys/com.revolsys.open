package com.revolsys.data.filter;

import com.revolsys.data.record.Record;
import java.util.function.Predicate;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.Point;

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
