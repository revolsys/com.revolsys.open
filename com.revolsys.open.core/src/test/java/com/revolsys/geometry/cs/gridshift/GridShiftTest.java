package com.revolsys.geometry.cs.gridshift;

import org.junit.Assert;
import org.junit.Test;

import com.revolsys.geometry.cs.gridshift.gsb.BinaryGridShiftFile;
import com.revolsys.geometry.cs.gridshift.nadcon5.Nadcon5CoordinatesOperation;
import com.revolsys.geometry.cs.projection.CoordinatesOperation;
import com.revolsys.geometry.cs.projection.CoordinatesOperationPoint;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;

public class GridShiftTest {

  @Test
  public void nadcon5() {
    try (
      RecordReader reader = RecordReader
        .newRecordReader("classpath:com/revolsys/geometry/cs/gridshift/Nadcon5ShiftTest.tsv")) {
      for (final Record record : reader) {
        final String id = record.getValue("ID");
        final double lon = record.getDouble("lon");
        final double lat = record.getDouble("lat");
        final String sourceDatumName = record.getString("sourceDatumName");
        final String targetDatumName = record.getString("targetDatumName");

        final CoordinatesOperation operation = new Nadcon5CoordinatesOperation(sourceDatumName,
          targetDatumName);
        final CoordinatesOperationPoint point = new CoordinatesOperationPoint(lon, lat);
        final double expectedLon = record.getDouble("expectedLon");
        final double expectedLat = record.getDouble("expectedLat");

        try {
          operation.perform(point);

          final double actualLon = point.x;
          final double actualLat = point.y;

          Assert.assertEquals(id + " lon", expectedLon, actualLon, 1e-7);
          Assert.assertEquals(id + " lat", expectedLat, actualLat, 1e-7);
        } catch (final IllegalArgumentException e) {
          if ("Transformation failure;coordinate is out of bounds".equals(e.getMessage())) {
            if (Double.isFinite(expectedLon)) {
              throw e;
            }
          }
        }
      }
    }
  }

  @Test
  public void testGridShift() {
    final BinaryGridShiftFile file = new BinaryGridShiftFile(
      "ftp://ftp.gdbc.gov.bc.ca/sections/outgoing/gsr/NTv2.0/BC_27_05.GSB", false);

    final CoordinatesOperationPoint point = new CoordinatesOperationPoint(-123, 49);
    System.out.println(point);
    file.getForwardOperation().perform(point);
    System.out.println(point);
    file.getReverseOperation().perform(point);
    System.out.println(point);
  }
}
