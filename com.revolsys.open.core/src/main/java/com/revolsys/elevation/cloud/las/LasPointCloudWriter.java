package com.revolsys.elevation.cloud.las;

import java.nio.ByteOrder;
import java.util.Map;
import java.util.UUID;

import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.cloud.las.pointformat.LasPoint;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.BaseCloseable;
import com.revolsys.io.channels.ChannelWriter;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Pair;

public class LasPointCloudWriter extends BaseObjectWithProperties implements BaseCloseable {

  private static final long MAX_UNSIGNED_INT = 1l << 32;

  private final Resource resource;

  protected ChannelWriter out;

  private LasPointCloud pointCloud;

  private Version version = LasVersion.VERSION_1_2;

  protected LasPointCloudHeader header;

  public LasPointCloudWriter(final LasPointCloud pointCloud, final Resource resource,
    final MapEx properties) {
    this(resource);
    setProperties(properties);
    setPointCloud(pointCloud);
  }

  public LasPointCloudWriter(final Resource resource) {
    this.resource = resource;
  }

  @Override
  public void close() {
    final ChannelWriter out = this.out;
    if (out != null) {
      out.flush();
      if (out.isSeekable()) {
        out.seek(0);
        writeHeader();
      }
      this.out = null;
      out.close();
    }
    this.pointCloud = null;
  }

  protected Map<Pair<String, Integer>, LasVariableLengthRecord> getLasProperties(
    final LasPointCloudHeader header) {
    return header.getLasProperties();
  }

  public Version getVersion() {
    return this.version;
  }

  protected void open() {
    this.out = this.resource.newChannelWriter(8192, ByteOrder.LITTLE_ENDIAN);
    writeHeader();
  }

  protected void setPointCloud(final LasPointCloud pointCloud) {
    this.pointCloud = pointCloud;
    this.header = this.pointCloud.getHeader().clone();
    this.header.setVersion(this.version);
    this.header.clear();
  }

  public void setVersion(final Version version) {
    this.version = version;
  }

  protected void writeHeader() {
    this.out.putString("LASF", 4);
    this.out.putUnsignedShort(this.header.getFileSourceId());
    this.out.putUnsignedShort(this.header.getGlobalEncoding());
    final UUID projectId = this.header.getProjectId();
    final long uuidLeast = projectId.getLeastSignificantBits();
    final long uuidMost = projectId.getMostSignificantBits();
    this.out.putLong(uuidLeast);
    this.out.putLong(uuidMost);

    final Version version = this.header.getVersion();
    this.out.putByte((byte)version.getMajor());
    this.out.putByte((byte)version.getMinor());
    this.out.putString(this.header.getSystemIdentifier(), 32);
    this.out.putString(this.header.getGeneratingSoftware(), 32);

    this.out.putUnsignedShort(this.header.getDayOfYear());
    this.out.putUnsignedShort(this.header.getYear());

    int headerSize = 227;
    if (version.atLeast(LasVersion.VERSION_1_3)) {
      headerSize += 8;
      if (version.atLeast(LasVersion.VERSION_1_4)) {
        headerSize += 140;
      }
    }
    this.out.putUnsignedShort(headerSize);

    final Map<Pair<String, Integer>, LasVariableLengthRecord> lasProperties = getLasProperties(
      this.header);
    final int numberOfVariableLengthRecords = lasProperties.size();
    int variableLengthRecordsSize = 0;
    for (final LasVariableLengthRecord record : lasProperties.values()) {
      variableLengthRecordsSize += 54 + record.getValueLength();
    }

    final long offsetToPointData = headerSize + variableLengthRecordsSize;
    this.out.putUnsignedInt(offsetToPointData);

    this.out.putUnsignedInt(numberOfVariableLengthRecords);

    final int pointFormatId = this.header.getPointFormatId();
    this.out.putUnsignedByte((short)pointFormatId);

    final int recordLength = this.header.getRecordLength();
    this.out.putUnsignedShort(recordLength);
    final long pointCount = this.header.getPointCount();
    if (pointCount > MAX_UNSIGNED_INT || pointFormatId >= 6) {
      this.out.putUnsignedInt(0);
    } else {
      this.out.putUnsignedInt(pointCount);
    }
    final long[] pointCountByReturn = this.header.getPointCountByReturn();
    for (int i = 0; i < 5; i++) {
      final long count = pointCountByReturn[i];
      if (count > MAX_UNSIGNED_INT || pointFormatId >= 6) {
        this.out.putUnsignedInt(0);
      } else {
        this.out.putUnsignedInt(count);
      }
    }
    final GeometryFactory geometryFactory = this.header.getGeometryFactory();
    for (int axisIndex = 0; axisIndex < 3; axisIndex++) {
      final double resolution = geometryFactory.getResolution(axisIndex);
      this.out.putDouble(resolution);
    }
    for (int axisIndex = 0; axisIndex < 3; axisIndex++) {
      final double offset = geometryFactory.getOffset(axisIndex);
      this.out.putDouble(offset);
    }
    final double[] bounds = this.header.getBounds();
    for (int axisIndex = 0; axisIndex < 3; axisIndex++) {
      final double max = bounds[3 + axisIndex];
      this.out.putDouble(max);
      final double min = bounds[axisIndex];
      this.out.putDouble(min);
    }

    if (version.atLeast(LasVersion.VERSION_1_3)) {
      this.out.putUnsignedLong(0); // startOfWaveformDataPacketRecord
      if (version.atLeast(LasVersion.VERSION_1_4)) {
        this.out.putUnsignedLong(0); // startOfFirstExtendedDataRecord
        this.out.putUnsignedInt(0); // numberOfExtendedVariableLengthRecords
        this.out.putUnsignedLong(pointCount);
        for (int i = 0; i < 15; i++) {
          final long count = pointCountByReturn[i];
          this.out.putUnsignedLong(count);
        }
      }
    }

    for (final LasVariableLengthRecord record : lasProperties.values()) {
      this.out.putUnsignedShort(0);
      final String userId = record.getUserId();
      this.out.putString(userId, 16);

      final int recordId = record.getRecordId();
      this.out.putUnsignedShort(recordId);

      final int valueLength = record.getValueLength();
      this.out.putUnsignedShort(valueLength);

      final String description = record.getDescription();
      this.out.putString(description, 32);

      final byte[] bytes = record.getBytes();
      this.out.putBytes(bytes);
    }
  }

  public void writePoint(final LasPoint point) {
    this.header.addCounts(point);
    point.writeLasPoint(this.out);
  }

  public void writePoints(final Iterable<LasPoint> points) {
    for (final LasPoint point : points) {
      writePoint(point);
    }
  }
}
