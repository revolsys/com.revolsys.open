package com.revolsys.gis.jts;

import com.revolsys.jts.geom.segment.LineSegment;

public interface LineSegmentVisitor {
  boolean visit(LineSegment segment);
}
