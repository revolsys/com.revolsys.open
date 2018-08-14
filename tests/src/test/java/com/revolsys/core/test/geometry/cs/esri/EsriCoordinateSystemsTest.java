package com.revolsys.core.test.geometry.cs.esri;

import org.junit.Assert;
import org.junit.Test;

import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;
import com.revolsys.util.Property;

public class EsriCoordinateSystemsTest {

  private void doFileTest(final String type) {
    try (
      RecordReader reader = RecordReader.newRecordReader(
        "../com.revolsys.open.coordinatesystems/src/main/data/esri/esri" + type + "Cs.tsv");) {
      for (final Record record : reader) {
        final int id = record.getInteger("ID");
        final String wkt = record.getString("WKT");
        final GeometryFactory geometryFactory = GeometryFactory.floating2d(wkt);
        final int coordinateSystemId = geometryFactory.getHorizontalCoordinateSystemId();
        Assert.assertEquals(id, coordinateSystemId);
        Assert.assertEquals(2, geometryFactory.getAxisCount());
        final String actualWkt = geometryFactory.toWktCs();
        if (Property.isEmpty(actualWkt)) {
          GeometryFactory.floating2d(wkt);
          geometryFactory.toWktCs();
        }
        Assert.assertEquals(wkt, actualWkt);
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
        "../com.revolsys.open.coordinatesystems/src/main/data/esri/esriVerticalCs.tsv");) {
      for (final Record record : reader) {
        final int id = record.getInteger("ID");
        final String wkt = record.getString("WKT");
        if (wkt.contains("VDATUM")) {
          final CoordinateSystem coordinateSystem = CoordinateSystem.getCoordinateSystem(wkt);
          final int coordinateSystemId = coordinateSystem.getHorizontalCoordinateSystemId();
          Assert.assertEquals(id, coordinateSystemId);
          final String actualWkt = coordinateSystem.toEsriWktCs();
          if (Property.isEmpty(actualWkt)) {
            CoordinateSystem.getCoordinateSystem(wkt);
            coordinateSystem.toEsriWktCs();
          }
          Assert.assertEquals(wkt, actualWkt);
        }
      }
    }
  }
}
