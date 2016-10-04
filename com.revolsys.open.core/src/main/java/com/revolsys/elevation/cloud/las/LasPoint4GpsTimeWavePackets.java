package com.revolsys.elevation.cloud.las;

import java.io.IOException;

import com.revolsys.io.endian.EndianInput;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.Exceptions;

public class LasPoint4GpsTimeWavePackets extends LasPoint1GpsTime implements LasPointWavePackets {
  public static LasPoint4GpsTimeWavePackets newLasPoint(final LasPointCloud pointCloud,
    final RecordDefinition recordDefinition, final EndianInput in) {
    try {
      return new LasPoint4GpsTimeWavePackets(pointCloud, recordDefinition, in);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  private short wavePacketDescriptorIndex;

  private long byteOffsetToWaveformData;

  private long waveformPacketSizeInBytes;

  private float returnPointWaveformLocation;

  private float xT;

  private float yT;

  private float zT;

  public LasPoint4GpsTimeWavePackets(final LasPointCloud pointCloud,
    final RecordDefinition recordDefinition, final EndianInput in) throws IOException {
    super(pointCloud, recordDefinition, in);
  }

  @Override
  public long getByteOffsetToWaveformData() {
    return this.byteOffsetToWaveformData;
  }

  @Override
  public float getReturnPointWaveformLocation() {
    return this.returnPointWaveformLocation;
  }

  @Override
  public long getWaveformPacketSizeInBytes() {
    return this.waveformPacketSizeInBytes;
  }

  @Override
  public short getWavePacketDescriptorIndex() {
    return this.wavePacketDescriptorIndex;
  }

  @Override
  public float getXT() {
    return this.xT;
  }

  @Override
  public float getYT() {
    return this.yT;
  }

  @Override
  public float getZT() {
    return this.zT;
  }

  @Override
  protected void read(final LasPointCloud pointCloud, final EndianInput in) throws IOException {
    super.read(pointCloud, in);
    this.wavePacketDescriptorIndex = in.readByte();
    this.byteOffsetToWaveformData = in.readLEUnsignedLong();
    this.waveformPacketSizeInBytes = in.readLEUnsignedInt();
    this.returnPointWaveformLocation = in.readLEFloat();
    this.xT = in.readLEFloat();
    this.yT = in.readLEFloat();
    this.zT = in.readLEFloat();
  }
}
