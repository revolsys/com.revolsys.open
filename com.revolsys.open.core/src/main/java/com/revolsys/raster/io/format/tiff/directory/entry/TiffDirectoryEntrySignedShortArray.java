package com.revolsys.raster.io.format.tiff.directory.entry;

import java.util.Arrays;

import com.revolsys.io.channels.ChannelReader;
import com.revolsys.raster.io.format.tiff.TiffDirectory;
import com.revolsys.raster.io.format.tiff.code.TiffFieldType;
import com.revolsys.raster.io.format.tiff.code.TiffTag;

public class TiffDirectoryEntrySignedShortArray extends AbstractTiffDirectoryEntry<short[]> {

  public TiffDirectoryEntrySignedShortArray(final TiffTag tag, final TiffDirectory directory,
    final ChannelReader in) {
    super(tag, directory, in);
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
  public short getShort(final int index) {
    return this.value[index];
  }

  @Override
  public String getString() {
    return Arrays.toString(this.value);
  }

  @Override
  public TiffFieldType getType() {
    return TiffFieldType.SSHORT;
  }

  @Override
  protected short[] loadValueDo(final ChannelReader in, final int count) {
    final short[] value = new short[count];
    for (int i = 0; i < count; i++) {
      value[i] = in.getShort();
    }
    return value;
  }
}
