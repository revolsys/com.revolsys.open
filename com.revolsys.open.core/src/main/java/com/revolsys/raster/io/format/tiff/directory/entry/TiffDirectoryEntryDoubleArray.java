package com.revolsys.raster.io.format.tiff.directory.entry;

import java.util.Arrays;

import com.revolsys.io.channels.ChannelReader;
import com.revolsys.raster.io.format.tiff.TiffDirectory;
import com.revolsys.raster.io.format.tiff.code.TiffFieldType;
import com.revolsys.raster.io.format.tiff.code.TiffTag;

public class TiffDirectoryEntryDoubleArray extends AbstractTiffDirectoryEntryArray<double[]> {

  public static TiffDirectoryEntryDoubleArray newEntry(final TiffFieldType type, final TiffTag tag,
    final TiffDirectory directory) {
    final ChannelReader in = directory.getIn();
    final long count = directory.readOffsetOrCount();
    final boolean bigTiff = directory.isBigTiff();
    if (count == 1 && bigTiff) {
      final double[] value = new double[(int)count];
      value[0] = in.getDouble();
      return new TiffDirectoryEntryDoubleArray(type, tag, count, value);

    } else {
      return new TiffDirectoryEntryDoubleArray(type, tag, directory, count);
    }
  }

  public TiffDirectoryEntryDoubleArray(final TiffFieldType type, final TiffTag tag,
    final long count, final double[] value) {
    super(type, tag, count, value);
  }

  public TiffDirectoryEntryDoubleArray(final TiffFieldType type, final TiffTag tag,
    final TiffDirectory directory, final long count) {
    super(type, tag, directory, count);
  }

  @Override
  public double getDouble(final int index) {
    loadValue();
    return this.value[index];
  }

  @Override
  public double[] getDoubleArray() {
    return getValue();
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
      value[i] = in.getDouble();
    }
    return value;
  }
}
