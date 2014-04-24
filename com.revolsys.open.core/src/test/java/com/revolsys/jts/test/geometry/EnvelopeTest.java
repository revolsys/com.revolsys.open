package com.revolsys.jts.test.geometry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.jts.TestConstants;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.util.EnvelopeUtil;
import com.revolsys.util.CollectionUtil;

public class EnvelopeTest implements TestConstants {
  private static final double[] NULL_BOUNDS = null;

  private void assertEnvelope(final BoundingBox boundingBox,
    final GeometryFactory geometryFactory, final boolean empty,
    final int axisCount, final double... bounds) {
    Assert.assertEquals("Geometry Factory", geometryFactory,
      boundingBox.getGeometryFactory());
    Assert.assertEquals("Empty", empty, boundingBox.isEmpty());
    Assert.assertEquals("Axis Count", axisCount, boundingBox.getAxisCount());
    Assert.assertEquals("Bounds", CollectionUtil.toList(bounds),
      CollectionUtil.toList(boundingBox.getBounds()));
    assertMinMax(boundingBox, -1, Double.NaN, Double.NaN);
    assertMinMax(boundingBox, axisCount + 1, Double.NaN, Double.NaN);
    if (bounds == null) {
      Assert.assertEquals("MinX", Double.NaN, boundingBox.getMinX(), 0);
      Assert.assertEquals("MinY", Double.NaN, boundingBox.getMinY(), 0);
      Assert.assertEquals("MaxX", Double.NaN, boundingBox.getMaxX(), 0);
      Assert.assertEquals("MaxY", Double.NaN, boundingBox.getMaxY(), 0);
    } else {
      for (int i = 0; i < axisCount; i++) {
        assertMinMax(boundingBox, i, bounds[i], bounds[axisCount + i]);
      }
      if (axisCount > 0) {
        Assert.assertEquals("MinX", bounds[0], boundingBox.getMinX(), 0);
        Assert.assertEquals("MaxX", bounds[axisCount], boundingBox.getMaxX(), 0);
      }
      if (axisCount > 1) {
        Assert.assertEquals("MinY", bounds[1], boundingBox.getMinY(), 0);
        Assert.assertEquals("MaxY", bounds[axisCount + 1],
          boundingBox.getMaxY(), 0);
      }

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
    final BoundingBox emptyNull = new Envelope((Coordinates[])null);
    assertEnvelope(emptyNull, null, true, 0, NULL_BOUNDS);

    final BoundingBox emptyList = new Envelope(new Coordinates[0]);
    assertEnvelope(emptyList, null, true, 0, NULL_BOUNDS);

    final BoundingBox emptyListWithNulls = new Envelope((Coordinates)null);
    assertEnvelope(emptyListWithNulls, null, true, 0, NULL_BOUNDS);

    // Different number of axis and values
    for (int axisCount = 2; axisCount < 4; axisCount++) {
      for (int valueCount = 1; valueCount < 10; valueCount++) {
        final Coordinates[] points = new Coordinates[valueCount];
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
          points[i] = new DoubleCoordinates(values);
        }
        final Envelope noGeometryFactory = new Envelope(points);
        assertEnvelope(noGeometryFactory, null, false, axisCount, bounds);

        final GeometryFactory gfFloating = GeometryFactory.getFactory(4326,
          axisCount);
        assertEnvelope(new Envelope(gfFloating, points), gfFloating, false,
          axisCount, bounds);

        final GeometryFactory gfFixed = GeometryFactory.getFactory(4326,
          axisCount, 10, 10);

        gfFixed.makePrecise(points);
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
    for (int axisCount = 1; axisCount < 4; axisCount++) {
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
    final BoundingBox emptyNull = new Envelope((Iterable<Coordinates>)null);
    assertEnvelope(emptyNull, null, true, 0, NULL_BOUNDS);

    final BoundingBox emptyList = new Envelope(new ArrayList<Coordinates>());
    assertEnvelope(emptyList, null, true, 0, NULL_BOUNDS);

    final BoundingBox emptyListWithNulls = new Envelope(
      Collections.<Coordinates> singleton(null));
    assertEnvelope(emptyListWithNulls, null, true, 0, NULL_BOUNDS);

    final BoundingBox emptyNullCoordinatesList = new Envelope(
      (CoordinatesList)null);
    assertEnvelope(emptyNullCoordinatesList, null, true, 0, NULL_BOUNDS);

    final BoundingBox emptyCoordinatesList = new Envelope(
      new DoubleCoordinatesList(0, 2));
    assertEnvelope(emptyCoordinatesList, null, true, 0, NULL_BOUNDS);

    // Different number of axis and values
    for (int axisCount = 2; axisCount < 4; axisCount++) {
      for (int valueCount = 1; valueCount < 10; valueCount++) {
        final List<Coordinates> points = new ArrayList<>();
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
          points.add(new DoubleCoordinates(values));
        }
        final Envelope noGeometryFactory = new Envelope(points);
        assertEnvelope(noGeometryFactory, null, false, axisCount, bounds);

        final GeometryFactory gfFloating = GeometryFactory.getFactory(4326,
          axisCount);
        assertEnvelope(new Envelope(gfFloating, points), gfFloating, false,
          axisCount, bounds);

        final GeometryFactory gfFixed = GeometryFactory.getFactory(4326,
          axisCount, 10, 10);

        gfFixed.makePrecise(points);
        final double[] boundsPrecise = gfFixed.copyPrecise(bounds);
        assertEnvelope(new Envelope(gfFixed, points), gfFixed, false,
          axisCount, boundsPrecise);
      }
    }
  }

}
