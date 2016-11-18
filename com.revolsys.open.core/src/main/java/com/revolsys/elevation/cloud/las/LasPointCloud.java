package com.revolsys.elevation.cloud.las;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import com.revolsys.elevation.cloud.PointCloud;
import com.revolsys.elevation.tin.SimpleTriangulatedIrregularNetworkBuilder;
import com.revolsys.elevation.tin.TriangulatedIrregularNetwork;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.util.BoundingBoxUtil;
import com.revolsys.io.endian.EndianInput;
import com.revolsys.io.endian.EndianInputStream;
import com.revolsys.io.endian.EndianOutputStream;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Exceptions;
import com.revolsys.util.Pair;

public class LasPointCloud implements PointCloud {

  private static final long MAX_UNSIGNED_INT = 1l << 32;

  private static final Map<Pair<String, Integer>, BiFunction<LasPointCloud, byte[], Object>> PROPERTY_FACTORY_BY_KEY = new HashMap<>();

  static {
    LasProjection.init(PROPERTY_FACTORY_BY_KEY);
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
    final TriangulatedIrregularNetwork tin = tinBuilder.newTriangulatedIrregularNetwork(maxPoints);
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

  protected LasVariableLengthRecord getLasProperty(final Pair<String, Integer> key) {
    return this.lasProperties.get(key);
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
      final BiFunction<LasPointCloud, byte[], Object> converter = PROPERTY_FACTORY_BY_KEY.get(key);
      if (converter != null) {
        property.convertValue(converter, this);
      }
    }
    return byteCount;
  }

  protected void removeLasProperties(final String userId) {
    for (final Iterator<LasVariableLengthRecord> iterator = this.lasProperties.values()
      .iterator(); iterator.hasNext();) {
      final LasVariableLengthRecord property = iterator.next();
      if (userId.equals(property.getUserId())) {
        iterator.remove();
      }
    }
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
        LasProjection.setGeometryFactory(this, geometryFactory);
      }
    }
  }

  protected void setGeometryFactoryInternal(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
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
