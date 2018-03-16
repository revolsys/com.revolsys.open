package com.revolsys.geometry.cs.projection;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.revolsys.geometry.cs.Ellipsoid;
import com.revolsys.math.Angle;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;

public class TransverseMercatorTest {

  @Test
  public void testFile() {
    int differenceCount = 0;
    int differenceCountDegrees = 0;
    final Map<Integer, CoordinatesProjection> projectionById = new HashMap<>();
    int i = 1;
    try (
      RecordReader recordReader = RecordReader
        .newRecordReader("../../com.revolsys.open-testdata/cs/transverseMercator/mascot.csv")) {
      for (final Record record : recordReader) {
        final String latString = record.getString("lat");
        final String lonString = record.getString("lon");
        final double lat = Angle.toDecimalDegrees(latString);
        final double lon = Angle.toDecimalDegrees(lonString);
        final double x = record.getDouble("x");
        final double y = record.getDouble("y");
        final int utmReferenceMeridian = record.getInteger("utmReferenceMeridian");
        CoordinatesProjection mercator = projectionById.get(utmReferenceMeridian);
        if (mercator == null) {
          final Ellipsoid ellipsoid = new Ellipsoid("NAD83", 6378137, 298.257222101);
          mercator = new TransverseMercatorUsgs(lonString, ellipsoid, utmReferenceMeridian, 0,
            0.9996, 500000, 0);
          projectionById.put(utmReferenceMeridian, mercator);
        }

        final double[] coordinates = new double[2];

        mercator.project(lon, lat, coordinates, 0);

        final double xActual = Math.round(coordinates[0] * 1000) / 1000.0;
        final double yActual = Math.round(coordinates[1] * 1000) / 1000.0;
        final double diff = 0.001;
        if (Math.abs(xActual - x) > diff && Math.abs(yActual - y) > diff) {
          // System.out.println(i);
          differenceCount++;
        }

        mercator.inverse(x, y, coordinates, 0);

        final double lonActual = Math.round(coordinates[0] * 100000000) / 100000000.0;
        final double latActual = Math.round(coordinates[1] * 100000000) / 100000000.0;
        if (lonActual != lon && latActual != lat) {
          // System.out.println(i);
          differenceCountDegrees++;
        }
        i++;
      }
    }
    System.out.println(i + "\t" + differenceCount);
    System.out.println(i + "\t" + differenceCountDegrees);
  }
}
