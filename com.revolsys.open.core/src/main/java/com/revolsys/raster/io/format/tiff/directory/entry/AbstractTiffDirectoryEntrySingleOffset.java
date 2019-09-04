package com.revolsys.raster.io.format.tiff.directory.entry;

import com.revolsys.io.channels.ChannelReader;
import com.revolsys.raster.io.format.tiff.TiffDirectory;
import com.revolsys.raster.io.format.tiff.code.TiffFieldType;
import com.revolsys.raster.io.format.tiff.code.TiffTag;

public abstract class AbstractTiffDirectoryEntrySingleOffset
  extends AbstractTiffDirectoryEntryCountOffset {

  public AbstractTiffDirectoryEntrySingleOffset(final TiffFieldType type, final TiffTag tag,
    final TiffDirectory directory, final ChannelReader in) {
    super(type, tag, directory, in);
    if (this.count != 1) {
      throw new IllegalArgumentException(
        "Expected count of 1 for " + tag.getClass().getSimpleName() + "." + tag);
    }
  }
}
