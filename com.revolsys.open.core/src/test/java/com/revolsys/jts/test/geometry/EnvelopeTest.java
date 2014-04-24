package com.revolsys.jts.test.geometry;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.jts.TestConstants;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.GeometryFactory;

public class EnvelopeTest implements TestConstants {
  private void assertEnvelope(final BoundingBox boundingBox,
    final GeometryFactory geometryFactory, final boolean empty,
    final int axisCount, final double... bounds) {
    Assert.assertEquals("Geometry Factory", geometryFactory,
      boundingBox.getGeometryFactory());
    Assert.assertEquals("Empty", empty, boundingBox.isEmpty());
    Assert.assertEquals("Axis Count", axisCount, boundingBox.getAxisCount());
    Assert.assertEquals("Bounds", bounds, boundingBox.getBounds());
    assertMinMax(boundingBox, 0, Double.NaN, Double.NaN);
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
  public void testConstructor() {
    final BoundingBox empty = new Envelope();
    assertEnvelope(empty, null, true, 0, (double[])null);
  }

  @Test
  public void testConstructorBounds() {
  }

  @Test
  public void testConstructorCoordinatesArray() {
    final BoundingBox emptyNull = new Envelope((Coordinates[])null);
    assertEnvelope(emptyNull, null, true, 0, (double[])null);

    final BoundingBox emptyList = new Envelope(new Coordinates[0]);
    assertEnvelope(emptyList, null, true, 0, (double[])null);

    final BoundingBox emptyListWithNulls = new Envelope((Coordinates)null);
    assertEnvelope(emptyListWithNulls, null, true, 0, (double[])null);
  }

  @Test
  public void testConstructorGeometryFactory() {
    final BoundingBox emptyNullGeometryFactory = new Envelope(
      (GeometryFactory)null);
    assertEnvelope(emptyNullGeometryFactory, null, true, 0, (double[])null);

    final BoundingBox emptyWithGeometryFactory = new Envelope(
      UTM10_GF_2_FLOATING);
    assertEnvelope(emptyWithGeometryFactory, UTM10_GF_2_FLOATING, true, 0,
      (double[])null);
  }

  @Test
  public void testConstructorIterable() {
    final BoundingBox emptyNull = new Envelope((Iterable<Coordinates>)null);
    assertEnvelope(emptyNull, null, true, 0, (double[])null);

    final BoundingBox emptyList = new Envelope(new ArrayList<Coordinates>());
    assertEnvelope(emptyList, null, true, 0, (double[])null);

    final BoundingBox emptyListWithNulls = new Envelope(
      Collections.<Coordinates> singleton(null));
    assertEnvelope(emptyListWithNulls, null, true, 0, (double[])null);

    final BoundingBox emptyNullCoordinatesList = new Envelope(
      (CoordinatesList)null);
    assertEnvelope(emptyNullCoordinatesList, null, true, 0, (double[])null);

    final BoundingBox emptyCoordinatesList = new Envelope(
      new DoubleCoordinatesList(0, 2));
    assertEnvelope(emptyCoordinatesList, null, true, 0, (double[])null);

  }

  @Test
  public void testConstructorXY() {
  }

}
