package com.revolsys.geometry.cs.gridshift;

import org.junit.Assert;
import org.junit.Test;

import com.revolsys.geometry.cs.epsg.EpsgId;
import com.revolsys.geometry.cs.gridshift.gsb.GsbGridShiftFile;
import com.revolsys.geometry.cs.gridshift.nadcon5.Nadcon5GridShiftOperation;
import com.revolsys.geometry.cs.projection.CoordinatesOperationPoint;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;

public class GridShiftTest {

  private static final GeometryFactory NAD27 = GeometryFactory.floating(EpsgId.NAD27, 2);

  private static final GeometryFactory NAD83 = GeometryFactory.floating(EpsgId.NAD83, 2);

  public void assertPoint(final double expectedX, final double expectedY, final Point actualPoint) {
    Assert.assertEquals(expectedX, actualPoint.getX(), 0);
    Assert.assertEquals(expectedY, actualPoint.getY(), 0);
  }

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

        final GridShiftOperation operation = new Nadcon5GridShiftOperation(sourceDatumName,
          targetDatumName);
        final CoordinatesOperationPoint point = new CoordinatesOperationPoint(lon, lat);
        final double expectedLon = record.getDouble("expectedLon");
        final double expectedLat = record.getDouble("expectedLat");

        try {
          if (operation.shift(point)) {

            final double actualLon = point.x;
            final double actualLat = point.y;

            Assert.assertEquals(id + " lon", expectedLon, actualLon, 1e-7);
            Assert.assertEquals(id + " lat", expectedLat, actualLat, 1e-7);
          } else {
            if (Double.isNaN(expectedLon)) {
              Assert.assertEquals(id + " lon", expectedLon, Double.NaN, 1e-7);
            }
          }
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
    final double lon = -123;
    final double lat = 49;
    final Point point = NAD27.point(lon, lat);
    final GsbGridShiftFile file = new GsbGridShiftFile(
      "ftp://ftp.gdbc.gov.bc.ca/sections/outgoing/gsr/NTv2.0/BC_27_05.GSB", false);
    try {
      file.addGridShiftOperation(NAD27, NAD83);

      final Point pointNad83 = point.convertGeometry(NAD83);
      assertPoint(-123.00130925827557, 48.99983063888219, pointNad83);
      final Point pointNad27 = pointNad83.convertGeometry(NAD27);
      assertPoint(lon, lat, pointNad27);
    } finally {
      file.removeGridShiftOperation(NAD27, NAD83);
    }
    // Test without grid shifts
    final Point pointNad83 = point.convertGeometry(NAD83);
    assertPoint(lon, lat, pointNad83);
    final Point pointNad27 = pointNad83.convertGeometry(NAD27);
    assertPoint(lon, lat, pointNad27);
  }
}
