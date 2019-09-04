package com.revolsys.raster.io.format.tiff.directory.entry;

import com.revolsys.io.channels.ChannelReader;
import com.revolsys.raster.io.format.tiff.TiffDirectory;
import com.revolsys.raster.io.format.tiff.code.TiffFieldType;
import com.revolsys.raster.io.format.tiff.code.TiffTag;

public class TiffDirectoryEntrySignedByte extends AbstractTiffDirectoryEntrySingle {

  private final short value;

  public TiffDirectoryEntrySignedByte(final TiffFieldType type, final TiffTag tag,
    final TiffDirectory directory) {
    super(type, tag, directory);
    final ChannelReader in = directory.getIn();
    this.value = in.getUnsignedByte();
    if (directory.isBigTiff()) {
      in.skipBytes(7);
    } else {
      in.skipBytes(3);
    }
  }

  @Override
  public short getShort() {
    return this.value;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getValue() {
    return (V)(Object)this.value;
  }
}
