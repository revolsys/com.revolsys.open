package com.revolsys.gis.model;

import org.junit.Test;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.gis.model.geometry.LineSegment;
import com.vividsolutions.jts.util.Assert;

public class LineSegmentTest {
  private static final GeometryFactory GEOMETRY_FACTORY_2D = GeometryFactory.getFactory(
    3005, 2, 1000, 0);

  private static final GeometryFactory GEOMETRY_FACTORY_3D = GeometryFactory.getFactory(
    3005, 3, 1000, 1000);

  public void assertLinearIntersection(double l1x1, double l1y1, double l1x2,
    double l1y2, double l2x1, double l2y1, double l2x2, double l2y2,
    double lx1, double ly1, double lx2, double ly2) {
    LineSegment line1 = new LineSegment(GEOMETRY_FACTORY_2D, l1x1, l1y1, l1x2,
      l1y2);
    LineSegment line2 = new LineSegment(GEOMETRY_FACTORY_2D, l2x1, l2y1, l2x2,
      l2y2);
    LineSegment line = new LineSegment(GEOMETRY_FACTORY_2D, lx1, ly1, lx2, ly2);

    CoordinatesList intersection = line1.getIntersection(line2);
    Assert.equals(line, intersection);
  }

  public DoubleCoordinates c(double... coordinates) {
    return new DoubleCoordinates(coordinates);
  }

  public void assertIntersection3d(Coordinates line1Start,
    Coordinates line1End, Coordinates line2Start, Coordinates line2End,
    Coordinates... expectedIntersection) {
    LineSegment line1 = new LineSegment(GEOMETRY_FACTORY_3D, line1Start,
      line1End);
    LineSegment line2 = new LineSegment(GEOMETRY_FACTORY_3D, line2Start,
      line2End);
    CoordinatesList points = new DoubleCoordinatesList(3, expectedIntersection);

    CoordinatesList intersection = line1.getIntersection(line2);
    Assert.equals(points, intersection);
  }

  private DoubleCoordinates c_0_0_0 = c(0, 0, 0);

  private DoubleCoordinates c_50_0_5 = c(50, 0, 5);

  private DoubleCoordinates c_50_0 = c(50, 0);

  private DoubleCoordinates c_50_50_5 = c(50, 50, 5);

  private DoubleCoordinates c_70_0_7 = c(70, 0, 7);

  private DoubleCoordinates c_70_0 = c(70, 0);

  private DoubleCoordinates c_100_0_10 = c(100, 0, 10);

  private DoubleCoordinates c_100_100_10 = c(100, 100, 10);

  private DoubleCoordinates c_0_100_1 = c(0, 100, 1);

  @Test
  public void linearIntersection() {
    // Equal
    assertIntersection3d(c_0_0_0, c_100_0_10, c_0_0_0, c_100_0_10, c_0_0_0,
      c_100_0_10);
    // First Start
    assertIntersection3d(c_0_0_0, c_50_0_5, c_0_0_0, c_100_0_10, c_0_0_0,
      c_50_0_5);
    // First End
    assertIntersection3d(c_50_0_5, c_100_0_10, c_0_0_0, c_100_0_10, c_50_0_5,
      c_100_0_10);
    // First Middle
    assertIntersection3d(c_50_0_5, c_70_0_7, c_0_0_0, c_100_0_10, c_50_0_5,
      c_70_0_7);
    // Second Start
    assertIntersection3d(c_0_0_0, c_100_0_10, c_0_0_0, c_50_0_5, c_0_0_0,
      c_50_0_5);
    // Second End
    assertIntersection3d(c_0_0_0, c_100_0_10, c_50_0_5, c_100_0_10, c_50_0_5,
      c_100_0_10);
    // Second Middle
    assertIntersection3d(c_0_0_0, c_100_0_10, c_50_0_5, c_70_0_7, c_50_0_5,
      c_70_0_7);
    // Reverse First Start
    assertIntersection3d(c_50_0_5, c_0_0_0, c_0_0_0, c_100_0_10, c_50_0_5,
      c_0_0_0);
    // Reverse First End
    assertIntersection3d(c_100_0_10, c_50_0_5, c_0_0_0, c_100_0_10, c_100_0_10,
      c_50_0_5);
    // Reverse First Middle
    assertIntersection3d(c_70_0_7, c_50_0_5, c_0_0_0, c_100_0_10, c_70_0_7,
      c_50_0_5);
    // Reverse Second Start
    assertIntersection3d(c_0_0_0, c_100_0_10, c_0_0_0, c_50_0_5, c_0_0_0,
      c_50_0_5);
    // Reverse Second End
    assertIntersection3d(c_0_0_0, c_100_0_10, c_70_0_7, c_50_0_5, c_50_0_5,
      c_70_0_7);

    // Reverse Second End No z on second line
    assertIntersection3d(c_0_0_0, c_100_0_10, c_70_0, c_50_0, c_50_0_5,
      c_70_0_7);

    // Middle Precision Model
    assertIntersection3d(c_0_0_0, c(100, 0.001, 10), c(50, 0.001),
      c(70, 0.001), c(50, 0.001, 5), c(70, 0.001, 7));
  }

  @Test
  public void pointIntersections() {
    // Cross
    assertIntersection3d(c_0_0_0, c_100_100_10, c_0_100_1, c_100_0_10,
      c_50_50_5);
    // Touch
    assertIntersection3d(c_0_0_0, c_100_0_10, c_50_0, c_100_100_10, c_50_0_5);
    // Touch approximate
    assertIntersection3d(c_0_0_0, c(100, 0.001, 10), c(50, 0.001, 5),
      c_100_100_10, c(50, 0.001, 5));

  }
}
