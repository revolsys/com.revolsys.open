package com.revolsys.raster.io.format.tiff.directory.entry;

import java.util.Arrays;

import com.revolsys.io.channels.ChannelReader;
import com.revolsys.raster.io.format.tiff.TiffDirectory;
import com.revolsys.raster.io.format.tiff.code.TiffFieldType;
import com.revolsys.raster.io.format.tiff.code.TiffTag;

public class TiffDirectoryEntrySignedRationalArray extends AbstractTiffDirectoryEntry<double[]> {

  public TiffDirectoryEntrySignedRationalArray(final TiffTag tag, final TiffDirectory directory,
    final ChannelReader in) {
    super(tag, directory, in);
  }

  @Override
  public double getDouble(final int index) {
    return this.value[index];
  }

  @Override
  public Number getNumber() {
    if (getCount() == 1) {
      return this.value[0];
    } else {
      throw new IllegalStateException("Cannot get single value from array of size " + getCount());
    }
  }

  @Override
  public Number getNumber(final int index) {
    return this.value[index];
  }

  @Override
  public String getString() {
    return Arrays.toString(this.value);
  }

  @Override
  public TiffFieldType getType() {
    return TiffFieldType.SRATIONAL;
  }

  @Override
  protected double[] loadValueDo(final ChannelReader in, final int count) {
    final double[] value = new double[count];
    for (int i = 0; i < count; i++) {
      final double numerator = in.getInt();
      final double denominator = in.getInt();
      value[i] = numerator / denominator;
    }
    return value;
  }
}
