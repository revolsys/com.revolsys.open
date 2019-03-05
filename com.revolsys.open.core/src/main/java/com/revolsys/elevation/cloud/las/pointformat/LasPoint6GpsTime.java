package com.revolsys.elevation.cloud.las.pointformat;

import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.cloud.las.LasPointCloud;
import com.revolsys.io.channels.ChannelReader;
import com.revolsys.io.channels.ChannelWriter;

public class LasPoint6GpsTime extends BaseLasPoint implements LasPointExtended {
  private static final long serialVersionUID = 1L;

  public static double getCurrentGpsTime() {
    return System.currentTimeMillis() / 1000.0 - 315964800;
  }

  private boolean scanDirectionFlag;

  private boolean edgeOfFlightLine;

  private boolean synthetic;

  private boolean keyPoint;

  private boolean withheld;

  private boolean overlap;

  private short classification;

  private short scanAngle;

  private short userData;

  private byte scannerChannel;

  private double gpsTime;

  private byte returnByte = 0b00010001;

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
  public byte getNumberOfReturns() {
    return (byte)(this.returnByte >> 4 & 0b1111);
  }

  @Override
  public LasPointFormat getPointFormat() {
    return LasPointFormat.ExtendedGpsTime;
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
  public LasPoint6GpsTime setClassification(final short classification) {
    if (classification < 0 && classification > 256) {
      throw new IllegalArgumentException("Invalid LAS classificaion " + classification
        + " not in 0..255 for record format " + getPointFormatId());
    }
    this.classification = classification;
    return this;
  }

  @Override
  public LasPoint6GpsTime setClassificationByte(final byte classificationByte) {
    throw new RuntimeException("Not implemented");
  }

  @Override
  public LasPoint6GpsTime setEdgeOfFlightLine(final boolean edgeOfFlightLine) {
    this.edgeOfFlightLine = edgeOfFlightLine;
    return this;
  }

  @Override
  public LasPoint6GpsTime setGpsTime(final double gpsTime) {
    this.gpsTime = gpsTime;
    return this;
  }

  @Override
  public LasPoint6GpsTime setKeyPoint(final boolean keyPoint) {
    this.keyPoint = keyPoint;
    return this;
  }

  @Override
  public LasPoint6GpsTime setNumberOfReturns(final byte numberOfReturns) {
    if (numberOfReturns >= 1 && numberOfReturns <= 31) {
      this.returnByte &= numberOfReturns | 0b11110000;
    } else {
      throw new IllegalArgumentException(
        "numberOfReturns must be in range 1..31: " + numberOfReturns);
    }
    return this;
  }

  @Override
  public void setOverlap(final boolean overlap) {
    this.overlap = overlap;
  }

  @Override
  public LasPoint6GpsTime setReturnByte(final byte returnByte) {
    throw new RuntimeException("Not implemented");
  }

  @Override
  public LasPoint6GpsTime setReturnNumber(final byte returnNumber) {
    if (returnNumber >= 1 && returnNumber <= 31) {
      this.returnByte &= returnNumber << 4 | 0b00001111;
    } else {
      throw new IllegalArgumentException("returnNumber must be in range 1..31: " + returnNumber);
    }
    return this;
  }

  public void setScanAngle(final short scanAngle) {
    this.scanAngle = scanAngle;
  }

  @Override
  public LasPoint6GpsTime setScanAngleRank(final byte scanAngleRank) {
    throw new UnsupportedOperationException();
  }

  @Override
  public LasPoint6GpsTime setScanDirectionFlag(final boolean scanDirectionFlag) {
    this.scanDirectionFlag = scanDirectionFlag;
    return this;
  }

  @Override
  public LasPoint6GpsTime setScannerChannel(final byte scannerChannel) {
    this.scannerChannel = scannerChannel;
    return this;
  }

  @Override
  public LasPoint6GpsTime setSynthetic(final boolean synthetic) {
    this.synthetic = synthetic;
    return this;
  }

  @Override
  public LasPoint6GpsTime setUserData(final short userData) {
    this.userData = userData;
    return this;
  }

  @Override
  public LasPoint6GpsTime setWithheld(final boolean withheld) {
    this.withheld = withheld;
    return this;
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
  public void writeLasPoint(final ChannelWriter out) {
    final int xRecord = getXInt();
    final int yRecord = getYInt();
    final int zRecord = getZInt();

    out.putInt(xRecord);
    out.putInt(yRecord);
    out.putInt(zRecord);

    out.putUnsignedShort(this.intensity);
    out.putByte(this.returnByte);

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
    out.putByte(classificationFlags);
    out.putUnsignedByte(this.classification);
    out.putUnsignedByte(this.userData);
    out.putShort(this.scanAngle);
    out.putUnsignedShort(this.pointSourceID);
    out.putDouble(this.gpsTime);
  }

}
