package com.revolsys.raster.io.format.tiff.directory.entry;

import com.revolsys.io.channels.ChannelReader;
import com.revolsys.raster.io.format.tiff.TiffDirectory;
import com.revolsys.raster.io.format.tiff.code.TiffFieldType;
import com.revolsys.raster.io.format.tiff.code.TiffTag;

public abstract class AbstractTiffDirectoryEntryArray<A>
  extends AbstractTiffDirectoryEntryCountOffset {

  protected A value;

  public AbstractTiffDirectoryEntryArray(final TiffFieldType type, final TiffTag tag,
    final ChannelReader in, final long count, final long offset) {
    super(type, tag, in, count, offset);
  }

  public AbstractTiffDirectoryEntryArray(final TiffFieldType type, final TiffTag tag,
    final long count, final A value) {
    super(type, tag, count);
    this.value = value;
  }

  public AbstractTiffDirectoryEntryArray(final TiffFieldType type, final TiffTag tag,
    final TiffDirectory directory) {
    super(type, tag, directory);
  }

  public AbstractTiffDirectoryEntryArray(final TiffFieldType type, final TiffTag tag,
    final TiffDirectory directory, final long count) {
    super(type, tag, directory, count);
  }

  @Override
  public double[] getDoubleArray() {
    final int count = (int)getCount();
    final double[] value = new double[count];
    for (int i = 0; i < count; i++) {
      value[i] = getDouble(i);
    }
    return value;
  }

  @Override
  public int[] getIntArray() {
    final int count = (int)getCount();
    final int[] value = new int[count];
    for (int i = 0; i < count; i++) {
      value[i] = getInt(i);
    }
    return value;
  }

  @Override
  public long[] getLongArray() {
    final int count = (int)getCount();
    final long[] value = new long[count];
    for (int i = 0; i < count; i++) {
      value[i] = getLong(i);
    }
    return value;
  }

  @SuppressWarnings("unchecked")
  @Override
  public final <V> V getValue() {
    loadValue();
    return (V)this.value;
  }

  @Override
  public boolean isArray() {
    return true;
  }

  protected abstract A loadArrayValueDo(ChannelReader in, int count);

  @Override
  protected void loadValueDo(final ChannelReader in, final int count) {
    this.value = loadArrayValueDo(in, count);
  }
}
