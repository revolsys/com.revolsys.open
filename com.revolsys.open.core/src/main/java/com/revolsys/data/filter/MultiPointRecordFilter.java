package com.revolsys.data.filter;

import com.revolsys.data.record.Record;
import com.revolsys.filter.Filter;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.MultiPoint;

public class MultiPointRecordFilter implements Filter<Record> {
  public static final MultiPointRecordFilter FILTER = new MultiPointRecordFilter();

  private MultiPointRecordFilter() {
  }

  @Override
  public boolean accept(final Record object) {
    final Geometry geometry = object.getGeometry();
    return geometry instanceof MultiPoint;
  }

  @Override
  public String toString() {
    return "MultiPoint";
  }

}
