package com.revolsys.raster.io.format.tiff.directory.entry;

import java.util.Arrays;

import com.revolsys.io.channels.ChannelReader;
import com.revolsys.raster.io.format.tiff.TiffDirectory;
import com.revolsys.raster.io.format.tiff.code.TiffFieldType;
import com.revolsys.raster.io.format.tiff.code.TiffTag;

public class TiffDirectoryEntryFloatArray extends AbstractTiffDirectoryEntryArray<float[]> {

  public static TiffDirectoryEntryFloatArray newEntry(final TiffFieldType type, final TiffTag tag,
    final TiffDirectory directory) {
    final ChannelReader in = directory.getIn();
    final long count = directory.readOffsetOrCount();
    final int maxInlineCount = directory.getMaxInlineCount(4);
    if (count <= maxInlineCount) {
      final float[] value = new float[(int)count];
      for (int i = 0; i < count; i++) {
        value[i] = in.getFloat();
      }
      in.skipBytes((int)(maxInlineCount - count) * 4);
      return new TiffDirectoryEntryFloatArray(type, tag, count, value);

    } else {
      return new TiffDirectoryEntryFloatArray(type, tag, directory, count);
    }
  }

  private TiffDirectoryEntryFloatArray(final TiffFieldType type, final TiffTag tag,
    final long count, final float[] value) {
    super(type, tag, count, value);
  }

  private TiffDirectoryEntryFloatArray(final TiffFieldType type, final TiffTag tag,
    final TiffDirectory directory, final long count) {
    super(type, tag, directory, count);
  }

  @Override
  public float getFloat(final int index) {
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
  protected float[] loadArrayValueDo(final ChannelReader in, final int count) {
    final float[] value = new float[count];
    for (int i = 0; i < count; i++) {
      value[i] = in.getFloat();
    }
    return value;
  }
}
