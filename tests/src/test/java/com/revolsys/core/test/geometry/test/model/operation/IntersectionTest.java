package com.revolsys.core.test.geometry.test.model.operation;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.collection.map.MapEx;
import com.revolsys.core.test.geometry.test.util.WKTOrWKBReader;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.wkb.ParseException;
import com.revolsys.geometry.wkb.WKBReader;
import com.revolsys.io.map.MapReader;
import com.revolsys.spring.resource.ClassPathResource;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class IntersectionTest extends TestCase {

  protected static Geometry newGeometry(final GeometryFactory geometryFactory,
    final String geometryText) {
    if (geometryText == null) {
      return null;
    } else if (WKTOrWKBReader.isHex(geometryText)) {
      final WKBReader reader = new WKBReader(geometryFactory);
      try {
        return reader.read(WKBReader.hexToBytes(geometryText));
      } catch (final ParseException e) {
        throw Exceptions.wrap(e);
      }
    } else {
      return geometryFactory.geometry(geometryText);
    }
  }

  public static Test suite() {
    final TestSuite suite = new TestSuite("Intersection");
    int index = 0;
    try (
      MapReader reader = MapReader.newMapReader(
        new ClassPathResource("/com/revolsys/jts/test/geometry/operation/intersection.tsv"))) {
      for (final MapEx map : reader) {
        index++;
        if (map.getString("active").equalsIgnoreCase("true")) {
          final String description = map.getString("description");
          final int srid = map.getInteger("srid", 0);
          final int axisCount = map.getInteger("axisCount", 2);
          final double scaleXy = map.getDouble("scaleXy", 0.0);
          final double scaleZ = map.getDouble("scaleZ", 0.0);
          final GeometryFactory geometryFactory = GeometryFactory.fixed(srid, axisCount, scaleXy,
            scaleXy, scaleZ);

          final String geometry1Wkt = map.getString("geometry1");
          final Geometry geometry1 = newGeometry(geometryFactory, geometry1Wkt);
          final String geometry2Wkt = map.getString("geometry2");
          final Geometry geometry2 = newGeometry(geometryFactory, geometry2Wkt);
          final String expectedIntersectionWkt = map.getString("expectedIntersection");
          final Geometry expectedIntersection = newGeometry(geometryFactory,
            expectedIntersectionWkt);

          final IntersectionTest test = new IntersectionTest(index, description, geometry1,
            geometry2, expectedIntersection);
          suite.addTest(test);
        }
      }
    }
    return suite;
  }

  private final Geometry geometry1;

  private final Geometry geometry2;

  private final Geometry expectedIntersection;

  public IntersectionTest(final int index, final String description, final Geometry geometry1,
    final Geometry geometry2, final Geometry expectedIntersection) {
    super(index + " " + description);
    this.geometry1 = geometry1;
    this.geometry2 = geometry2;
    this.expectedIntersection = expectedIntersection;
  }

  private String message(final String message, final Geometry actualIntersection) {
    return message + "\ngeometry1=" + this.geometry1 + "\ngeometry2=" + this.geometry2 + "\n";
  }

  @Override
  protected void runTest() throws Throwable {
    final Geometry actualIntersection = this.geometry1.intersection(this.geometry2);

    final boolean equals = actualIntersection.equalsExact(this.expectedIntersection, 0.000000001);
    if (!equals) {
      junit.framework.Assert.failNotEquals(message("Intersection", actualIntersection),
        this.expectedIntersection, actualIntersection);
    }
  }
}
