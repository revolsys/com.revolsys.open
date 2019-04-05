package com.revolsys.core.test.geometry.cs.projection;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import org.jeometry.common.math.Angle;
import org.jeometry.coordinatesystem.model.Ellipsoid;
import org.jeometry.coordinatesystem.operation.CoordinatesOperationPoint;
import org.jeometry.coordinatesystem.operation.projection.CoordinatesProjection;
import org.jeometry.coordinatesystem.operation.projection.TransverseMercator;
import org.jeometry.coordinatesystem.operation.projection.TransverseMercatorThomas;
import org.jeometry.coordinatesystem.operation.projection.TransverseMercatorUsgs;
import org.junit.Assert;
import org.junit.Test;

import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;

public class TransverseMercatorTest {

  private static final double DP_5 = 5e-5;

  private static final double DP_4 = 1e-4;

  private void assertDms(final String label, final int degrees, final int minutes,
    final double seconds, final double decimalDegrees, final double secondsPrecision) {
    final int degrees2 = Math.abs((int)decimalDegrees);
    final double f = (Math.abs(decimalDegrees) - degrees2) * 60;
    int minutes2 = (int)f;
    double seconds2 = (f - minutes2) * 60;
    if (Math.abs(seconds2 - 60) <= 1e-6) {
      seconds2 = 0;
      ++minutes2;
    }

    Assert.assertEquals(label + "\tnegative", degrees < 0, decimalDegrees < 0);
    Assert.assertEquals(label + "\tdegrees", Math.abs(degrees), degrees2);
    Assert.assertEquals(label + "\tminutes", minutes, minutes2);
    Assert.assertEquals(label + "\tseconds", seconds, seconds2, secondsPrecision);
  }

  private boolean equalDms(final String a, final String b, final double minDiff) {
    if (a.equals(b)) {
      return true;
    } else {
      final int lastIndex1 = a.lastIndexOf(' ');
      final int lastIndex2 = b.lastIndexOf(' ');
      if (lastIndex1 == lastIndex2) {
        if (a.substring(0, lastIndex1).equals(b.substring(0, lastIndex1))) {
          final String secondsString1 = a.substring(lastIndex1 + 1);
          final String secondsString2 = b.substring(lastIndex1 + 1);
          final double seconds1 = Double
            .parseDouble(secondsString1.substring(0, secondsString1.length() - 1));
          final double seconds2 = Double
            .parseDouble(secondsString2.substring(0, secondsString2.length() - 1));
          final double diff = Math.abs(seconds1 - seconds2);
          return diff < minDiff;
        }
      }
    }
    return false;
  }

  public boolean equalDms(final String lonExpected, final String latExpected,
    final String lonActual, final String latActual) {
    if (!equalDms(lonExpected, lonActual, 0.00005)) {
      return false;
    } else if (!equalDms(latExpected, latActual, 0.00005)) {
      return false;
    } else {
      return true;
    }
  }

  // @Test
  // public void testMascotJhs() {
  // testUtmMascot(TransverseMercatorJhs::newUtm);
  // }

  @Test
  public void testMascotThomas() {
    final Map<Integer, CoordinatesProjection> projectionById = new HashMap<>();
    try (
      RecordReader recordReader = RecordReader
        .newRecordReader("../../com.revolsys.open-testdata/cs/transverseMercator/mascot.csv")) {
      for (final Record record : recordReader) {
        final String monumentId = record.getValue("monumentId");
        final int longitudeDegrees = record.getInteger("longitudeDegrees");
        final int longitudeMinutes = record.getInteger("longitudeMinutes");
        final double longitudeSeconds = record.getDouble("longitudeSeconds");
        final int latitudeDegrees = record.getInteger("latitudeDegrees");
        final int latitudeMinutes = record.getInteger("latitudeMinutes");
        final double latitudeSeconds = record.getDouble("latitudeSeconds");
        final double lon = record.getDouble("lon");
        final double lat = record.getDouble("lat");
        final double lonCalc = Angle.toDecimalDegrees(longitudeDegrees, longitudeMinutes,
          longitudeSeconds);
        final double latCalc = Angle.toDecimalDegrees(latitudeDegrees, latitudeMinutes,
          latitudeSeconds);
        final String lonStringCalc = Angle.toDegreesMinutesSecondsLon(lon, 5);
        final String latStringCalc = Angle.toDegreesMinutesSecondsLat(lat, 5);

        Assert.assertEquals(monumentId + "\tlon", lon, lonCalc, 1e-12);
        Assert.assertEquals(monumentId + "\tlat", lat, latCalc, 1e-12);

        final double x = record.getDouble("x");
        final double y = record.getDouble("y");
        final int utmReferenceMeridian = record.getInteger("utmReferenceMeridian");
        CoordinatesProjection mercator = projectionById.get(utmReferenceMeridian);
        if (mercator == null) {
          final Ellipsoid ellipsoid = new Ellipsoid("NAD83", 6378137, 298.257222101);
          mercator = new TransverseMercatorThomas("", ellipsoid, utmReferenceMeridian, 0, 0.9996,
            500000, 0);
          projectionById.put(utmReferenceMeridian, mercator);
        }

        final CoordinatesOperationPoint point = new CoordinatesOperationPoint(Math.toRadians(lon),
          Math.toRadians(lat));

        mercator.project(point);

        final double xActual = point.x;
        final double yActual = point.y;
        Assert.assertEquals(monumentId + "\tproj x", x, xActual, 1e-3);
        Assert.assertEquals(monumentId + "\tproj y", y, yActual, 2e-3);

        mercator.inverse(point);

        final double lonActual = Math.toDegrees(point.x);
        final double latActual = Math.toDegrees(point.y);
        assertDms(monumentId + "\tlon", longitudeDegrees, longitudeMinutes, longitudeSeconds,
          lonActual, DP_5);
        assertDms(monumentId + "\tlat", latitudeDegrees, latitudeMinutes, latitudeSeconds,
          latActual, DP_5);

        final String lonStringActual = Angle.toDegreesMinutesSecondsLon(lonActual, 5);
        final String latStringActual = Angle.toDegreesMinutesSecondsLat(latActual, 5);
        if (!equalDms(lonStringCalc, lonStringActual, 0.00005)) {
          Assert.assertEquals(monumentId + "\tlon dms", lonStringCalc, lonStringActual);
        }
        if (!equalDms(latStringCalc, latStringActual, 0.00005)) {
          Assert.assertEquals(monumentId + "\tlat dms", latStringCalc, latStringActual);
        }
      }
    }
  }

  @Test
  public void testMascotUsgs() {
    testUtmMascot(TransverseMercatorUsgs::newUtm);
  }

  private void testUtmMascot(
    final BiFunction<Ellipsoid, Double, TransverseMercator> projectionFactory) {
    final Map<Integer, CoordinatesProjection> projectionById = new HashMap<>();
    try (
      RecordReader recordReader = RecordReader
        .newRecordReader("../../com.revolsys.open-testdata/cs/transverseMercator/mascot.csv")) {
      for (final Record record : recordReader) {
        final String monumentId = record.getValue("monumentId");
        final int longitudeDegrees = record.getInteger("longitudeDegrees");
        final int longitudeMinutes = record.getInteger("longitudeMinutes");
        final double longitudeSeconds = record.getDouble("longitudeSeconds");
        final int latitudeDegrees = record.getInteger("latitudeDegrees");
        final int latitudeMinutes = record.getInteger("latitudeMinutes");
        final double latitudeSeconds = record.getDouble("latitudeSeconds");
        final double lon = record.getDouble("lon");
        final double lat = record.getDouble("lat");
        final double lonCalc = Angle.toDecimalDegrees(longitudeDegrees, longitudeMinutes,
          longitudeSeconds);
        final double latCalc = Angle.toDecimalDegrees(latitudeDegrees, latitudeMinutes,
          latitudeSeconds);

        Assert.assertEquals(monumentId + "\tlon", lon, lonCalc, 1e-12);
        Assert.assertEquals(monumentId + "\tlat", lat, latCalc, 1e-12);

        final double x = record.getDouble("x");
        final double y = record.getDouble("y");
        final int utmReferenceMeridian = record.getInteger("utmReferenceMeridian");
        CoordinatesProjection mercator = projectionById.get(utmReferenceMeridian);
        if (mercator == null) {
          final Ellipsoid ellipsoid = new Ellipsoid("NAD83", 6378137, 298.257222101);
          mercator = projectionFactory.apply(ellipsoid, (double)utmReferenceMeridian);
          projectionById.put(utmReferenceMeridian, mercator);
        }

        final CoordinatesOperationPoint point = new CoordinatesOperationPoint(Math.toRadians(lon),
          Math.toRadians(lat));

        mercator.project(point);

        final double xActual = point.x;
        final double yActual = point.y;
        Assert.assertEquals(monumentId + "\tproj x", x, xActual, 1e-3);
        Assert.assertEquals(monumentId + "\tproj y", y, yActual, 2e-3);

        mercator.inverse(point);

        final double lonActual = Math.toDegrees(point.x);
        final double latActual = Math.toDegrees(point.y);
        assertDms(monumentId + "\tlon", longitudeDegrees, longitudeMinutes, longitudeSeconds,
          lonActual, DP_4);
        assertDms(monumentId + "\tlat", latitudeDegrees, latitudeMinutes, latitudeSeconds,
          latActual, DP_4);
      }
    }
  }
}
