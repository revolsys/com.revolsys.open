package com.revolsys.gis.jts;

import com.vividsolutions.jts.geom.LineSegment;

public interface LineSegmentVisitor {
  boolean visit(
    LineSegment segment);
}
