package com.revolsys.gis.model.geometry.algorithm.locate;

import com.revolsys.collection.Visitor;
import com.revolsys.gis.model.coordinates.LineSegmentUtil;
import com.revolsys.gis.model.geometry.GeometryFactoryI;
import com.revolsys.gis.model.geometry.LineSegment;
import com.revolsys.gis.model.geometry.algorithm.RayCrossingCounter;

public class PointInArea extends RayCrossingCounter implements
  Visitor<LineSegment> {

  private final GeometryFactoryI geometryFactory;

  public PointInArea(final GeometryFactoryI geometryFactory, final double x,
    final double y) {
    super(x, y);
    this.geometryFactory = geometryFactory;
  }

  // TODO note this only works if the close segment is within the same
  @Override
  public boolean visit(final LineSegment segment) {
    final double x1 = segment.getX(0);
    final double y1 = segment.getY(0);
    final double x2 = segment.getX(1);
    final double y2 = segment.getY(1);
    final double x = getX();
    final double y = getY();
    final double distance = LineSegmentUtil.distance(x1, y1, x2, y2, x, y);
    final double minDistance = 1 / geometryFactory.getScaleXY();
    if (distance < minDistance) {
      setPointOnSegment(true);
    } else {
      countSegment(x1, y1, x2, y2);
    }
    return true;
  }
}
