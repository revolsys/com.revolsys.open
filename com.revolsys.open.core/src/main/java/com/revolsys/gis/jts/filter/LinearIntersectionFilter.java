package com.revolsys.gis.jts.filter;

import com.revolsys.filter.Filter;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.IntersectionMatrix;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.prep.PreparedGeometry;
import com.revolsys.jts.geom.prep.PreparedGeometryFactory;

public class LinearIntersectionFilter implements Filter<LineString> {

  private final LineString line;

  private final PreparedGeometry preparedLine;

  private final BoundingBox envelope;

  public LinearIntersectionFilter(final LineString line) {
    this.line = line;
    this.preparedLine = PreparedGeometryFactory.prepare(line);
    this.envelope = line.getBoundingBox();
  }

  @Override
  public boolean accept(final LineString line) {
    final BoundingBox envelope = line.getBoundingBox();
    if (envelope.intersects(this.envelope)) {
      if (preparedLine.intersects(line)) {
        final IntersectionMatrix relate = this.line.relate(line);
        if (relate.isOverlaps(1, 1) || relate.isContains() || relate.isWithin()) {
          return true;
        }
      }
    }
    return false;
  }
}
