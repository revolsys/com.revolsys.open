package com.revolsys.gis.graph.linestring;

import org.junit.Assert;
import org.junit.Test;

import com.revolsys.geometry.graph.linestring.LineStringRelate;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.editor.LineStringEditor;
import com.revolsys.geometry.util.LineStringUtil;
import com.revolsys.record.io.format.wkt.WktParser;

public class LineStringRelateTest {
  private static final GeometryFactory GEOMETRY_FACTORY = GeometryFactory.fixed(3005, 3, 1000.0,
    1000.0, 1.0);

  private static final double[] OFFSETS = {
    1200000, 570000, 0
  };

  public static LineString createLineString(final double... coordinates) {
    final int axisCount = 2;
    final LineStringEditor editor = new LineStringEditor(coordinates.length / axisCount, axisCount);
    for (int i = 0; i < editor.getVertexCount(); i++) {
      for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
        final double value = OFFSETS[axisIndex] + coordinates[i * axisCount + axisIndex];
        editor.setCoordinate(i, axisIndex, value);
      }
    }

    return GEOMETRY_FACTORY.lineString(editor);
  }

  public static Lineal multiLineString(final double... coordinates) {
    final LineString line = createLineString(coordinates);
    return GEOMETRY_FACTORY.lineal(line);
  }

  final WktParser wkt = new WktParser(GEOMETRY_FACTORY);

  public void assertEquals(final Geometry geometry1, final Geometry geometry2) {
    final boolean equals = geometry1.equalsExact(geometry2);
    Assert.assertTrue(geometry1 + "!=" + geometry2, equals);
  }

  @Test
  public void testEndOverlaps() {
    final LineString line1 = createLineString(20, 20, 40, 20, 60, 20);
    final LineString line2 = createLineString(40, 20, 60, 20, 80, 20);
    final Lineal line1OverlapLine2 = multiLineString(40, 20, 60, 20);

    // ---
    // ---
    testEndOverlaps(line1, line2, line1OverlapLine2, true);

    // ---
    // ---
    testEndOverlaps(line1, line1, null, false);

    // ---
    // \--
    testEndOverlaps(line1, createLineString(60, 20, 60, 20, 80, 20), null, false);

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

  public void testEndOverlaps(final LineString line1, final LineString line2,
    final double endDistance, final Lineal expectedIntersection, final boolean expectedValue) {
    final LineStringRelate relate = new LineStringRelate(line1, line2);
    final boolean endOverlaps = relate.isEndOverlaps(1);
    Assert.assertEquals("Ends overlaps\n" + line1 + "\n" + line2 + "\n", expectedValue,
      endOverlaps);
    if (endOverlaps) {
      final Lineal overlap = relate.getOverlap();
      final LineString overlapLine = (LineString)overlap.getGeometry(0);
      if (!LineStringUtil.isEndsWithinDistance(overlapLine, line1, 30.0)) {
        assertEquals(expectedIntersection, overlap);
      }
    }

  }

  public void testEndOverlaps(final LineString line1, final LineString line2,
    final Lineal expectedIntersection, final boolean expectedValue) {
    final LineStringRelate relate = new LineStringRelate(line1, line2);
    final boolean endOverlaps = relate.isEndOverlaps(1);
    Assert.assertEquals("Ends overlaps\n" + line1 + "\n" + line2 + "\n", expectedValue,
      endOverlaps);
    if (endOverlaps) {
      assertEquals(expectedIntersection, relate.getOverlap());
    }

  }

  public void testIntersection(final LineString line1, final LineString line2,
    final Lineal expectedIntersection) {
    final LineStringRelate relate = new LineStringRelate(line1, line2);
    final Lineal intersection = relate.getOverlap();
    assertEquals(expectedIntersection, intersection);
  }

  @Test
  public void testIntersects() {
    final LineString line1 = createLineString(20, 20, 40, 20, 60, 20);
    final LineString line2 = createLineString(40, 20, 60, 20, 80, 20);
    final Lineal expectedIntersection = multiLineString(40, 20, 60, 20);
    testIntersection(line1, line2, expectedIntersection);

  }
}
