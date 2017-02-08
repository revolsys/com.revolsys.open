package com.revolsys.elevation.cloud.las;

public interface LasPointWavePackets extends LasPoint {

  long getByteOffsetToWaveformData();

  float getReturnPointWaveformLocation();

  long getWaveformPacketSizeInBytes();

  short getWavePacketDescriptorIndex();

  float getXT();

  float getYT();

  float getZT();

}
