package com.revolsys.elevation.cloud.las;

import java.io.IOException;
import java.nio.ByteOrder;
import java.sql.Date;
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
import java.util.function.Predicate;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.cloud.PointCloud;
import com.revolsys.elevation.cloud.las.decoder.ArithmeticDecoder;
import com.revolsys.elevation.tin.TriangulatedIrregularNetwork;
import com.revolsys.elevation.tin.quadedge.QuadEdgeDelaunayTinBuilder;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.util.BoundingBoxUtil;
import com.revolsys.io.BaseCloseable;
import com.revolsys.io.channels.ChannelReader;
import com.revolsys.io.endian.EndianOutputStream;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Exceptions;
import com.revolsys.util.Pair;

public class LasPointCloud implements PointCloud, BaseCloseable, MapSerializer {

  private static final long MAX_UNSIGNED_INT = 1l << 32;

  private static final Map<Pair<String, Integer>, BiFunction<LasPointCloud, byte[], Object>> PROPERTY_FACTORY_BY_KEY = new HashMap<>();

  static {
    LasProjection.init(PROPERTY_FACTORY_BY_KEY);
    LasZipHeader.init(PROPERTY_FACTORY_BY_KEY);
  }

  public static void forEachPoint(final Object source, final Consumer<? super LasPoint> action) {
    final Resource resource = Resource.getResource(source);
    try (
      final LasPointCloud pointCloud = new LasPointCloud(resource)) {
      pointCloud.forEachPoint(action);
    }
  }

  private double[] bounds = BoundingBoxUtil.newBounds(3);

  private int dayOfYear = new GregorianCalendar().get(Calendar.DAY_OF_YEAR);

  private int fileSourceId;

  private String generatingSoftware = "RevolutionGIS";

  private GeometryFactory geometryFactory = GeometryFactory.DEFAULT_3D;

  private int globalEncoding = 0;

  private final Map<Pair<String, Integer>, LasVariableLengthRecord> lasProperties = new LinkedHashMap<>();

  private LasVersion version = LasVersion.VERSION_1_4;

  private double offsetX = 0;

  private double offsetY = 0;

  private double offsetZ = 0;

  private long pointCount = 0;

  private final long[] pointCountByReturn = {
    0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l
  };

  private LasPointFormat pointFormat = LasPointFormat.Core;

  private List<LasPoint> points = new ArrayList<>();

  private final byte[] projectId = new byte[16];

  private RecordDefinition recordDefinition;

  private int recordLength = 20;

  private double resolutionX = 0.001;

  private double resolutionY = 0.001;

  private double resolutionZ = 0.001;

  private Resource resource;

  private String systemIdentifier = "TRANSFORMATION";

  private int year = new GregorianCalendar().get(Calendar.YEAR);

  private Date date;

  private int headerSize;

  private long pointRecordsOffset;

  private boolean laszip;

  private ChannelReader reader;

  private ArithmeticDecoder decoder;

  public LasPointCloud(final GeometryFactory geometryFactory) {
    this(LasPointFormat.Core, geometryFactory);
  }

  public LasPointCloud(final LasPointFormat pointFormat, final GeometryFactory geometryFactory) {
    this.pointFormat = pointFormat;
    if (this.pointFormat.getId() > 5) {
      this.globalEncoding |= 0b10000;
    }
    this.recordLength = pointFormat.getRecordLength();
    setGeometryFactory(geometryFactory);
  }

  public LasPointCloud(final Resource resource) {
    this.resource = resource;
    readHeader(resource);
  }

  public LasPointCloud(final Resource resource, final GeometryFactory geometryFactory) {
    this.resource = resource;
    setGeometryFactory(geometryFactory);
    readHeader(resource);
  }

  @SuppressWarnings("unchecked")
  public <P extends LasPoint0Core> P addPoint(final double x, final double y, final double z) {
    final LasPoint lasPoint = this.pointFormat.newLasPoint(x, y, z);
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

  public void clear() {
    this.pointCount = 0;
    this.points.clear();
  }

  @Override
  public void close() {
    final ChannelReader reader = this.reader;
    this.reader = null;
    this.decoder = null;
    if (reader != null) {
      reader.close();
    }
  }

  public void forEachPoint(final Consumer<? super LasPoint> action) {
    if (this.reader == null) {
      this.points.forEach(action);
    } else if (this.laszip) {
      try (
        BaseCloseable closable = this) {
        for (int i = 0; i < this.pointCount; i++) {
          final LasPoint point = this.pointFormat.readLasPoint(this, this.reader);
          action.accept(point);
        }
      } finally {
        this.reader = null;
      }
    } else {
      try (
        BaseCloseable closable = this) {
        for (int i = 0; i < this.pointCount; i++) {
          final LasPoint point = this.pointFormat.readLasPoint(this, this.reader);
          action.accept(point);
        }
      } finally {
        this.reader = null;
      }
    }
  }

  public BoundingBox getBoundingBox() {
    return this.geometryFactory.newBoundingBox(3, this.bounds);
  }

  public int getDayOfYear() {
    return this.dayOfYear;
  }

  public int getFileSourceId() {
    return this.fileSourceId;
  }

  public String getGeneratingSoftware() {
    return this.generatingSoftware;
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

  public long getPointCount() {
    return this.pointCount;
  }

  public LasPointFormat getPointFormat() {
    return this.pointFormat;
  }

  public int getPointFormatId() {
    return this.pointFormat.getId();
  }

  public List<LasPoint> getPoints() {
    return this.points;
  }

  public byte[] getProjectId() {
    return this.projectId;
  }

  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  public int getRecordLength() {
    return this.recordLength;
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

  public String getSystemIdentifier() {
    return this.systemIdentifier;
  }

  public LasVersion getVersion() {
    return this.version;
  }

  public int getYear() {
    return this.year;
  }

  public TriangulatedIrregularNetwork newTriangulatedIrregularNetwork() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final QuadEdgeDelaunayTinBuilder tinBuilder = new QuadEdgeDelaunayTinBuilder(geometryFactory);
    forEachPoint((lasPoint) -> {
      tinBuilder.insertVertex(lasPoint);
    });
    final TriangulatedIrregularNetwork tin = tinBuilder.newTriangulatedIrregularNetwork();
    return tin;
  }

  public TriangulatedIrregularNetwork newTriangulatedIrregularNetwork(
    final Predicate<? super Point> filter) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final QuadEdgeDelaunayTinBuilder tinBuilder = new QuadEdgeDelaunayTinBuilder(geometryFactory);
    forEachPoint((lasPoint) -> {
      if (filter.test(lasPoint)) {
        tinBuilder.insertVertex(lasPoint);
      }
    });
    final TriangulatedIrregularNetwork tin = tinBuilder.newTriangulatedIrregularNetwork();
    return tin;
  }

  public void read() {
    forEachPoint(this.points::add);
  }

  @SuppressWarnings("unchecked")
  public <P extends Point> void read(final Predicate<P> filter) {
    if (this.reader != null) {
      forEachPoint((point) -> {
        if (filter.test((P)point)) {
          this.points.add(point);
        }
      });
    }
  }

  @SuppressWarnings("unused")
  private void readHeader(final Resource resource) {
    try {
      this.reader = resource.newChannelReader(8096, ByteOrder.LITTLE_ENDIAN);
      if (this.reader.getUsAsciiString(4).equals("LASF")) {
        this.fileSourceId = this.reader.getUnsignedShort();
        this.globalEncoding = this.reader.getUnsignedShort();

        // final long guid1 = Buffers.getLEUnsignedInt(header);
        // final int guid2 = Buffers.getLEUnsignedShort(header);
        // final int guid3 = Buffers.getLEUnsignedShort(header);
        // final byte[] guid4 = header.getBytes(8);
        this.reader.getBytes(this.projectId);

        this.version = new LasVersion(this.reader);
        this.systemIdentifier = this.reader.getUsAsciiString(32);
        this.generatingSoftware = this.reader.getUsAsciiString(32);
        this.dayOfYear = this.reader.getUnsignedShort();
        this.year = this.reader.getUnsignedShort();
        final Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.YEAR, this.year);
        calendar.set(Calendar.DAY_OF_YEAR, this.dayOfYear);
        this.date = new Date(calendar.getTimeInMillis());
        this.headerSize = this.reader.getUnsignedShort();
        this.pointRecordsOffset = this.reader.getUnsignedInt();
        final long numberOfVariableLengthRecords = this.reader.getUnsignedInt();
        int pointFormatId = this.reader.getUnsignedByte();
        if (pointFormatId > 127) {
          pointFormatId -= 128;
          this.laszip = true;
          this.decoder = new ArithmeticDecoder(this.reader);
        }
        this.pointFormat = LasPointFormat.getById(pointFormatId);
        this.recordLength = this.reader.getUnsignedShort();
        this.pointCount = (int)this.reader.getUnsignedInt();
        for (int i = 0; i < 5; i++) {
          this.pointCountByReturn[i] = this.reader.getUnsignedInt();
        }
        this.resolutionX = this.reader.getDouble();
        this.resolutionY = this.reader.getDouble();
        this.resolutionZ = this.reader.getDouble();

        final int coordinateSystemId = this.geometryFactory.getCoordinateSystemId();
        this.geometryFactory = GeometryFactory.fixed(coordinateSystemId, 3, 1 / this.resolutionX,
          1 / this.resolutionZ);

        this.offsetX = this.reader.getDouble();
        this.offsetY = this.reader.getDouble();
        this.offsetZ = this.reader.getDouble();
        final double maxX = this.reader.getDouble();
        final double minX = this.reader.getDouble();
        final double maxY = this.reader.getDouble();
        final double minY = this.reader.getDouble();
        final double maxZ = this.reader.getDouble();
        final double minZ = this.reader.getDouble();

        if (this.headerSize > 227) {

          if (this.version.atLeast(LasVersion.VERSION_1_3)) {
            final long startOfWaveformDataPacketRecord = this.reader.getUnsignedLong(); // TODO
            // unsigned
            // long
            // long support
            // needed
            if (this.version.atLeast(LasVersion.VERSION_1_4)) {
              final long startOfFirstExetendedDataRecord = this.reader.getUnsignedLong();
              final long numberOfExtendedVariableLengthRecords = this.reader.getUnsignedInt();
              this.pointCount = this.reader.getUnsignedLong();
              for (int i = 0; i < 15; i++) {
                this.pointCountByReturn[i] = this.reader.getUnsignedLong();
              }
            }
          }
        }
        this.headerSize += readVariableLengthRecords(numberOfVariableLengthRecords);
        this.bounds = new double[] {
          minX, minY, minZ, maxX, maxY, maxZ
        };
        if (this.version.equals(LasVersion.VERSION_1_0)) {
          this.reader.skipBytes(2);
          this.headerSize += 2;
        }
        final int skipCount = (int)(this.pointRecordsOffset - this.headerSize);
        this.reader.skipBytes(skipCount); // Skip to first point record

        this.recordDefinition = this.pointFormat.newRecordDefinition(this.geometryFactory);
        this.points = new ArrayList<>((int)this.pointCount);

      } else {
        throw new IllegalArgumentException(resource + " is not a valid LAS file");
      }
    } catch (final IOException e) {
      throw Exceptions.wrap("Error reading " + resource, e);
    }
  }

  private int readVariableLengthRecords(final long numberOfVariableLengthRecords)
    throws IOException {
    int byteCount = 0;
    for (int i = 0; i < numberOfVariableLengthRecords; i++) {
      @SuppressWarnings("unused")
      final int reserved = this.reader.getUnsignedShort(); // Ignore reserved value;
      final String userId = this.reader.getUsAsciiString(16);
      final int recordId = this.reader.getUnsignedShort();
      final int valueLength = this.reader.getUnsignedShort();
      final String description = this.reader.getUsAsciiString(32);
      final byte[] bytes = this.reader.getBytes(valueLength);
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
      double scaleXY = geometryFactory.getScaleXY();
      if (scaleXY == 0) {
        scaleXY = 1 / this.resolutionX;
      }
      double scaleZ = geometryFactory.getScaleZ();
      if (scaleZ == 0) {
        scaleZ = 1 / this.resolutionZ;
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

  @Override
  public MapEx toMap() {
    final MapEx map = new LinkedHashMapEx();
    addToMap(map, "version", this.version);
    addToMap(map, "fileSourceId", this.fileSourceId, 0);
    addToMap(map, "systemIdentifier", this.systemIdentifier);
    addToMap(map, "generatingSoftware", this.generatingSoftware);
    addToMap(map, "date", this.date);
    addToMap(map, "headerSize", this.headerSize);
    addToMap(map, "pointRecordsOffset", this.pointRecordsOffset, 0);
    addToMap(map, "pointFormat", this.pointFormat.getId());
    addToMap(map, "pointCount", this.pointCount);
    int returnCount = 15;
    if (this.pointFormat.getId() < 6) {
      returnCount = 5;
    }
    int returnIndex = 0;
    final List<Long> pointCountByReturn = new ArrayList<>();
    for (final long pointCountForReturn : this.pointCountByReturn) {
      if (returnIndex < returnCount) {
        pointCountByReturn.add(pointCountForReturn);
      }
      returnIndex++;
    }
    addToMap(map, "pointCountByReturn", pointCountByReturn);
    return map;
  }

  public void writePointCloud(final Object target) {
    final Resource resource = Resource.getResource(target);
    try (
      EndianOutputStream out = resource.newBufferedOutputStream(EndianOutputStream::new)) {
      out.writeBytes("LASF");
      out.writeLEUnsignedShort(this.fileSourceId);
      out.writeLEUnsignedShort(this.globalEncoding);

      out.write(this.projectId);

      out.write((byte)this.version.getMajor());
      out.write((byte)this.version.getMinor());
      // out.writeString("TRANSFORMATION", 32); // System Identifier
      // out.writeString("RevolutionGis", 32); // Generating Software
      out.writeString(this.systemIdentifier, 32); // System Identifier
      out.writeString(this.generatingSoftware, 32); // Generating Software

      out.writeLEUnsignedShort(this.dayOfYear);
      out.writeLEUnsignedShort(this.year);

      int headerSize = 227;
      if (this.version.atLeast(LasVersion.VERSION_1_3)) {
        headerSize += 8;
        if (this.version.atLeast(LasVersion.VERSION_1_4)) {
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
      out.writeLEUnsignedShort(this.recordLength);
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

      if (this.version.atLeast(LasVersion.VERSION_1_3)) {
        out.writeLEUnsignedLong(0); // startOfWaveformDataPacketRecord
        if (this.version.atLeast(LasVersion.VERSION_1_4)) {
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
      for (final LasPoint point : this.points) {
        point.write(this, out);
      }
    }
  }
}
