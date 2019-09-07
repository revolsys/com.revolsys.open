package com.revolsys.raster.io.format.tiff.directory.entry;

import com.revolsys.io.channels.ChannelReader;
import com.revolsys.raster.io.format.tiff.TiffDirectory;
import com.revolsys.raster.io.format.tiff.code.TiffFieldType;
import com.revolsys.raster.io.format.tiff.code.TiffTag;

public class TiffDirectoryEntryUndefinedArray extends TiffDirectoryEntrySignedByteArray {

  public TiffDirectoryEntryUndefinedArray(final TiffTag tag, final TiffDirectory directory,
    final ChannelReader in) {
    super(tag, directory, in);
  }

  @Override
  public TiffFieldType getType() {
    return TiffFieldType.UNDEFINED;
  }

}
