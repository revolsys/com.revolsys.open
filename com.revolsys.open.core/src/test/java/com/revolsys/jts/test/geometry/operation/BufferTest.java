package com.revolsys.jts.test.geometry.operation;

import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.junit.Assert;
import org.springframework.core.io.ClassPathResource;

import com.revolsys.io.AbstractMapReaderFactory;
import com.revolsys.io.Reader;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.MultiPolygon;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.operation.buffer.Buffer;
import com.revolsys.jts.operation.buffer.BufferParameters;
import com.revolsys.jts.test.geometry.TestUtil;
import com.revolsys.util.CollectionUtil;

public class BufferTest extends TestCase {

  private static boolean hasHoles(final Geometry geometry) {
    if (geometry.isEmpty()) {
      return false;
    } else if (geometry instanceof Polygon) {
      final Polygon polygon = (Polygon)geometry;
      return polygon.getRingCount() > 1;
    }
    final MultiPolygon multiPolygon = (MultiPolygon)geometry;
    for (final Polygon polygon : multiPolygon.getPolygons()) {
      if (hasHoles(polygon)) {
        return true;
      }
    }
    return false;
  }

  public static Test suite() {
    final TestSuite suite = new TestSuite("Buffer");
    int i = 0;
    try (
      Reader<Map<String, Object>> reader = AbstractMapReaderFactory.mapReader(new ClassPathResource(
        "/com/revolsys/jts/test/geometry/operation/buffer.csv"))) {
      for (final Map<String, Object> map : reader) {
        i++;
        final int srid = CollectionUtil.getInteger(map, "srid", 0);
        final int axisCount = CollectionUtil.getInteger(map, "axisCount", 2);
        final double scaleXy = CollectionUtil.getDouble(map, "scaleXy", 0.0);
        final double scaleZ = CollectionUtil.getDouble(map, "scaleZ", 0.0);
        final GeometryFactory geometryFactory = GeometryFactory.getFactory(
          srid, axisCount, scaleXy, scaleZ);

        final String sourceWkt = (String)map.get("sourceWkt");
        final Geometry sourceGeometry = geometryFactory.geometry(sourceWkt);

        final double distance = CollectionUtil.getDouble(map, "bufferDistance",
          0.0);

        final int quadrantSegments = CollectionUtil.getInteger(map,
          "quadrantSegments", BufferParameters.DEFAULT_QUADRANT_SEGMENTS);
        final int endCapStyle = CollectionUtil.getInteger(map, "endCapStyle",
          BufferParameters.CAP_ROUND);
        final int joinStyle = CollectionUtil.getInteger(map, "joinStyle",
          BufferParameters.JOIN_ROUND);
        final double mitreLimit = CollectionUtil.getDouble(map, "mitreLimit",
          BufferParameters.DEFAULT_MITRE_LIMIT);

        final BufferParameters parameters = new BufferParameters(
          quadrantSegments, endCapStyle, joinStyle, mitreLimit);

        final Boolean expectedEmpty = CollectionUtil.getBoolean(map,
          "expectedEmpty");
        final Boolean expectedHoles = CollectionUtil.getBoolean(map,
          "expectedHoles");
        final Boolean expectedContains = CollectionUtil.getBoolean(map,
          "expectedContains");
        final Double expectedArea = CollectionUtil.getDouble(map,
          "expectedArea");

        final String expectedWkt = (String)map.get("expectedWkt");
        final Geometry expectedGeometry = geometryFactory.geometry(expectedWkt);

        final BufferTest test = new BufferTest(i, sourceGeometry, distance,
          parameters, expectedEmpty, expectedHoles, expectedContains,
          expectedArea, expectedGeometry);
        suite.addTest(test);

      }
    }
    return suite;
  }

  private final Geometry source;

  private final Geometry expected;

  private final BufferParameters parameters;

  private final double distance;

  private final Boolean expectedEmpty;

  private final Boolean expectedHoles;

  private final Boolean expectedContains;

  private final Double expectedArea;

  public BufferTest(final int index, final Geometry source,
    final double distance, final BufferParameters parameters,
    final Boolean expectedEmpty, final Boolean expectedHoles,
    final Boolean expectedContains, final Double expectedArea,
    final Geometry expected) {
    super(String.valueOf(index));
    this.source = source;
    this.distance = distance;
    this.parameters = parameters;
    this.expectedEmpty = expectedEmpty;
    this.expected = expected;
    this.expectedHoles = expectedHoles;
    this.expectedContains = expectedContains;
    this.expectedArea = expectedArea;
    setName(index + " " + source.getDataType() + " " + distance);
  }

  private boolean contains(final Geometry a, final Geometry b) {
    if (b.isEmpty()) {
      return true;
    } else {
      return a.contains(b);
    }
  }

  private String message(final String message, final Geometry actual) {
    return message + "\noriginal=" + source + "\nbuffered=" + actual + "\n";
  }

  @Override
  protected void runTest() throws Throwable {
    final Geometry actual = Buffer.buffer(source, distance, parameters);
    final boolean empty = actual.isEmpty();
    if (expectedEmpty != null) {
      Assert.assertEquals(message("Empty", actual), expectedEmpty, empty);
    }
    if (expectedArea != null) {
      final double area = actual.getArea();
      Assert.assertEquals(message("Area", actual), expectedArea, area, 0);
    }
    if (expected != null) {
      if (!actual.equalsExact2d(expected)) {
        TestUtil.failNotEquals(message("Geometry Equal", actual), expected,
          actual);
      }
    }
    if (expectedContains != null) {
      final boolean contains = contains(actual, source);
      Assert.assertEquals(message("Contains", actual), expectedContains,
        contains);
    }
    if (expectedHoles != null) {
      final boolean hasHoles = hasHoles(actual);
      Assert.assertEquals(message("Holes", actual), expectedHoles, hasHoles);
    }
  }
}
