package com.revolsys.elevation.cloud.las;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.cloud.las.pointformat.LasPoint;
import com.revolsys.elevation.cloud.las.pointformat.LasPointFormat;
import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.BoundingBoxProxy;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.GeometryFactoryProxy;
import com.revolsys.geometry.util.RectangleUtil;
import com.revolsys.io.channels.ChannelReader;
import com.revolsys.io.endian.EndianOutputStream;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Exceptions;
import com.revolsys.util.Pair;

public class LasPointCloudHeader implements BoundingBoxProxy, GeometryFactoryProxy, MapSerializer {

  private static final Map<Pair<String, Integer>, BiFunction<LasPointCloudHeader, byte[], Object>> PROPERTY_FACTORY_BY_KEY = new HashMap<>();

  static {
    LasProjection.init(PROPERTY_FACTORY_BY_KEY);
    LasZipHeader.init(PROPERTY_FACTORY_BY_KEY);
  }

  public static final Version VERSION_1_0 = new Version(1, 0);

  public static final Version VERSION_1_1 = new Version(1, 1);

  public static final Version VERSION_1_2 = new Version(1, 2);

  public static final Version VERSION_1_3 = new Version(1, 3);

  public static final Version VERSION_1_4 = new Version(1, 4);

  private static final long MAX_UNSIGNED_INT = 1l << 32;

  private final double[] bounds;

  private int dayOfYear = new GregorianCalendar().get(Calendar.DAY_OF_YEAR);

  private int fileSourceId;

  private String generatingSoftware = "RevolutionGIS";

  private GeometryFactory geometryFactory = GeometryFactory.fixed3d(1000.0, 1000.0, 1000.0);

  private int globalEncoding = 0;

  private final Map<Pair<String, Integer>, LasVariableLengthRecord> lasProperties = new LinkedHashMap<>();

  private Version version = LasPointCloudHeader.VERSION_1_2;

  private long pointCount = 0;

  private final long[] pointCountByReturn = {
    0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l
  };

  private LasPointFormat pointFormat = LasPointFormat.Core;

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

  private LasZipHeader lasZipHeader;

  @SuppressWarnings("unused")
  public LasPointCloudHeader(final ChannelReader reader, final GeometryFactory geometryFactory) {
    setGeometryFactory(geometryFactory);
    try {
      if (reader.getUsAsciiString(4).equals("LASF")) {
        this.fileSourceId = reader.getUnsignedShort();
        this.globalEncoding = reader.getUnsignedShort();

        // final long guid1 = Buffers.getLEUnsignedInt(header);
        // final int guid2 = Buffers.getLEUnsignedShort(header);
        // final int guid3 = Buffers.getLEUnsignedShort(header);
        // final byte[] guid4 = header.getBytes(8);
        reader.getBytes(this.projectId);

        this.version = new Version(reader);
        this.systemIdentifier = reader.getUsAsciiString(32);
        this.generatingSoftware = reader.getUsAsciiString(32);
        this.dayOfYear = reader.getUnsignedShort();
        this.year = reader.getUnsignedShort();
        final Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.YEAR, this.year);
        calendar.set(Calendar.DAY_OF_YEAR, this.dayOfYear);
        this.date = new Date(calendar.getTimeInMillis());
        this.headerSize = reader.getUnsignedShort();
        this.pointRecordsOffset = reader.getUnsignedInt();
        final long numberOfVariableLengthRecords = reader.getUnsignedInt();
        int pointFormatId = reader.getUnsignedByte();
        if (pointFormatId > 127) {
          pointFormatId -= 128;
          this.laszip = true;
        }
        this.pointFormat = LasPointFormat.getById(pointFormatId);
        this.recordLength = reader.getUnsignedShort();
        this.pointCount = (int)reader.getUnsignedInt();
        for (int i = 0; i < 5; i++) {
          this.pointCountByReturn[i] = reader.getUnsignedInt();
        }
        final double scaleX = 1 / reader.getDouble();
        final double scaleY = 1 / reader.getDouble();
        final double scaleZ = 1 / reader.getDouble();
        final double offsetX = reader.getDouble();
        final double offsetY = reader.getDouble();
        final double offsetZ = reader.getDouble();

        final CoordinateSystem coordinateSystem = this.geometryFactory.getHorizontalCoordinateSystem();
        this.geometryFactory = GeometryFactory.newWithOffsets(coordinateSystem, offsetX, scaleX,
          offsetY, scaleY, offsetZ, scaleZ);

        final double maxX = reader.getDouble();
        final double minX = reader.getDouble();
        final double maxY = reader.getDouble();
        final double minY = reader.getDouble();
        final double maxZ = reader.getDouble();
        final double minZ = reader.getDouble();

        if (this.headerSize > 227) {

          if (this.version.atLeast(LasPointCloudHeader.VERSION_1_3)) {
            final long startOfWaveformDataPacketRecord = reader.getUnsignedLong(); // TODO
            // unsigned
            // long
            // long support
            // needed
            if (this.version.atLeast(LasPointCloudHeader.VERSION_1_4)) {
              final long startOfFirstExetendedDataRecord = reader.getUnsignedLong();
              final long numberOfExtendedVariableLengthRecords = reader.getUnsignedInt();
              this.pointCount = reader.getUnsignedLong();
              for (int i = 0; i < 15; i++) {
                this.pointCountByReturn[i] = reader.getUnsignedLong();
              }
            }
          }
        }
        this.headerSize += readVariableLengthRecords(reader, numberOfVariableLengthRecords);
        this.bounds = new double[] {
          minX, minY, minZ, maxX, maxY, maxZ
        };
        if (this.version.equals(LasPointCloudHeader.VERSION_1_0)) {
          reader.skipBytes(2);
          this.headerSize += 2;
        }
        final int skipCount = (int)(this.pointRecordsOffset - this.headerSize);
        reader.skipBytes(skipCount); // Skip to first point record

        this.recordDefinition = this.pointFormat.newRecordDefinition(this.geometryFactory);

      } else {
        throw new IllegalArgumentException(this.resource + " is not a valid LAS file");
      }
    } catch (final IOException e) {
      throw Exceptions.wrap("Error reading " + this.resource, e);
    }
  }

  public LasPointCloudHeader(final LasPointFormat pointFormat,
    final GeometryFactory geometryFactory) {
    this.pointFormat = pointFormat;
    if (this.pointFormat.getId() > 5) {
      this.globalEncoding |= 0b10000;
    }
    this.recordLength = pointFormat.getRecordLength();
    setGeometryFactory(geometryFactory);
    this.bounds = RectangleUtil.newBounds(3);
  }

  protected void addProperty(final LasVariableLengthRecord property) {
    final Pair<String, Integer> key = property.getKey();
    this.lasProperties.put(key, property);
  }

  protected void clear() {
    this.pointCount = 0;
    for (int i = 0; i < this.pointCountByReturn.length; i++) {
      this.pointCountByReturn[i] = 0;
    }
    Arrays.fill(this.bounds, Double.NaN);
  }

  @Override
  public BoundingBox getBoundingBox() {
    return this.geometryFactory.newBoundingBox(3, this.bounds);
  }

  public Date getDate() {
    return this.date;
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

  public int getGlobalEncoding() {
    return this.globalEncoding;
  }

  protected LasVariableLengthRecord getLasProperty(final Pair<String, Integer> key) {
    return this.lasProperties.get(key);
  }

  public LasZipHeader getLasZipHeader() {
    return this.lasZipHeader;
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

  public boolean isLaszip() {
    return this.laszip;
  }

  public LasPoint newLasPoint(final LasPointCloud lasPointCloud, final double x, final double y,
    final double z) {
    this.pointCount++;
    this.pointCountByReturn[0]++;
    RectangleUtil.expand(this.bounds, 3, x, y, z);
    return this.pointFormat.newLasPoint(lasPointCloud, x, y, z);
  }

  private int readVariableLengthRecords(final ChannelReader reader,
    final long numberOfVariableLengthRecords) throws IOException {
    int byteCount = 0;
    for (int i = 0; i < numberOfVariableLengthRecords; i++) {
      @SuppressWarnings("unused")
      final int reserved = reader.getUnsignedShort(); // Ignore reserved
                                                      // value;
      final String userId = reader.getUsAsciiString(16);
      final int recordId = reader.getUnsignedShort();
      final int valueLength = reader.getUnsignedShort();
      final String description = reader.getUsAsciiString(32);
      final byte[] bytes = reader.getBytes(valueLength);
      final LasVariableLengthRecord property = new LasVariableLengthRecord(userId, recordId,
        description, bytes);
      addProperty(property);
      byteCount += 54 + valueLength;
    }
    for (final Entry<Pair<String, Integer>, LasVariableLengthRecord> entry : this.lasProperties
      .entrySet()) {
      final Pair<String, Integer> key = entry.getKey();
      final LasVariableLengthRecord property = entry.getValue();
      final BiFunction<LasPointCloudHeader, byte[], Object> converter = PROPERTY_FACTORY_BY_KEY
        .get(key);
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
    if (geometryFactory != null) {
      final CoordinateSystem coordinateSystem = geometryFactory.getHorizontalCoordinateSystem();
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
    if (this.geometryFactory != null) {
      final int coordinateSystemId = this.geometryFactory.getCoordinateSystemId();
      if (coordinateSystemId > 0) {
        addToMap(map, "coordinateSystemId", coordinateSystemId);
      }

      final CoordinateSystem coordinateSystem = this.geometryFactory.getHorizontalCoordinateSystem();
      if (coordinateSystem != null) {
        addToMap(map, "coordinateSystemName", coordinateSystem.getCoordinateSystemName());
        addToMap(map, "coordinateSystem", coordinateSystem.toEsriWktCs());
      }
    }
    addToMap(map, "boundingBox", getBoundingBox());
    addToMap(map, "headerSize", this.headerSize);
    if (this.laszip) {
      addToMap(map, "laszip", this.lasZipHeader);
    }
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

  public void writeHeader(final EndianOutputStream out) {
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
    if (this.version.atLeast(LasPointCloudHeader.VERSION_1_3)) {
      headerSize += 8;
      if (this.version.atLeast(LasPointCloudHeader.VERSION_1_4)) {
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

    if (this.version.atLeast(LasPointCloudHeader.VERSION_1_3)) {
      out.writeLEUnsignedLong(0); // startOfWaveformDataPacketRecord
      if (this.version.atLeast(LasPointCloudHeader.VERSION_1_4)) {
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
  }
}
