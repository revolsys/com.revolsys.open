package com.revolsys.raster.io.format.tiff.directory.entry;

import java.util.Arrays;

import com.revolsys.io.channels.ChannelReader;
import com.revolsys.raster.io.format.tiff.TiffDirectory;
import com.revolsys.raster.io.format.tiff.code.TiffFieldType;
import com.revolsys.raster.io.format.tiff.code.TiffTag;

public class TiffDirectoryEntrySignedIntArray extends AbstractTiffDirectoryEntry<int[]> {

  public TiffDirectoryEntrySignedIntArray(final TiffTag tag, final TiffDirectory directory,
    final ChannelReader in) {
    super(tag, directory, in);
  }

  @Override
  public int getInt(final int index) {
    return this.value[index];
  }

  @Override
  public int[] getIntArray() {
    return getValue();
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
    return TiffFieldType.SLONG;
  }

  @Override
  protected int[] loadValueDo(final ChannelReader in, final int count) {
    final int[] value = new int[count];
    for (int i = 0; i < count; i++) {
      value[i] = in.getInt();
    }
    return value;
  }
}
