package com.revolsys.gis.graph.linestring;

import org.apache.commons.jexl.junit.Asserter;
import org.junit.Assert;
import org.junit.Test;

import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.gis.jts.LineStringUtil;
import com.revolsys.jts.geom.Point;
import com.revolsys.gis.model.coordinates.SimpleGeometryFactory;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.io.wkt.WktParser;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.MultiLineString;

public class LineStringRelateTest {
  private static final GeometryFactory GEOMETRY_FACTORY = GeometryFactory.getFactory(
    3005, 1000.0, 1.0);

  final WktParser wkt = new WktParser(GEOMETRY_FACTORY);

  private static final double[] OFFSETS = {
    1200000, 570000, 0
  };

  public static LineString createLineString(double... coordinates) {
    final int axisCount = 2;
    DoubleCoordinatesList points = new DoubleCoordinatesList(coordinates.length
      / axisCount, axisCount);
    for (int i = 0; i < points.size(); i++) {
      for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
        final double value = OFFSETS[axisIndex]
          + coordinates[i * axisCount + axisIndex];
        points.setValue(i, axisIndex, value);
      }
    }

    return GEOMETRY_FACTORY.createLineString(points);
  }

  public static MultiLineString multiLineString(double... coordinates) {
    LineString line = createLineString(coordinates);
    return GEOMETRY_FACTORY.multiLineString(line);
  }

  @Test
  public void testIntersects() {
    LineString line1 = createLineString(20, 20, 40, 20, 60, 20);
    LineString line2 = createLineString(40, 20, 60, 20, 80, 20);
    MultiLineString expectedIntersection = multiLineString(40, 20, 60, 20);
    testIntersection(line1, line2, expectedIntersection);

  }

  @Test
  public void testEndOverlaps() {
    final LineString line1 = createLineString(20, 20, 40, 20, 60, 20);
    final LineString line2 = createLineString(40, 20, 60, 20, 80, 20);
    final MultiLineString line1OverlapLine2 = multiLineString(40, 20, 60,
      20);

    // ---
    // ---
    testEndOverlaps(line1, line2, line1OverlapLine2, true);

    // ---
    // ---
    testEndOverlaps(line1, line1, null, false);

    // ---
    // \--
    testEndOverlaps(line1, createLineString(60, 20, 60, 20, 80, 20), null,
      false);

    // ---
    // --
    testEndOverlaps(line1, createLineString(20, 20, 40, 20), null, false);

    // ---
    // 10---
    testEndOverlaps(line1, line2, 10, line1OverlapLine2, true);

    // ---
    // 5---
    testEndOverlaps(line1, createLineString(20, 20, 40, 20), 5, null, false);
  }

  public void testEndOverlaps(LineString line1, LineString line2,
    MultiLineString expectedIntersection, boolean expectedValue) {
    LineStringRelate relate = new LineStringRelate(line1, line2);
    final boolean endOverlaps = relate.isEndOverlaps(1);
    Assert.assertEquals("Ends overlaps\n" + line1 + "\n" + line2 + "\n",
      expectedValue, endOverlaps);
    if (endOverlaps) {
      assertEquals(expectedIntersection, relate.getOverlap());
    }

  }

  public void testEndOverlaps(LineString line1, LineString line2,
    double endDistance, MultiLineString expectedIntersection,
    boolean expectedValue) {
    LineStringRelate relate = new LineStringRelate(line1, line2);
    final boolean endOverlaps = relate.isEndOverlaps(1);
    Assert.assertEquals("Ends overlaps\n" + line1 + "\n" + line2 + "\n",
      expectedValue, endOverlaps);
    if (endOverlaps) {
      final MultiLineString overlap = relate.getOverlap();
      final LineString overlapLine = (LineString)overlap.getGeometryN(0);
      if (!LineStringUtil.isEndsWithinDistance(overlapLine, line1, 30.0)) {
        assertEquals(expectedIntersection, overlap);
      }
    }

  }

  public void testIntersection(LineString line1, LineString line2,
    MultiLineString expectedIntersection) {
    LineStringRelate relate = new LineStringRelate(line1, line2);
    final MultiLineString intersection = relate.getOverlap();
    assertEquals(expectedIntersection, intersection);
  }

  public void assertEquals(Geometry geometry1, final Geometry geometry2) {
    final boolean equals = geometry1.equalsExact(geometry2);
    Assert.assertTrue(geometry1 + "!=" + geometry2, equals);
  }
}
