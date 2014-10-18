package com.revolsys.jts.operation.valid;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.segment.Segment;

public class SegmentError extends AbstractGeometryValidationError {

  private final int[] segmentId;

  public SegmentError(final String message, final Segment segment) {
    super(message, segment.getGeometry());
    this.segmentId = segment.getSegmentId();
  }

  @Override
  public Geometry getErrorGeometry() {
    return getSegment();
  }

  @Override
  public Point getErrorPoint() {
    return getSegment().getStartPoint();
  }

  public Segment getSegment() {
    final Geometry geometry = getGeometry();
    final Segment segment = geometry.getSegment(this.segmentId);
    return segment;
  }

  public int[] getSegmentId() {
    return this.segmentId;
  }
}
