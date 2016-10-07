package com.revolsys.elevation.cloud.las;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.revolsys.collection.map.Maps;
import com.revolsys.datatype.DataTypes;
import com.revolsys.elevation.cloud.PointCloud;
import com.revolsys.elevation.tin.TriangulatedIrregularNetwork;
import com.revolsys.elevation.tin.TriangulatedIrregularNetworkBuilder;
import com.revolsys.geometry.cs.Area;
import com.revolsys.geometry.cs.Authority;
import com.revolsys.geometry.cs.Axis;
import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.cs.GeographicCoordinateSystem;
import com.revolsys.geometry.cs.LinearUnit;
import com.revolsys.geometry.cs.ProjectedCoordinateSystem;
import com.revolsys.geometry.cs.Projection;
import com.revolsys.geometry.cs.ProjectionParameterNames;
import com.revolsys.geometry.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.endian.EndianInput;
import com.revolsys.io.endian.EndianInputStream;
import com.revolsys.raster.io.format.tiff.TiffImage;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionBuilder;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Exceptions;
import com.revolsys.util.Pair;
import com.revolsys.util.function.Function2;
import com.revolsys.util.function.Function3;

public class LasPointCloud implements PointCloud {
  private static final Map<Integer, Integer> RECORD_LENGTHS = Maps.<Integer, Integer> buildHash() //
    .add(0, 20)
    .add(1, 28)
    .add(2, 26)
    .add(3, 34)
    .add(4, 57)
    .add(5, 63)
    .add(6, 30)
    .add(7, 36)
    .add(8, 38)
    .add(9, 59)
    .add(10, 67)
    .getMap();

  private static final Map<Integer, Function3<LasPointCloud, RecordDefinition, EndianInput, LasPoint0Core>> RECORD_READER = Maps
    .<Integer, Function3<LasPointCloud, RecordDefinition, EndianInput, LasPoint0Core>> buildHash() //
    .add(0, LasPoint0Core::newLasPoint)
    .add(1, LasPoint1GpsTime::newLasPoint)
    .add(2, LasPoint2Rgb::newLasPoint)
    .add(3, LasPoint3GpsTimeRgb::newLasPoint)
    .add(4, LasPoint4GpsTimeWavePackets::newLasPoint)
    .add(5, LasPoint5GpsTimeRgbWavePackets::newLasPoint)
    .add(6, LasPoint6GpsTime::newLasPoint)
    .add(7, LasPoint7GpsTimeRgb::newLasPoint)
    .add(8, LasPoint8GpsTimeRgbNir::newLasPoint)
    .add(9, LasPoint9GpsTimeWavePackets::newLasPoint)
    .add(10, LasPoint10GpsTimeRgbNirWavePackets::newLasPoint)
    .getMap();

  private static Object convertGeoTiffProjection(final LasPointCloud lasPointCloud,
    final byte[] bytes) {
    try {
      final List<Double> doubleParams = new ArrayList<>();
      {
        final LasVariableLengthRecord doubleParamsProperty = lasPointCloud.lasProperties
          .get(new Pair<>("LASF_Projection", 34736));
        if (doubleParamsProperty != null) {
          final byte[] doubleParamBytes = doubleParamsProperty.getBytes();
          final ByteBuffer buffer = ByteBuffer.wrap(doubleParamBytes);
          buffer.order(ByteOrder.LITTLE_ENDIAN);
          for (int i = 0; i < doubleParamBytes.length / 8; i++) {
            final double value = buffer.getDouble();
            doubleParams.add(value);
          }
        }
      }
      byte[] asciiParamsBytes;
      {
        final LasVariableLengthRecord asciiParamsProperty = lasPointCloud.lasProperties
          .get(new Pair<>("LASF_Projection", 34737));
        if (asciiParamsProperty == null) {
          asciiParamsBytes = new byte[0];
        } else {
          asciiParamsBytes = asciiParamsProperty.getBytes();
        }
      }
      final Map<Integer, Object> properties = new LinkedHashMap<>();
      try (
        final EndianInput in = new EndianInputStream(new ByteArrayInputStream(bytes))) {
        final int keyDirectoryVersion = in.readLEUnsignedShort();
        final int keyRevision = in.readLEUnsignedShort();
        final int minorRevision = in.readLEUnsignedShort();
        final int numberOfKeys = in.readLEUnsignedShort();
        for (int i = 0; i < numberOfKeys; i++) {
          final int keyId = in.readLEUnsignedShort();
          final int tagLocation = in.readLEUnsignedShort();
          final int count = in.readLEUnsignedShort();
          final int offset = in.readLEUnsignedShort();
          if (tagLocation == 0) {
            properties.put(keyId, offset);
          } else if (tagLocation == 34736) {
            final double value = doubleParams.get(offset);
            properties.put(keyId, value);
          } else if (tagLocation == 34737) {
            final String value = new String(asciiParamsBytes, offset, count,
              StandardCharsets.US_ASCII);
            properties.put(keyId, value);
          }
        }
      }
      GeometryFactory geometryFactory = null;
      int coordinateSystemId = Maps.getInteger(properties, TiffImage.PROJECTED_COORDINATE_SYSTEM_ID,
        0);
      if (coordinateSystemId == 0) {
        coordinateSystemId = Maps.getInteger(properties, TiffImage.GEOGRAPHIC_TYPE_GEO_KEY, 0);
        if (coordinateSystemId != 0) {
          geometryFactory = GeometryFactory.floating(coordinateSystemId, 2);
        }
      } else if (coordinateSystemId <= 0 || coordinateSystemId == 32767) {
        final int geoSrid = Maps.getInteger(properties, TiffImage.GEOGRAPHIC_TYPE_GEO_KEY, 0);
        if (geoSrid != 0) {
          if (geoSrid > 0 && geoSrid < 32767) {
            final GeographicCoordinateSystem geographicCoordinateSystem = EpsgCoordinateSystems
              .getCoordinateSystem(geoSrid);
            final String name = "unknown";
            final Projection projection = TiffImage.getProjection(properties);
            final Area area = null;

            final Map<String, Object> parameters = new LinkedHashMap<>();
            TiffImage.addDoubleParameter(parameters, ProjectionParameterNames.STANDARD_PARALLEL_1,
              properties, TiffImage.STANDARD_PARALLEL_1_KEY);
            TiffImage.addDoubleParameter(parameters, ProjectionParameterNames.STANDARD_PARALLEL_2,
              properties, TiffImage.STANDARD_PARALLEL_2_KEY);
            TiffImage.addDoubleParameter(parameters, ProjectionParameterNames.LONGITUDE_OF_CENTER,
              properties, TiffImage.LONGITUDE_OF_CENTER_2_KEY);
            TiffImage.addDoubleParameter(parameters, ProjectionParameterNames.LATITUDE_OF_CENTER,
              properties, TiffImage.LATITUDE_OF_CENTER_2_KEY);
            TiffImage.addDoubleParameter(parameters, ProjectionParameterNames.FALSE_EASTING,
              properties, TiffImage.FALSE_EASTING_KEY);
            TiffImage.addDoubleParameter(parameters, ProjectionParameterNames.FALSE_NORTHING,
              properties, TiffImage.FALSE_NORTHING_KEY);

            final LinearUnit linearUnit = TiffImage.getLinearUnit(properties);
            final List<Axis> axis = null;
            final Authority authority = null;
            final ProjectedCoordinateSystem coordinateSystem = new ProjectedCoordinateSystem(
              coordinateSystemId, name, geographicCoordinateSystem, area, projection, parameters,
              linearUnit, axis, authority, false);
            final CoordinateSystem epsgCoordinateSystem = EpsgCoordinateSystems
              .getCoordinateSystem(coordinateSystem);
            geometryFactory = GeometryFactory.floating(epsgCoordinateSystem.getCoordinateSystemId(),
              2);
          }
        }
      } else {
        geometryFactory = GeometryFactory.floating(coordinateSystemId, 2);
      }
      lasPointCloud.setGeometryFactory(geometryFactory);
      return geometryFactory;
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  private static RecordDefinition newRecordDefinition(final GeometryFactory geometryFactory,
    final int recordType) {
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
    if (recordType == 1 || recordType >= 3) {
      builder.addField("gpsTime", DataTypes.DOUBLE, true);
    }
    if (recordType == 2 || recordType == 3 || recordType == 5 || recordType == 7 || recordType == 8
      || recordType == 10) {
      builder.addField("red", DataTypes.INT, true);
      builder.addField("green", DataTypes.INT, true);
      builder.addField("blue", DataTypes.INT, true);
    }
    if (recordType == 8 || recordType == 10) {
      builder.addField("nir", DataTypes.INT, true);
    }
    if (recordType == 4 || recordType == 5 || recordType == 9 || recordType == 10) {
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

  private final Map<Pair<String, Integer>, Function2<LasPointCloud, byte[], Object>> vlrFactory = Maps
    .<Pair<String, Integer>, Function2<LasPointCloud, byte[], Object>> buildHash() //
    .add(new Pair<>("LASF_Projection", 34735), LasPointCloud::convertGeoTiffProjection) //
    .getMap();

  private double scaleX;

  private double scaleY;

  private double scaleZ;

  private double offsetX;

  private double offsetY;

  private double offsetZ;

  private int pointDataRecordLength;

  private int pointDataRecordFormat;

  private GeometryFactory geometryFactory = GeometryFactory.DEFAULT;

  private final List<LasPoint0Core> points = new ArrayList<>();

  private final Map<Pair<String, Integer>, LasVariableLengthRecord> lasProperties = new LinkedHashMap<>();

  private BoundingBox boundingBox;

  public LasPointCloud(final Resource resource) {
    try (
      final EndianInput in = resource.newBufferedInputStream(EndianInputStream::new)) {
      if (in.readUsAsciiString(4).equals("LASF")) {
        final int fileSourceId = in.readLEUnsignedShort();
        final int globalEncording = in.readLEUnsignedShort();
        final long guid1 = in.readLEUnsignedInt();
        final int guid2 = in.readLEUnsignedShort();
        final int guid3 = in.readLEUnsignedShort();
        final byte[] guid4 = in.readBytes(8);
        final int majorVersion = in.readUnsignedByte();
        final int minorVersion = in.readUnsignedByte();
        final String systemIdentifier = in.readUsAsciiString(32);
        final String generatingSoftware = in.readUsAsciiString(32);
        final int dayOfYear = in.readLEUnsignedShort();
        final int year = in.readLEUnsignedShort();
        int headerSize = in.readLEUnsignedShort();
        final long offsetToPointData = in.readLEUnsignedInt();
        final long numberOfVariableLengthRecords = in.readLEUnsignedInt();
        this.pointDataRecordFormat = in.readUnsignedByte();
        this.pointDataRecordLength = in.readLEUnsignedShort();
        long numberOfPointRecords = in.readLEUnsignedInt();
        List<Long> numberOfPointRecordsByReturn = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
          numberOfPointRecordsByReturn.add(in.readLEUnsignedInt());
        }
        this.scaleX = in.readLEDouble();
        this.scaleY = in.readLEDouble();
        this.scaleZ = in.readLEDouble();
        this.offsetX = in.readLEDouble();
        this.offsetY = in.readLEDouble();
        this.offsetZ = in.readLEDouble();
        final double maxX = in.readLEDouble();
        final double minX = in.readLEDouble();
        final double maxY = in.readLEDouble();
        final double minY = in.readLEDouble();
        final double maxZ = in.readLEDouble();
        final double minZ = in.readLEDouble();
        int knownVersionHeaderSize = 227;
        if (majorVersion > 1 || majorVersion == 1 && minorVersion >= 3) {
          final long startOfWaveformDataPacketRecord = in.readLEUnsignedLong(); // TODO unsigned
                                                                                // long
                                                                                // long support
                                                                                // needed
          knownVersionHeaderSize += 8;
          if (majorVersion == 1 || majorVersion == 1 && minorVersion >= 4) {
            final long startOfFirstExetendedDataRecord = in.readLEUnsignedLong();
            // long support needed
            final long numberOfExtendedVariableLengthRecords = in.readLEUnsignedLong();
            numberOfPointRecords = in.readLEUnsignedLong();
            numberOfPointRecordsByReturn = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
              numberOfPointRecordsByReturn.add(in.readLEUnsignedLong());
            }
            knownVersionHeaderSize += 140;
          }
        }
        in.skipBytes(headerSize - knownVersionHeaderSize); // Skip to end of header
        headerSize += readVariableLengthRecords(in, numberOfVariableLengthRecords);
        double[] bounds = {
          minX, minY, minZ, maxX, maxY, maxZ
        };
        this.boundingBox = this.geometryFactory.newBoundingBox(3, bounds);
        final int skipCount = (int)(offsetToPointData - headerSize);
        in.skipBytes(skipCount); // Skip to first point record
        final RecordDefinition recordDefinition = newRecordDefinition(this.geometryFactory,
          this.pointDataRecordFormat);
        final Function3<LasPointCloud, RecordDefinition, EndianInput, LasPoint0Core> pointReader = RECORD_READER
          .get(this.pointDataRecordFormat);
        final int expectedRecordLength = RECORD_LENGTHS.get(this.pointDataRecordFormat);
        final int recordLengthDiff = this.pointDataRecordLength - expectedRecordLength;
        for (int i = 0; i < numberOfPointRecords; i++) {
          final LasPoint0Core point = pointReader.apply(this, recordDefinition, in);
          this.points.add(point);
          if (recordLengthDiff > 0) {
            in.skipBytes(recordLengthDiff);
          }
        }
      } else {
        throw new IllegalArgumentException(resource + " is not a valid LAS file");
      }
    } catch (final IOException e) {
      throw Exceptions.wrap("Error reading " + resource, e);
    }
  }

  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  public double getOffsetX() {
    return this.offsetX;
  }

  public double getOffsetY() {
    return this.offsetY;
  }

  public double getOffsetZ() {
    return this.offsetZ;
  }

  public int getPointDataRecordFormat() {
    return this.pointDataRecordFormat;
  }

  public int getPointDataRecordLength() {
    return this.pointDataRecordLength;
  }

  public List<LasPoint0Core> getPoints() {
    return this.points;
  }

  public double getScaleX() {
    return this.scaleX;
  }

  public double getScaleY() {
    return this.scaleY;
  }

  public double getScaleZ() {
    return this.scaleZ;
  }

  public TriangulatedIrregularNetwork newTriangulatedIrregularNetwork() {
    final TriangulatedIrregularNetworkBuilder tin = new TriangulatedIrregularNetworkBuilder(
      this.boundingBox, false);
    int i = 0;
    for (final LasPoint0Core point : this.points) {
      tin.insertNode(point);
      if (i < 100) {
        i++;
      } else {
        break;
      }
    }
    tin.finishEditing();
    return tin;
  }

  public int readVariableLengthRecords(final EndianInput in,
    final long numberOfVariableLengthRecords) throws IOException {
    int byteCount = 0;
    for (int i = 0; i < numberOfVariableLengthRecords; i++) {
      in.readLEShort(); // Ignore reserved value;
      final String userId = in.readUsAsciiString(16);
      final int recordId = in.readLEUnsignedShort();
      final int valueLength = in.readLEUnsignedShort();
      final String description = in.readUsAsciiString(32);
      final byte[] bytes = in.readBytes(valueLength);
      final Pair<String, Integer> key = new Pair<>(userId, recordId);
      final LasVariableLengthRecord property = new LasVariableLengthRecord(userId, recordId,
        description, bytes);
      this.lasProperties.put(key, property);
      byteCount += 54 + valueLength;
    }
    for (final Entry<Pair<String, Integer>, LasVariableLengthRecord> entry : this.lasProperties
      .entrySet()) {
      final Pair<String, Integer> key = entry.getKey();
      final LasVariableLengthRecord property = entry.getValue();
      final Function2<LasPointCloud, byte[], Object> converter = this.vlrFactory.get(key);
      if (converter != null) {
        property.convertValue(converter, this);
      }
    }
    return byteCount;
  }

  protected void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }
}
