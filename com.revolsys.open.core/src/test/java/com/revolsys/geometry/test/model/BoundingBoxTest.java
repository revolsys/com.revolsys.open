package com.revolsys.geometry.test.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.measure.Measure;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.junit.Assert;
import org.junit.Test;

import com.revolsys.collection.list.Lists;
import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Punctual;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleXY;
import com.revolsys.geometry.model.impl.PointDouble;
import com.revolsys.geometry.test.TestConstants;
import com.revolsys.geometry.util.RectangleUtil;
import com.revolsys.util.number.Doubles;

// TODO
//clipToCoordinateSystem()
//clone()
//covers(Geometry)
//covers(Point)
//convert(GeometryFactory)
//covers(BoundingBox)
//covers(Point)
//covers(double, double)
//distance(BoundingBox)
//distance(Geometry)
//equals(Object)
//expand(Point)
//expand(double)
//expand(double, double)
//expandPercent(double)
//expandPercent(double, double)
//expandToInclude(BoundingBox)
//expandToInclude(Record)
//expandToInclude(Geometry)
//expandToInclude(Point)
//getBottomLeftPoint()
//getBottomRightPoint()
//getCentre()
//getCentreX()
//getCentreY()
//getCornerPoint(int)
//getCornerPoints()
//getTopLeftPoint()
//getTopRightPoint()
//hashCode()
//intersection(BoundingBox)
//intersects(BoundingBox)
//intersects(Point)
//intersects(double, double)
//intersects(Geometry)
//move(double, double)
//toGeometry()
//toPolygon()
//toPolygon(GeometryFactory)
//toPolygon(GeometryFactory, int)
//toPolygon(GeometryFactory, int, int)
//toPolygon(int)
//toPolygon(int, int)

public class BoundingBoxTest implements TestConstants {
  private static final List<GeometryFactory> GEOMETRY_FACTORIES = Arrays.asList(
    GeometryFactory.DEFAULT_3D.convertAxisCount(2), GeometryFactory.fixed2d(3005, 1.0, 1.0));

  private static final double[] NULL_BOUNDS = null;

  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  private void assertBoundingBox(final Geometry geometry, final BoundingBox boundingBox,
    final GeometryFactory geometryFactory, final boolean empty, final int axisCount,
    final double... bounds) {
    Assert.assertEquals("Geometry Factory", geometryFactory, boundingBox.getGeometryFactory());
    Assert.assertEquals("Empty", empty, boundingBox.isEmpty());
    Assert.assertEquals("Axis Count", axisCount, boundingBox.getAxisCount());
    Assert.assertEquals("Bounds", Lists.newArray(bounds),
      Lists.newArray(boundingBox.getMinMaxValues()));

    Unit unit = SI.METRE;
    Unit lengthUnit = SI.METRE;
    final StringBuilder wkt = new StringBuilder();
    final int srid = boundingBox.getCoordinateSystemId();
    if (geometryFactory == GeometryFactory.DEFAULT_3D) {
      Assert.assertEquals("coordinateSystem", null, boundingBox.getCoordinateSystem());
      Assert.assertEquals("srid", 0, srid);
    } else {
      if (srid > 0) {
        wkt.append("SRID=");
        wkt.append(srid);
        wkt.append(";");
      }
      Assert.assertEquals("srid", geometryFactory.getCoordinateSystemId(), srid);
      final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
      Assert.assertEquals("coordinateSystem", coordinateSystem, boundingBox.getCoordinateSystem());
      if (coordinateSystem != null) {
        unit = coordinateSystem.getUnit();
        lengthUnit = coordinateSystem.getLengthUnit();
      }
    }
    wkt.append("BBOX");
    assertMinMax(boundingBox, -1, Double.NaN, Double.NaN);
    assertMinMax(boundingBox, axisCount + 1, Double.NaN, Double.NaN);
    double width = 0;
    double height = 0;

    double minX = Double.NaN;
    double maxX = Double.NaN;
    double minY = Double.NaN;
    double maxY = Double.NaN;
    double area = 0;
    if (bounds == null) {
      wkt.append(" EMPTY");

    } else {
      minX = bounds[0];
      maxX = bounds[axisCount];
      if (axisCount > 1) {
        minY = bounds[1];
        maxY = bounds[axisCount + 1];
        width = Math.abs(maxX - minX);
        height = Math.abs(maxY - minY);
        area = width * height;
      } else {
        area = 0;
      }
      if (axisCount == 3) {
        wkt.append(" Z");
      } else if (axisCount == 4) {
        wkt.append(" ZM");
      } else if (axisCount != 2) {
        wkt.append(" ");
        wkt.append(axisCount);
      }
      wkt.append("(");
      for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
        if (axisIndex > 0) {
          wkt.append(',');
        }
        wkt.append(Doubles.toString(bounds[axisIndex]));
      }
      wkt.append(' ');
      for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
        if (axisIndex > 0) {
          wkt.append(',');
        }
        wkt.append(Doubles.toString(bounds[axisCount + axisIndex]));
      }
      wkt.append(')');
      for (int i = 0; i < axisCount; i++) {
        assertMinMax(boundingBox, i, bounds[i], bounds[axisCount + i]);

        Assert.assertEquals("Minimum " + i, Measure.valueOf(bounds[i], unit),
          boundingBox.getMinimum(i));
        Assert.assertEquals("Maximum" + i, Measure.valueOf(bounds[axisCount + i], unit),
          boundingBox.getMaximum(i));

        Assert.assertEquals("Minimum " + i, bounds[i], boundingBox.getMinimum(i, unit), 0);
        Assert.assertEquals("Maximum " + i, bounds[axisCount + i], boundingBox.getMaximum(i, unit),
          0);
      }
    }
    Assert.assertEquals("MinX", minX, boundingBox.getMinX(), 0);
    Assert.assertEquals("MaxX", maxX, boundingBox.getMaxX(), 0);

    Assert.assertEquals("MinimumX", Measure.valueOf(minX, unit), boundingBox.getMinimum(0));
    Assert.assertEquals("MaximumX", Measure.valueOf(maxX, unit), boundingBox.getMaximum(0));

    Assert.assertEquals("MinimumX", minX, boundingBox.getMinimum(0, unit), 0);
    Assert.assertEquals("MaximumX", maxX, boundingBox.getMaximum(0, unit), 0);

    Assert.assertEquals("MinY", minY, boundingBox.getMinY(), 0);
    Assert.assertEquals("MaxY", maxY, boundingBox.getMaxY(), 0);

    Assert.assertEquals("MinimumY", Measure.valueOf(minY, unit), boundingBox.getMinimum(1));
    Assert.assertEquals("MaximumY", Measure.valueOf(maxY, unit), boundingBox.getMaximum(1));

    Assert.assertEquals("MinimumY", minY, boundingBox.getMinimum(1, unit), 0);
    Assert.assertEquals("MaximumY", maxY, boundingBox.getMaximum(1, unit), 0);

    Assert.assertEquals("WKT", wkt.toString(), boundingBox.toString());
    Assert.assertEquals("Area", area, boundingBox.getArea(), 0);
    Assert.assertEquals("Width", width, boundingBox.getWidth(), 0);
    Assert.assertEquals("Width", width, boundingBox.getWidthLength().doubleValue(lengthUnit), 0);
    Assert.assertEquals("Width", Measure.valueOf(width, lengthUnit), boundingBox.getWidthLength());
    Assert.assertEquals("Height", height, boundingBox.getHeight(), 0);
    Assert.assertEquals("Height", height, boundingBox.getHeightLength().doubleValue(lengthUnit), 0);
    Assert.assertEquals("Height", Measure.valueOf(height, lengthUnit),
      boundingBox.getHeightLength());

    Assert.assertEquals("Aspect Ratio", width / height, boundingBox.getAspectRatio(), 0);

    if (geometry != null) {
      if (geometry.isEmpty()) {
        final boolean intersects = geometry.intersects(boundingBox);
        Assert.assertFalse("Bounding Box Intersects Empty", intersects);
      } else {
        final boolean intersects = geometry.intersects(boundingBox);
        Assert.assertTrue("Bounding Box Intersects", intersects);

        // Test outside
        Assert.assertFalse("Bounding Box Intersects",
          geometry.intersects(boundingBox.move(-100, -100)));
        Assert.assertFalse("Bounding Box Intersects",
          geometry.intersects(boundingBox.move(-100, 0)));
        Assert.assertFalse("Bounding Box Intersects",
          geometry.intersects(boundingBox.move(-100, 100)));
        Assert.assertFalse("Bounding Box Intersects",
          geometry.intersects(boundingBox.move(0, -100)));
        Assert.assertFalse("Bounding Box Intersects",
          geometry.intersects(boundingBox.move(0, 100)));
        Assert.assertFalse("Bounding Box Intersects",
          geometry.intersects(boundingBox.move(100, -100)));
        Assert.assertFalse("Bounding Box Intersects",
          geometry.intersects(boundingBox.move(100, 0)));
        Assert.assertFalse("Bounding Box Intersects",
          geometry.intersects(boundingBox.move(100, 100)));

      }
    }
  }

  private void assertIntersects(final boolean intersectsExpected, final double minX1,
    final double minY1, final double maxX1, final double maxY1, final double minX2,
    final double minY2, final double maxX2, final double maxY2) {
    {
      final BoundingBox boundingBox1 = new BoundingBoxDoubleXY(minX1, minY1, maxX1, maxY1);
      final BoundingBox boundingBox2 = new BoundingBoxDoubleXY(minX2, minY2, maxX2, maxY2);
      final boolean intersectsActual = boundingBox1.intersects(boundingBox2);
      Assert.assertEquals("Intersects", intersectsExpected, intersectsActual);
    }
    {
      final BoundingBox boundingBox1 = new BoundingBoxDoubleXY(minX1, minY1, maxX1, maxY1);
      final BoundingBox boundingBox2 = new BoundingBoxDoubleXY(minX2, minY2, maxX2, maxY2);
      final boolean intersectsActual = boundingBox1.intersects(boundingBox2);
      Assert.assertEquals("Intersects", intersectsExpected, intersectsActual);
    }
  }

  private void assertMinMax(final BoundingBox boundingBox, final int axisIndex,
    final double expectedMin, final double expectedMax) {
    final double min = boundingBox.getMin(axisIndex);
    Assert.assertEquals("Min " + axisIndex, expectedMin, min, 0);
    final double max = boundingBox.getMax(axisIndex);
    Assert.assertEquals("Max " + axisIndex, expectedMax, max, 0);
  }

  @Test
  public void testConstructorCoordinatesArray() {

    final BoundingBox emptyList = BoundingBoxDoubleXY.newBoundingBox(new Point[0]);
    assertBoundingBox(null, emptyList, GeometryFactory.DEFAULT_3D, true, 2, NULL_BOUNDS);

    // Different number of axis and values
    for (int axisCount = 2; axisCount < 6; axisCount++) {
      for (int valueCount = 1; valueCount < 10; valueCount++) {
        Point[] points = new Point[valueCount];
        final double[] bounds = RectangleUtil.newBounds(axisCount);
        for (int i = 0; i < valueCount; i++) {
          final double[] values = new double[axisCount];
          for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
            final double value = Math.random() * 360 - 180;
            values[axisIndex] = value;
            final double min = bounds[axisIndex];
            if (Double.isNaN(min) || value < min) {
              bounds[axisIndex] = value;
            }
            final double max = bounds[axisCount + axisIndex];
            if (Double.isNaN(max) || value > max) {
              bounds[axisCount + axisIndex] = value;
            }
          }
          points[i] = new PointDouble(values);
        }
        final GeometryFactory noGf = GeometryFactory.DEFAULT_3D.convertAxisCount(axisCount);
        final BoundingBox noGeometryFactory = noGf.newBoundingBox(axisCount, points);
        assertBoundingBox(null, noGeometryFactory, noGf, false, axisCount, bounds);

        final GeometryFactory gfFloating = GeometryFactory.floating(4326, axisCount);
        assertBoundingBox(null, gfFloating.newBoundingBox(axisCount, points), gfFloating, false,
          axisCount, bounds);

        final double[] scales = GeometryFactory.newScalesFixed(axisCount, 10.0);
        final GeometryFactory gfFixed = GeometryFactory.fixed(4326, axisCount, scales);

        points = gfFixed.getPrecise(points);
        final double[] boundsPrecise = gfFixed.copyPrecise(bounds);
        assertBoundingBox(null, gfFixed.newBoundingBox(axisCount, points), gfFixed, false,
          axisCount, boundsPrecise);
      }
    }
  }

  @Test
  public void testConstructorEmpty() {
    final BoundingBox empty = BoundingBox.empty();
    assertBoundingBox(null, empty, GeometryFactory.DEFAULT_3D, true, 2, NULL_BOUNDS);

    final BoundingBox emptyNullGeometryFactory = BoundingBox.empty();
    assertBoundingBox(null, emptyNullGeometryFactory, GeometryFactory.DEFAULT_3D, true, 2,
      NULL_BOUNDS);

    final BoundingBox emptyWithGeometryFactory = UTM10_GF_2_FLOATING.newBoundingBoxEmpty();
    assertBoundingBox(null, emptyWithGeometryFactory, UTM10_GF_2_FLOATING, true, 2, NULL_BOUNDS);
  }

  @Test
  public void testConstructorIterable() {
    final BoundingBox emptyNull = BoundingBoxDoubleXY.newBoundingBox((Iterable<Point>)null);
    assertBoundingBox(null, emptyNull, GeometryFactory.DEFAULT_3D, true, 2, NULL_BOUNDS);

    final BoundingBox emptyList = BoundingBoxDoubleXY.newBoundingBox(new ArrayList<Point>());
    assertBoundingBox(null, emptyList, GeometryFactory.DEFAULT_3D, true, 2, NULL_BOUNDS);

    final BoundingBox emptyListWithNulls = BoundingBoxDoubleXY
      .newBoundingBox(Collections.<Point> singleton(null));
    assertBoundingBox(null, emptyListWithNulls, GeometryFactory.DEFAULT_3D, true, 2, NULL_BOUNDS);

    final BoundingBox emptyNullCoordinatesList = BoundingBoxDoubleXY
      .newBoundingBox((Iterable<Point>)null);
    assertBoundingBox(null, emptyNullCoordinatesList, GeometryFactory.DEFAULT_3D, true, 2,
      NULL_BOUNDS);

    final BoundingBox emptyCoordinatesList = BoundingBoxDoubleXY
      .newBoundingBox(new ArrayList<Point>());
    assertBoundingBox(null, emptyCoordinatesList, GeometryFactory.DEFAULT_3D, true, 2, NULL_BOUNDS);

    // Different number of axis and values
    for (int axisCount = 2; axisCount < 6; axisCount++) {
      for (int valueCount = 1; valueCount < 10; valueCount++) {
        final List<Point> points = new ArrayList<>();
        final double[] bounds = RectangleUtil.newBounds(axisCount);
        for (int i = 0; i < valueCount; i++) {
          final double[] values = new double[axisCount];
          for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
            final double value = Math.random() * 360 - 180;
            values[axisIndex] = value;
            final double min = bounds[axisIndex];
            if (Double.isNaN(min) || value < min) {
              bounds[axisIndex] = value;
            }
            final double max = bounds[axisCount + axisIndex];
            if (Double.isNaN(max) || value > max) {
              bounds[axisCount + axisIndex] = value;
            }
          }
          points.add(new PointDouble(values));
        }
        final GeometryFactory noGf = GeometryFactory.DEFAULT_3D.convertAxisCount(axisCount);
        final BoundingBox noGeometryFactory = noGf.newBoundingBox(axisCount, points);
        assertBoundingBox(null, noGeometryFactory, noGf, false, axisCount, bounds);

        final GeometryFactory gfFloating = GeometryFactory.floating(4326, axisCount);
        assertBoundingBox(null, gfFloating.newBoundingBox(axisCount, points), gfFloating, false,
          axisCount, bounds);

        final double[] scales = GeometryFactory.newScalesFixed(axisCount, 10.0);
        final GeometryFactory gfFixed = GeometryFactory.fixed(4326, axisCount, scales);

        final double[] boundsPrecise = gfFixed.copyPrecise(bounds);
        assertBoundingBox(null, gfFixed.newBoundingBox(axisCount, points), gfFixed, false,
          axisCount, boundsPrecise);
      }
    }
  }

  @Test
  public void testIntersects() {
    assertIntersects(//
      true, //
      0, 0, 10, 10, //
      0, 0, 10, 10 //
    );
    assertIntersects(//
      true, //
      0, 0, 10, 10, //
      2, 2, 4, 4//
    );
    assertIntersects(//
      true, //
      0, 0, 10, 10, //
      10, 10, 11, 11//
    );
    assertIntersects(//
      true, //
      0, 0, 10, 10, //
      -1, -1, 0, 0//
    );
    assertIntersects(//
      true, //
      0, 0, 10, 10, //
      -1, 10, 0, 11//
    );
    assertIntersects(//
      false, //
      0, 0, 10, 10, //
      12, 12, 14, 14//
    );
  }

  @Test
  public void testLineString() {
    for (final GeometryFactory geometryFactory : GEOMETRY_FACTORIES) {
      final LineString empty = geometryFactory.lineString();
      final BoundingBox boundingBoxEmpty = empty.getBoundingBox();
      assertBoundingBox(empty, boundingBoxEmpty, geometryFactory, true, 2, NULL_BOUNDS);

      final LineString geometry1 = geometryFactory.lineString(2, 3.0, 4.0, 1.0, 2.0);
      final BoundingBox boundingBox1 = geometry1.getBoundingBox();
      assertBoundingBox(geometry1, boundingBox1, geometryFactory, false, 2, 1.0, 2.0, 3.0, 4.0);
    }
  }

  @Test
  public void testMultiLineString() {
    for (final GeometryFactory geometryFactory : GEOMETRY_FACTORIES) {
      final Lineal empty = geometryFactory.lineal();
      final BoundingBox boundingBoxEmpty = empty.getBoundingBox();
      assertBoundingBox(empty, boundingBoxEmpty, geometryFactory, true, 2, NULL_BOUNDS);

      final Lineal geometry1 = geometryFactory.lineal(2, new double[][] {
        {
          3.0, 4.0, 1.0, 2.0
        }, {
          7.0, 8.0, 5.0, 6.0
        }
      });
      final BoundingBox boundingBox1 = geometry1.getBoundingBox();
      assertBoundingBox(geometry1, boundingBox1, geometryFactory, false, 2, 1.0, 2.0, 7.0, 8.0);
    }
  }

  @Test
  public void testMultiPoint() {
    for (final GeometryFactory geometryFactory : GEOMETRY_FACTORIES) {
      final Punctual empty = geometryFactory.punctual();
      final BoundingBox boundingBoxEmpty = empty.getBoundingBox();
      assertBoundingBox(empty, boundingBoxEmpty, geometryFactory, true, 2, NULL_BOUNDS);

      final Punctual geometry1 = geometryFactory.punctual(2, 3.0, 4.0, 1.0, 2.0);
      final BoundingBox boundingBox1 = geometry1.getBoundingBox();
      assertBoundingBox(geometry1, boundingBox1, geometryFactory, false, 2, 1.0, 2.0, 3.0, 4.0);
    }
  }

  @Test
  public void testPoint() {
    for (final GeometryFactory geometryFactory : GEOMETRY_FACTORIES) {
      final Point empty = geometryFactory.point();
      final BoundingBox boundingBoxEmpty = empty.getBoundingBox();
      assertBoundingBox(empty, boundingBoxEmpty, geometryFactory, true, 2, NULL_BOUNDS);

      final Point geometry1 = geometryFactory.point(1, 2);
      final BoundingBox boundingBox10 = geometry1.getBoundingBox();
      assertBoundingBox(geometry1, boundingBox10, geometryFactory, false, 2, 1.0, 2.0, 1.0, 2.0);
    }
  }
}
