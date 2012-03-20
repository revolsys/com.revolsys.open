package com.revolsys.gis.data.model.filter;

import com.revolsys.filter.Filter;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.graph.linestring.LineStringRelate;
import com.vividsolutions.jts.geom.LineString;

public class LineEqualWithinDistance implements Filter<LineString> {

  public static Filter<DataObject> getFilter(
    DataObject object,
    double maxDistance) {
    LineString line = object.getGeometryValue();
    LineEqualWithinDistance lineFilter = new LineEqualWithinDistance(line,
      maxDistance);
    return new DataObjectGeometryFilter<LineString>(lineFilter);
  }

  private double maxDistance;

  private LineString line;

  public LineEqualWithinDistance(LineString line, double maxDistance) {
    this.line = line;
    this.maxDistance = maxDistance;
  }

  public boolean accept(LineString line2) {
    LineStringRelate relate = new LineStringRelate(line, line2, maxDistance);
    return relate.isEqual();
  }
}
