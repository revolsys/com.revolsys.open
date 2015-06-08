package com.revolsys.jts.operation.simple;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.segment.LineSegment;
import com.revolsys.jts.operation.valid.GeometryError;

public class SelfOverlapLineSegmentError extends GeometryError {
  public SelfOverlapLineSegmentError(final Geometry geometry, final LineSegment segment) {
    super("Self Overlap at Line", geometry, segment);
  }
}
