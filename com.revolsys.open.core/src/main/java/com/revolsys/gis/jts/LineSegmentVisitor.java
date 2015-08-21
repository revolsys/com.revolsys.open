package com.revolsys.gis.jts;

import com.revolsys.geometry.model.segment.LineSegment;

public interface LineSegmentVisitor {
  boolean visit(LineSegment segment);
}
