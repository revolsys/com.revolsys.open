package com.revolsys.raster.io.format.tiff.directory.entry;

import com.revolsys.io.channels.ChannelReader;
import com.revolsys.raster.io.format.tiff.TiffDirectory;
import com.revolsys.raster.io.format.tiff.code.TiffFieldType;
import com.revolsys.raster.io.format.tiff.code.TiffTag;

public class TiffDirectoryEntryUnsignedByte extends AbstractTiffDirectoryEntrySingle {

  private final byte value;

  public TiffDirectoryEntryUnsignedByte(final TiffFieldType type, final TiffTag tag,
    final TiffDirectory directory) {
    super(type, tag, directory);
    final ChannelReader in = directory.getIn();
    this.value = in.getByte();
    if (directory.isBigTiff()) {
      in.skipBytes(7);
    } else {
      in.skipBytes(3);
    }
  }

  @Override
  public byte getByte() {
    return this.value;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getValue() {
    return (V)(Object)this.value;
  }
}
