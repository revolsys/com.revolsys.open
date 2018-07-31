package com.revolsys.geometry.test.model.operation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;

import com.revolsys.collection.map.Maps;
import com.revolsys.geometry.io.GeometryReader;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineCap;
import com.revolsys.geometry.model.LineJoin;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.geometry.operation.buffer.BufferParameters;
import com.revolsys.geometry.test.model.GeometryAssertUtil;
import com.revolsys.io.Reader;
import com.revolsys.io.map.MapReader;
import com.revolsys.spring.resource.ClassPathResource;
import com.revolsys.spring.resource.PathResource;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class BufferTest extends TestCase {

  private static boolean hasHoles(final Geometry geometry) {
    if (geometry.isEmpty()) {
      return false;
    } else if (geometry instanceof Polygon) {
      final Polygon polygon = (Polygon)geometry;
      return polygon.getRingCount() > 1;
    }
    if (geometry instanceof Polygonal) {
      final Polygonal polygonal = (Polygonal)geometry;
      for (final Polygon polygon : polygonal.polygons()) {
        if (hasHoles(polygon)) {
          return true;
        }
      }
    }
    return false;
  }

  public static void performanceTest() throws Throwable {
    // JTS takes 3.4 seconds
    final PathResource resource = new PathResource(
      "/Users/paustin/Development/ALL/com.revolsys.open/com.revolsys.open.core/src/test/resources/com/revolsys/jts/test/data/world.wkt");
    List<Geometry> geometries = new ArrayList<>();
    try (
      Reader<Geometry> reader = GeometryReader.newGeometryReader(resource)) {
      geometries = reader.toList();
    }

    for (final Geometry geometry : geometries) {
      geometry.buffer(5);
    }

    final long time = System.currentTimeMillis();
    for (final Geometry geometry : geometries) {
      geometry.buffer(5);
    }
    System.out.println(System.currentTimeMillis() - time);

  }

  public static Test suite() {
    final TestSuite suite = new TestSuite("Buffer");
    int i = 0;
    try (
      MapReader reader = MapReader.newMapReader(
        new ClassPathResource("/com/revolsys/jts/test/geometry/operation/buffer.csv"))) {
      for (final Map<String, Object> map : reader) {
        i++;
        final int srid = Maps.getInteger(map, "srid", 0);
        final int axisCount = Maps.getInteger(map, "axisCount", 2);
        final double scaleXy = Maps.getDouble(map, "scaleXy", 0.0);
        final double scaleZ = Maps.getDouble(map, "scaleZ", 0.0);
        final double[] scales = {
          scaleXy, scaleXy, scaleZ
        };
        final GeometryFactory geometryFactory = GeometryFactory.fixed(srid, axisCount, scales);

        final String sourceWkt = (String)map.get("sourceWkt");
        final Geometry sourceGeometry = geometryFactory.geometry(sourceWkt);

        final double distance = Maps.getDouble(map, "bufferDistance", 0.0);

        final int quadrantSegments = Maps.getInteger(map, "quadrantSegments",
          BufferParameters.DEFAULT_QUADRANT_SEGMENTS);
        final LineCap endCapStyle = LineCap
          .fromGeometryValue(Maps.getInteger(map, "endCapStyle", LineCap.ROUND.getGeometryValue()));
        final LineJoin joinStyle = LineJoin
          .fromGeometryValue(Maps.getInteger(map, "joinStyle", LineJoin.ROUND.getGeometryValue()));
        final double mitreLimit = Maps.getDouble(map, "mitreLimit",
          BufferParameters.DEFAULT_MITRE_LIMIT);

        final BufferParameters parameters = new BufferParameters(quadrantSegments, endCapStyle,
          joinStyle, mitreLimit);

        final Boolean expectedEmpty = Maps.getBoolean(map, "expectedEmpty");
        final Boolean expectedHoles = Maps.getBoolean(map, "expectedHoles");
        final Boolean expectedContains = Maps.getBoolean(map, "expectedContains");
        final Double expectedArea = Maps.getDouble(map, "expectedArea");

        final String expectedWkt = (String)map.get("expectedWkt");
        final Geometry expectedGeometry = geometryFactory.geometry(expectedWkt);

        final BufferTest test = new BufferTest(i, sourceGeometry, distance, parameters,
          expectedEmpty, expectedHoles, expectedContains, expectedArea, expectedGeometry);
        suite.addTest(test);

      }
    }
    return suite;
  }

  private final double distance;

  private final Geometry expected;

  private final Double expectedArea;

  private final Boolean expectedContains;

  private final Boolean expectedEmpty;

  private final Boolean expectedHoles;

  private final BufferParameters parameters;

  private final Geometry source;

  public BufferTest(final int index, final Geometry source, final double distance,
    final BufferParameters parameters, final Boolean expectedEmpty, final Boolean expectedHoles,
    final Boolean expectedContains, final Double expectedArea, final Geometry expected) {
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
    return message + "\noriginal=" + this.source + "\nbuffered=" + actual + "\n";
  }

  @Override
  protected void runTest() throws Throwable {
    final Geometry actual = this.source.buffer(this.distance, this.parameters);
    final boolean empty = actual.isEmpty();
    if (this.expectedEmpty != null) {
      Assert.assertEquals(message("Empty", actual), this.expectedEmpty, empty);
    }
    if (this.expectedArea != null) {
      final double area = actual.getArea();
      Assert.assertEquals(message("Area", actual), this.expectedArea, area, 0);
    }
    if (this.expected != null) {
      if (!actual.equals(2, this.expected)) {
        GeometryAssertUtil.failNotEquals(message("Geometry Equal", actual), this.expected, actual);
      }
    }
    if (this.expectedContains != null) {
      final boolean contains = contains(actual, this.source);
      Assert.assertEquals(message("Contains", actual), this.expectedContains, contains);
    }
    if (this.expectedHoles != null) {
      final boolean hasHoles = hasHoles(actual);
      Assert.assertEquals(message("Holes", actual), this.expectedHoles, hasHoles);
    }
  }
}
