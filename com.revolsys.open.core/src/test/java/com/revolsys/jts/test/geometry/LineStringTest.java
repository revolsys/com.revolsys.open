package com.revolsys.jts.test.geometry;

import junit.framework.Assert;

import org.junit.Test;

import com.revolsys.gis.cs.GeographicCoordinateSystem;
import com.revolsys.gis.cs.ProjectedCoordinateSystem;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.PointList;
import com.revolsys.jts.geom.impl.PointDouble;

public class LineStringTest {

  public static void assertCoordinatesEquals(final Point point,
    final double... coordinates) {
    Assert.assertEquals("Is Empty", false, point.isEmpty());
    Assert.assertEquals("Geometry Count", 1, point.getGeometryCount());
    Assert.assertNotNull("Not Null First Vertex", point.getVertex(0));
    Assert.assertEquals("Axis Count", coordinates.length, point.getAxisCount());
    for (int axisIndex = -1; axisIndex < point.getAxisCount() + 1; axisIndex++) {
      final double value = point.getCoordinate(axisIndex);
      if (axisIndex < 0 || axisIndex >= coordinates.length) {
        if (!Double.isNaN(value)) {
          Assert.failNotEquals("Value NaN", Double.NaN, value);
        }
      } else {
        Assert.assertEquals("Coordinate Value", coordinates[axisIndex], value);
      }
    }
  }

  public static void assertEmpty(final LineString line) {
    Assert.assertEquals("Is Empty", true, line.isEmpty());
    Assert.assertEquals("Geometry Count", 0, line.getGeometryCount());
    Assert.assertNull("Null First Vertex", line.getVertex(0));
    for (int axisIndex = -1; axisIndex < line.getAxisCount() + 1; axisIndex++) {
      final double value = line.getCoordinate(0, axisIndex);
      if (!Double.isNaN(value)) {
        Assert.failNotEquals("Value NaN", Double.NaN, value);
      }
    }
  }

  public static void assertEquals(final Point point,
    final double... coordinates) {
    final GeometryFactory geometryFactory = point.getGeometryFactory();

    final GeometryFactory geometryFactory2;
    final int axisCount = geometryFactory.getAxisCount();
    if (geometryFactory.getCoordinateSystem() instanceof ProjectedCoordinateSystem) {
      final ProjectedCoordinateSystem projectedCs = (ProjectedCoordinateSystem)geometryFactory.getCoordinateSystem();
      final GeographicCoordinateSystem geographicCoordinateSystem = projectedCs.getGeographicCoordinateSystem();
      geometryFactory2 = GeometryFactory.floating(
        geographicCoordinateSystem.getId(), axisCount);
    } else {
      geometryFactory2 = GeometryFactory.floating(26910, axisCount);
    }

    assertCoordinatesEquals(point, coordinates);

    final Point clone = point.clone();
    assertCoordinatesEquals(clone, coordinates);

    final Point converted = point.convert(geometryFactory);
    assertCoordinatesEquals(converted, coordinates);
    Assert.assertSame(point, converted);

    final Point convertedOther = point.convert(geometryFactory2);
    final Point convertedBack = convertedOther.convert(geometryFactory);
    assertCoordinatesEquals(convertedBack, coordinates);
    Assert.assertNotSame(point, convertedBack);

    final Point copy = point.copy(geometryFactory);
    assertCoordinatesEquals(copy, coordinates);
    Assert.assertNotSame(point, copy);

    final Point copyOther = point.convert(geometryFactory2);
    final Point copyBack = copyOther.convert(geometryFactory);
    assertCoordinatesEquals(copyBack, coordinates);
    Assert.assertNotSame(point, copyBack);

    final String string = point.toString();
    final Point pointString = geometryFactory.geometry(string);
    assertCoordinatesEquals(pointString, coordinates);

    final String wkt = point.toWkt();
    final Point pointWkt = geometryFactory.geometry(wkt);
    assertCoordinatesEquals(pointWkt, coordinates);

  }

  private void assertEquals(final double[] coordinates,
    final double[] coordinatesLessNaN, final Point pointCoordinatesListAllAxis,
    final Point pointCoordinatesListExtraAxis,
    final Point pointCoordinatesListLessAxis) {
    assertEquals(pointCoordinatesListAllAxis, coordinates);
    assertEquals(pointCoordinatesListExtraAxis, coordinates);
    assertEquals(pointCoordinatesListLessAxis, coordinatesLessNaN);
  }

  private void assertObjectContsructor(final GeometryFactory geometryFactory,
    final double[] coordinates, final double[] coordinatesLessNaN,
    final Point pointAll, final Point pointExtra, final Point pointLess) {
    final Point pointAllAxis = geometryFactory.point((Object)pointAll);
    final Point pointExtraAxis = geometryFactory.point((Object)pointExtra);
    final Point pointLessAxis = geometryFactory.point((Object)pointLess);

    assertEquals(coordinates, coordinatesLessNaN, pointAllAxis, pointExtraAxis,
      pointLessAxis);
  }

  @Test
  public void constructEmpty() {
    for (int axisCount = 2; axisCount < 4; axisCount++) {
      final GeometryFactory geometryFactory = GeometryFactory.fixed(26910,
        axisCount, 1000.0, 1000.0);

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

      final LineString pointCoordinatesSize0 = geometryFactory.lineString(new PointDouble(
        0));
      assertEmpty(pointCoordinatesSize0);

      // PointList Constructor
      final LineString pointCoordinatesListNull = geometryFactory.lineString((PointList)null);
      assertEmpty(pointCoordinatesListNull);

      final LineString pointCoordinatesListSize0 = geometryFactory.lineString(new DoubleCoordinatesList(
        0, axisCount));
      assertEmpty(pointCoordinatesListSize0);

      // double[] Constructor
      final LineString pointDoubleArray0Null = geometryFactory.lineString(0,
        (double[])null);
      assertEmpty(pointDoubleArray0Null);

      final LineString pointDoubleArray2Null = geometryFactory.lineString(2,
        (double[])null);
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
      final GeometryFactory geometryFactory = GeometryFactory.fixed(26910,
        axisCount, 1000.0, 1000.0);
      final GeometryFactory geometryFactoryExtra = GeometryFactory.floating(
        26910, axisCount + 1);
      final GeometryFactory geometryFactoryLess = GeometryFactory.floating(
        26910, axisCountLess);
      final double[] coordinatesExtra = new double[axisCount + 1];
      final double[] coordinates = new double[axisCount];
      final double[] coordinatesLess = new double[axisCountLess];
      final double[] coordinatesLessNaN = new double[axisCount];
      for (int i = 0; i < axisCount; i++) {
        double value;
        switch (i) {
          case 0:
            value = 500000;
          break;
          case 1:
            value = 6000000;
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
      assertEquals(coordinates, coordinatesLessNaN, pointDoubleAllAxis,
        pointDoubleExtraAxis, pointDoubleLessAxis);
      assertObjectContsructor(geometryFactory, coordinates, coordinatesLessNaN,
        pointDoubleAllAxis, pointDoubleExtraAxis, pointDoubleLessAxis);

      // Coordinates
      final Point pointCoordinatesAllAxis = geometryFactory.point(new PointDouble(
        coordinates));
      final Point pointCoordinatesExtraAxis = geometryFactory.point(new PointDouble(
        coordinatesExtra));
      final Point pointCoordinatesLessAxis = geometryFactory.point(new PointDouble(
        coordinatesLess));
      assertEquals(coordinates, coordinatesLessNaN, pointCoordinatesAllAxis,
        pointCoordinatesExtraAxis, pointCoordinatesLessAxis);
      assertObjectContsructor(geometryFactory, coordinates, coordinatesLessNaN,
        pointCoordinatesAllAxis, pointCoordinatesExtraAxis,
        pointCoordinatesLessAxis);

      // PointList
      final Point pointCoordinatesListAllAxis = geometryFactory.point(new DoubleCoordinatesList(
        axisCount, coordinates));
      final Point pointCoordinatesListExtraAxis = geometryFactory.point(new DoubleCoordinatesList(
        axisCount, coordinatesExtra));
      final Point pointCoordinatesListLessAxis = geometryFactory.point(new DoubleCoordinatesList(
        axisCountLess, coordinatesLess));
      assertEquals(coordinates, coordinatesLessNaN,
        pointCoordinatesListAllAxis, pointCoordinatesListExtraAxis,
        pointCoordinatesListLessAxis);
      assertObjectContsructor(geometryFactory, coordinates, coordinatesLessNaN,
        pointCoordinatesListAllAxis, pointCoordinatesListExtraAxis,
        pointCoordinatesListLessAxis);

      // Object Point
      final Point pointAll = pointDoubleAllAxis;
      final Point pointExtra = geometryFactoryExtra.point(coordinatesExtra);
      final Point pointLess = geometryFactoryLess.point(coordinatesLess);
      assertObjectContsructor(geometryFactory, coordinates, coordinatesLessNaN,
        pointAll, pointExtra, pointLess);
    }
  }

  @Test
  public void testFromFile() {
    TestUtil.doTestGeometry(getClass(), "LineString.csv");
  }

  @Test
  public void testSubList() {
    final GeometryFactory geometryFactory = GeometryFactory.wgs84();
    final LineString line = geometryFactory.lineString(2, 0.0, 0, 2, 2, 3, 3,
      4, 4, 5, 5);
    assertEmpty(line.subLine(0));

    TestUtil.equalsExact(2, line.subLine(2),
      geometryFactory.lineString(2, 0.0, 0, 2, 2));
    TestUtil.equalsExact(2, line.subLine(2, geometryFactory.point(10, 10)),
      geometryFactory.lineString(2, 0.0, 0, 2, 2, 10, 10));
    TestUtil.equalsExact(
      2,
      line.subLine(geometryFactory.point(-1, -1), 0, 2,
        geometryFactory.point(10, 10)),
      geometryFactory.lineString(2, -1.0, -1.0, 0.0, 0, 2, 2, 10, 10));

    final LineString actualFromToIndexMaxLength = line.subLine(
      geometryFactory.point(-1, -1), 3, 3, geometryFactory.point(10, 10));
    final LineString expectedFromToIndexMaxLength = geometryFactory.lineString(
      2, -1.0, -1.0, 4, 4, 5, 5, 10, 10);
    TestUtil.equalsExact(2, actualFromToIndexMaxLength,
      expectedFromToIndexMaxLength);
  }
}
