package com.revolsys.data.filter;

import com.revolsys.data.record.Record;
import com.revolsys.filter.Filter;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.Point;

public class PointRecordFilter implements Filter<Record> {
  public static final PointRecordFilter FILTER = new PointRecordFilter();

  private PointRecordFilter() {
  }

  @Override
  public boolean accept(final Record object) {
    final Geometry geometry = object.getGeometryValue();
    return geometry instanceof Point;
  }

  @Override
  public String toString() {
    return "Point";
  }

}
