package com.revolsys.raster.io.format.tiff.directory.entry;

import java.util.Arrays;

import com.revolsys.io.channels.ChannelReader;
import com.revolsys.raster.io.format.tiff.TiffDirectory;
import com.revolsys.raster.io.format.tiff.code.TiffFieldType;
import com.revolsys.raster.io.format.tiff.code.TiffTag;

public class TiffDirectoryEntryUnsignedRationalArray
  extends AbstractTiffDirectoryEntryArray<double[]> {

  public TiffDirectoryEntryUnsignedRationalArray(final TiffFieldType type, final TiffTag tag,
    final TiffDirectory directory) {
    super(type, tag, directory);
  }

  public TiffDirectoryEntryUnsignedRationalArray(final TiffFieldType type, final TiffTag tag,
    final TiffDirectory directory, final long count) {
    super(type, tag, directory, count);
  }

  @Override
  public double getDouble(final int index) {
    loadValue();
    return this.value[index];
  }

  @Override
  public Number getNumber(final int index) {
    loadValue();
    return this.value[index];
  }

  @Override
  public String getString() {
    loadValue();
    return Arrays.toString(this.value);
  }

  @Override
  protected double[] loadArrayValueDo(final ChannelReader in, final int count) {
    final double[] value = new double[count];
    for (int i = 0; i < count; i++) {
      final double numerator = in.getUnsignedInt();
      final double denominator = in.getUnsignedInt();
      value[i] = numerator / denominator;
    }
    return value;
  }
}
