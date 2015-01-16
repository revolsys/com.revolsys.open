package com.revolsys.jts.algorithm;

import com.revolsys.collection.Visitor;
import com.revolsys.gis.model.coordinates.LineSegmentUtil;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.segment.LineSegment;

public class PointInArea extends RayCrossingCounter implements
Visitor<LineSegment> {

  private final GeometryFactory geometryFactory;

  public PointInArea(final GeometryFactory geometryFactory, final double x,
    final double y) {
    super(x, y);
    this.geometryFactory = geometryFactory;
  }

  @Override
  public boolean visit(final LineSegment segment) {
    final double x1 = segment.getX(0);
    final double y1 = segment.getY(0);
    final double x2 = segment.getX(1);
    final double y2 = segment.getY(1);
    final double x = getX();
    final double y = getY();
    final double scaleXY = this.geometryFactory.getScaleXY();
    if (!this.geometryFactory.isFloating()) {
      final double distance = LineSegmentUtil.distanceLinePoint(x1, y1, x2, y2, x, y);
      final double minDistance = 1 / scaleXY;
      if (distance < minDistance) {
        setPointOnSegment(true);
        return true;
      }
    }
    countSegment(x1, y1, x2, y2);

    return true;
  }
}
