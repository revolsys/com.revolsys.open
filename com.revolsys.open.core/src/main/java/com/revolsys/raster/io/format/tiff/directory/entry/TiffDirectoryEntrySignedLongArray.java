package com.revolsys.raster.io.format.tiff.directory.entry;

import java.util.Arrays;

import com.revolsys.io.channels.ChannelReader;
import com.revolsys.raster.io.format.tiff.TiffDirectory;
import com.revolsys.raster.io.format.tiff.code.TiffFieldType;
import com.revolsys.raster.io.format.tiff.code.TiffTag;

public class TiffDirectoryEntrySignedLongArray extends AbstractTiffDirectoryEntryArray<long[]> {

  public static TiffDirectoryEntrySignedLongArray newEntry(final TiffFieldType type,
    final TiffTag tag, final TiffDirectory directory) {
    final ChannelReader in = directory.getIn();
    final long count = directory.readOffsetOrCount();
    final int maxInlineCount = directory.getMaxInlineCount(8);
    if (count <= maxInlineCount) {
      final long[] value = new long[(int)count];
      for (int i = 0; i < count; i++) {
        value[i] = in.getUnsignedInt();
      }
      in.skipBytes((int)(maxInlineCount - count) * 8);
      return new TiffDirectoryEntrySignedLongArray(type, tag, count, value);

    } else {
      return new TiffDirectoryEntrySignedLongArray(type, tag, directory, count);
    }
  }

  private TiffDirectoryEntrySignedLongArray(final TiffFieldType type, final TiffTag tag,
    final long count, final long[] value) {
    super(type, tag, count, value);
  }

  private TiffDirectoryEntrySignedLongArray(final TiffFieldType type, final TiffTag tag,
    final TiffDirectory directory, final long count) {
    super(type, tag, directory, count);
  }

  @Override
  public long getLong(final int index) {
    loadValue();
    return this.value[index];
  }

  @Override
  public long[] getLongArray() {
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
  protected long[] loadArrayValueDo(final ChannelReader in, final int count) {
    final long[] value = new long[count];
    for (int i = 0; i < count; i++) {
      value[i] = in.getLong();
    }
    return value;
  }
}
