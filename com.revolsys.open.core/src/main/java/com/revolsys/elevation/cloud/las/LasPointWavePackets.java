package com.revolsys.elevation.cloud.las;

public interface LasPointWavePackets {

  long getByteOffsetToWaveformData();

  float getReturnPointWaveformLocation();

  long getWaveformPacketSizeInBytes();

  short getWavePacketDescriptorIndex();

  float getXT();

  float getYT();

  float getZT();

}
