package com.revolsys.elevation.cloud.las;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.revolsys.io.Buffers;
import com.revolsys.io.endian.EndianOutput;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.Exceptions;

public class LasPoint4GpsTimeWavePackets extends LasPoint1GpsTime implements LasPointWavePackets {
  private static final long serialVersionUID = 1L;

  public static LasPoint4GpsTimeWavePackets newLasPoint(final LasPointCloud pointCloud,
    final RecordDefinition recordDefinition, final ByteBuffer buffer) {
    try {
      return new LasPoint4GpsTimeWavePackets(pointCloud, recordDefinition, buffer);
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

  public LasPoint4GpsTimeWavePackets(final LasPointCloud pointCloud, final double x, final double y,
    final double z) {
    super(pointCloud, x, y, z);
  }

  public LasPoint4GpsTimeWavePackets(final LasPointCloud pointCloud,
    final RecordDefinition recordDefinition, final ByteBuffer buffer) throws IOException {
    super(pointCloud, recordDefinition, buffer);
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
  protected void read(final LasPointCloud pointCloud, final ByteBuffer buffer) throws IOException {
    super.read(pointCloud, buffer);
    this.wavePacketDescriptorIndex = buffer.get();
    this.byteOffsetToWaveformData = Buffers.getLEUnsignedLong(buffer);
    this.waveformPacketSizeInBytes = Buffers.getLEUnsignedInt(buffer);
    this.returnPointWaveformLocation = buffer.getFloat();
    this.xT = buffer.getFloat();
    this.yT = buffer.getFloat();
    this.zT = buffer.getFloat();
  }

  @Override
  protected void write(final LasPointCloud pointCloud, final EndianOutput out) {
    super.write(pointCloud, out);
    out.write(this.wavePacketDescriptorIndex);
    out.writeLEUnsignedLong(this.byteOffsetToWaveformData);
    out.writeLEUnsignedInt(this.waveformPacketSizeInBytes);
    out.writeLEFloat(this.returnPointWaveformLocation);
    out.writeLEFloat(this.xT);
    out.writeLEFloat(this.yT);
    out.writeLEFloat(this.zT);
  }
}
