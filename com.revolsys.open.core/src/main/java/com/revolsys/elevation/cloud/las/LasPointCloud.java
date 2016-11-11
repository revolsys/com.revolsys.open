package com.revolsys.elevation.cloud.las;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import com.revolsys.collection.map.Maps;
import com.revolsys.elevation.cloud.PointCloud;
import com.revolsys.elevation.tin.SimpleTriangulatedIrregularNetworkBuilder;
import com.revolsys.elevation.tin.TriangulatedIrregularNetwork;
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
import com.revolsys.geometry.util.BoundingBoxUtil;
import com.revolsys.io.endian.EndianInput;
import com.revolsys.io.endian.EndianInputStream;
import com.revolsys.io.endian.EndianOutputStream;
import com.revolsys.raster.io.format.tiff.TiffImage;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Exceptions;
import com.revolsys.util.Pair;

public class LasPointCloud implements PointCloud {
  private static final int LASF_PROJECTION_TIFF_GEO_ASCII_PARAMS = 34737;

  private static final int LASF_PROJECTION_TIFF_GEO_KEY_DIRECTORY_TAG = 34735;

  private static final int LASF_PROJECTION_TIFF_GEO_DOUBLE_PARAMS = 34736;

  private static final String LASF_PROJECTION = "LASF_Projection";

  private static final int LASF_PROJECTION_WKT_MATH_TRANSFORM = 2111;

  private static final int LASF_PROJECTION_WKT_COORDINATE_SYSTEM = 2112;

  private static final long MAX_UNSIGNED_INT = 1l << 32;

  @SuppressWarnings("unused")
  private static Object convertGeoTiffProjection(final LasPointCloud lasPointCloud,
    final byte[] bytes) {
    try {
      final List<Double> doubleParams = new ArrayList<>();
      {
        final LasVariableLengthRecord doubleParamsProperty = lasPointCloud.lasProperties
          .get(new Pair<>(LASF_PROJECTION, LASF_PROJECTION_TIFF_GEO_DOUBLE_PARAMS));
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
          .get(new Pair<>(LASF_PROJECTION, LASF_PROJECTION_TIFF_GEO_ASCII_PARAMS));
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
          } else if (tagLocation == LASF_PROJECTION_TIFF_GEO_DOUBLE_PARAMS) {
            final double value = doubleParams.get(offset);
            properties.put(keyId, value);
          } else if (tagLocation == LASF_PROJECTION_TIFF_GEO_ASCII_PARAMS) {
            final String value = new String(asciiParamsBytes, offset, count,
              StandardCharsets.US_ASCII);
            properties.put(keyId, value);
          }
        }
      }
      GeometryFactory geometryFactory = GeometryFactory.DEFAULT_3D;
      final double[] scaleFactors = new double[] {
        1.0 / lasPointCloud.resolutionX, 1.0 / lasPointCloud.resolutionZ
      };
      int coordinateSystemId = Maps.getInteger(properties, TiffImage.PROJECTED_COORDINATE_SYSTEM_ID,
        0);
      if (coordinateSystemId == 0) {
        coordinateSystemId = Maps.getInteger(properties, TiffImage.GEOGRAPHIC_TYPE_GEO_KEY, 0);
        if (coordinateSystemId != 0) {
          geometryFactory = GeometryFactory.fixed(coordinateSystemId, 3, scaleFactors);
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
            geometryFactory = GeometryFactory.fixed(epsgCoordinateSystem.getCoordinateSystemId(), 3,
              scaleFactors);
          }
        }
      } else {
        geometryFactory = GeometryFactory.fixed(coordinateSystemId, 3, scaleFactors);
      }
      lasPointCloud.geometryFactory = geometryFactory;
      return geometryFactory;
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  public static void forEachPoint(final Resource resource,
    final Consumer<? super LasPoint0Core> action) {
    final LasPointCloud pointCloud = new LasPointCloud(resource);
    pointCloud.forEachPoint(action);
  }

  public static TriangulatedIrregularNetwork newTriangulatedIrregularNetwork(
    final Resource resource) {
    return newTriangulatedIrregularNetwork(resource, Integer.MAX_VALUE);
  }

  public static TriangulatedIrregularNetwork newTriangulatedIrregularNetwork(
    final Resource resource, final int maxPoints) {
    final LasPointCloud pointCloud = new LasPointCloud(resource);
    final SimpleTriangulatedIrregularNetworkBuilder tinBuilder = new SimpleTriangulatedIrregularNetworkBuilder(
      pointCloud.getGeometryFactory());
    pointCloud.forEachPoint((lasPoint) -> {
      if (lasPoint.getReturnNumber() == 1) {
        tinBuilder.insertVertex(lasPoint);
      }
    });
    System.out.println(tinBuilder.getVertexCount());
    final long time = System.currentTimeMillis();
    final TriangulatedIrregularNetwork tin = tinBuilder.newTriangulatedIrregularNetwork(maxPoints);
    System.out.println(System.currentTimeMillis() - time);
    return tin;
  }

  private double[] bounds = BoundingBoxUtil.newBounds(3);

  private int dayOfYear = new GregorianCalendar().get(Calendar.DAY_OF_YEAR);

  private int fileSourceId;

  private String generatingSoftware = "RevolutionGIS";

  private GeometryFactory geometryFactory = GeometryFactory.DEFAULT_3D;

  private int globalEncoding = 0;

  private EndianInputStream in;

  private final Map<Pair<String, Integer>, LasVariableLengthRecord> lasProperties = new LinkedHashMap<>();

  private int majorVersion = 1;

  private int minorVersion = 2;

  private final long[] pointCountByReturn = {
    0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l
  };

  private double offsetX = 0;

  private double offsetY = 0;

  private double offsetZ = 0;

  private LasPointFormat pointFormat = LasPointFormat.Core;

  private int pointDataRecordLength = 20;

  private List<LasPoint0Core> points = new ArrayList<>();

  private final byte[] projectId = new byte[16];

  private long pointCount = 0;

  private RecordDefinition recordDefinition;

  private int recordLengthDiff = 0;

  private Resource resource;

  private double resolutionX = 0.001;

  private double resolutionY = 0.001;

  private double resolutionZ = 0.001;

  private String systemIdentifier = "TRANSFORMATION";

  private final Map<Pair<String, Integer>, BiFunction<LasPointCloud, byte[], Object>> vlrFactory = Maps
    .<Pair<String, Integer>, BiFunction<LasPointCloud, byte[], Object>> buildHash() //
    .add(new Pair<>(LASF_PROJECTION, LASF_PROJECTION_TIFF_GEO_KEY_DIRECTORY_TAG),
      LasPointCloud::convertGeoTiffProjection) //
    .getMap();

  private int year = new GregorianCalendar().get(Calendar.YEAR);

  public LasPointCloud(final GeometryFactory geometryFactory) {
    this(LasPointFormat.Core, geometryFactory);
  }

  public LasPointCloud(final LasPointFormat pointFormat, final GeometryFactory geometryFactory) {
    this.pointFormat = pointFormat;
    if (this.pointFormat.getId() > 5) {
      if (this.majorVersion < 1) {
        this.majorVersion = 1;
        this.minorVersion = 4;
      } else if (this.majorVersion == 1 && this.minorVersion < 4) {
        this.minorVersion = 4;
      }
      this.globalEncoding |= 0b10000;
    }
    this.pointDataRecordLength = pointFormat.getRecordLength();
    setGeometryFactory(geometryFactory);
  }

  @SuppressWarnings("unused")
  public LasPointCloud(final Resource resource) {
    this.resource = resource;
    try {
      this.in = resource.newBufferedInputStream(EndianInputStream::new);
      if (this.in.readUsAsciiString(4).equals("LASF")) {
        this.fileSourceId = this.in.readLEUnsignedShort();
        this.globalEncoding = this.in.readLEUnsignedShort();

        // final long guid1 = this.in.readLEUnsignedInt();
        // final int guid2 = this.in.readLEUnsignedShort();
        // final int guid3 = this.in.readLEUnsignedShort();
        // final byte[] guid4 = this.in.readBytes(8);
        this.in.read(this.projectId);

        this.majorVersion = this.in.readUnsignedByte();
        this.minorVersion = this.in.readUnsignedByte();
        this.systemIdentifier = this.in.readUsAsciiString(32);
        this.generatingSoftware = this.in.readUsAsciiString(32);
        this.dayOfYear = this.in.readLEUnsignedShort();
        this.year = this.in.readLEUnsignedShort();
        int headerSize = this.in.readLEUnsignedShort();
        final long offsetToPointData = this.in.readLEUnsignedInt();
        final long numberOfVariableLengthRecords = this.in.readLEUnsignedInt();
        final int pointFormatId = this.in.readUnsignedByte();
        this.pointFormat = LasPointFormat.getById(pointFormatId);
        this.pointDataRecordLength = this.in.readLEUnsignedShort();
        this.pointCount = (int)this.in.readLEUnsignedInt();
        for (int i = 0; i < 5; i++) {
          this.pointCountByReturn[i] = this.in.readLEUnsignedInt();
        }
        this.resolutionX = this.in.readLEDouble();
        this.resolutionY = this.in.readLEDouble();
        this.resolutionZ = this.in.readLEDouble();
        this.offsetX = this.in.readLEDouble();
        this.offsetY = this.in.readLEDouble();
        this.offsetZ = this.in.readLEDouble();
        final double maxX = this.in.readLEDouble();
        final double minX = this.in.readLEDouble();
        final double maxY = this.in.readLEDouble();
        final double minY = this.in.readLEDouble();
        final double maxZ = this.in.readLEDouble();
        final double minZ = this.in.readLEDouble();
        int knownVersionHeaderSize = 227;
        if (this.majorVersion > 1 || this.majorVersion == 1 && this.minorVersion >= 3) {
          final long startOfWaveformDataPacketRecord = this.in.readLEUnsignedLong(); // TODO
                                                                                     // unsigned
          // long
          // long support
          // needed
          knownVersionHeaderSize += 8;
          if (this.majorVersion > 1 || this.majorVersion == 1 && this.minorVersion >= 4) {
            final long startOfFirstExetendedDataRecord = this.in.readLEUnsignedLong();
            // long support needed
            final long numberOfExtendedVariableLengthRecords = this.in.readLEUnsignedInt();
            this.pointCount = this.in.readLEUnsignedLong();
            for (int i = 0; i < 15; i++) {
              this.pointCountByReturn[i] = this.in.readLEUnsignedLong();
            }
            knownVersionHeaderSize += 140;
          }
        }
        this.in.skipBytes(headerSize - knownVersionHeaderSize); // Skip to end
                                                                // of header
        headerSize += readVariableLengthRecords(this.in, numberOfVariableLengthRecords);
        this.bounds = new double[] {
          minX, minY, minZ, maxX, maxY, maxZ
        };
        final int skipCount = (int)(offsetToPointData - headerSize);
        this.in.skipBytes(skipCount); // Skip to first point record
        this.recordDefinition = this.pointFormat.newRecordDefinition(this.geometryFactory);
        final int expectedRecordLength = this.pointFormat.getRecordLength();
        this.recordLengthDiff = this.pointDataRecordLength - expectedRecordLength;
        this.points = new ArrayList<>((int)this.pointCount);
      } else {
        throw new IllegalArgumentException(resource + " is not a valid LAS file");
      }
    } catch (final IOException e) {
      throw Exceptions.wrap("Error reading " + resource, e);
    }
  }

  @SuppressWarnings("unchecked")
  public <P extends LasPoint0Core> P addPoint(final double x, final double y, final double z) {
    final LasPoint0Core lasPoint = this.pointFormat.newLasPoint(this, x, y, z);
    this.points.add(lasPoint);
    this.pointCount++;
    this.pointCountByReturn[0]++;
    BoundingBoxUtil.expand(this.bounds, 3, x, y, z);
    return (P)lasPoint;
  }

  protected void addProperty(final LasVariableLengthRecord property) {
    final Pair<String, Integer> key = property.getKey();
    this.lasProperties.put(key, property);
  }

  public void forEachPoint(final Consumer<? super LasPoint0Core> action) {
    if (this.in == null) {
      this.points.forEach(action);
    } else {
      try (
        EndianInput in = this.in) {
        for (int i = 0; i < this.pointCount; i++) {
          final LasPoint0Core point = this.pointFormat.readLasPoint(this, this.recordDefinition,
            in);
          action.accept(point);
          if (this.recordLengthDiff > 0) {
            in.skipBytes(this.recordLengthDiff);
          }
        }
      } catch (final IOException e) {
        throw Exceptions.wrap("Error reading " + this.resource, e);
      } finally {
        this.in = null;
      }
    }
  }

  public BoundingBox getBoundingBox() {
    return this.geometryFactory.newBoundingBox(3, this.bounds);
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

  public int getPointDataRecordLength() {
    return this.pointDataRecordLength;
  }

  public LasPointFormat getPointFormat() {
    return this.pointFormat;
  }

  public List<LasPoint0Core> getPoints() {
    return this.points;
  }

  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  public double getResolutionX() {
    return this.resolutionX;
  }

  public double getResolutionY() {
    return this.resolutionY;
  }

  public double getResolutionZ() {
    return this.resolutionZ;
  }

  public void read() {
    forEachPoint(this.points::add);
  }

  private int readVariableLengthRecords(final EndianInput in,
    final long numberOfVariableLengthRecords) throws IOException {
    int byteCount = 0;
    for (int i = 0; i < numberOfVariableLengthRecords; i++) {
      @SuppressWarnings("unused")
      final int reserved = in.readLEUnsignedShort(); // Ignore reserved value;
      final String userId = in.readUsAsciiString(16);
      final int recordId = in.readLEUnsignedShort();
      final int valueLength = in.readLEUnsignedShort();
      final String description = in.readUsAsciiString(32);
      final byte[] bytes = in.readBytes(valueLength);
      final LasVariableLengthRecord property = new LasVariableLengthRecord(userId, recordId,
        description, bytes);
      addProperty(property);
      byteCount += 54 + valueLength;
    }
    for (final Entry<Pair<String, Integer>, LasVariableLengthRecord> entry : this.lasProperties
      .entrySet()) {
      final Pair<String, Integer> key = entry.getKey();
      final LasVariableLengthRecord property = entry.getValue();
      final BiFunction<LasPointCloud, byte[], Object> converter = this.vlrFactory.get(key);
      if (converter != null) {
        property.convertValue(converter, this);
      }
    }
    return byteCount;
  }

  protected void setGeometryFactory(GeometryFactory geometryFactory) {
    final int coordinateSystemId = geometryFactory.getCoordinateSystemId();
    if (coordinateSystemId <= 0) {
      throw new IllegalArgumentException("A valid EPSG coordinate system must be specified");
    } else {
      double scaleXY = geometryFactory.getScaleXy();
      if (scaleXY == 0) {
        scaleXY = 0.001;
      }
      double scaleZ = geometryFactory.getScaleZ();
      if (scaleZ == 0) {
        scaleZ = 0.001;
      }
      geometryFactory = GeometryFactory.fixed(coordinateSystemId, scaleXY, scaleZ);

      final boolean changedCoordinateSystem = !geometryFactory
        .isSameCoordinateSystem(this.geometryFactory);
      this.geometryFactory = geometryFactory;
      this.recordDefinition = this.pointFormat.newRecordDefinition(this.geometryFactory);
      this.resolutionX = geometryFactory.getResolutionXy();
      this.resolutionY = geometryFactory.getResolutionXy();
      this.resolutionZ = geometryFactory.getResolutionZ();
      if (changedCoordinateSystem) {
        for (final Iterator<LasVariableLengthRecord> iterator = this.lasProperties.values()
          .iterator(); iterator.hasNext();) {
          final LasVariableLengthRecord property = iterator.next();
          if (LASF_PROJECTION.equals(property.getUserId())) {
            iterator.remove();
          }
        }
        final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
        if (coordinateSystem != null) {
          if (this.pointFormat.getId() <= 5) {
            // TODO create VLR for coordinate system
          } else {
            final String wkt = EpsgCoordinateSystems.toWkt(coordinateSystem);
            final byte[] stringBytes = wkt.getBytes(StandardCharsets.UTF_8);
            final byte[] bytes = new byte[stringBytes.length + 1];
            System.arraycopy(stringBytes, 0, bytes, 0, stringBytes.length);
            final LasVariableLengthRecord property = new LasVariableLengthRecord(LASF_PROJECTION,
              LASF_PROJECTION_WKT_COORDINATE_SYSTEM, "WKT", bytes, geometryFactory);
            addProperty(property);
          }
        }
      }
    }
  }

  public void writePointCloud(final Object target) {
    final Resource resource = Resource.getResource(target);
    try (
      EndianOutputStream out = resource.newBufferedOutputStream(EndianOutputStream::new)) {
      out.writeBytes("LASF");
      out.writeLEUnsignedShort(this.fileSourceId);
      out.writeLEUnsignedShort(this.globalEncoding);

      out.write(this.projectId);

      out.write((byte)this.majorVersion);
      out.write((byte)this.minorVersion);
      // out.writeString("TRANSFORMATION", 32); // System Identifier
      // out.writeString("RevolutionGis", 32); // Generating Software
      out.writeString(this.systemIdentifier, 32); // System Identifier
      out.writeString(this.generatingSoftware, 32); // Generating Software

      out.writeLEUnsignedShort(this.dayOfYear);
      out.writeLEUnsignedShort(this.year);

      int headerSize = 227;
      if (this.majorVersion > 1 || this.majorVersion == 1 && this.minorVersion >= 3) {
        headerSize += 8;
        if (this.majorVersion > 1 || this.majorVersion == 1 && this.minorVersion >= 4) {
          headerSize += 140;
        }
      }
      out.writeLEUnsignedShort(headerSize);

      final int numberOfVariableLengthRecords = this.lasProperties.size();
      int variableLengthRecordsSize = 0;
      for (final LasVariableLengthRecord record : this.lasProperties.values()) {
        variableLengthRecordsSize += 54 + record.getBytes().length;
      }

      final long offsetToPointData = headerSize + variableLengthRecordsSize;
      out.writeLEUnsignedInt(offsetToPointData);

      out.writeLEUnsignedInt(numberOfVariableLengthRecords);

      final int pointFormatId = this.pointFormat.getId();
      out.write(pointFormatId);
      out.writeLEUnsignedShort(this.pointDataRecordLength);
      if (this.pointCount > MAX_UNSIGNED_INT) {
        out.writeLEUnsignedInt(0);
      } else {
        out.writeLEUnsignedInt(this.pointCount);
      }
      for (int i = 0; i < 5; i++) {
        final long count = this.pointCountByReturn[i];
        if (count > MAX_UNSIGNED_INT) {
          out.writeLEUnsignedInt(0);
        } else {
          out.writeLEUnsignedInt(count);
        }
      }
      out.writeLEDouble(this.resolutionX);
      out.writeLEDouble(this.resolutionY);
      out.writeLEDouble(this.resolutionZ);

      out.writeLEDouble(this.offsetX);
      out.writeLEDouble(this.offsetY);
      out.writeLEDouble(this.offsetZ);

      for (int axisIndex = 0; axisIndex < 3; axisIndex++) {
        final double max = this.bounds[3 + axisIndex];
        out.writeLEDouble(max);
        final double min = this.bounds[axisIndex];
        out.writeLEDouble(min);
      }

      if (this.majorVersion > 1 || this.majorVersion == 1 && this.minorVersion >= 3) {
        out.writeLEUnsignedLong(0); // startOfWaveformDataPacketRecord
        if (this.majorVersion > 1 || this.majorVersion == 1 && this.minorVersion >= 4) {
          out.writeLEUnsignedLong(0); // startOfFirstExetendedDataRecord
          out.writeLEUnsignedInt(0); // numberOfExtendedVariableLengthRecords
          out.writeLEUnsignedLong(this.pointCount);
          for (int i = 0; i < 15; i++) {
            final long count = this.pointCountByReturn[i];
            out.writeLEUnsignedLong(count);
          }
        }
      }

      for (final LasVariableLengthRecord record : this.lasProperties.values()) {
        out.writeLEUnsignedShort(0);
        final String userId = record.getUserId();
        out.writeString(userId, 16);

        final int recordId = record.getRecordId();
        out.writeLEUnsignedShort(recordId);

        final int valueLength = record.getValueLength();
        out.writeLEUnsignedShort(valueLength);

        final String description = record.getDescription();
        out.writeString(description, 32);

        final byte[] bytes = record.getBytes();
        out.write(bytes);

      }
      for (final LasPoint0Core point : this.points) {
        point.write(this, out);
      }
    }
  }

}
