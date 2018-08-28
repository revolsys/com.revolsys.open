package com.revolsys.core.test.geometry.cs.projection;

import java.util.function.BiFunction;

import org.junit.Assert;

import com.revolsys.core.test.util.TestUtil;
import com.revolsys.geometry.cs.GeographicCoordinateSystem;
import com.revolsys.geometry.cs.ProjectedCoordinateSystem;
import com.revolsys.geometry.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.geometry.cs.projection.CoordinatesOperationPoint;
import com.revolsys.geometry.cs.projection.CoordinatesProjection;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;
import com.revolsys.spring.resource.ClassPathResource;
import com.revolsys.testapi.RunnableTestCase;
import com.revolsys.util.Strings;

import junit.framework.TestSuite;

public class CoordinatesProjectionTest {
  private static void addPointTests(final TestSuite suite) {
    final TestSuite testSuite = new TestSuite("point");
    suite.addTest(testSuite);

    try (
      RecordReader reader = RecordReader
        .newRecordReader(new ClassPathResource("point.tsv", CoordinatesProjectionTest.class))) {
      for (final Record record : reader) {
        final String name = record.getString("Projection Name");

        final String coordinateSystemId = record.getString("coordinateSystemId");
        final String x = record.getString("x");
        final String y = record.getString("y");
        final String testName = Strings.toString("_", name, coordinateSystemId, x, y);
        final RunnableTestCase testCase = new RunnableTestCase(testName, () -> point(record));
        testSuite.addTest(testCase);
      }
    }
  }

  private static void assertDegrees(final double lon, final double lat, final double λActual,
    final double φActual) {
    final double lonActual = Math.toDegrees(λActual);
    final double latActual = Math.toDegrees(φActual);
    assertLatLon("", lon, lat, lonActual, latActual);
  }

  private static void assertLatLon(final String prefix, final double lon, final double lat,
    final double lonActual, final double latActual) {
    Assert.assertEquals(prefix + "lon", lon, lonActual, 1e-20);
    Assert.assertEquals(prefix + "lat", lat, latActual, 1e-20);
  }

  private static void assertPoint(final String prefix, final GeometryFactory geometryFactory,
    final double x, final double y, final GeometryFactory geographicGeometryFactory,
    final double lon, final double lat, final BiFunction<Point, GeometryFactory, Point> function) {
    final Point point = geometryFactory.point(x, y);
    final Point geographicPoint = function.apply(point, geographicGeometryFactory);
    final double lonActual = geographicPoint.getX();
    final double latActual = geographicPoint.getY();
    assertLatLon(prefix, lon, lat, lonActual, latActual);
    final Point projectedPoint = function.apply(geographicPoint, geometryFactory);
    final double xActual = projectedPoint.getX();
    final double yActual = projectedPoint.getY();
    assertXyMM(prefix, x, y, xActual, yActual);
  }

  private static void assertProjection(final ProjectedCoordinateSystem coordinateSystem,
    final double x, final double y, final double lon, final double lat) {
    final CoordinatesProjection projection = coordinateSystem.getCoordinatesProjection();
    final CoordinatesOperationPoint opPoint = new CoordinatesOperationPoint(x, y);
    projection.inverse(opPoint);
    final double λActual = opPoint.x;
    final double φActual = opPoint.y;
    assertDegrees(lon, lat, λActual, φActual);
    projection.project(opPoint);
    final double xActual = opPoint.x;
    final double yActual = opPoint.y;
    assertXyMM("CoordinatesProjection ", x, y, xActual, yActual);

    final GeographicCoordinateSystem geographicCoordinateSystem = coordinateSystem
      .getGeographicCoordinateSystem();
    final GeometryFactory geometryFactory = coordinateSystem.getGeometryFactoryFloating(2);
    final GeometryFactory geographicGeometryFactory = geographicCoordinateSystem
      .getGeometryFactoryFloating(2);
    assertPoint("convertGeometry", geometryFactory, x, y, geographicGeometryFactory, lon, lat,
      Point::convertGeometry);
  }

  private static void assertXyMM(final String prefix, final double x, final double y,
    final double xActual, final double yActual) {
    Assert.assertEquals(prefix + "x", x, xActual, 2e-3);
    Assert.assertEquals(prefix + "y", y, yActual, 2e-3);
  }

  private static void point(final Record record) {
    TestUtil.logValues(CoordinatesProjectionTest.class, record);
    final int coordinateSystemId = record.getInteger("coordinateSystemId");
    final double x = record.getDouble("x");
    final double y = record.getDouble("y");
    final double lon = record.getDouble("expectedLon");
    final double lat = record.getDouble("expectedLat");
    final ProjectedCoordinateSystem coordinateSystem = EpsgCoordinateSystems
      .getCoordinateSystem(coordinateSystemId);
    assertProjection(coordinateSystem, x, y, lon, lat);
  }

  public static TestSuite suite() {
    TestUtil.enableInfo(CoordinatesProjectionTest.class);
    final TestSuite suite = new TestSuite(CoordinatesProjectionTest.class.getName());

    addPointTests(suite);
    return suite;
  }
}
