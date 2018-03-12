package com.revolsys.geometry.cs.esri;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.revolsys.collection.map.Maps;
import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.cs.GeographicCoordinateSystem;
import com.revolsys.geometry.cs.ParameterName;
import com.revolsys.geometry.cs.ParameterValue;
import com.revolsys.geometry.cs.PrimeMeridian;
import com.revolsys.geometry.cs.ProjectedCoordinateSystem;
import com.revolsys.geometry.cs.Ellipsoid;
import com.revolsys.geometry.cs.VerticalCoordinateSystem;
import com.revolsys.geometry.cs.WktCsParser;
import com.revolsys.geometry.cs.datum.GeodeticDatum;
import com.revolsys.geometry.cs.datum.VerticalDatum;
import com.revolsys.geometry.cs.unit.AngularUnit;
import com.revolsys.geometry.cs.unit.LinearUnit;
import com.revolsys.io.channels.ChannelWriter;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;

public class EsriCoordinateSystemsLoader {
  public static void main(final String[] args) {
    new EsriCoordinateSystemsLoader().run();
  }

  private final String mainPath = "../com.revolsys.open.coordinatesystems/src/main/";

  private final Map<String, Integer> geographicIdByName = new HashMap<>();

  private void geographic() {
    final Map<ByteArray, Map<Integer, GeographicCoordinateSystem>> csBymd5 = new LinkedHashMap<>();
    try (
      RecordReader reader = RecordReader
        .newRecordReader(this.mainPath + "data/esri/esriGeographicCs.tsv");
      final ChannelWriter writer = ChannelWriter
        .newChannelWriter(this.mainPath + "resources/CoordinateSystems/esri/Geographic.cs")) {

      for (final Record record : reader) {
        final int id = record.getInteger("ID");
        final String wkt = record.getString("WKT");
        final GeographicCoordinateSystem coordinateSystem = WktCsParser.read(wkt);
        final byte[] digest = coordinateSystem.md5Digest();
        Maps.addToMap(Maps::newTree, csBymd5, new ByteArray(digest), id, coordinateSystem);

        final GeodeticDatum datum = coordinateSystem.getDatum();
        final Ellipsoid ellipsoid = datum.getEllipsoid();
        final PrimeMeridian primeMeridian = coordinateSystem.getPrimeMeridian();
        final AngularUnit angularUnit = coordinateSystem.getAngularUnit();

        final String csName = coordinateSystem.getCoordinateSystemName();
        this.geographicIdByName.put(csName, id);
        final String datumName = datum.getName();
        final String spheroidName = ellipsoid.getName();
        final double semiMajorAxis = ellipsoid.getSemiMajorAxis();
        final double inverseFlattening = ellipsoid.getInverseFlattening();
        final String primeMeridianName = primeMeridian.getName();
        final double longitude = primeMeridian.getLongitude();
        final String angularUnitName = angularUnit.getName();
        final double conversionFactor = angularUnit.getConversionFactor();

        writer.putInt(id);
        writer.putStringUtf8ByteCount(csName);
        writer.putStringUtf8ByteCount(datumName);
        writer.putStringUtf8ByteCount(spheroidName);
        writer.putDouble(semiMajorAxis);
        writer.putDouble(inverseFlattening);
        writer.putStringUtf8ByteCount(primeMeridianName);
        writer.putDouble(longitude);
        writer.putStringUtf8ByteCount(angularUnitName);
        writer.putDouble(conversionFactor);
      }
    }
    writeDigestFile(csBymd5, "Geographic");
  }

  private void projected() {
    final Map<ByteArray, Map<Integer, ProjectedCoordinateSystem>> csBymd5 = new LinkedHashMap<>();
    try (
      RecordReader reader = RecordReader
        .newRecordReader(this.mainPath + "data/esri/esriProjectedCs.tsv");
      final ChannelWriter writer = ChannelWriter
        .newChannelWriter(this.mainPath + "resources/CoordinateSystems/esri/Projected.cs")) {

      for (final Record record : reader) {
        final int id = record.getInteger("ID");
        final String wkt = record.getString("WKT");
        final ProjectedCoordinateSystem coordinateSystem = WktCsParser.read(wkt);
        final byte[] digest = coordinateSystem.md5Digest();
        Maps.addToMap(Maps::newTree, csBymd5, new ByteArray(digest), id, coordinateSystem);

        final String csName = coordinateSystem.getCoordinateSystemName();
        final GeographicCoordinateSystem geographicCoordinateSystem = coordinateSystem
          .getGeographicCoordinateSystem();
        final String geographicCoordinateSystemName = geographicCoordinateSystem
          .getCoordinateSystemName();
        final int geographicCoordinateSystemId = this.geographicIdByName
          .getOrDefault(geographicCoordinateSystemName, 0);
        if (geographicCoordinateSystemId == 0) {
          System.out.println(wkt);
        }
        final String projectionName = coordinateSystem.getCoordinateOperationMethod().getName();
        final Map<ParameterName, ParameterValue> parameterValues = coordinateSystem
          .getParameterValues();
        final LinearUnit linearUnit = coordinateSystem.getLinearUnit();
        final String unitName = linearUnit.getName();
        final double conversionFactor = linearUnit.getConversionFactor();
        writer.putInt(id);
        writer.putStringUtf8ByteCount(csName);
        writer.putInt(geographicCoordinateSystemId);
        writer.putStringUtf8ByteCount(projectionName);
        writeParameters(writer, parameterValues);
        writer.putStringUtf8ByteCount(unitName);
        writer.putDouble(conversionFactor);
      }
    }
    writeDigestFile(csBymd5, "Projected");
  }

  public void run() {
    geographic();
    projected();
    vertical();
  }

  private void vertical() {
    final Map<ByteArray, Map<Integer, VerticalCoordinateSystem>> csBymd5 = new LinkedHashMap<>();
    try (
      RecordReader reader = RecordReader
        .newRecordReader(this.mainPath + "data/esri/esriVerticalCs.tsv");
      final ChannelWriter writer = ChannelWriter
        .newChannelWriter(this.mainPath + "resources/CoordinateSystems/esri/Vertical.cs")) {

      for (final Record record : reader) {
        final int id = record.getInteger("ID");
        final String wkt = record.getString("WKT");
        final VerticalCoordinateSystem coordinateSystem = WktCsParser.read(wkt);
        final byte[] digest = coordinateSystem.md5Digest();
        Maps.addToMap(Maps::newTree, csBymd5, new ByteArray(digest), id, coordinateSystem);

        final VerticalDatum datum = coordinateSystem.getDatum();
        if (datum != null) {
          final Map<ParameterName, ParameterValue> parameterValues = coordinateSystem
            .getParameterValues();
          final LinearUnit linearUnit = coordinateSystem.getLinearUnit();

          final String csName = coordinateSystem.getCoordinateSystemName();
          this.geographicIdByName.put(csName, id);
          final String datumName = datum.getName();
          final String linearUnitName = linearUnit.getName();
          final double conversionFactor = linearUnit.getConversionFactor();

          writer.putInt(id);
          writer.putStringUtf8ByteCount(csName);
          writer.putStringUtf8ByteCount(datumName);
          writeParameters(writer, parameterValues);
          writer.putStringUtf8ByteCount(linearUnitName);
          writer.putDouble(conversionFactor);
        }
      }
    }
    writeDigestFile(csBymd5, "Vertical");
  }

  private <C extends CoordinateSystem> void writeDigestFile(
    final Map<ByteArray, Map<Integer, C>> csBymd5, final String csType) {
    try (
      final ChannelWriter writer = ChannelWriter.newChannelWriter(
        this.mainPath + "resources/CoordinateSystems/esri/" + csType + ".digest")) {

      for (final Entry<ByteArray, Map<Integer, C>> entry : csBymd5.entrySet()) {
        final ByteArray digest = entry.getKey();
        writer.putBytes(digest.getData());
        final Map<Integer, C> coordinateSystemById = entry.getValue();
        writer.putShort((short)coordinateSystemById.size());
        for (final Integer id : coordinateSystemById.keySet()) {
          writer.putInt(id);
        }
      }
    }
  }

  private void writeParameters(final ChannelWriter writer,
    final Map<ParameterName, ParameterValue> parameterValues) {
    final int parameterCount = parameterValues.size();
    writer.putByte((byte)parameterCount);
    for (final Entry<ParameterName, ParameterValue> entry : parameterValues.entrySet()) {
      final ParameterName parameterName = entry.getKey();
      final ParameterValue parameterValue = entry.getValue();
      final String name = parameterName.getName();
      final Object value = parameterValue.getOriginalValue();
      writer.putStringUtf8ByteCount(name);
      writer.putStringUtf8ByteCount(DataTypes.toString(value));
    }
  }
}
