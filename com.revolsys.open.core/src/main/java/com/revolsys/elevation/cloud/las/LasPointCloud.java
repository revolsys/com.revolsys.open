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
import com.revolsys.elevation.cloud.las.pointformat.LasPoint;
import com.revolsys.elevation.cloud.las.pointformat.LasPoint0Core;
import com.revolsys.elevation.cloud.las.pointformat.LasPointFormat;
import com.revolsys.elevation.cloud.las.zip.ArithmeticDecoder;
import com.revolsys.elevation.cloud.las.zip.LazDecompress;
import com.revolsys.elevation.cloud.las.zip.LazDecompressGpsTime11V1;
import com.revolsys.elevation.cloud.las.zip.LazDecompressGpsTime11V2;
import com.revolsys.elevation.cloud.las.zip.LazDecompressPoint10V1;
import com.revolsys.elevation.cloud.las.zip.LazDecompressPoint10V2;
import com.revolsys.elevation.cloud.las.zip.LazDecompressRgb12V1;
import com.revolsys.elevation.cloud.las.zip.LazDecompressRgb12V2;
import com.revolsys.elevation.cloud.las.zip.LazItemType;
import com.revolsys.elevation.tin.TriangulatedIrregularNetwork;
import com.revolsys.elevation.tin.quadedge.QuadEdgeDelaunayTinBuilder;
import com.revolsys.geometry.cs.CoordinateSystem;
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

  public static final Version VERSION_1_0 = new Version(1, 0);

  public static final Version VERSION_1_1 = new Version(1, 1);

  public static final Version VERSION_1_2 = new Version(1, 2);

  public static final Version VERSION_1_3 = new Version(1, 3);

  public static final Version VERSION_1_4 = new Version(1, 4);

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

  private GeometryFactory geometryFactory = GeometryFactory.fixedNoSrid(1000.0, 1000.0, 1000.0);

  private int globalEncoding = 0;

  private final Map<Pair<String, Integer>, LasVariableLengthRecord> lasProperties = new LinkedHashMap<>();

  private Version version = LasPointCloud.VERSION_1_4;

  private long pointCount = 0;

  private final long[] pointCountByReturn = {
    0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l
  };

  private LasPointFormat pointFormat = LasPointFormat.Core;

  private List<LasPoint> points = new ArrayList<>();

  private final byte[] projectId = new byte[16];

  private RecordDefinition recordDefinition;

  private int recordLength = 20;

  private Resource resource;

  private String systemIdentifier = "TRANSFORMATION";

  private int year = new GregorianCalendar().get(Calendar.YEAR);

  private Date date;

  private int headerSize;

  private long pointRecordsOffset;

  private boolean laszip;

  private ChannelReader reader;

  private LasZipHeader lasZipHeader;

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
    final LasPoint lasPoint = this.pointFormat.newLasPoint(this, x, y, z);
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
    if (reader != null) {
      reader.close();
    }
  }

  public void forEachPoint(final Consumer<? super LasPoint> action) {
    if (this.reader == null) {
      this.points.forEach(action);
    } else if (this.pointCount == 0) {
      this.reader = null;
    } else if (this.laszip) {
      forEachPointLaz(action);
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

  private void forEachPointLaz(final Consumer<? super LasPoint> action) {
    try (
      ArithmeticDecoder decoder = new ArithmeticDecoder(this.reader);
      BaseCloseable closable = this;) {
      final LazDecompress[] pointDecompressors = newLazDecompressors(decoder);

      if (this.lasZipHeader.isCompressor(LasZipHeader.LASZIP_COMPRESSOR_POINTWISE)) {
        forEachPointLazPointwise(decoder, pointDecompressors, action);
      } else {
        forEachPointLazChunked(decoder, pointDecompressors, action);
      }
    } finally {
      this.reader = null;
    }
  }

  private void forEachPointLazChunked(final ArithmeticDecoder decoder,
    final LazDecompress[] pointDecompressors, final Consumer<? super LasPoint> action) {
    final long chunkTableOffset = this.reader.getLong();
    final long chunkSize = this.lasZipHeader.getChunkSize();
    long chunkReadCount = chunkSize;
    for (int i = 0; i < this.pointCount; i++) {
      final LasPoint point;
      final LasPointFormat pointFormat = this.pointFormat;
      if (chunkSize == chunkReadCount) {
        point = pointFormat.readLasPoint(this, this.reader);
        for (final LazDecompress pointDecompressor : pointDecompressors) {
          pointDecompressor.init(point);
        }
        decoder.reset();
        chunkReadCount = 0;
      } else {
        point = pointFormat.newLasPoint(this);
        for (final LazDecompress pointDecompressor : pointDecompressors) {
          pointDecompressor.read(point);
        }
      }
      action.accept(point);
      chunkReadCount++;
    }
  }

  private void forEachPointLazPointwise(final ArithmeticDecoder decoder,
    final LazDecompress[] pointDecompressors, final Consumer<? super LasPoint> action) {
    {
      final LasPoint point = this.pointFormat.readLasPoint(this, this.reader);
      for (final LazDecompress pointDecompressor : pointDecompressors) {
        pointDecompressor.init(point);
      }
      decoder.reset();

      action.accept(point);
    }
    for (int i = 1; i < this.pointCount; i++) {
      final LasPoint point = this.pointFormat.newLasPoint(this);
      for (final LazDecompress pointDecompressor : pointDecompressors) {
        pointDecompressor.read(point);
      }
      action.accept(point);
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

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  protected LasVariableLengthRecord getLasProperty(final Pair<String, Integer> key) {
    return this.lasProperties.get(key);
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

  public String getSystemIdentifier() {
    return this.systemIdentifier;
  }

  public Version getVersion() {
    return this.version;
  }

  public int getYear() {
    return this.year;
  }

  public LazDecompress[] newLazDecompressors(final ArithmeticDecoder decoder) {
    final int numItems = this.lasZipHeader.getNumItems();
    final LazDecompress[] pointDecompressors = new LazDecompress[numItems];
    for (int i = 0; i < numItems; i++) {
      final LazItemType type = this.lasZipHeader.getType(i);
      final int version = this.lasZipHeader.getVersion(i);
      if (version < 1 || version > 2) {
        throw new RuntimeException(version + " not yet supported");
      }
      switch (type) {
        case POINT10:
          if (version == 1) {
            pointDecompressors[i] = new LazDecompressPoint10V1(this, decoder);
          } else {
            pointDecompressors[i] = new LazDecompressPoint10V2(this, decoder);
          }
        break;
        case GPSTIME11:
          if (version == 1) {
            pointDecompressors[i] = new LazDecompressGpsTime11V1(decoder);
          } else {
            pointDecompressors[i] = new LazDecompressGpsTime11V2(decoder);
          }
        break;
        case RGB12:
          if (version == 1) {
            pointDecompressors[i] = new LazDecompressRgb12V1(decoder);
          } else {
            pointDecompressors[i] = new LazDecompressRgb12V2(decoder);
          }
        break;

        default:
          throw new RuntimeException(type + " not yet supported");
      }
    }
    return pointDecompressors;
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
    if (this.reader != null) {
      this.points = new ArrayList<>((int)this.pointCount);
      forEachPoint(this.points::add);
    }
  }

  @SuppressWarnings("unchecked")
  public <P extends Point> int read(final Predicate<P> filter) {
    if (this.reader != null) {
      this.points = new ArrayList<>((int)this.pointCount);
      forEachPoint((point) -> {
        if (filter.test((P)point)) {
          this.points.add(point);
        }
      });
    }
    return this.points.size();
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

        this.version = new Version(this.reader);
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
        }
        this.pointFormat = LasPointFormat.getById(pointFormatId);
        this.recordLength = this.reader.getUnsignedShort();
        this.pointCount = (int)this.reader.getUnsignedInt();
        for (int i = 0; i < 5; i++) {
          this.pointCountByReturn[i] = this.reader.getUnsignedInt();
        }
        final double scaleX = 1 / this.reader.getDouble();
        final double scaleY = 1 / this.reader.getDouble();
        final double scaleZ = 1 / this.reader.getDouble();
        final double offsetX = this.reader.getDouble();
        final double offsetY = this.reader.getDouble();
        final double offsetZ = this.reader.getDouble();

        final CoordinateSystem coordinateSystem = this.geometryFactory.getCoordinateSystem();
        this.geometryFactory = GeometryFactory.newWithOffsets(coordinateSystem, offsetX, scaleX,
          offsetY, scaleY, offsetZ, scaleZ);

        final double maxX = this.reader.getDouble();
        final double minX = this.reader.getDouble();
        final double maxY = this.reader.getDouble();
        final double minY = this.reader.getDouble();
        final double maxZ = this.reader.getDouble();
        final double minZ = this.reader.getDouble();

        if (this.headerSize > 227) {

          if (this.version.atLeast(LasPointCloud.VERSION_1_3)) {
            final long startOfWaveformDataPacketRecord = this.reader.getUnsignedLong(); // TODO
            // unsigned
            // long
            // long support
            // needed
            if (this.version.atLeast(LasPointCloud.VERSION_1_4)) {
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
        if (this.version.equals(LasPointCloud.VERSION_1_0)) {
          this.reader.skipBytes(2);
          this.headerSize += 2;
        }
        final int skipCount = (int)(this.pointRecordsOffset - this.headerSize);
        this.reader.skipBytes(skipCount); // Skip to first point record

        this.recordDefinition = this.pointFormat.newRecordDefinition(this.geometryFactory);

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
      final int reserved = this.reader.getUnsignedShort(); // Ignore reserved
                                                           // value;
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

  public void setCoordinateSystemInternal(final CoordinateSystem coordinateSystem) {
    this.geometryFactory = this.geometryFactory.convertCoordinateSystem(coordinateSystem);
  }

  protected void setGeometryFactory(final GeometryFactory geometryFactory) {
    final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
    if (coordinateSystem == null) {
      throw new IllegalArgumentException("A valid  coordinate system must be specified");
    } else {
      double scaleX = geometryFactory.getScaleX();
      if (scaleX == 0) {
        scaleX = 1000;
      }
      double scaleY = geometryFactory.getScaleY();
      if (scaleY == 0) {
        scaleY = 1000;
      }
      double scaleZ = geometryFactory.getScaleZ();
      if (scaleZ == 0) {
        scaleZ = 1000;
      }
      final double offsetX = geometryFactory.getOffsetX();
      final double offsetY = geometryFactory.getOffsetY();
      final double offsetZ = geometryFactory.getOffsetZ();
      this.geometryFactory = GeometryFactory.newWithOffsets(coordinateSystem, offsetX, scaleX,
        offsetY, scaleY, offsetZ, scaleZ);

      LasProjection.setCoordinateSystem(this, coordinateSystem);
    }
  }

  public void setLasZipHeader(final LasZipHeader lasZipHeader) {
    this.lasZipHeader = lasZipHeader;
  }

  @Override
  public double toDoubleX(final int x) {
    return this.geometryFactory.toDoubleX(x);
  }

  @Override
  public double toDoubleY(final int y) {
    return this.geometryFactory.toDoubleY(y);
  }

  @Override
  public double toDoubleZ(final int z) {
    return this.geometryFactory.toDoubleZ(z);
  }

  @Override
  public int toIntX(final double x) {
    return this.geometryFactory.toIntX(x);
  }

  @Override
  public int toIntY(final double y) {
    return this.geometryFactory.toIntY(y);
  }

  @Override
  public int toIntZ(final double z) {
    return this.geometryFactory.toIntZ(z);
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
      if (this.version.atLeast(LasPointCloud.VERSION_1_3)) {
        headerSize += 8;
        if (this.version.atLeast(LasPointCloud.VERSION_1_4)) {
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
      for (int axisIndex = 0; axisIndex < 3; axisIndex++) {
        final double resolution = this.geometryFactory.getResolution(axisIndex);
        out.writeLEDouble(resolution);
      }
      for (int axisIndex = 0; axisIndex < 3; axisIndex++) {
        final double offset = this.geometryFactory.getOffset(axisIndex);
        out.writeLEDouble(offset);
      }

      for (int axisIndex = 0; axisIndex < 3; axisIndex++) {
        final double max = this.bounds[3 + axisIndex];
        out.writeLEDouble(max);
        final double min = this.bounds[axisIndex];
        out.writeLEDouble(min);
      }

      if (this.version.atLeast(LasPointCloud.VERSION_1_3)) {
        out.writeLEUnsignedLong(0); // startOfWaveformDataPacketRecord
        if (this.version.atLeast(LasPointCloud.VERSION_1_4)) {
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
        point.write(out);
      }
    }
  }
}
