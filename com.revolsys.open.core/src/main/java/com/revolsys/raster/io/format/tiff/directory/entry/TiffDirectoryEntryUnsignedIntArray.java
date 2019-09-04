package com.revolsys.raster.io.format.tiff.directory.entry;

import java.util.Arrays;

import com.revolsys.io.channels.ChannelReader;
import com.revolsys.raster.io.format.tiff.TiffDirectory;
import com.revolsys.raster.io.format.tiff.code.TiffFieldType;
import com.revolsys.raster.io.format.tiff.code.TiffTag;

public class TiffDirectoryEntryUnsignedIntArray extends AbstractTiffDirectoryEntryArray<long[]> {

  public static TiffDirectoryEntryUnsignedIntArray newEntry(final TiffFieldType type,
    final TiffTag tag, final TiffDirectory directory, final ChannelReader in) {
    final long count = directory.readOffsetOrCount(in);
    final int maxInlineCount = directory.getMaxInlineCount(4);
    if (count <= maxInlineCount) {
      final long[] value = new long[(int)count];
      for (int i = 0; i < count; i++) {
        value[i] = in.getUnsignedInt();
      }
      in.skipBytes((int)(maxInlineCount - count) * 4);
      return new TiffDirectoryEntryUnsignedIntArray(type, tag, count, value);

    } else {
      return new TiffDirectoryEntryUnsignedIntArray(type, tag, directory, in, count);
    }
  }

  private TiffDirectoryEntryUnsignedIntArray(final TiffFieldType type, final TiffTag tag,
    final long count, final long[] value) {
    super(type, tag, count, value);
  }

  private TiffDirectoryEntryUnsignedIntArray(final TiffFieldType type, final TiffTag tag,
    final TiffDirectory directory, final ChannelReader in, final long count) {
    super(type, tag, directory, in, count);
  }

  @Override
  public long getLong(final int index) {
    return this.value[index];
  }

  @Override
  public long[] getLongArray() {
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
  protected long[] loadArrayValueDo(final ChannelReader in, final int count) {
    final long[] value = new long[count];
    for (int i = 0; i < count; i++) {
      value[i] = in.getUnsignedInt();
    }
    return value;
  }
}
