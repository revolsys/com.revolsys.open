package com.revolsys.gis.jts;

import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.Polygon;

public class LineStringMitredBuffer implements LineSegmentVisitor {
  private Polygon buffer;

  private final double distance;

  public LineStringMitredBuffer(final double distance) {
    this.distance = distance;
  }

  /**
   * @return the buffer
   */
  public Polygon getBuffer() {
    return buffer;
  }

  @Override
  public boolean visit(final LineSegment segment) {
    final Polygon segmentBuffer = JtsGeometryUtil.getMitredBuffer(segment,
      distance);
    if (buffer == null) {
      buffer = segmentBuffer;
    } else {
      buffer = (Polygon)buffer.union(segmentBuffer);
    }
    return true;
  }

}
