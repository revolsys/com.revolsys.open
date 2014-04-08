package com.revolsys.gis.jts;

import com.revolsys.jts.geom.LineSegment;

public interface LineSegmentVisitor {
  boolean visit(LineSegment segment);
}
