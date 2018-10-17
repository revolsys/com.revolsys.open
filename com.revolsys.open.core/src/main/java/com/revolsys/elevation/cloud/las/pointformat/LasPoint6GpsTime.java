package com.revolsys.elevation.cloud.las.pointformat;

import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.cloud.las.LasPointCloud;
import com.revolsys.io.channels.ChannelReader;
import com.revolsys.io.endian.EndianOutput;

public class LasPoint6GpsTime extends BaseLasPoint implements LasPointExtended {
  private static final long serialVersionUID = 1L;

  public static double getCurrentGpsTime() {
    return System.currentTimeMillis() / 1000.0 - 315964800;
  }

  private int intensity;

  private boolean scanDirectionFlag;

  private boolean edgeOfFlightLine;

  private boolean synthetic;

  private boolean keyPoint;

  private boolean withheld;

  private boolean overlap;

  private short classification;

  private short scanAngle;

  private short userData;

  private int pointSourceID;

  private byte scannerChannel;

  private double gpsTime;

  private byte returnByte;

  public LasPoint6GpsTime(final LasPointCloud pointCloud) {
    super(pointCloud);
    this.gpsTime = getCurrentGpsTime();
  }

  @Override
  public LasPoint6GpsTime clone() {
    return (LasPoint6GpsTime)super.clone();
  }

  @Override
  public short getClassification() {
    return this.classification;
  }

  @Override
  public byte getClassificationByte() {
    throw new RuntimeException("Not implemented");
  }

  @Override
  public double getGpsTime() {
    return this.gpsTime;
  }

  @Override
  public int getIntensity() {
    return this.intensity;
  }

  @Override
  public byte getNumberOfReturns() {
    return (byte)(this.returnByte >> 4 & 0b1111);
  }

  @Override
  public LasPointFormat getPointFormat() {
    return LasPointFormat.ExtendedGpsTime;
  }

  @Override
  public int getPointSourceID() {
    return this.pointSourceID;
  }

  @Override
  public byte getReturnByte() {
    throw new RuntimeException("Not implemented");
  }

  @Override
  public byte getReturnNumber() {
    return (byte)(this.returnByte & 0b1111);
  }

  public short getScanAngle() {
    return this.scanAngle;
  }

  @Override
  public double getScanAngleDegrees() {
    return this.scanAngle * 0.006;
  }

  @Override
  public byte getScanAngleRank() {
    throw new UnsupportedOperationException();
  }

  @Override
  public byte getScannerChannel() {
    return this.scannerChannel;
  }

  @Override
  public short getUserData() {
    return this.userData;
  }

  @Override
  public boolean isEdgeOfFlightLine() {
    return this.edgeOfFlightLine;
  }

  @Override
  public boolean isKeyPoint() {
    return this.keyPoint;
  }

  @Override
  public boolean isOverlap() {
    return this.overlap;
  }

  @Override
  public boolean isScanDirectionFlag() {
    return this.scanDirectionFlag;
  }

  @Override
  public boolean isSynthetic() {
    return this.synthetic;
  }

  @Override
  public boolean isWithheld() {
    return this.withheld;
  }

  @Override
  public void read(final LasPointCloud pointCloud, final ChannelReader reader) {
    final int xRecord = reader.getInt();
    final int yRecord = reader.getInt();
    final int zRecord = reader.getInt();
    setXYZ(xRecord, yRecord, zRecord);
    this.intensity = reader.getUnsignedShort();
    this.returnByte = reader.getByte();

    final byte classificationByte = reader.getByte();
    this.classification = reader.getUnsignedByte();
    this.userData = reader.getUnsignedByte();
    this.scanAngle = reader.getShort();
    this.pointSourceID = reader.getUnsignedShort();
    this.gpsTime = reader.getDouble();

    this.synthetic = (classificationByte & 0b1) == 1;
    this.keyPoint = (classificationByte >> 1 & 0b1) == 1;
    this.withheld = (classificationByte >> 2 & 0b1) == 1;
    this.overlap = (classificationByte >> 3 & 0b1) == 1;
    this.scannerChannel = (byte)(classificationByte >> 4 & 0b11);
    this.scanDirectionFlag = (classificationByte >> 6 & 0b1) == 1;
    this.edgeOfFlightLine = (classificationByte >> 7 & 0b1) == 1;

  }

  @Override
  public void setClassification(final byte classification) {
    this.classification = classification;
  }

  @Override
  public void setClassificationByte(final byte classificationByte) {
    throw new RuntimeException("Not implemented");
  }

  @Override
  public void setEdgeOfFlightLine(final boolean edgeOfFlightLine) {
    this.edgeOfFlightLine = edgeOfFlightLine;
  }

  @Override
  public void setGpsTime(final double gpsTime) {
    this.gpsTime = gpsTime;
  }

  @Override
  public void setIntensity(final int intensity) {
    this.intensity = intensity;
  }

  @Override
  public void setKeyPoint(final boolean keyPoint) {
    this.keyPoint = keyPoint;
  }

  @Override
  public void setNumberOfReturns(final byte numberOfReturns) {
    this.returnByte &= numberOfReturns | 0b11110000;
  }

  @Override
  public void setOverlap(final boolean overlap) {
    this.overlap = overlap;
  }

  @Override
  public void setPointSourceID(final int pointSourceID) {
    this.pointSourceID = pointSourceID;
  }

  @Override
  public void setReturnByte(final byte returnByte) {
    throw new RuntimeException("Not implemented");
  }

  @Override
  public void setReturnNumber(final byte returnNumber) {
    this.returnByte &= returnNumber << 4 | 0b00001111;
  }

  public void setScanAngle(final short scanAngle) {
    this.scanAngle = scanAngle;
  }

  @Override
  public void setScanAngleRank(final byte scanAngleRank) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setScanDirectionFlag(final boolean scanDirectionFlag) {
    this.scanDirectionFlag = scanDirectionFlag;
  }

  @Override
  public void setScannerChannel(final byte scannerChannel) {
    this.scannerChannel = scannerChannel;
  }

  @Override
  public void setSynthetic(final boolean synthetic) {
    this.synthetic = synthetic;
  }

  @Override
  public void setUserData(final short userData) {
    this.userData = userData;
  }

  @Override
  public void setWithheld(final boolean withheld) {
    this.withheld = withheld;
  }

  @Override
  public MapEx toMap() {
    final MapEx map = super.toMap();
    addToMap(map, "intensity", this.intensity);
    addToMap(map, "returnNumber", getReturnNumber());
    addToMap(map, "numberOfReturns", getNumberOfReturns());
    addToMap(map, "scanDirectionFlag", this.scanDirectionFlag);
    addToMap(map, "edgeOfFlightLine", this.edgeOfFlightLine);
    addToMap(map, "classification", this.classification);
    addToMap(map, "synthetic", this.synthetic);
    addToMap(map, "keyPoint", this.keyPoint);
    addToMap(map, "withheld", this.withheld);
    addToMap(map, "scanAngle", getScanAngleDegrees());
    addToMap(map, "userData", this.userData);
    addToMap(map, "pointSourceID", this.pointSourceID);
    addToMap(map, "overlap", this.overlap);
    addToMap(map, "scannerChannel", this.scannerChannel);
    addToMap(map, "gpsTime", this.gpsTime);
    return map;
  }

  @Override
  public void write(final EndianOutput out) {
    final int xRecord = getXInt();
    final int yRecord = getYInt();
    final int zRecord = getZInt();

    out.writeLEInt(xRecord);
    out.writeLEInt(yRecord);
    out.writeLEInt(zRecord);

    out.writeLEUnsignedShort(this.intensity);
    out.write(this.returnByte);

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
    out.writeLEShort(this.scanAngle);
    out.writeLEUnsignedShort(this.pointSourceID);
    out.writeLEDouble(this.gpsTime);
  }

}
