package com.revolsys.elevation.cloud.las;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.impl.PointDoubleXYZ;
import com.revolsys.io.Buffers;
import com.revolsys.io.endian.EndianOutput;
import com.revolsys.record.Record;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.Exceptions;

public class LasPoint0Core extends PointDoubleXYZ implements Record {

  private static final long serialVersionUID = 1L;

  public static LasPoint0Core newLasPoint(final LasPointCloud pointCloud,
    final RecordDefinition recordDefinition, final ByteBuffer buffer) {
    try {
      return new LasPoint0Core(pointCloud, recordDefinition, buffer);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  private int intensity;

  private byte returnNumber;

  private byte numberOfReturns;

  private boolean scanDirectionFlag;

  private boolean edgeOfFlightLine;

  private boolean synthetic;

  private boolean keyPoint;

  private boolean withheld;

  private boolean overlap;

  private byte classification;

  private short scanAngleRank;

  private short userData;

  private int pointSourceID;

  private byte scannerChannel;

  private final RecordDefinition recordDefinition;

  public LasPoint0Core(final LasPointCloud pointCloud, final double x, final double y,
    final double z) {
    this.recordDefinition = pointCloud.getRecordDefinition();
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public LasPoint0Core(final LasPointCloud pointCloud, final RecordDefinition recordDefinition,
    final ByteBuffer buffer) throws IOException {
    this.recordDefinition = recordDefinition;
    final int xRecord = buffer.getInt();
    final int yRecord = buffer.getInt();
    final int zRecord = buffer.getInt();
    this.x = pointCloud.getOffsetX() + xRecord * pointCloud.getResolutionX();
    this.y = pointCloud.getOffsetY() + yRecord * pointCloud.getResolutionY();
    this.z = pointCloud.getOffsetZ() + zRecord * pointCloud.getResolutionZ();

    read(pointCloud, buffer);
  }

  @Override
  public LasPoint0Core clone() {
    return (LasPoint0Core)super.clone();
  }

  @Override
  public int compareTo(final Object other) {
    return Record.super.compareTo(other);
  }

  @Override
  public double distance(final Geometry geometry) {
    return super.distance(geometry);
  }

  public byte getClassification() {
    return this.classification;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends Geometry> T getGeometry() {
    return (T)this;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.recordDefinition.getGeometryFactory();
  }

  public int getIntensity() {
    return this.intensity;
  }

  public byte getNumberOfReturns() {
    return this.numberOfReturns;
  }

  public int getPointSourceID() {
    return this.pointSourceID;
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  public byte getReturnNumber() {
    return this.returnNumber;
  }

  public short getScanAngleRank() {
    return this.scanAngleRank;
  }

  public byte getScannerChannel() {
    return this.scannerChannel;
  }

  public short getUserData() {
    return this.userData;
  }

  public boolean isEdgeOfFlightLine() {
    return this.edgeOfFlightLine;
  }

  public boolean isKeyPoint() {
    return this.keyPoint;
  }

  public boolean isOverlap() {
    return this.overlap;
  }

  public boolean isScanDirectionFlag() {
    return this.scanDirectionFlag;
  }

  public boolean isSynthetic() {
    return this.synthetic;
  }

  public boolean isWithheld() {
    return this.withheld;
  }

  protected void read(final LasPointCloud pointCloud, final ByteBuffer buffer) throws IOException {
    final int pointDataRecordFormat = pointCloud.getPointFormat().getId();
    this.intensity = Buffers.getLEUnsignedShort(buffer);
    if (pointDataRecordFormat < 6) {
      final byte returnBits = buffer.get();
      this.returnNumber = (byte)(returnBits & 0b111);
      this.numberOfReturns = (byte)(returnBits >> 3 & 0b111);
      this.scanDirectionFlag = (returnBits >> 6 & 0b1) == 1;
      this.edgeOfFlightLine = (returnBits >> 7 & 0b1) == 1;

      final byte classificationByte = buffer.get();
      this.classification = (byte)(classificationByte & 0b11111);
      this.synthetic = (classificationByte >> 5 & 0b1) == 1;
      this.keyPoint = (classificationByte >> 6 & 0b1) == 1;
      this.withheld = (classificationByte >> 7 & 0b1) == 1;
      this.scanAngleRank = buffer.get();
      this.userData = buffer.get();
      this.pointSourceID = Buffers.getLEUnsignedShort(buffer);
    } else {
      final byte returnBits = buffer.get();
      this.returnNumber = (byte)(returnBits & 0b1111);
      this.numberOfReturns = (byte)(returnBits >> 4 & 0b1111);

      final byte classificationByte = buffer.get();
      this.synthetic = (classificationByte & 0b1) == 1;
      this.keyPoint = (classificationByte >> 1 & 0b1) == 1;
      this.withheld = (classificationByte >> 2 & 0b1) == 1;
      this.overlap = (classificationByte >> 3 & 0b1) == 1;
      this.scannerChannel = (byte)(classificationByte >> 4 & 0b11);
      this.scanDirectionFlag = (classificationByte >> 6 & 0b1) == 1;
      this.edgeOfFlightLine = (classificationByte >> 7 & 0b1) == 1;
      this.userData = buffer.get();
      this.scanAngleRank = buffer.getShort();
      this.pointSourceID = Buffers.getLEUnsignedShort(buffer);
    }
  }

  @Override
  public String toString() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    final StringBuilder s = new StringBuilder();
    s.append(recordDefinition.getPath()).append("(\n");
    for (int i = 0; i < recordDefinition.getFieldCount(); i++) {
      Object value = getValue(i);
      if (value != null) {
        if (value == this) {
          value = toEwkt();
        }
        final String fieldName = recordDefinition.getFieldName(i);
        s.append(fieldName).append('=').append(value).append('\n');
      }
    }
    s.append(')');
    return s.toString();
  }

  protected void write(final LasPointCloud pointCloud, final EndianOutput out) {
    final int xRecord = (int)Math
      .round((this.x - pointCloud.getOffsetX()) / pointCloud.getResolutionX());
    final int yRecord = (int)Math
      .round((this.y - pointCloud.getOffsetY()) / pointCloud.getResolutionY());
    final int zRecord = (int)Math
      .round((this.z - pointCloud.getOffsetZ()) / pointCloud.getResolutionZ());

    out.writeLEInt(xRecord);
    out.writeLEInt(yRecord);
    out.writeLEInt(zRecord);

    final int pointDataRecordFormat = pointCloud.getPointFormat().getId();
    out.writeLEUnsignedShort(this.intensity);
    if (pointDataRecordFormat < 6) {
      int returnBits = this.returnNumber;
      returnBits |= this.numberOfReturns << 3;
      if (this.scanDirectionFlag) {
        returnBits |= 0b100000;
      }
      if (this.edgeOfFlightLine) {
        returnBits |= 0b1000000;
      }
      out.write(returnBits);
      int classificationByte = this.classification;
      if (this.synthetic) {
        classificationByte |= 0b10000;
      }
      if (this.keyPoint) {
        classificationByte |= 0b100000;
      }
      if (this.withheld) {
        classificationByte |= 0b1000000;
      }
      out.write(classificationByte);
      out.write((byte)this.scanAngleRank);
      out.write(this.userData);
      out.writeLEUnsignedShort(this.pointSourceID);
    } else {
      byte returnBits = this.returnNumber;
      returnBits |= this.numberOfReturns << 4;
      out.write(returnBits);

      byte classificationFlags = 0;
      if (this.synthetic) {
        classificationFlags |= 0b1;
      }
      if (this.keyPoint) {
        classificationFlags |= 0b10;
      }
      if (this.withheld) {
        classificationFlags |= 0b100;
      }
      if (this.overlap) {
        classificationFlags |= 0b1000;
      }
      this.scannerChannel = (byte)(classificationFlags >> 4 & 0b11);
      if (this.scanDirectionFlag) {
        classificationFlags |= 0b100000;
      }
      if (this.edgeOfFlightLine) {
        classificationFlags |= 0b1000000;
      }
      out.write(classificationFlags);
      out.write(this.classification);
      out.write(this.userData);
      out.writeLEShort(this.scanAngleRank);
      out.writeLEUnsignedShort(this.pointSourceID);
    }
  }
}
