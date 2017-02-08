package com.revolsys.elevation.cloud.las;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import com.revolsys.collection.map.IntHashMap;
import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.channels.ChannelReader;
import com.revolsys.record.code.Code;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionBuilder;

public enum LasPointFormat implements Code {

  Core( //
    0, //
    20, //
    LasPoint0Core::new, //
    LasPoint0Core::new, //
    LasVersion.VERSION_1_0 //
  ), //
  GpsTime( //
    1, //
    28, //
    LasPoint1GpsTime::new, //
    LasPoint1GpsTime::new, //
    LasVersion.VERSION_1_0 //
  ), //
  Rgb( //
    2, //
    26, //
    LasPoint2Rgb::new, //
    LasPoint2Rgb::new, //
    LasVersion.VERSION_1_2), //
  GpsTimeRgb( //
    3, //
    34, //
    LasPoint3GpsTimeRgb::new, //
    LasPoint3GpsTimeRgb::new, //
    LasVersion.VERSION_1_2), //
  GpsTimeWavePackets( //
    4, //
    57, //
    LasPoint4GpsTimeWavePackets::new, //
    LasPoint4GpsTimeWavePackets::new, //
    LasVersion.VERSION_1_3), //
  GpsTimeRgbWavePackets( //
    5, //
    63, //
    LasPoint5GpsTimeRgbWavePackets::new, //
    LasPoint5GpsTimeRgbWavePackets::new, //
    LasVersion.VERSION_1_3), //
  ExtendedGpsTime( //
    6, //
    30, //
    LasPoint6GpsTime::new, //
    LasPoint6GpsTime::new, //
    LasVersion.VERSION_1_4 //
  ), //
  ExtendedGpsTimeRgb( //
    7, //
    36, //
    LasPoint7GpsTimeRgb::new, //
    LasPoint7GpsTimeRgb::new, //
    LasVersion.VERSION_1_4 //
  ), //
  ExtendedGpsTimeRgbNir( //
    8, //
    38, //
    LasPoint8GpsTimeRgbNir::new, //
    LasPoint8GpsTimeRgbNir::new, //
    LasVersion.VERSION_1_4 //
  ), //
  ExtendedGpsTimeWavePackets( //
    9, //
    59, //
    LasPoint9GpsTimeWavePackets::new, //
    LasPoint9GpsTimeWavePackets::new, //
    LasVersion.VERSION_1_4 //
  ), //
  ExtendedGpsTimeRgbNirWavePackets( //
    10, //
    67, //
    LasPoint10GpsTimeRgbNirWavePackets::new, //
    LasPoint10GpsTimeRgbNirWavePackets::new, //
    LasVersion.VERSION_1_4 //
  );

  private interface PointConstructor {
    LasPoint apply(double x, double y, double z);
  }

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

  private BiFunction<LasPointCloud, ChannelReader, LasPoint> recordReader;

  private PointConstructor pointConstructor;

  private LasVersion minVersion;

  private Supplier<LasPoint> constructor;

  private LasPointFormat(final int id, final int recordLength, final Supplier<LasPoint> constructor,
    final PointConstructor pointConstructor, final LasVersion minVersion) {
    this.id = id;
    this.recordLength = recordLength;
    this.constructor = constructor;
    this.pointConstructor = pointConstructor;
    this.minVersion = minVersion;
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

  public LasVersion getMinVersion() {
    return this.minVersion;
  }

  public int getRecordLength() {
    return this.recordLength;
  }

  public BiFunction<LasPointCloud, ChannelReader, LasPoint> getRecordReader() {
    return this.recordReader;
  }

  public LasPoint newLasPoint(final double x, final double y, final double z) {
    return this.pointConstructor.apply(x, y, z);
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

  public LasPoint readLasPoint(final LasPointCloud lasPointCloud, final ChannelReader reader) {
    final LasPoint point = this.constructor.get();
    point.read(lasPointCloud, reader);
    return point;
  }
}
