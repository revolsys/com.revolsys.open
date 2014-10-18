package com.revolsys.jts.operation.simple;

import com.revolsys.jts.geom.segment.Segment;
import com.revolsys.jts.operation.valid.SegmentError;

public class SelfOverlapSegmentError extends SegmentError {
  public SelfOverlapSegmentError(final Segment segment) {
    super("Self Overlap at Segment", segment);
  }
}
