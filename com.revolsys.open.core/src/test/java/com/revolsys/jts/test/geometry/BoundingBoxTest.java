package com.revolsys.jts.test.geometry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.measure.Measure;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.junit.Assert;
import org.junit.Test;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.jts.TestConstants;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.PointList;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.PointDouble;
import com.revolsys.jts.util.EnvelopeUtil;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.MathUtil;

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
//expandToInclude(DataObject)
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
  private static final double[] NULL_BOUNDS = null;

  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  private void assertEnvelope(final BoundingBox boundingBox,
    final GeometryFactory geometryFactory, final boolean empty,
    final int axisCount, final double... bounds) {
    Assert.assertEquals("Geometry Factory", geometryFactory,
      boundingBox.getGeometryFactory());
    Assert.assertEquals("Empty", empty, boundingBox.isEmpty());
    Assert.assertEquals("Axis Count", axisCount, boundingBox.getAxisCount());
    Assert.assertEquals("Bounds", CollectionUtil.toList(bounds),
      CollectionUtil.toList(boundingBox.getBounds()));

    Unit unit = SI.METRE;
    Unit lengthUnit = SI.METRE;
    final StringBuffer wkt = new StringBuffer();
    final int srid = boundingBox.getSrid();
    if (geometryFactory == null) {
      Assert.assertEquals("coordinateSystem", null,
        boundingBox.getCoordinateSystem());
      Assert.assertEquals("srid", 0, srid);
    } else {
      if (srid > 0) {
        wkt.append("SRID=");
        wkt.append(srid);
        wkt.append(";");
      }
      Assert.assertEquals("srid", geometryFactory.getSrid(), srid);
      final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
      Assert.assertEquals("coordinateSystem", coordinateSystem,
        boundingBox.getCoordinateSystem());
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
        wkt.append(MathUtil.toString(bounds[axisIndex]));
      }
      wkt.append(' ');
      for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
        if (axisIndex > 0) {
          wkt.append(',');
        }
        wkt.append(MathUtil.toString(bounds[axisCount + axisIndex]));
      }
      wkt.append(')');
      for (int i = 0; i < axisCount; i++) {
        assertMinMax(boundingBox, i, bounds[i], bounds[axisCount + i]);

        Assert.assertEquals("Minimum " + i, Measure.valueOf(bounds[i], unit),
          boundingBox.getMinimum(i));
        Assert.assertEquals("Maximum" + i,
          Measure.valueOf(bounds[axisCount + i], unit),
          boundingBox.getMaximum(i));

        Assert.assertEquals("Minimum " + i, bounds[i],
          boundingBox.getMinimum(i, unit), 0);
        Assert.assertEquals("Maximum " + i, bounds[axisCount + i],
          boundingBox.getMaximum(i, unit), 0);
      }
    }
    Assert.assertEquals("MinX", minX, boundingBox.getMinX(), 0);
    Assert.assertEquals("MaxX", maxX, boundingBox.getMaxX(), 0);

    Assert.assertEquals("MinimumX", Measure.valueOf(minX, unit),
      boundingBox.getMinimum(0));
    Assert.assertEquals("MaximumX", Measure.valueOf(maxX, unit),
      boundingBox.getMaximum(0));

    Assert.assertEquals("MinimumX", minX, boundingBox.getMinimum(0, unit), 0);
    Assert.assertEquals("MaximumX", maxX, boundingBox.getMaximum(0, unit), 0);

    Assert.assertEquals("MinY", minY, boundingBox.getMinY(), 0);
    Assert.assertEquals("MaxY", maxY, boundingBox.getMaxY(), 0);

    Assert.assertEquals("MinimumY", Measure.valueOf(minY, unit),
      boundingBox.getMinimum(1));
    Assert.assertEquals("MaximumY", Measure.valueOf(maxY, unit),
      boundingBox.getMaximum(1));

    Assert.assertEquals("MinimumY", minY, boundingBox.getMinimum(1, unit), 0);
    Assert.assertEquals("MaximumY", maxY, boundingBox.getMaximum(1, unit), 0);

    Assert.assertEquals("WKT", wkt.toString(), boundingBox.toString());
    Assert.assertEquals("Area", area, boundingBox.getArea(), 0);
    Assert.assertEquals("Width", width, boundingBox.getWidth(), 0);
    Assert.assertEquals("Width", width, boundingBox.getWidthLength()
      .doubleValue(lengthUnit), 0);
    Assert.assertEquals("Width", Measure.valueOf(width, lengthUnit),
      boundingBox.getWidthLength());
    Assert.assertEquals("Height", height, boundingBox.getHeight(), 0);
    Assert.assertEquals("Height", height, boundingBox.getHeightLength()
      .doubleValue(lengthUnit), 0);
    Assert.assertEquals("Height", Measure.valueOf(height, lengthUnit),
      boundingBox.getHeightLength());

    Assert.assertEquals("Aspect Ratio", width / height,
      boundingBox.getAspectRatio(), 0);

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
    final BoundingBox emptyNull = new Envelope((Point[])null);
    assertEnvelope(emptyNull, null, true, 0, NULL_BOUNDS);

    final BoundingBox emptyList = new Envelope(new Point[0]);
    assertEnvelope(emptyList, null, true, 0, NULL_BOUNDS);

    final BoundingBox emptyListWithNulls = new Envelope((Point)null);
    assertEnvelope(emptyListWithNulls, null, true, 0, NULL_BOUNDS);

    // Different number of axis and values
    for (int axisCount = 2; axisCount < 6; axisCount++) {
      for (int valueCount = 1; valueCount < 10; valueCount++) {
        Point[] points = new Point[valueCount];
        final double[] bounds = EnvelopeUtil.createBounds(axisCount);
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
        final Envelope noGeometryFactory = new Envelope(points);
        assertEnvelope(noGeometryFactory, null, false, axisCount, bounds);

        final GeometryFactory gfFloating = GeometryFactory.getFactory(4326,
          axisCount);
        assertEnvelope(new Envelope(gfFloating, points), gfFloating, false,
          axisCount, bounds);

        final GeometryFactory gfFixed = GeometryFactory.getFactory(4326,
          axisCount, 10, 10);

        points = gfFixed.getPrecise(points);
        final double[] boundsPrecise = gfFixed.copyPrecise(bounds);
        assertEnvelope(new Envelope(gfFixed, points), gfFixed, false,
          axisCount, boundsPrecise);
      }
    }
  }

  @Test
  public void testConstructorDoubleArray() {
    // Empty
    assertEnvelope(new Envelope(0), null, true, 0, NULL_BOUNDS);
    assertEnvelope(new Envelope(-1), null, true, 0, NULL_BOUNDS);
    assertEnvelope(new Envelope(2), null, true, 0, NULL_BOUNDS);
    assertEnvelope(new Envelope(2, NULL_BOUNDS), null, true, 0, NULL_BOUNDS);

    // Different number of axis and values
    for (int axisCount = 1; axisCount < 6; axisCount++) {
      for (int valueCount = 1; valueCount < 10; valueCount++) {
        final double[] values = new double[axisCount * valueCount];
        final double[] bounds = EnvelopeUtil.createBounds(axisCount);
        for (int i = 0; i < valueCount; i++) {
          for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
            final double value = Math.random() * 360 - 180;
            values[i * axisCount + axisIndex] = value;
            final double min = bounds[axisIndex];
            if (Double.isNaN(min) || value < min) {
              bounds[axisIndex] = value;
            }
            final double max = bounds[axisCount + axisIndex];
            if (Double.isNaN(max) || value > max) {
              bounds[axisCount + axisIndex] = value;
            }
          }
        }
        final Envelope noGeometryFactory = new Envelope(axisCount, values);
        assertEnvelope(noGeometryFactory, null, false, axisCount, bounds);

        if (axisCount > 1) {
          final GeometryFactory gfFloating = GeometryFactory.getFactory(4326,
            axisCount);
          assertEnvelope(new Envelope(gfFloating, axisCount, values),
            gfFloating, false, axisCount, bounds);

          final GeometryFactory gfFixed = GeometryFactory.getFactory(4326,
            axisCount, 10, 10);
          final double[] valuesPrecise = gfFixed.copyPrecise(values);
          final double[] boundsPrecise = gfFixed.copyPrecise(bounds);
          assertEnvelope(new Envelope(gfFixed, axisCount, valuesPrecise),
            gfFixed, false, axisCount, boundsPrecise);
        }
      }
    }
  }

  @Test
  public void testConstructorEmpty() {
    final BoundingBox empty = new Envelope();
    assertEnvelope(empty, null, true, 0, NULL_BOUNDS);

    final BoundingBox emptyNullGeometryFactory = new Envelope(
      (GeometryFactory)null);
    assertEnvelope(emptyNullGeometryFactory, null, true, 0, NULL_BOUNDS);

    final BoundingBox emptyWithGeometryFactory = new Envelope(
      UTM10_GF_2_FLOATING);
    assertEnvelope(emptyWithGeometryFactory, UTM10_GF_2_FLOATING, true, 0,
      NULL_BOUNDS);
  }

  @Test
  public void testConstructorIterable() {
    final BoundingBox emptyNull = new Envelope((Iterable<Point>)null);
    assertEnvelope(emptyNull, null, true, 0, NULL_BOUNDS);

    final BoundingBox emptyList = new Envelope(new ArrayList<Point>());
    assertEnvelope(emptyList, null, true, 0, NULL_BOUNDS);

    final BoundingBox emptyListWithNulls = new Envelope(
      Collections.<Point> singleton(null));
    assertEnvelope(emptyListWithNulls, null, true, 0, NULL_BOUNDS);

    final BoundingBox emptyNullCoordinatesList = new Envelope(
      (PointList)null);
    assertEnvelope(emptyNullCoordinatesList, null, true, 0, NULL_BOUNDS);

    final BoundingBox emptyCoordinatesList = new Envelope(
      new DoubleCoordinatesList(0, 2));
    assertEnvelope(emptyCoordinatesList, null, true, 0, NULL_BOUNDS);

    // Different number of axis and values
    for (int axisCount = 2; axisCount < 6; axisCount++) {
      for (int valueCount = 1; valueCount < 10; valueCount++) {
        final List<Point> points = new ArrayList<>();
        final double[] bounds = EnvelopeUtil.createBounds(axisCount);
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
        final Envelope noGeometryFactory = new Envelope(points);
        assertEnvelope(noGeometryFactory, null, false, axisCount, bounds);

        final GeometryFactory gfFloating = GeometryFactory.getFactory(4326,
          axisCount);
        assertEnvelope(new Envelope(gfFloating, points), gfFloating, false,
          axisCount, bounds);

        final GeometryFactory gfFixed = GeometryFactory.getFactory(4326,
          axisCount, 10, 10);

        final double[] boundsPrecise = gfFixed.copyPrecise(bounds);
        assertEnvelope(new Envelope(gfFixed, points), gfFixed, false,
          axisCount, boundsPrecise);
      }
    }
  }

}
