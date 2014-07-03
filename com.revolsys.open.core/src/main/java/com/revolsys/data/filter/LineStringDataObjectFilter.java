package com.revolsys.data.filter;

import com.revolsys.data.record.Record;
import com.revolsys.filter.Filter;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.LineString;

public class LineStringDataObjectFilter implements Filter<Record> {

  public static final LineStringDataObjectFilter FILTER = new LineStringDataObjectFilter();

  private LineStringDataObjectFilter() {
  }

  @Override
  public boolean accept(final Record object) {
    final Geometry geometry = object.getGeometryValue();
    return geometry instanceof LineString;
  }

  @Override
  public String toString() {
    return "LineString";
  }

}
