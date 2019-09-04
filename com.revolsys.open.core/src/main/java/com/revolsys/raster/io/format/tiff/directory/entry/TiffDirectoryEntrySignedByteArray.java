package com.revolsys.raster.io.format.tiff.directory.entry;

import java.util.Arrays;

import com.revolsys.io.channels.ChannelReader;
import com.revolsys.raster.io.format.tiff.TiffDirectory;
import com.revolsys.raster.io.format.tiff.code.TiffFieldType;
import com.revolsys.raster.io.format.tiff.code.TiffTag;

public class TiffDirectoryEntrySignedByteArray extends AbstractTiffDirectoryEntryArray<byte[]> {

  public static TiffDirectoryEntrySignedByteArray newEntry(final TiffFieldType type,
    final TiffTag tag, final TiffDirectory directory) {
    final ChannelReader in = directory.getIn();
    final long count = directory.readOffsetOrCount();
    final int maxInlineCount = directory.getMaxInlineCount(1);
    if (count <= maxInlineCount) {
      final byte[] value = new byte[(int)count];
      for (int i = 0; i < count; i++) {
        value[i] = in.getByte();
      }
      in.skipBytes((int)(maxInlineCount - count));
      return new TiffDirectoryEntrySignedByteArray(type, tag, count, value);
    } else {
      return new TiffDirectoryEntrySignedByteArray(type, tag, directory, count);
    }
  }

  private TiffDirectoryEntrySignedByteArray(final TiffFieldType type, final TiffTag tag,
    final long count, final byte[] value) {
    super(type, tag, count, value);
  }

  public TiffDirectoryEntrySignedByteArray(final TiffFieldType type, final TiffTag tag,
    final TiffDirectory directory, final long count) {
    super(type, tag, directory, count);
  }

  @Override
  public byte getByte(final int index) {
    loadValue();
    return this.value[index];
  }

  @Override
  public byte[] getByteArray() {
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
  protected byte[] loadArrayValueDo(final ChannelReader in, final int count) {
    final byte[] value = new byte[count];
    for (int i = 0; i < count; i++) {
      value[i] = in.getByte();
    }
    return value;
  }
}
