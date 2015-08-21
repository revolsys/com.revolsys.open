package com.revolsys.geometry.algorithm;

import java.util.function.Consumer;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.segment.LineSegment;
import com.revolsys.gis.model.coordinates.LineSegmentUtil;

public class PointInArea extends RayCrossingCounter implements Consumer<LineSegment> {

  private final GeometryFactory geometryFactory;

  public PointInArea(final GeometryFactory geometryFactory, final double x, final double y) {
    super(x, y);
    this.geometryFactory = geometryFactory;
  }

  @Override
  public void accept(final LineSegment segment) {
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
        return;
      }
    }
    countSegment(x1, y1, x2, y2);
  }
}
