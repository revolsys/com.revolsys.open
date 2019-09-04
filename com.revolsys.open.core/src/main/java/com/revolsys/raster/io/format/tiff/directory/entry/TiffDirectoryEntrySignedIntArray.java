package com.revolsys.raster.io.format.tiff.directory.entry;

import java.util.Arrays;

import com.revolsys.io.channels.ChannelReader;
import com.revolsys.raster.io.format.tiff.TiffDirectory;
import com.revolsys.raster.io.format.tiff.code.TiffFieldType;
import com.revolsys.raster.io.format.tiff.code.TiffTag;

public class TiffDirectoryEntrySignedIntArray extends AbstractTiffDirectoryEntryArray<int[]> {

  public static TiffDirectoryEntrySignedIntArray newEntry(final TiffFieldType type,
    final TiffTag tag, final TiffDirectory directory, final ChannelReader in) {
    final long count = directory.readOffsetOrCount(in);
    final int maxInlineCount = directory.getMaxInlineCount(4);
    if (count <= maxInlineCount) {
      final int[] value = new int[(int)count];
      for (int i = 0; i < count; i++) {
        value[i] = in.getInt();
      }
      in.skipBytes((int)(maxInlineCount - count) * 4);
      return new TiffDirectoryEntrySignedIntArray(type, tag, count, value);

    } else {
      return new TiffDirectoryEntrySignedIntArray(type, tag, directory, in, count);
    }
  }

  private TiffDirectoryEntrySignedIntArray(final TiffFieldType type, final TiffTag tag,
    final long count, final int[] value) {
    super(type, tag, count, value);
  }

  private TiffDirectoryEntrySignedIntArray(final TiffFieldType type, final TiffTag tag,
    final TiffDirectory directory, final ChannelReader in, final long count) {
    super(type, tag, directory, in, count);
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
  public Number getNumber(final int index) {
    return this.value[index];
  }

  @Override
  public String getString() {
    return Arrays.toString(this.value);
  }

  @Override
  protected int[] loadArrayValueDo(final ChannelReader in, final int count) {
    final int[] value = new int[count];
    for (int i = 0; i < count; i++) {
      value[i] = in.getInt();
    }
    return value;
  }
}
