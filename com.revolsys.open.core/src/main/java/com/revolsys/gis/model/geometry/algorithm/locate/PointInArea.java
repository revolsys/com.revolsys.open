package com.revolsys.gis.model.geometry.algorithm.locate;

import com.revolsys.collection.Visitor;
import com.revolsys.gis.model.geometry.LineSegment;
import com.revolsys.gis.model.geometry.algorithm.RayCrossingCounter;

public class PointInArea extends RayCrossingCounter implements
  Visitor<LineSegment> {

  public PointInArea(final double x, final double y) {
    super(x, y);
  }

  @Override
  public boolean visit(final LineSegment segment) {
    final double x1 = segment.getX(0);
    final double y1 = segment.getY(0);
    final double x2 = segment.getX(1);
    final double y2 = segment.getY(1);
    countSegment(x1, y1, x2, y2);
    return true;
  }
}
