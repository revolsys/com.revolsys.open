package com.revolsys.raster.io.format.tiff.directory.entry;

import com.revolsys.io.channels.ChannelReader;
import com.revolsys.raster.io.format.tiff.TiffDirectory;
import com.revolsys.raster.io.format.tiff.code.TiffFieldType;
import com.revolsys.raster.io.format.tiff.code.TiffTag;

public abstract class AbstractTiffDirectoryEntrySingle extends AbstractTiffDirectoryEntry {

  public AbstractTiffDirectoryEntrySingle(final TiffFieldType type, final TiffTag tag,
    final TiffDirectory directory) {
    super(type, tag);
    final long count;
    final ChannelReader in = directory.getIn();
    if (directory.isBigTiff()) {
      count = in.getLong();
    } else {
      count = in.getUnsignedInt();
    }
    if (count != 1) {
      throw new IllegalArgumentException(
        "Expected count of 1 for " + tag.getClass().getSimpleName() + "." + tag);
    }
  }

}
