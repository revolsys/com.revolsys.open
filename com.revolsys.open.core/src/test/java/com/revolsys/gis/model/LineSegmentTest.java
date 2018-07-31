package com.revolsys.gis.model;

import org.junit.Assert;
import org.junit.Test;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDouble;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.geometry.model.segment.LineSegment;
import com.revolsys.geometry.model.segment.LineSegmentDouble;
import com.revolsys.geometry.model.segment.LineSegmentDoubleGF;
import com.revolsys.geometry.test.model.GeometryAssertUtil;

public class LineSegmentTest {
  private static final GeometryFactory GEOMETRY_FACTORY_2D = GeometryFactory.fixed2d(3005, 1000.0,
    1000.0);

  private static final GeometryFactory GEOMETRY_FACTORY_3D = GeometryFactory.fixed3d(3005, 1000.0,
    1000.0, 1000.0);

  private final Point c_0_0_0 = GEOMETRY_FACTORY_3D.point(0, 0, 0);

  private final Point c_0_0_N = GEOMETRY_FACTORY_3D.point(0, 0, Double.NaN);

  private final Point c_0_100_1 = GEOMETRY_FACTORY_3D.point(0, 100, 1);

  private final Point c_100_0_10 = GEOMETRY_FACTORY_3D.point(100, 0, 10);

  private final Point c_100_100_10 = GEOMETRY_FACTORY_3D.point(100, 100, 10);

  private final Point c_50_0 = GEOMETRY_FACTORY_2D.point(50, 0);

  private final Point c_50_0_5 = GEOMETRY_FACTORY_3D.point(50, 0, 5);

  private final Point c_70_0 = GEOMETRY_FACTORY_2D.point(70, 0);

  private final Point c_70_0_7 = GEOMETRY_FACTORY_3D.point(70, 0, 7);

  public void assertIntersection3d(final Point line1Start, final Point line1End,
    final Point line2Start, final Point line2End, final Geometry expectedIntersection) {
    final LineSegment line1 = new LineSegmentDoubleGF(GEOMETRY_FACTORY_3D, line1Start, line1End);
    final LineSegment line2 = new LineSegmentDoubleGF(GEOMETRY_FACTORY_3D, line2Start, line2End);

    final Geometry intersection = line1.getIntersection(line2);
    if (!GeometryAssertUtil.equalsExact(3, intersection, expectedIntersection)) {
      GeometryAssertUtil.failNotEquals("Equals Exact", expectedIntersection, intersection);
    }
  }

  public void assertLinearIntersection(final double l1x1, final double l1y1, final double l1x2,
    final double l1y2, final double l2x1, final double l2y1, final double l2x2, final double l2y2,
    final double lx1, final double ly1, final double lx2, final double ly2) {
    final LineSegment line1 = new LineSegmentDoubleGF(GEOMETRY_FACTORY_2D, 2, l1x1, l1y1, l1x2,
      l1y2);
    final LineSegment line2 = new LineSegmentDoubleGF(GEOMETRY_FACTORY_2D, 2, l2x1, l2y1, l2x2,
      l2y2);
    final LineSegment line = new LineSegmentDoubleGF(GEOMETRY_FACTORY_2D, 2, lx1, ly1, lx2, ly2);

    final Geometry intersection = line1.getIntersection(line2);
    GeometryAssertUtil.equalsExact(2, intersection, line);
  }

  @Test
  public void linearIntersection() {
    // Equal
    assertIntersection3d(this.c_0_0_0, this.c_100_0_10, this.c_0_0_0, this.c_100_0_10,
      GEOMETRY_FACTORY_3D.lineString(this.c_0_0_0, this.c_100_0_10));
    // First Start
    assertIntersection3d(this.c_0_0_0, this.c_50_0_5, this.c_0_0_0, this.c_100_0_10,
      GEOMETRY_FACTORY_3D.lineString(this.c_0_0_0, this.c_50_0_5));
    // First End
    assertIntersection3d(this.c_50_0_5, this.c_100_0_10, this.c_0_0_0, this.c_100_0_10,
      GEOMETRY_FACTORY_3D.lineString(this.c_50_0_5, this.c_100_0_10));
    // First Middle
    assertIntersection3d(this.c_50_0_5, this.c_70_0_7, this.c_0_0_0, this.c_100_0_10,
      GEOMETRY_FACTORY_3D.lineString(this.c_50_0_5, this.c_70_0_7));
    // Second Start
    assertIntersection3d(this.c_0_0_0, this.c_100_0_10, this.c_0_0_0, this.c_50_0_5,
      GEOMETRY_FACTORY_3D.lineString(this.c_0_0_0, this.c_50_0_5));
    // Second End
    assertIntersection3d(this.c_0_0_0, this.c_100_0_10, this.c_50_0_5, this.c_100_0_10,
      GEOMETRY_FACTORY_3D.lineString(this.c_50_0_5, this.c_100_0_10));
    // Second Middle
    assertIntersection3d(this.c_0_0_0, this.c_100_0_10, this.c_50_0_5, this.c_70_0_7,
      GEOMETRY_FACTORY_3D.lineString(this.c_50_0_5, this.c_70_0_7));
    // Reverse First Start
    assertIntersection3d(this.c_50_0_5, this.c_0_0_0, this.c_0_0_0, this.c_100_0_10,
      GEOMETRY_FACTORY_3D.lineString(this.c_50_0_5, this.c_0_0_0));
    // Reverse First End
    assertIntersection3d(this.c_100_0_10, this.c_50_0_5, this.c_0_0_0, this.c_100_0_10,
      GEOMETRY_FACTORY_3D.lineString(this.c_100_0_10, this.c_50_0_5));
    // Reverse First Middle
    assertIntersection3d(this.c_70_0_7, this.c_50_0_5, this.c_0_0_0, this.c_100_0_10,
      GEOMETRY_FACTORY_3D.lineString(this.c_70_0_7, this.c_50_0_5));
    // Reverse Second Start
    assertIntersection3d(this.c_0_0_0, this.c_100_0_10, this.c_0_0_0, this.c_50_0_5,
      GEOMETRY_FACTORY_3D.lineString(this.c_0_0_0, this.c_50_0_5));
    // Reverse Second End
    assertIntersection3d(this.c_0_0_0, this.c_100_0_10, this.c_70_0_7, this.c_50_0_5,
      GEOMETRY_FACTORY_3D.lineString(this.c_50_0_5, this.c_70_0_7));

    // Reverse Second End No z on second line
    assertIntersection3d(this.c_0_0_0, this.c_100_0_10, this.c_70_0, this.c_50_0,
      GEOMETRY_FACTORY_3D.lineString(this.c_50_0_5, this.c_70_0_7));

    // Middle Precision Model
    assertIntersection3d(this.c_0_0_0, point(100, 0.001, 10), point(50, 0.001), point(70, 0.001),
      GEOMETRY_FACTORY_3D.lineString(3, 50.0, 0.001, 5, 70, 0.001, 7));
  }

  public PointDouble point(final double... coordinates) {
    return new PointDouble(coordinates);
  }

  @Test
  public void pointIntersections() {
    // Cross
    assertIntersection3d(this.c_0_0_0, this.c_100_100_10, this.c_0_100_1, this.c_100_0_10,
      GEOMETRY_FACTORY_3D.point(50, 50, 5));

    assertIntersection3d(this.c_0_0_N, this.c_100_100_10, this.c_0_100_1, this.c_100_0_10,
      GEOMETRY_FACTORY_3D.point(50, 50, 5.5));

    // Touch
    assertIntersection3d(this.c_0_0_0, this.c_100_0_10, this.c_50_0, this.c_100_100_10,
      this.c_50_0_5);

    // Touch approximate
    assertIntersection3d(this.c_0_0_0, point(100, 0.001, 10), point(50, 0.001, 5),
      this.c_100_100_10, point(50, 0.001, 5));
  }

  public void testDistancePointLinePerpendicular() {
    final LineSegmentDouble segment = new LineSegmentDouble(2, 0.0, 0, 1.0, 0);
    Assert.assertEquals(0.5, segment.distancePerpendicular(new PointDoubleXY(0.5, 0.5)), 0.000001);
    Assert.assertEquals(0.5, segment.distancePerpendicular(new PointDoubleXY(3.5, 0.5)), 0.000001);
    Assert.assertEquals(0.707106, segment.distancePerpendicular(new PointDoubleXY(1.0, 0)),
      0.000001);
  }
}
