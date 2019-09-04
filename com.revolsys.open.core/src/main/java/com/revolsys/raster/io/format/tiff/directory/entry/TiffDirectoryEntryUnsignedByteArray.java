package com.revolsys.raster.io.format.tiff.directory.entry;

import java.util.Arrays;

import com.revolsys.io.channels.ChannelReader;
import com.revolsys.raster.io.format.tiff.TiffDirectory;
import com.revolsys.raster.io.format.tiff.code.TiffFieldType;
import com.revolsys.raster.io.format.tiff.code.TiffTag;

public class TiffDirectoryEntryUnsignedByteArray extends AbstractTiffDirectoryEntryArray<short[]> {

  public static TiffDirectoryEntryUnsignedByteArray newEntry(final TiffFieldType type,
    final TiffTag tag, final TiffDirectory directory) {
    final ChannelReader in = directory.getIn();
    final long count = directory.readOffsetOrCount();
    final int maxInlineCount = directory.getMaxInlineCount(1);
    if (count <= maxInlineCount) {
      final short[] value = new short[(int)count];
      for (int i = 0; i < count; i++) {
        value[i] = in.getUnsignedByte();
      }
      in.skipBytes((int)(maxInlineCount - count));
      return new TiffDirectoryEntryUnsignedByteArray(type, tag, count, value);

    } else {
      return new TiffDirectoryEntryUnsignedByteArray(type, tag, directory, count);
    }
  }

  private TiffDirectoryEntryUnsignedByteArray(final TiffFieldType type, final TiffTag tag,
    final long count, final short[] value) {
    super(type, tag, count, value);
  }

  private TiffDirectoryEntryUnsignedByteArray(final TiffFieldType type, final TiffTag tag,
    final TiffDirectory directory, final long count) {
    super(type, tag, directory, count);
  }

  @Override
  public Number getNumber(final int index) {
    loadValue();
    return this.value[index];
  }

  @Override
  public short getShort(final int index) {
    loadValue();
    return this.value[index];
  }

  @Override
  public String getString() {
    loadValue();
    return Arrays.toString(this.value);
  }

  @Override
  protected short[] loadArrayValueDo(final ChannelReader in, final int count) {
    final short[] value = new short[count];
    for (int i = 0; i < count; i++) {
      value[i] = in.getUnsignedByte();
    }
    return value;
  }
}
