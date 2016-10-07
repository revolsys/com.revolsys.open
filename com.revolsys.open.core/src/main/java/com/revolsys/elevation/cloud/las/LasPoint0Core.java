package com.revolsys.elevation.cloud.las;

import java.io.IOException;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.impl.PointDoubleXYZ;
import com.revolsys.io.endian.EndianInput;
import com.revolsys.record.Record;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.Exceptions;

public class LasPoint0Core extends PointDoubleXYZ implements Record {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private static double[] getCoordinates(final LasPointCloud pointCloud,
    final RecordDefinition recordDefinition, final EndianInput in) throws IOException {
    final int xRecord = in.readLEInt();
    final int yRecord = in.readLEInt();
    final int zRecord = in.readLEInt();
    final double x = pointCloud.getOffsetX() + xRecord * pointCloud.getScaleX();
    final double y = pointCloud.getOffsetY() + yRecord * pointCloud.getScaleY();
    final double z = pointCloud.getOffsetZ() + zRecord * pointCloud.getScaleZ();
    return new double[] {
      x, y, z
    };
  }

  public static LasPoint0Core newLasPoint(final LasPointCloud pointCloud,
    final RecordDefinition recordDefinition, final EndianInput in) {
    try {
      return new LasPoint0Core(pointCloud, recordDefinition, in);
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

  public LasPoint0Core(final LasPointCloud pointCloud, final RecordDefinition recordDefinition,
    final EndianInput in) throws IOException {
    super(recordDefinition.getGeometryFactory(), getCoordinates(pointCloud, recordDefinition, in));
    this.recordDefinition = recordDefinition;
    read(pointCloud, in);
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

  protected void read(final LasPointCloud pointCloud, final EndianInput in) throws IOException {
    final int pointDataRecordFormat = pointCloud.getPointDataRecordFormat();
    this.intensity = in.readLEUnsignedShort();
    if (pointDataRecordFormat < 6) {
      final byte returnBits = in.readByte();
      this.returnNumber = (byte)(returnBits & 0b111);
      this.numberOfReturns = (byte)(returnBits >> 3 & 0b111);
      this.scanDirectionFlag = (returnBits >> 6 & 0b1) == 1;
      this.edgeOfFlightLine = (returnBits >> 7 & 0b1) == 1;

      final byte classificationByte = in.readByte();
      this.classification = (byte)(classificationByte & 0b11111);
      this.synthetic = (classificationByte >> 5 & 0b1) == 1;
      this.keyPoint = (classificationByte >> 6 & 0b1) == 1;
      this.withheld = (classificationByte >> 7 & 0b1) == 1;
      this.scanAngleRank = in.readByte();
      this.userData = in.readByte();
      this.pointSourceID = in.readLEUnsignedShort();
    } else {
      final byte returnBits = in.readByte();
      this.returnNumber = (byte)(returnBits & 0b1111);
      this.numberOfReturns = (byte)(returnBits >> 4 & 0b1111);

      final byte classificationByte = in.readByte();
      this.synthetic = (classificationByte & 0b1) == 1;
      this.keyPoint = (classificationByte >> 1 & 0b1) == 1;
      this.withheld = (classificationByte >> 2 & 0b1) == 1;
      this.overlap = (classificationByte >> 3 & 0b1) == 1;
      this.scannerChannel = (byte)(classificationByte >> 4 & 0b11);
      this.scanDirectionFlag = (classificationByte >> 6 & 0b1) == 1;
      this.edgeOfFlightLine = (classificationByte >> 7 & 0b1) == 1;
      this.userData = in.readByte();
      this.scanAngleRank = in.readLEShort();
      this.pointSourceID = in.readLEUnsignedShort();
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
}
