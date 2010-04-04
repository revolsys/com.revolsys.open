package com.revolsys.gis.jts.filter;

import com.revolsys.filter.Filter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.IntersectionMatrix;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;

public class LinearIntersectionFilter implements Filter<LineString> {

  private final LineString line;

  private final PreparedGeometry preparedLine;

  public LinearIntersectionFilter(
    final LineString line) {
    this.line = line;
    this.preparedLine = PreparedGeometryFactory.prepare(line);
  }

  public boolean accept(
    final LineString line) {
    if (preparedLine.intersects(line)) {
      final Geometry edgeEnvelope = line.getEnvelope();
      final Geometry envelope = this.line.getEnvelope();
      if (edgeEnvelope.intersects(envelope)) {
        final IntersectionMatrix relate = this.line.relate(line);
        if (relate.isOverlaps(1, 1) || relate.isContains() || relate.isWithin()) {
          return true;
        }
      }
    }
    return false;
  }
}
