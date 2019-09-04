package com.revolsys.raster.io.format.tiff.directory.entry;

import com.revolsys.io.channels.ChannelReader;
import com.revolsys.raster.io.format.tiff.TiffDirectory;
import com.revolsys.raster.io.format.tiff.code.TiffFieldType;
import com.revolsys.raster.io.format.tiff.code.TiffTag;

public class TiffDirectoryEntryUnsignedShort extends AbstractTiffDirectoryEntrySingle {

  private final int value;

  public TiffDirectoryEntryUnsignedShort(final TiffFieldType type, final TiffTag tag,
    final TiffDirectory directory, final ChannelReader in) {
    super(type, tag, directory, in);
    this.value = in.getUnsignedShort();
    if (directory.isBigTiff()) {
      in.skipBytes(6);
    } else {
      in.skipBytes(2);
    }
  }

  @Override
  public int getInt() {
    return this.value;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getValue() {
    return (V)(Object)this.value;
  }
}
