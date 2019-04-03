package com.revolsys.core.test.geometry.cs.esri;

import org.jeometry.coordinatesystem.model.CoordinateSystem;
import org.jeometry.coordinatesystem.model.VerticalCoordinateSystem;
import org.junit.Assert;
import org.junit.Test;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;

public class EsriCoordinateSystemsTest {

  private void doFileTest(final String type) {
    try (
      RecordReader reader = RecordReader.newRecordReader(
        "../../jeometry/jeometry-coordinatesystem/src/main/data/esri/esri" + type + "Cs.tsv");) {
      for (final Record record : reader) {
        final int id = record.getInteger("ID");
        final String wkt = record.getString("WKT");
        final GeometryFactory geometryFactory = GeometryFactory.floating2d(wkt);
        final int coordinateSystemId = geometryFactory.getHorizontalCoordinateSystemId();
        final String coordinateSystemName = geometryFactory.getCoordinateSystemName();
        Assert.assertEquals(coordinateSystemName + " ID", id, coordinateSystemId);
        Assert.assertEquals(coordinateSystemName + " axisCount", 2, geometryFactory.getAxisCount());
        final String actualWkt = geometryFactory.toWktCs();
        Assert.assertEquals(coordinateSystemName + " WKT", wkt, actualWkt);
      }
    }
  }

  @Test
  public void geographicCoordinateSystems() {
    doFileTest("Geographic");
  }

  @Test
  public void projectedCoordinateSystems() {
    doFileTest("Projected");
  }

  @Test
  public void verticalCoordinateSystems() {
    try (
      RecordReader reader = RecordReader.newRecordReader(
        "../../jeometry/jeometry-coordinatesystem/src/main/data/esri/esriVerticalCs.tsv");) {
      for (final Record record : reader) {
        final int id = record.getInteger("ID");
        final String wkt = record.getString("WKT");
        if (wkt.contains("VDATUM")) {
          final VerticalCoordinateSystem coordinateSystem = CoordinateSystem
            .getCoordinateSystem(wkt);
          final int coordinateSystemId = coordinateSystem.getCoordinateSystemId();
          Assert.assertEquals(coordinateSystem.getCoordinateSystemName() + " ID", id,
            coordinateSystemId);
          final String actualWkt = coordinateSystem.toEsriWktCs();
          Assert.assertEquals(coordinateSystem.getCoordinateSystemName() + " WKT", wkt, actualWkt);
        }
      }
    }
  }
}
