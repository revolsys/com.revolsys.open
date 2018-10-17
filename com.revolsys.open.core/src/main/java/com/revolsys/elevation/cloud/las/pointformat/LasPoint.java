package com.revolsys.elevation.cloud.las.pointformat;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.cloud.las.LasPointCloud;
import com.revolsys.geometry.model.Point;
import com.revolsys.io.channels.ChannelReader;
import com.revolsys.io.endian.EndianOutput;
import com.revolsys.io.map.MapSerializer;

public interface LasPoint extends Point, MapSerializer {

  @Override
  LasPoint clone();

  default int getBlue() {
    return 0;
  }

  short getClassification();

  byte getClassificationByte();

  default double getGpsTime() {
    return 315964800;
  }

  default int getGreen() {
    return 0;
  }

  int getIntensity();

  byte getNumberOfReturns();

  LasPointFormat getPointFormat();

  default int getPointFormatId() {
    return getPointFormat().getId();
  }

  int getPointSourceID();

  default int getRed() {
    return 0;
  }

  byte getReturnByte();

  byte getReturnNumber();

  double getScanAngleDegrees();

  byte getScanAngleRank();

  byte getScannerChannel();

  short getUserData();

  int getXInt();

  int getYInt();

  int getZInt();

  boolean isEdgeOfFlightLine();

  boolean isKeyPoint();

  default boolean isPointFormat(final LasPointFormat pointFormat) {
    return pointFormat.equals(getPointFormat());
  }

  boolean isScanDirectionFlag();

  boolean isSynthetic();

  boolean isWithheld();

  void read(LasPointCloud pointCloud, ChannelReader reader);

  default void setBlue(final int blue) {
  }

  void setClassification(byte classification);

  void setClassificationByte(byte classificationByte);

  void setEdgeOfFlightLine(boolean edgeOfFlightLine);

  default void setGpsTime(final double gpsTime) {
  }

  default void setGreen(final int green) {
  }

  void setIntensity(int intensity);

  void setKeyPoint(boolean keyPoint);

  void setNumberOfReturns(byte numberOfReturns);

  void setPointSourceID(int pointSourceID);

  default void setRed(final int red) {
  }

  void setReturnByte(byte returnByte);

  void setReturnNumber(byte returnNumber);

  void setScanAngleRank(byte scanAngleRank);

  void setScanDirectionFlag(boolean scanDirectionFlag);

  void setScannerChannel(byte scannerChannel);

  void setSynthetic(boolean synthetic);

  void setUserData(short userData);

  void setWithheld(boolean withheld);

  void setXYZ(int x, int y, int z);

  @Override
  default MapEx toMap() {
    final MapEx map = new LinkedHashMapEx();
    return map;
  }

  void write(final EndianOutput out);
}
