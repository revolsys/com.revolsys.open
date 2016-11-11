package com.revolsys.elevation.cloud.las;

import com.revolsys.collection.map.IntHashMap;
import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.endian.EndianInput;
import com.revolsys.record.code.Code;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionBuilder;
import com.revolsys.util.function.Function3;

public enum LasPointFormat implements Code {
  Core(0, 20, LasPoint0Core::newLasPoint), //
  GpsTime(1, 28, LasPoint1GpsTime::newLasPoint), //
  Rgb(2, 26, LasPoint2Rgb::newLasPoint), //
  GpsTimeRgb(3, 34, LasPoint3GpsTimeRgb::newLasPoint), //
  GpsTimeWavePackets(4, 57, LasPoint4GpsTimeWavePackets::newLasPoint), //
  GpsTimeRgbWavePackets(5, 63, LasPoint5GpsTimeRgbWavePackets::newLasPoint), //
  ExtendedGpsTime(6, 30, LasPoint6GpsTime::newLasPoint), //
  ExtendedGpsTimeRgb(7, 36, LasPoint7GpsTimeRgb::newLasPoint), //
  ExtendedGpsTimeRgbNir(8, 38, LasPoint8GpsTimeRgbNir::newLasPoint), //
  ExtendedGpsTimeWavePackets(9, 59, LasPoint9GpsTimeWavePackets::newLasPoint), //
  ExtendedGpsTimeRgbNirWavePackets(10, 67, LasPoint10GpsTimeRgbNirWavePackets::newLasPoint);

  private static final IntHashMap<LasPointFormat> FORMAT_BY_ID = new IntHashMap<>();

  static {
    for (final LasPointFormat format : values()) {
      FORMAT_BY_ID.put(format.id, format);
    }
  }

  public static LasPointFormat getById(final int id) {
    final LasPointFormat format = FORMAT_BY_ID.get(id);
    if (format == null) {
      throw new IllegalArgumentException("Unsupported Las Point format=" + id);
    } else {
      return format;
    }
  }

  private int id;

  private int recordLength;

  private Function3<LasPointCloud, RecordDefinition, EndianInput, LasPoint0Core> recordReader;

  private LasPointFormat(final int id, final int recordLength,
    final Function3<LasPointCloud, RecordDefinition, EndianInput, LasPoint0Core> recordReader) {
    this.id = id;
    this.recordLength = recordLength;
    this.recordReader = recordReader;
  }

  @Override
  public String getCode() {
    return Integer.toString(this.id);
  }

  @Override
  public String getDescription() {
    return toString();
  }

  public int getId() {
    return this.id;
  }

  public int getRecordLength() {
    return this.recordLength;
  }

  public Function3<LasPointCloud, RecordDefinition, EndianInput, LasPoint0Core> getRecordReader() {
    return this.recordReader;
  }

  public RecordDefinition newRecordDefinition(final GeometryFactory geometryFactory) {
    final RecordDefinitionBuilder builder = new RecordDefinitionBuilder("/LAS_POINT") //
      .addField("x", DataTypes.DOUBLE, true) //
      .addField("y", DataTypes.DOUBLE, true) //
      .addField("z", DataTypes.DOUBLE, true) //
      .addField("intensity", DataTypes.INT, false) //
      .addField("returnNumber", DataTypes.BYTE, true) //
      .addField("numberOfReturns", DataTypes.BYTE, true) //
      .addField("scanDirectionFlag", DataTypes.BOOLEAN, true) //
      .addField("edgeOfFlightLine", DataTypes.BOOLEAN, true) //
      .addField("classification", DataTypes.BYTE, true) //
      .addField("synthetic", DataTypes.BOOLEAN, true) //
      .addField("keyPoint", DataTypes.BOOLEAN, true) //
      .addField("withheld", DataTypes.BOOLEAN, true) //
      .addField("overlap", DataTypes.BOOLEAN, true) //
      .addField("scanAngleRank", DataTypes.SHORT, true) //
      .addField("userData", DataTypes.SHORT, false) //
      .addField("pointSourceID", DataTypes.INT, true) //
      .setGeometryFactory(geometryFactory) //
    ;
    if (this.id == 1 || this.id >= 3) {
      builder.addField("gpsTime", DataTypes.DOUBLE, true);
    }
    if (this.id == 2 || this.id == 3 || this.id == 5 || this.id == 7 || this.id == 8
      || this.id == 10) {
      builder.addField("red", DataTypes.INT, true);
      builder.addField("green", DataTypes.INT, true);
      builder.addField("blue", DataTypes.INT, true);
    }
    if (this.id == 8 || this.id == 10) {
      builder.addField("nir", DataTypes.INT, true);
    }
    if (this.id == 4 || this.id == 5 || this.id == 9 || this.id == 10) {
      builder.addField("wavePacketDescriptorIndex", DataTypes.SHORT, true);
      builder.addField("byteOffsetToWaveformData", DataTypes.LONG, true);
      builder.addField("waveformPacketSizeInBytes", DataTypes.LONG, true);
      builder.addField("returnPointWaveformLocation", DataTypes.FLOAT, true);
      builder.addField("xT", DataTypes.FLOAT, true);
      builder.addField("yT", DataTypes.FLOAT, true);
      builder.addField("zT", DataTypes.FLOAT, true);
    }

    builder.addField("geometry", DataTypes.POINT, true);
    return builder.getRecordDefinition();
  }

  public LasPoint0Core readLasPoint(final LasPointCloud lasPointCloud,
    final RecordDefinition recordDefinition, final EndianInput in) {
    return this.recordReader.apply(lasPointCloud, recordDefinition, in);
  }
}
