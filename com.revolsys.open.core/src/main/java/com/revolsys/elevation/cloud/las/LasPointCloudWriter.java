package com.revolsys.elevation.cloud.las;

import java.nio.ByteOrder;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.revolsys.elevation.cloud.PointCloud;
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

  private ChannelWriter out;

  private LasPointCloud pointCloud;

  public LasPointCloudWriter(final Resource resource) {
    this.resource = resource;
  }

  @Override
  public void close() {
    this.pointCloud = null;
    final ChannelWriter out = this.out;
    if (out != null) {
      this.out = null;
      out.close();
    }
  }

  protected Map<Pair<String, Integer>, LasVariableLengthRecord> getLasProperties(
    final LasPointCloudHeader header) {
    return header.getLasProperties();
  }

  protected void setPointCloud(final LasPointCloud pointCloud) {
    this.pointCloud = pointCloud;
  }

  protected void writeHeader(final LasPointCloudHeader header) {
    this.out.putString("LASF", 4);
    this.out.putUnsignedShort(header.getFileSourceId());
    this.out.putUnsignedShort(header.getGlobalEncoding());
    final UUID projectId = header.getProjectId();
    final long uuidLeast = projectId.getLeastSignificantBits();
    final long uuidMost = projectId.getMostSignificantBits();
    this.out.putLong(uuidLeast);
    this.out.putLong(uuidMost);

    final Version version = header.getVersion();
    this.out.putByte((byte)version.getMajor());
    this.out.putByte((byte)version.getMinor());
    this.out.putString(header.getSystemIdentifier(), 32);
    this.out.putString(header.getGeneratingSoftware(), 32);

    this.out.putUnsignedShort(header.getDayOfYear());
    this.out.putUnsignedShort(header.getYear());

    int headerSize = 227;
    if (version.atLeast(LasVersion.VERSION_1_3)) {
      headerSize += 8;
      if (version.atLeast(LasVersion.VERSION_1_4)) {
        headerSize += 140;
      }
    }
    this.out.putUnsignedShort(headerSize);

    final Map<Pair<String, Integer>, LasVariableLengthRecord> lasProperties = getLasProperties(
      header);
    final int numberOfVariableLengthRecords = lasProperties.size();
    int variableLengthRecordsSize = 0;
    for (final LasVariableLengthRecord record : lasProperties.values()) {
      variableLengthRecordsSize += 54 + record.getValueLength();
    }

    final long offsetToPointData = headerSize + variableLengthRecordsSize;
    this.out.putUnsignedInt(offsetToPointData);

    this.out.putUnsignedInt(numberOfVariableLengthRecords);

    final int pointFormatId = header.getPointFormatId();
    this.out.putUnsignedByte((short)pointFormatId);

    final int recordLength = header.getRecordLength();
    this.out.putUnsignedShort(recordLength);
    final long pointCount = header.getPointCount();
    if (pointCount > MAX_UNSIGNED_INT) {
      this.out.putUnsignedInt(0);
    } else {
      this.out.putUnsignedInt(pointCount);
    }
    final long[] pointCountByReturn = header.getPointCountByReturn();
    for (int i = 0; i < 5; i++) {
      final long count = pointCountByReturn[i];
      if (count > MAX_UNSIGNED_INT) {
        this.out.putUnsignedInt(0);
      } else {
        this.out.putUnsignedInt(count);
      }
    }
    final GeometryFactory geometryFactory = header.getGeometryFactory();
    for (int axisIndex = 0; axisIndex < 3; axisIndex++) {
      final double resolution = geometryFactory.getResolution(axisIndex);
      this.out.putDouble(resolution);
    }
    for (int axisIndex = 0; axisIndex < 3; axisIndex++) {
      final double offset = geometryFactory.getOffset(axisIndex);
      this.out.putDouble(offset);
    }
    final double[] bounds = header.getBounds();
    for (int axisIndex = 0; axisIndex < 3; axisIndex++) {
      final double max = bounds[3 + axisIndex];
      this.out.putDouble(max);
      final double min = bounds[axisIndex];
      this.out.putDouble(min);
    }

    if (version.atLeast(LasVersion.VERSION_1_3)) {
      this.out.putUnsignedLong(0); // startOfWaveformDataPacketRecord
      if (version.atLeast(LasVersion.VERSION_1_4)) {
        this.out.putUnsignedLong(0); // startOfFirstExetendedDataRecord
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

  public boolean writePointCloud(final PointCloud<?> pointCloud) {
    if (pointCloud instanceof LasPointCloud) {
      final LasPointCloud lasPointCloud = (LasPointCloud)pointCloud;
      setPointCloud(lasPointCloud);
      this.out = this.resource.newChannelWriter(8192, ByteOrder.LITTLE_ENDIAN);

      final LasPointCloudHeader header = lasPointCloud.getHeader();
      writeHeader(header);
      final List<LasPoint> points = lasPointCloud.getPoints();
      writePoints(this.out, points);
      return true;
    } else {
      return false;
    }
  }

  protected void writePoints(final ChannelWriter out, final List<LasPoint> points) {
    for (final LasPoint point : points) {
      point.writeLasPoint(out);
    }
  }
}
