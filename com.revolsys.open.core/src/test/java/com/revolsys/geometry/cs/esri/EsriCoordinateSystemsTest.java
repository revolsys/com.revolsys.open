package com.revolsys.geometry.cs.esri;

import org.junit.Assert;
import org.junit.Test;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;

public class EsriCoordinateSystemsTest {

  @Test
  public void geographicCoordinateSystems() {
    try (
      RecordReader reader = RecordReader
        .newRecordReader("src/main/data/esri/esriGeographicCs.tsv");) {
      for (final Record record : reader) {
        final int id = record.getInteger("ID");
        final String wkt = record.getString("WKT");
        final GeometryFactory geometryFactory = GeometryFactory.floating2d(wkt);
        final int coordinateSystemId = geometryFactory.getCoordinateSystemId();
        Assert.assertEquals(id, coordinateSystemId);
        Assert.assertEquals(2, geometryFactory.getAxisCount());
      }

    }
  }

  @Test
  public void projectedCoordinateSystems() {
    try (
      RecordReader reader = RecordReader
        .newRecordReader("src/main/data/esri/esriProjectedCs.tsv");) {
      for (final Record record : reader) {
        final int id = record.getInteger("ID");
        final String wkt = record.getString("WKT");
        final GeometryFactory geometryFactory = GeometryFactory.floating2d(wkt);
        final int coordinateSystemId = geometryFactory.getCoordinateSystemId();
        Assert.assertEquals(id, coordinateSystemId);
        Assert.assertEquals(2, geometryFactory.getAxisCount());
      }

    }
  }
}
