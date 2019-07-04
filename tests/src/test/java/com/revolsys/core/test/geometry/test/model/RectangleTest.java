package com.revolsys.core.test.geometry.test.model;

import com.revolsys.core.test.util.TestUtil;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.impl.RectangleXY;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;
import com.revolsys.spring.resource.ClassPathResource;
import com.revolsys.testapi.GeometryAssert;
import com.revolsys.testapi.RunnableTestCase;

import junit.framework.TestSuite;

public class RectangleTest {

  private static final GeometryFactory GEOMETRY_FACTORY = GeometryFactory.fixed2d(1000, 1000);

  private static void addIntersectionTests(final TestSuite suite) {
    final TestSuite intersectsSuite = new TestSuite("Intersection");
    suite.addTest(intersectsSuite);

    try (
      RecordReader reader = RecordReader.newRecordReader(
        new ClassPathResource("Rectangle-Intersection.tsv", RectangleTest.class))) {
      for (final Record record : reader) {
        final String name = record.getString("Name");

        final String type = record.getString("GeometryType");
        intersectsSuite
          .addTest(new RunnableTestCase(type + "_" + name, () -> intersection(record)));
      }
    }
  }

  private static void assertInsertion(final String message, final Geometry geometry,
    final double minX, final double minY, final double maxX, final double maxY, Geometry expected,
    final boolean normalize) {
    final RectangleXY rectangle = GEOMETRY_FACTORY.newRectangleCorners(minX, minY, maxX, maxY);
    if (normalize) {
      expected = expected.normalize();
    }
    // if ("LineString_on top to right".equals(message)) {
    // Debug.noOp();
    // }

    Geometry actualIntersectsRectangle = geometry.intersectionBbox(rectangle);
    if (normalize) {
      actualIntersectsRectangle = actualIntersectsRectangle.normalize();
    }
    GeometryAssert.assertEqualsExact(message, 2, expected, actualIntersectsRectangle);

    Geometry actualIntersectsGeometry = rectangle.intersection(geometry);
    if (normalize) {
      actualIntersectsGeometry = actualIntersectsGeometry.normalize();
    }
    GeometryAssert.assertEqualsExact(message, 2, expected, actualIntersectsGeometry);
  }

  private static Geometry getGeometry(final Record record, final String fieldName) {
    final String wkt = record.getString(fieldName);
    return GEOMETRY_FACTORY.geometry(wkt);
  }

  private static void intersection(final Record record) {
    final String name = record.getString("Name");
    final String type = record.getString("GeometryType");
    final String rectangle = record.getString("Rectangle");
    final Geometry geometry = getGeometry(record, "Geometry");

    Geometry expected = getGeometry(record, "Geometry_Expected");
    if (expected == null) {
      expected = geometry;
    }
    final String[] rectangleParts = rectangle.split(",");
    final double minX = Double.valueOf(rectangleParts[0]);
    final double minY = Double.valueOf(rectangleParts[1]);
    final double maxX = Double.valueOf(rectangleParts[2]);
    final double maxY = Double.valueOf(rectangleParts[3]);
    TestUtil.logValues(RectangleTest.class, record);
    assertInsertion(type + "_" + name, geometry, minX, minY, maxX, maxY, expected, false);
    if (geometry instanceof LineString) {
      final Geometry geometryReverse = geometry.reverse();
      final Geometry expectedReverse = expected.reverse();
      assertInsertion(type + "_" + name, geometryReverse, minX, minY, maxX, maxY, expectedReverse,
        true);
    }
  }

  public static TestSuite suite() {
    TestUtil.enableInfo(RectangleTest.class);
    final TestSuite suite = new TestSuite(RectangleTest.class.getName());

    addIntersectionTests(suite);
    return suite;
  }
}
