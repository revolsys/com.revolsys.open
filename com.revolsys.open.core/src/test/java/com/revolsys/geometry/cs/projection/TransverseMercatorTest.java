package com.revolsys.geometry.cs.projection;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.revolsys.geometry.cs.ProjectedCoordinateSystem;
import com.revolsys.geometry.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.math.Angle;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;

public class TransverseMercatorTest {

  @Test
  public void testFile() {
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
        final int coordinateSystemId = record.getInteger("coordinateSystemId");
        CoordinatesProjection mercator = projectionById.get(coordinateSystemId);
        if (mercator == null) {
          final ProjectedCoordinateSystem coordinateSystem = EpsgCoordinateSystems
            .getCoordinateSystem(coordinateSystemId);
          mercator = coordinateSystem.getCoordinatesProjection();
          projectionById.put(coordinateSystemId, mercator);
        }

        final double[] coordinates = new double[2];

        mercator.project(lon, lat, coordinates, 0);

        final double xActual = Math.round(coordinates[0] * 1000) / 1000.0;
        final double yActual = Math.round(coordinates[1] * 1000) / 1000.0;
        Assert.assertEquals(i + ": x", x, xActual, 0.002);
        Assert.assertEquals(i + ": y", y, yActual, 0.002);
        i++;
      }
    }
  }
}
