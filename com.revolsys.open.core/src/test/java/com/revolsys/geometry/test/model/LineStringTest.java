package com.revolsys.geometry.test.model;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.revolsys.geometry.cs.GeographicCoordinateSystem;
import com.revolsys.geometry.cs.ProjectedCoordinateSystem;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Side;
import com.revolsys.geometry.model.impl.PointDouble;
import com.revolsys.geometry.model.metrics.PointLineStringMetrics;

public class LineStringTest {

  private static final double START_X = 500000;

  private static final double START_Y = 6000000;

  public static void assertCoordinatesEquals(final Point point, final double... coordinates) {
    Assert.assertEquals("Is Empty", false, point.isEmpty());
    Assert.assertEquals("Geometry Count", 1, point.getGeometryCount());
    Assert.assertNotNull("Not Null First Vertex", point.getVertex(0));
    Assert.assertEquals("Axis Count", coordinates.length, point.getAxisCount());
    for (int axisIndex = -1; axisIndex < point.getAxisCount() + 1; axisIndex++) {
      final double value = point.getCoordinate(axisIndex);
      if (axisIndex < 0 || axisIndex >= coordinates.length) {
        if (!Double.isNaN(value)) {
          TestUtil.failNotEquals("Value NaN", Double.NaN, value);
        }
      } else {
        Assert.assertEquals("Coordinate Value", coordinates[axisIndex], value, 0);
      }
    }
  }

  private static void assertDistanceAlong(final double distanceAlong, final double distance,
    final double x, final double y, final Side side, final double... coordinates) {
    final GeometryFactory geometryFactory = GeometryFactory.worldMercator().convertAxisCount(2);
    final Point point = geometryFactory.point(x, y);
    final LineString line = geometryFactory.lineString(2, coordinates);
    final double actual = line.distanceAlong(point);
    Assert.assertEquals("Distance Along", distanceAlong, actual, 0.0000001);

    final PointLineStringMetrics metrics = line.getMetrics(point);
    Assert.assertEquals("Metrics Distance Along", distanceAlong, metrics.getDistanceAlong(),
      0.0000001);
    Assert.assertEquals("Metrics Distance", distance, metrics.getDistance(), 0.0000001);
    Assert.assertEquals("Metrics Side", side, metrics.getSide());
    Assert.assertEquals("Metrics Length", line.getLength(), metrics.getLineLength(), 0.0000001);
    Assert.assertEquals("Distance Along -> Metrics", distanceAlong, metrics.getDistanceAlong(),
      0.0000001);

    if (side != null) {
      final Side reverseSide = Side.opposite(side);
      final LineString reverseLine = line.reverse();
      final PointLineStringMetrics reverseMetrics = reverseLine.getMetrics(point);
      Assert.assertEquals("Reverse Metrics Side", reverseSide, reverseMetrics.getSide());

    }

  }

  public static void assertEmpty(final LineString line) {
    Assert.assertEquals("Is Empty", true, line.isEmpty());
    Assert.assertEquals("Geometry Count", 0, line.getGeometryCount());
    Assert.assertNull("Null First Vertex", line.getVertex(0));
    for (int axisIndex = -1; axisIndex < line.getAxisCount() + 1; axisIndex++) {
      final double value = line.getCoordinate(0, axisIndex);
      if (!Double.isNaN(value)) {
        TestUtil.failNotEquals("Value NaN", Double.NaN, value);
      }
    }
  }

  public static void assertEquals(final Point point, final double... coordinates) {
    final GeometryFactory geometryFactory = point.getGeometryFactory();

    final GeometryFactory geometryFactory2;
    final int axisCount = geometryFactory.getAxisCount();
    if (geometryFactory.getCoordinateSystem() instanceof ProjectedCoordinateSystem) {
      final ProjectedCoordinateSystem projectedCs = (ProjectedCoordinateSystem)geometryFactory
        .getCoordinateSystem();
      final GeographicCoordinateSystem geographicCoordinateSystem = projectedCs
        .getGeographicCoordinateSystem();
      geometryFactory2 = GeometryFactory
        .floating(geographicCoordinateSystem.getCoordinateSystemId(), axisCount);
    } else {
      geometryFactory2 = GeometryFactory.floating(26910, axisCount);
    }

    assertCoordinatesEquals(point, coordinates);

    final Point clone = point.newPoint();
    assertCoordinatesEquals(clone, coordinates);

    final Point converted = point.convertGeometry(geometryFactory);
    assertCoordinatesEquals(converted, coordinates);
    Assert.assertSame(point, converted);

    final Point convertedOther = point.convertGeometry(geometryFactory2);
    final Point convertedBack = convertedOther.convertGeometry(geometryFactory);
    assertCoordinatesEquals(convertedBack, coordinates);
    Assert.assertNotSame(point, convertedBack);

    final Point copy = point.newGeometry(geometryFactory);
    assertCoordinatesEquals(copy, coordinates);
    Assert.assertNotSame(point, copy);

    final Point copyOther = point.convertGeometry(geometryFactory2);
    final Point copyBack = copyOther.convertGeometry(geometryFactory);
    assertCoordinatesEquals(copyBack, coordinates);
    Assert.assertNotSame(point, copyBack);

    final String string = point.toString();
    final Point pointString = geometryFactory.geometry(string);
    assertCoordinatesEquals(pointString, coordinates);

    final String wkt = point.toEwkt();
    final Point pointWkt = geometryFactory.geometry(wkt);
    assertCoordinatesEquals(pointWkt, coordinates);

  }

  public static void assertMerge(final boolean loop, final LineString line1, final LineString line2,
    final double mergeX, final double mergeY, final double... expectedCoordinates) {
    final GeometryFactory geometryFactory = line1.getGeometryFactory();
    final int axisCount = geometryFactory.getAxisCount();
    final LineString expectedMergedLine = geometryFactory.lineString(axisCount,
      expectedCoordinates);

    final Point mergePoint = geometryFactory.point(mergeX, mergeY);

    final LineString mergedLine = line1.merge(mergePoint, line2);
    TestUtil.assertEqualsExact(axisCount, expectedMergedLine, mergedLine);

    if (!loop) {
      final LineString mergedLine2 = line1.merge(line2);
      TestUtil.assertEqualsExact(axisCount, expectedMergedLine, mergedLine2);
    }
  }

  public static void assertSplit(final LineString line, final Point splitPoint,
    final LineString... splitLines) {
    final GeometryFactory geometryFactory = line.getGeometryFactory();
    final int axisCount = geometryFactory.getAxisCount();

    final List<LineString> actualSplitLines = line.split(splitPoint);
    final int splitCount = splitLines.length;
    Assert.assertEquals("Split Count", splitCount, actualSplitLines.size());

    for (int i = 0; i < splitLines.length; i++) {
      final LineString expectedSplitLine = splitLines[i];
      final LineString actualSplitLine = actualSplitLines.get(i);
      TestUtil.assertEqualsExact(axisCount, expectedSplitLine, actualSplitLine);
    }
  }

  private void assertEquals(final double[] coordinates, final double[] coordinatesLessNaN,
    final Point pointCoordinatesListAllAxis, final Point pointCoordinatesListExtraAxis,
    final Point pointCoordinatesListLessAxis) {
    assertEquals(pointCoordinatesListAllAxis, coordinates);
    assertEquals(pointCoordinatesListExtraAxis, coordinates);
    assertEquals(pointCoordinatesListLessAxis, coordinatesLessNaN);
  }

  private void assertObjectContsructor(final GeometryFactory geometryFactory,
    final double[] coordinates, final double[] coordinatesLessNaN, final Point pointAll,
    final Point pointExtra, final Point pointLess) {
    final Point pointAllAxis = geometryFactory.point((Object)pointAll);
    final Point pointExtraAxis = geometryFactory.point((Object)pointExtra);
    final Point pointLessAxis = geometryFactory.point((Object)pointLess);

    assertEquals(coordinates, coordinatesLessNaN, pointAllAxis, pointExtraAxis, pointLessAxis);
  }

  @Test
  public void constructEmpty() {
    for (int axisCount = 2; axisCount < 4; axisCount++) {
      final GeometryFactory geometryFactory = GeometryFactory.fixed(26910, axisCount,
        GeometryFactory.newScalesFixed(axisCount, 1000.0));

      // Empty Constructor
      final LineString pointEmpty = geometryFactory.lineString();
      assertEmpty(pointEmpty);

      // Point[] Constructor

      final LineString pointCoordinatesArrayNull = geometryFactory.lineString((Point[])null);
      assertEmpty(pointCoordinatesArrayNull);

      final LineString pointCoordinatesArraySize0 = geometryFactory.lineString(new Point[0]);
      assertEmpty(pointCoordinatesArraySize0);

      final LineString pointCoordinatesNull = geometryFactory.lineString((Point)null);
      assertEmpty(pointCoordinatesNull);

      final LineString pointCoordinatesSize0 = geometryFactory.lineString(new PointDouble(0));
      assertEmpty(pointCoordinatesSize0);

      // LineString Constructor
      final LineString pointCoordinatesListNull = geometryFactory.lineString((LineString)null);
      assertEmpty(pointCoordinatesListNull);

      final LineString pointCoordinatesListSize0 = geometryFactory
        .lineString(geometryFactory.lineString());
      assertEmpty(pointCoordinatesListSize0);

      // double[] Constructor
      final LineString pointDoubleArray0Null = geometryFactory.lineString(0, (double[])null);
      assertEmpty(pointDoubleArray0Null);

      final LineString pointDoubleArray2Null = geometryFactory.lineString(2, (double[])null);
      assertEmpty(pointDoubleArray2Null);

      final LineString pointDoubleArray0NoValue = geometryFactory.lineString(0);
      assertEmpty(pointDoubleArray0NoValue);

      final LineString pointDoubleArray2NoValue = geometryFactory.lineString(2);
      assertEmpty(pointDoubleArray2NoValue);

      // LineString Constructor
      final LineString pointLineStringNull = geometryFactory.lineString((LineString)null);
      assertEmpty(pointLineStringNull);
    }
  }

  @Test
  public void constructLineString() {
    for (int axisCount = 2; axisCount < 4; axisCount++) {
      int axisCountLess = axisCount;
      if (axisCountLess > 2) {
        axisCountLess--;
      }
      final GeometryFactory geometryFactory = GeometryFactory.fixed(26910, axisCount,
        GeometryFactory.newScalesFixed(axisCount, 1000.0));
      final GeometryFactory geometryFactoryExtra = GeometryFactory.floating(26910, axisCount + 1);
      final GeometryFactory geometryFactoryLess = GeometryFactory.floating(26910, axisCountLess);
      final double[] coordinatesExtra = new double[axisCount + 1];
      final double[] coordinates = new double[axisCount];
      final double[] coordinatesLess = new double[axisCountLess];
      final double[] coordinatesLessNaN = new double[axisCount];
      for (int i = 0; i < axisCount; i++) {
        double value;
        switch (i) {
          case 0:
            value = START_X;
          break;
          case 1:
            value = START_Y;
          break;
          default:
            value = i * 10 + i;
        }
        coordinates[i] = value;
        coordinatesExtra[i] = value;
        coordinatesLessNaN[i] = value;
        if (i < axisCountLess) {
          coordinatesLess[i] = value;
        } else {
          coordinatesLessNaN[i] = Double.NaN;
        }
      }
      coordinatesExtra[coordinatesExtra.length - 1] = 6;

      // double[]
      final Point pointDoubleAllAxis = geometryFactory.point(coordinates);
      final Point pointDoubleExtraAxis = geometryFactory.point(coordinatesExtra);
      final Point pointDoubleLessAxis = geometryFactory.point(coordinatesLess);
      assertEquals(coordinates, coordinatesLessNaN, pointDoubleAllAxis, pointDoubleExtraAxis,
        pointDoubleLessAxis);
      assertObjectContsructor(geometryFactory, coordinates, coordinatesLessNaN, pointDoubleAllAxis,
        pointDoubleExtraAxis, pointDoubleLessAxis);

      // Coordinates
      final Point pointCoordinatesAllAxis = geometryFactory.point(new PointDouble(coordinates));
      final Point pointCoordinatesExtraAxis = geometryFactory
        .point(new PointDouble(coordinatesExtra));
      final Point pointCoordinatesLessAxis = geometryFactory
        .point(new PointDouble(coordinatesLess));
      assertEquals(coordinates, coordinatesLessNaN, pointCoordinatesAllAxis,
        pointCoordinatesExtraAxis, pointCoordinatesLessAxis);
      assertObjectContsructor(geometryFactory, coordinates, coordinatesLessNaN,
        pointCoordinatesAllAxis, pointCoordinatesExtraAxis, pointCoordinatesLessAxis);

      // Object Point
      final Point pointAll = pointDoubleAllAxis;
      final Point pointExtra = geometryFactoryExtra.point(coordinatesExtra);
      final Point pointLess = geometryFactoryLess.point(coordinatesLess);
      assertObjectContsructor(geometryFactory, coordinates, coordinatesLessNaN, pointAll,
        pointExtra, pointLess);
    }
  }

  @Test
  public void testDistanceAlong() {
    final double[] horizontal1 = new double[] {
      0, 0, //
      1, 0
    };
    // Diagonal
    final double[] diagonal = new double[] {
      1, 1, //
      2, 2
    };
    assertDistanceAlong(0, 0, 0, 0, null, horizontal1);
    assertDistanceAlong(1, 0, 1, 0, null, horizontal1);
    assertDistanceAlong(0.5, 0, 0.5, 0, null, horizontal1);

    // Before
    assertDistanceAlong(-0.5, 0.5, -0.5, 0, null, horizontal1);
    assertDistanceAlong(-0.5, 0.5099019513592785, -0.5, 0.1, Side.LEFT, horizontal1);
    assertDistanceAlong(-1.4142135623730951, 1.4142135623730951, 0.0, 0.0, null, diagonal);

    // Above
    assertDistanceAlong(0.5, 1, 0.5, 1, Side.LEFT, horizontal1);

    // After
    assertDistanceAlong(1.5, 0.5, 1.5, 0, null, horizontal1);
    assertDistanceAlong(2.8284271247461903, 1.4142135623730951, 3.0, 3.0, null, diagonal);

    // Right angle
    final double[] rightAngle1 = new double[] {
      0, 1, //
      1, 1, //
      1, 0
    };
    assertDistanceAlong(1, 0.7071067811865476, 1.5, 1.5, Side.LEFT, rightAngle1);
    assertDistanceAlong(1, 0.5, 1.0, 1.5, Side.LEFT, rightAngle1);
    assertDistanceAlong(1, 0.5, 1.5, 1.0, Side.LEFT, rightAngle1);

  }

  @Test
  public void testFromFile() {
    TestUtil.doTestGeometry(getClass(), "LineString.csv");
  }

  @Test
  public void testMerge() {
    final GeometryFactory geometryFactory = GeometryFactory.fixed(26910, 3, 1.0, 1, 1);

    // Last point is duplicated
    final LineString line1 = geometryFactory.lineString(3, START_X, START_Y, 0, START_X + 100,
      START_Y + 100, 1, START_X + 200, START_Y + 200, 2, START_X + 300, START_Y + 300, 3,
      START_X + 300, START_Y + 300, 3);
    final LineString line1Reverse = line1.reverse();

    // Every point is duplicated
    final LineString line2 = geometryFactory.lineString(3, START_X + 300, START_Y + 300, 3,
      START_X + 300, START_Y + 300, 3, START_X + 400, START_Y + 400, 4, START_X + 400,
      START_Y + 400, 4, START_X + 500, START_Y + 500, 5, START_X + 500, START_Y + 500, 5);
    final LineString line2Reverse = line2.reverse();

    // Line to make a loop
    final LineString line3 = geometryFactory.lineString(3, START_X + 300, START_Y + 300, 3, START_X,
      START_Y, 0);
    final LineString line3Reverse = line3.reverse();

    // Forwards, Forwards
    assertMerge(false, line1, line2, START_X + 300, START_Y + 300, START_X, START_Y, 0,
      START_X + 100, START_Y + 100, 1, START_X + 200, START_Y + 200, 2, START_X + 300,
      START_Y + 300, 3, START_X + 400, START_Y + 400, 4, START_X + 500, START_Y + 500, 5);

    // Forwards, Reverse
    assertMerge(false, line1, line2Reverse, START_X + 300, START_Y + 300, START_X, START_Y, 0,
      START_X + 100, START_Y + 100, 1, START_X + 200, START_Y + 200, 2, START_X + 300,
      START_Y + 300, 3, START_X + 400, START_Y + 400, 4, START_X + 500, START_Y + 500, 5);

    // Reverse, Forwards
    assertMerge(false, line1Reverse, line2, START_X + 300, START_Y + 300, START_X + 500,
      START_Y + 500, 5, START_X + 400, START_Y + 400, 4, START_X + 300, START_Y + 300, 3,
      START_X + 200, START_Y + 200, 2, START_X + 100, START_Y + 100, 1, START_X, START_Y, 0);

    // Reverse, Reverse
    assertMerge(false, line1Reverse, line2Reverse, START_X + 300, START_Y + 300, START_X + 500,
      START_Y + 500, 5, START_X + 400, START_Y + 400, 4, START_X + 300, START_Y + 300, 3,
      START_X + 200, START_Y + 200, 2, START_X + 100, START_Y + 100, 1, START_X, START_Y, 0);

    // Loop Forwards, Forwards
    assertMerge(true, line1, line3, START_X + 300, START_Y + 300, START_X, START_Y, 0,
      START_X + 100, START_Y + 100, 1, START_X + 200, START_Y + 200, 2, START_X + 300,
      START_Y + 300, 3, START_X, START_Y, 0);

    // Loop Forwards, Reverse
    assertMerge(true, line1, line3Reverse, START_X + 300, START_Y + 300, START_X, START_Y, 0,
      START_X + 100, START_Y + 100, 1, START_X + 200, START_Y + 200, 2, START_X + 300,
      START_Y + 300, 3, START_X, START_Y, 0);

    // Loop Reverse, Forwards
    assertMerge(true, line1Reverse, line3, START_X + 300, START_Y + 300, START_X, START_Y, 0,
      START_X + 300, START_Y + 300, 3, START_X + 200, START_Y + 200, 2, START_X + 100,
      START_Y + 100, 1, START_X, START_Y, 0);

    // Loop Reverse, Reverse
    assertMerge(true, line1Reverse, line3Reverse, START_X + 300, START_Y + 300, START_X, START_Y, 0,
      START_X + 300, START_Y + 300, 3, START_X + 200, START_Y + 200, 2, START_X + 100,
      START_Y + 100, 1, START_X, START_Y, 0);

  }

  @Test
  public void testSplit() {
    final GeometryFactory geometryFactory = GeometryFactory.fixed(26910, 3, 1000.0, 1000.0, 1);

    // Last point is duplicated
    final LineString line = geometryFactory.lineString(3, START_X, START_Y, 0, START_X + 100,
      START_Y + 100, 1, START_X + 200, START_Y + 100, 2, START_X + 100, START_Y, 3);

    // From vertex
    assertSplit(line, geometryFactory.point(START_X, START_Y), line);

    // To vertex
    assertSplit(line, geometryFactory.point(START_X + 100, START_Y), line);

    // Middle vertex
    final LineString lineVertexMiddle1 = geometryFactory.lineString(3, START_X, START_Y, 0,
      START_X + 100, START_Y + 100, 1);
    final LineString lineVertexMiddle2 = geometryFactory.lineString(3, START_X + 100, START_Y + 100,
      1, START_X + 200, START_Y + 100, 2, START_X + 100, START_Y, 3);
    assertSplit(line, geometryFactory.point(START_X + 100, START_Y + 100), lineVertexMiddle1,
      lineVertexMiddle2);

    // Middle vertex
    final LineString lineVertexClose1 = geometryFactory.lineString(3, START_X, START_Y, 0,
      START_X + 100, START_Y + 100, 1, START_X + 99.999, START_Y + 100.001, 1);
    final LineString lineVertexClose2 = geometryFactory.lineString(3, START_X + 99.999,
      START_Y + 100.001, 1, START_X + 100, START_Y + 100, 1, START_X + 200, START_Y + 100, 2,
      START_X + 100, START_Y, 3);
    assertSplit(line, geometryFactory.point(START_X + 99.999, START_Y + 100.001, 1),
      lineVertexClose1, lineVertexClose2);

    // Middle of first segment
    for (final double offset : new double[] {
      0.001, 50, 99.999
    }) {
      final double x = START_X + offset;
      final double y = START_Y + offset;
      final LineString lineSegmentFirst1 = geometryFactory.lineString(3, START_X, START_Y, 0, x, y,
        offset);
      final LineString lineSegmentFirst2 = geometryFactory.lineString(3, x, y, offset,
        START_X + 100, START_Y + 100, 1, START_X + 200, START_Y + 100, 2, START_X + 100, START_Y,
        3);
      assertSplit(line, geometryFactory.point(x, y, offset), lineSegmentFirst1, lineSegmentFirst2);
    }
  }

  @Test
  public void testSubList() {
    final GeometryFactory geometryFactory = GeometryFactory.wgs84();
    final LineString line = geometryFactory.lineString(2, 0.0, 0, 2, 2, 3, 3, 4, 4, 5, 5);
    assertEmpty(line.subLine(0));

    TestUtil.equalsExact(2, line.subLine(2), geometryFactory.lineString(2, 0.0, 0, 2, 2));
    TestUtil.equalsExact(2, line.subLine(2, geometryFactory.point(10, 10)),
      geometryFactory.lineString(2, 0.0, 0, 2, 2, 10, 10));
    TestUtil.equalsExact(2,
      line.subLine(geometryFactory.point(-1, -1), 0, 2, geometryFactory.point(10, 10)),
      geometryFactory.lineString(2, -1.0, -1.0, 0.0, 0, 2, 2, 10, 10));

    final LineString actualFromToIndexMaxLength = line.subLine(geometryFactory.point(-1, -1), 3, 3,
      geometryFactory.point(10, 10));
    final LineString expectedFromToIndexMaxLength = geometryFactory.lineString(2, -1.0, -1.0, 4, 4,
      5, 5, 10, 10);
    TestUtil.equalsExact(2, actualFromToIndexMaxLength, expectedFromToIndexMaxLength);
  }
}
