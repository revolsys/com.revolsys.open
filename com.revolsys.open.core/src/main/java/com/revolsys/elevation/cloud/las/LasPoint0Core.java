package com.revolsys.elevation.cloud.las;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.impl.PointDoubleXYZ;
import com.revolsys.io.channels.ChannelReader;
import com.revolsys.io.endian.EndianOutput;
import com.revolsys.record.io.format.json.Json;

public class LasPoint0Core extends PointDoubleXYZ implements LasPoint {

  private static final long serialVersionUID = 1L;

  private int intensity;

  private short scanAngleRank;

  private short userData;

  private int pointSourceID;

  private byte scannerChannel;

  private byte returnByte;

  private byte classificationByte;

  public LasPoint0Core() {
  }

  public LasPoint0Core(final double x, final double y, final double z) {
    super(x, y, z);
  }

  @Override
  public LasPoint0Core clone() {
    return (LasPoint0Core)super.clone();
  }

  @Override
  public byte getClassification() {
    return (byte)(this.classificationByte & 0b11111);
  }

  @Override
  public int getIntensity() {
    return this.intensity;
  }

  @Override
  public byte getNumberOfReturns() {
    return (byte)(this.returnByte >> 3 & 0b111);
  }

  @Override
  public LasPointFormat getPointFormat() {
    return LasPointFormat.Core;
  }

  @Override
  public int getPointSourceID() {
    return this.pointSourceID;
  }

  @Override
  public byte getReturnNumber() {
    return (byte)(this.returnByte & 0b111);
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
    return (this.returnByte >> 7 & 0b1) == 1;
  }

  @Override
  public boolean isKeyPoint() {
    return (this.classificationByte >> 6 & 0b1) == 1;
  }

  @Override
  public boolean isScanDirectionFlag() {
    return (this.returnByte >> 6 & 0b1) == 1;
  }

  @Override
  public boolean isSynthetic() {
    return (this.classificationByte >> 5 & 0b1) == 1;
  }

  @Override
  public boolean isWithheld() {
    return (this.classificationByte >> 7 & 0b1) == 1;
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

    this.classificationByte = reader.getByte();
    this.scanAngleRank = reader.getByte();
    this.userData = reader.getByte();
    this.pointSourceID = reader.getUnsignedShort();
  }

  @Override
  public void setClassification(final byte classification) {
    this.classificationByte &= classification | 0b11100000;
  }

  @Override
  public void setEdgeOfFlightLine(final boolean edgeOfFlightLine) {
    if (edgeOfFlightLine) {
      this.returnByte |= 0b1000000;
    } else {
      this.returnByte &= ~0b1000000;
    }
  }

  @Override
  public void setIntensity(final int intensity) {
    this.intensity = intensity;
  }

  @Override
  public void setKeyPoint(final boolean keyPoint) {
    if (keyPoint) {
      this.classificationByte |= 0b1000000;
    } else {
      this.classificationByte &= ~0b1000000;
    }
  }

  @Override
  public void setNumberOfReturns(final byte numberOfReturns) {
    this.returnByte &= numberOfReturns | 0b11111000;
  }

  @Override
  public void setPointSourceID(final int pointSourceID) {
    this.pointSourceID = pointSourceID;
  }

  @Override
  public void setReturnNumber(final byte returnNumber) {
    this.returnByte &= returnNumber << 3 | 0b11000111;
  }

  @Override
  public void setScanAngleRank(final short scanAngleRank) {
    this.scanAngleRank = scanAngleRank;
  }

  @Override
  public void setScanDirectionFlag(final boolean scanDirectionFlag) {
    if (scanDirectionFlag) {
      this.returnByte |= 0b100000;
    } else {
      this.returnByte &= ~0b100000;
    }
  }

  @Override
  public void setScannerChannel(final byte scannerChannel) {
    this.scannerChannel = scannerChannel;
  }

  @Override
  public void setSynthetic(final boolean synthetic) {
    if (synthetic) {
      this.classificationByte |= 0b100000;
    } else {
      this.classificationByte &= ~0b100000;
    }
  }

  @Override
  public void setUserData(final short userData) {
    this.userData = userData;
  }

  @Override
  public void setWithheld(final boolean withheld) {
    if (withheld) {
      this.classificationByte |= 0b10000000;
    } else {
      this.classificationByte &= ~0b10000000;
    }
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
    addToMap(map, "scanDirectionFlag", isScanDirectionFlag());
    addToMap(map, "edgeOfFlightLine", isEdgeOfFlightLine());
    addToMap(map, "classification", getClassification());
    addToMap(map, "synthetic", isSynthetic());
    addToMap(map, "keyPoint", isKeyPoint());
    addToMap(map, "withheld", isWithheld());
    addToMap(map, "scanAngleRank", this.scanAngleRank);
    addToMap(map, "userData", this.userData);
    addToMap(map, "pointSourceID", this.pointSourceID);
    addToMap(map, "scannerChannel", this.scannerChannel);
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
    out.write(this.classificationByte);
    out.write((byte)this.scanAngleRank);
    out.write(this.userData);
    out.writeLEUnsignedShort(this.pointSourceID);
  }
}
