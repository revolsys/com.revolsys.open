package com.revolsys.raster.io.format.tiff.directory.entry;

import com.revolsys.io.channels.ChannelReader;
import com.revolsys.raster.io.format.tiff.TiffDirectory;
import com.revolsys.raster.io.format.tiff.code.TiffFieldType;
import com.revolsys.raster.io.format.tiff.code.TiffTag;

public class TiffDirectoryEntrySignedInt extends AbstractTiffDirectoryEntrySingle {

  private final long offset;

  public TiffDirectoryEntrySignedInt(final TiffFieldType type, final TiffTag tag,
    final TiffDirectory directory, final ChannelReader in) {
    super(type, tag, directory, in);
    this.offset = in.getUnsignedInt();
    if (directory.isBigTiff()) {
      in.skipBytes(4);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getValue() {
    return (V)(Object)this.offset;
  }
}
