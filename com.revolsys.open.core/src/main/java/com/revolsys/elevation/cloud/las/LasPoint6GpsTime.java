package com.revolsys.elevation.cloud.las;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.impl.PointDoubleXYZ;
import com.revolsys.io.channels.ChannelReader;
import com.revolsys.io.endian.EndianOutput;
import com.revolsys.record.io.format.json.Json;

public class LasPoint6GpsTime extends PointDoubleXYZ implements LasPointExtended {
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

  private byte classification;

  private short scanAngleRank;

  private short userData;

  private int pointSourceID;

  private byte scannerChannel;

  private double gpsTime;

  private byte returnByte;

  public LasPoint6GpsTime() {
    this.gpsTime = getCurrentGpsTime();
  }

  public LasPoint6GpsTime(final double x, final double y, final double z) {
    super(x, y, z);
    this.gpsTime = getCurrentGpsTime();
  }

  @Override
  public LasPoint6GpsTime clone() {
    return (LasPoint6GpsTime)super.clone();
  }

  @Override
  public byte getClassification() {
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

  @Override
  public short getScanAngleRank() {
    return this.scanAngleRank;
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
    this.x = pointCloud.getOffsetX() + xRecord * pointCloud.getResolutionX();
    this.y = pointCloud.getOffsetY() + yRecord * pointCloud.getResolutionY();
    this.z = pointCloud.getOffsetZ() + zRecord * pointCloud.getResolutionZ();
    this.intensity = reader.getUnsignedShort();
    this.returnByte = reader.getByte();

    final byte classificationByte = reader.getByte();
    this.synthetic = (classificationByte & 0b1) == 1;
    this.keyPoint = (classificationByte >> 1 & 0b1) == 1;
    this.withheld = (classificationByte >> 2 & 0b1) == 1;
    this.overlap = (classificationByte >> 3 & 0b1) == 1;
    this.scannerChannel = (byte)(classificationByte >> 4 & 0b11);
    this.scanDirectionFlag = (classificationByte >> 6 & 0b1) == 1;
    this.edgeOfFlightLine = (classificationByte >> 7 & 0b1) == 1;
    this.userData = reader.getByte();
    this.scanAngleRank = reader.getShort();
    this.pointSourceID = reader.getUnsignedShort();
    this.gpsTime = reader.getDouble();
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

  @Override
  public void setScanAngleRank(final short scanAngleRank) {
    this.scanAngleRank = scanAngleRank;
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
  public void setX(final double x) {
    this.x = x;
  }

  @Override
  public void setY(final double y) {
    this.y = y;
  }

  @Override
  public void setZ(final double z) {
    this.z = z;
  }

  @Override
  public MapEx toMap() {
    final MapEx map = new LinkedHashMapEx();
    addToMap(map, "x", this.x);
    addToMap(map, "y", this.y);
    addToMap(map, "z", this.z);
    addToMap(map, "intensity", this.intensity);
    addToMap(map, "returnNumber", getReturnNumber());
    addToMap(map, "numberOfReturns", getNumberOfReturns());
    addToMap(map, "scanDirectionFlag", this.scanDirectionFlag);
    addToMap(map, "edgeOfFlightLine", this.edgeOfFlightLine);
    addToMap(map, "classification", this.classification);
    addToMap(map, "synthetic", this.synthetic);
    addToMap(map, "keyPoint", this.keyPoint);
    addToMap(map, "withheld", this.withheld);
    addToMap(map, "scanAngleRank", this.scanAngleRank);
    addToMap(map, "userData", this.userData);
    addToMap(map, "pointSourceID", this.pointSourceID);
    addToMap(map, "overlap", this.overlap);
    addToMap(map, "scannerChannel", this.scannerChannel);
    addToMap(map, "gpsTime", this.gpsTime);
    return map;
  }

  @Override
  public String toString() {
    return Json.toString(toMap());
  }

  @Override
  public void write(final LasPointCloud pointCloud, final EndianOutput out) {
    final int xRecord = (int)Math
      .round((this.x - pointCloud.getOffsetX()) / pointCloud.getResolutionX());
    final int yRecord = (int)Math
      .round((this.y - pointCloud.getOffsetY()) / pointCloud.getResolutionY());
    final int zRecord = (int)Math
      .round((this.z - pointCloud.getOffsetZ()) / pointCloud.getResolutionZ());

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
    out.writeLEShort(this.scanAngleRank);
    out.writeLEUnsignedShort(this.pointSourceID);
    out.writeLEDouble(this.gpsTime);
  }

}
