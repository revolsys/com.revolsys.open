package com.revolsys.data.filter;

import com.revolsys.data.record.Record;
import com.revolsys.filter.Filter;
import com.revolsys.jts.geom.Geometry;

public class GeometryValueFilter implements Filter<Record> {
  private final Geometry geometry;

  public GeometryValueFilter(final Record object) {
    this(object.getGeometryValue());
  }

  public GeometryValueFilter(final Geometry geometry) {
    this.geometry = geometry;
  }

  @Override
  public boolean accept(final Record object) {
    final Geometry value = object.getGeometryValue();
    if (value == geometry) {
      return true;
    } else if (value != null && geometry != null) {
      return value.equals(geometry);
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return " geometry == " + geometry;
  }

}
