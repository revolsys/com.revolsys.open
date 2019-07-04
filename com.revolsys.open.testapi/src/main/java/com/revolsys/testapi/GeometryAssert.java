package com.revolsys.testapi;

import org.junit.Assert;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.Reader;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;
import com.revolsys.spring.resource.ClassPathResource;
import com.revolsys.spring.resource.Resource;

public class GeometryAssert {

  public static boolean assertEqualsExact(final int axisCount, final Geometry expectedGeometry,
    final Geometry actualGeometry) {
    return assertEqualsExact("Equals Exact", axisCount, expectedGeometry, actualGeometry);
  }

  public static boolean assertEqualsExact(final String message, final int axisCount,
    final Geometry expectedGeometry, final Geometry actualGeometry) {
    if (actualGeometry.equals(axisCount, expectedGeometry)) {
      return true;
    } else {
      failNotEquals(message, expectedGeometry, actualGeometry);
      return false;
    }
  }

  public static boolean assertEqualsGeometry(final String message, final Geometry expectedGeometry,
    final Geometry actualGeometry) {
    if (expectedGeometry.isEmpty()) {
      if (actualGeometry.isEmpty()) {
        return true;
      } else {
        return failNotEquals(message, expectedGeometry, actualGeometry);
      }
    } else if (actualGeometry.equals(expectedGeometry)) {
      return true;
    } else {
      return failNotEquals(message, expectedGeometry, actualGeometry);
    }
  }

  public static void doTestGeometry(final Class<?> clazz, final String file) {
    boolean valid = true;
    final Resource resource = new ClassPathResource(file, clazz);
    try (
      Reader<Record> reader = RecordReader.newRecordReader(resource)) {
      int i = 0;
      for (final Record object : reader) {
        final int srid = object.getInteger("srid");
        final int axisCount = object.getInteger("axisCount");
        final double scaleXy = object.getInteger("scaleXy");
        final double scaleZ = object.getInteger("scaleZ");
        final double[] scales = {
          scaleXy, scaleXy, scaleZ
        };
        final GeometryFactory geometryFactory = GeometryFactory.fixed(srid, axisCount, scales);
        final String wkt = object.getValue("wkt");
        final Geometry geometry = geometryFactory.geometry(wkt);
        valid &= equalsExpectedWkt(i, object, geometry);
        GeometryFactory otherGeometryFactory = geometryFactory.getGeometryFactory();
        if (otherGeometryFactory.isProjected()) {
          otherGeometryFactory = otherGeometryFactory.convertAxisCountAndScales(axisCount, scales);
        } else {
          otherGeometryFactory = GeometryFactory.fixed(3005, axisCount, scales);
        }
        final Geometry convertedGeometry = geometry.convertGeometry(otherGeometryFactory);
        final Geometry convertedBackGeometry = convertedGeometry.convertGeometry(geometryFactory);
        valid &= equalsExpectedGeometry(i, convertedBackGeometry, geometry);
        i++;
      }
    }
    if (!valid) {
      Assert.fail("Has Errors");
    }
  }

  public static boolean equalsExact(final int axisCount, final Geometry actualGeometry,
    final Geometry expectedGeometry) {
    if (actualGeometry.equals(axisCount, expectedGeometry)) {
      return true;
    } else {
      System.err.println("Equals Exact\t" + expectedGeometry + "\t" + actualGeometry);
      return false;
    }
  }

  public static boolean equalsExpectedGeometry(final int i, final Geometry actualGeometry,
    final Geometry expectedGeometry) {
    final int actualSrid = actualGeometry.getHorizontalCoordinateSystemId();
    final int expectedSrid = expectedGeometry.getHorizontalCoordinateSystemId();
    if (actualSrid != expectedSrid) {
      System.err.println(i + "\tEquals Srid\t" + expectedSrid + "\t" + actualSrid);
      return false;
    } else if (actualGeometry.equals(2, expectedGeometry)) {
      return true;
    } else {
      System.err.println(i + "\tEquals Exact\t" + expectedGeometry + "\t" + actualGeometry);
      return false;
    }
  }

  public static boolean equalsExpectedWkt(final int i, final Record object,
    final Geometry actualGeometry) {
    final GeometryFactory geometryFactory = GeometryFactory.DEFAULT_3D;
    final String wkt = object.getValue("expectedWkt");
    final Geometry expectedGeometry = geometryFactory.geometry(wkt, true);
    return equalsExpectedGeometry(i, actualGeometry, expectedGeometry);
  }

  public static boolean failNotEquals(final String message, final Object expected,
    final Object actual) {
    final String text = format(message, expected, actual);
    Assert.fail(text);
    return false;
  }

  public static String format(final String message, final Object expected, final Object actual) {
    String formatted = "";
    if (message != null && !message.equals("")) {
      formatted = message + " ";
    }
    final String expectedString = String.valueOf(expected);
    final String actualString = String.valueOf(actual);
    if (expectedString.equals(actualString)) {
      return formatted + "expected: " + formatClassAndValue(expected, expectedString) + " but was: "
        + formatClassAndValue(actual, actualString);
    } else {
      return formatted + "expected:<" + expectedString + "> but was:<" + actualString + ">";
    }
  }

  public static String formatClassAndValue(final Object value, final String valueString) {
    final String className = value == null ? "null" : value.getClass().getName();
    return className + "<" + valueString + ">";
  }

}
