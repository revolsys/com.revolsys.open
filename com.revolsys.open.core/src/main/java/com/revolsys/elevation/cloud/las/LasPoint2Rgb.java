package com.revolsys.elevation.cloud.las;

import java.io.IOException;

import com.revolsys.io.endian.EndianInput;
import com.revolsys.io.endian.EndianOutput;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.Exceptions;

public class LasPoint2Rgb extends LasPoint0Core implements LasPointRgb {
  private static final long serialVersionUID = 1L;

  public static LasPoint2Rgb newLasPoint(final LasPointCloud pointCloud,
    final RecordDefinition recordDefinition, final EndianInput in) {
    try {
      return new LasPoint2Rgb(pointCloud, recordDefinition, in);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  private int red;

  private int green;

  private int blue;

  public LasPoint2Rgb(final LasPointCloud pointCloud, final double x, final double y,
    final double z) {
    super(pointCloud, x, y, z);
  }

  public LasPoint2Rgb(final LasPointCloud pointCloud, final RecordDefinition recordDefinition,
    final EndianInput in) throws IOException {
    super(pointCloud, recordDefinition, in);
  }

  @Override
  public int getBlue() {
    return this.blue;
  }

  @Override
  public int getGreen() {
    return this.green;
  }

  @Override
  public int getRed() {
    return this.red;
  }

  @Override
  protected void read(final LasPointCloud pointCloud, final EndianInput in) throws IOException {
    super.read(pointCloud, in);
    this.red = in.readLEUnsignedShort();
    this.green = in.readLEUnsignedShort();
    this.blue = in.readLEUnsignedShort();
  }

  @Override
  protected void write(final LasPointCloud pointCloud, final EndianOutput out) {
    super.write(pointCloud, out);
    out.writeLEUnsignedShort(this.red);
    out.writeLEUnsignedShort(this.green);
    out.writeLEUnsignedShort(this.blue);
  }
}
