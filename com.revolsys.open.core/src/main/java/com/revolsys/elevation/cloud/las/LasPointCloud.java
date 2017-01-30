package com.revolsys.elevation.cloud.las;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
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
import com.revolsys.elevation.tin.TriangulatedIrregularNetwork;
import com.revolsys.elevation.tin.quadedge.QuadEdgeDelaunayTinBuilder;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.util.BoundingBoxUtil;
import com.revolsys.io.BaseCloseable;
import com.revolsys.io.Buffers;
import com.revolsys.io.endian.EndianOutputStream;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Exceptions;
import com.revolsys.util.Pair;

public class LasPointCloud implements PointCloud, BaseCloseable, MapSerializer {

  private static final int BUFFER_RECORD_COUNT = 1000;

  private static final int MAX_BUFFER_SIZE = 8096;

  private static final long MAX_UNSIGNED_INT = 1l << 32;

  private static final Map<Pair<String, Integer>, BiFunction<LasPointCloud, byte[], Object>> PROPERTY_FACTORY_BY_KEY = new HashMap<>();

  static {
    LasProjection.init(PROPERTY_FACTORY_BY_KEY);
  }

  public static void forEachPoint(final Object source,
    final Consumer<? super LasPoint0Core> action) {
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

  private ReadableByteChannel in;

  private final Map<Pair<String, Integer>, LasVariableLengthRecord> lasProperties = new LinkedHashMap<>();

  private short majorVersion = 1;

  private short minorVersion = 2;

  private double offsetX = 0;

  private double offsetY = 0;

  private double offsetZ = 0;

  private long pointCount = 0;

  private final long[] pointCountByReturn = {
    0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l
  };

  private LasPointFormat pointFormat = LasPointFormat.Core;

  private List<LasPoint0Core> points = new ArrayList<>();

  private final byte[] projectId = new byte[16];

  private ByteBuffer recordBuffer;

  private int recordBufferProcessedRecordCount;

  private int recordBufferReadRecordCount;

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

  public void clear() {
    this.pointCount = 0;
    this.points.clear();
  }

  @Override
  public void close() {
    if (this.in != null) {
      try {
        this.in.close();
      } catch (final IOException e) {
      }
      this.in = null;
    }
  }

  public void forEachPoint(final Consumer<? super LasPoint0Core> action) {
    if (this.in == null) {
      this.points.forEach(action);
    } else {
      try (
        BaseCloseable closable = this) {
        for (int i = 0; i < this.pointCount; i++) {
          if (this.recordBufferProcessedRecordCount >= this.recordBufferReadRecordCount) {
            if (!readBuffer()) {
              return;
            }
          }
          this.recordBufferProcessedRecordCount++;
          final LasPoint0Core point = this.pointFormat.readLasPoint(this, this.recordDefinition,
            this.recordBuffer);
          action.accept(point);
        }
      } finally {
        this.in = null;
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

  public short getMajorVersion() {
    return this.majorVersion;
  }

  public short getMinorVersion() {
    return this.minorVersion;
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

  public List<LasPoint0Core> getPoints() {
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

  public String getVersion() {
    return this.majorVersion + "." + this.minorVersion;
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
    if (this.in != null) {
      forEachPoint((point) -> {
        if (filter.test((P)point)) {
          this.points.add(point);
        }
      });
    }
  }

  private boolean readBuffer() {
    try {
      final ByteBuffer recordBuffer = this.recordBuffer;
      recordBuffer.clear();
      int bufferByteCount = 0;
      final int recordLength = this.recordLength;
      do {
        final int readCount = this.in.read(recordBuffer);
        if (readCount == -1) {
          return false;
        } else {
          bufferByteCount += readCount;
        }
      } while (bufferByteCount == 0 || bufferByteCount % recordLength != 0);
      this.recordBufferProcessedRecordCount = 0;
      this.recordBufferReadRecordCount = bufferByteCount / recordLength;
      recordBuffer.flip();
      return true;
    } catch (final IOException e) {
      throw Exceptions.wrap("Error reading " + this.resource, e);
    }
  }

  @SuppressWarnings("unused")
  private void readHeader(final Resource resource) {
    try {
      this.in = resource.newReadableByteChannel();
      final ByteBuffer header = ByteBuffer.allocate(227);
      header.order(ByteOrder.LITTLE_ENDIAN);
      Buffers.readAll(this.in, header);
      if (Buffers.getUsAsciiString(header, 4).equals("LASF")) {
        this.fileSourceId = Buffers.getLEUnsignedShort(header);
        this.globalEncoding = Buffers.getLEUnsignedShort(header);

        // final long guid1 = Buffers.getLEUnsignedInt(header);
        // final int guid2 = Buffers.getLEUnsignedShort(header);
        // final int guid3 = Buffers.getLEUnsignedShort(header);
        // final byte[] guid4 = header.getBytes(8);
        header.get(this.projectId);

        this.majorVersion = Buffers.getUnsignedByte(header);
        this.minorVersion = Buffers.getUnsignedByte(header);
        this.systemIdentifier = Buffers.getUsAsciiString(header, 32);
        this.generatingSoftware = Buffers.getUsAsciiString(header, 32);
        this.dayOfYear = Buffers.getLEUnsignedShort(header);
        this.year = Buffers.getLEUnsignedShort(header);
        final Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.YEAR, this.year);
        calendar.set(Calendar.DAY_OF_YEAR, this.dayOfYear);
        this.date = new Date(calendar.getTimeInMillis());
        this.headerSize = Buffers.getLEUnsignedShort(header);
        this.pointRecordsOffset = Buffers.getLEUnsignedInt(header);
        final long numberOfVariableLengthRecords = Buffers.getLEUnsignedInt(header);
        final int pointFormatId = Buffers.getUnsignedByte(header);
        this.pointFormat = LasPointFormat.getById(pointFormatId);
        this.recordLength = Buffers.getLEUnsignedShort(header);
        final int bufferSize = MAX_BUFFER_SIZE / this.recordLength * this.recordLength;
        this.recordBuffer = ByteBuffer.allocateDirect(bufferSize);
        this.recordBuffer.order(ByteOrder.LITTLE_ENDIAN);
        this.pointCount = (int)Buffers.getLEUnsignedInt(header);
        for (int i = 0; i < 5; i++) {
          this.pointCountByReturn[i] = Buffers.getLEUnsignedInt(header);
        }
        this.resolutionX = header.getDouble();
        this.resolutionY = header.getDouble();
        this.resolutionZ = header.getDouble();

        final int coordinateSystemId = this.geometryFactory.getCoordinateSystemId();
        this.geometryFactory = GeometryFactory.fixed(coordinateSystemId, 3, 1 / this.resolutionX,
          1 / this.resolutionZ);

        this.offsetX = header.getDouble();
        this.offsetY = header.getDouble();
        this.offsetZ = header.getDouble();
        final double maxX = header.getDouble();
        final double minX = header.getDouble();
        final double maxY = header.getDouble();
        final double minY = header.getDouble();
        final double maxZ = header.getDouble();
        final double minZ = header.getDouble();

        if (this.headerSize > 227) {
          final ByteBuffer extendedHeader = ByteBuffer.allocate(this.headerSize - 227);
          extendedHeader.order(ByteOrder.LITTLE_ENDIAN);
          Buffers.readAll(this.in, extendedHeader);

          if (this.majorVersion > 1 || this.majorVersion == 1 && this.minorVersion >= 3) {
            final long startOfWaveformDataPacketRecord = Buffers.getLEUnsignedLong(extendedHeader); // TODO
            // unsigned
            // long
            // long support
            // needed
            if (this.majorVersion > 1 || this.majorVersion == 1 && this.minorVersion >= 4) {
              final long startOfFirstExetendedDataRecord = Buffers
                .getLEUnsignedLong(extendedHeader);
              // long support needed
              final long numberOfExtendedVariableLengthRecords = Buffers
                .getLEUnsignedInt(extendedHeader);
              this.pointCount = Buffers.getLEUnsignedLong(extendedHeader);
              for (int i = 0; i < 15; i++) {
                this.pointCountByReturn[i] = Buffers.getLEUnsignedLong(extendedHeader);
              }
            }
          }
        }
        this.headerSize += readVariableLengthRecords(numberOfVariableLengthRecords);
        this.bounds = new double[] {
          minX, minY, minZ, maxX, maxY, maxZ
        };
        final int skipCount = (int)(this.pointRecordsOffset - this.headerSize);
        Buffers.skipBytes(this.in, header, skipCount); // Skip to first point record
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
    final ByteBuffer vlrHeader = ByteBuffer.allocate(54);
    int byteCount = 0;
    for (int i = 0; i < numberOfVariableLengthRecords; i++) {
      Buffers.readAll(this.in, vlrHeader);
      @SuppressWarnings("unused")
      final int reserved = Buffers.getLEUnsignedShort(vlrHeader); // Ignore reserved value;
      final String userId = Buffers.getUsAsciiString(vlrHeader, 16);
      final int recordId = Buffers.getLEUnsignedShort(vlrHeader);
      final int valueLength = Buffers.getLEUnsignedShort(vlrHeader);
      final String description = Buffers.getUsAsciiString(vlrHeader, 32);
      final byte[] bytes = new byte[valueLength];
      final ByteBuffer vlrData = ByteBuffer.wrap(bytes);
      Buffers.readAll(this.in, vlrData);
      final LasVariableLengthRecord property = new LasVariableLengthRecord(userId, recordId,
        description, bytes);
      addProperty(property);
      byteCount += 54 + valueLength;
      vlrHeader.clear();
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
    addToMap(map, "majorVersion", this.majorVersion);
    addToMap(map, "minorVersion", this.minorVersion);
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
