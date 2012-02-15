package com.revolsys.gis.data.model.filter;

import com.revolsys.filter.Filter;
import com.revolsys.gis.data.model.DataObject;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

public class LineStringDataObjectFilter implements Filter<DataObject> {

  public static final LineStringDataObjectFilter FILTER = new LineStringDataObjectFilter();

  private LineStringDataObjectFilter() {
  }

  public boolean accept(final DataObject object) {
    final Geometry geometry = object.getGeometryValue();
    return geometry instanceof LineString;
  }

  @Override
  public String toString() {
    return "LineString";
  }

}
