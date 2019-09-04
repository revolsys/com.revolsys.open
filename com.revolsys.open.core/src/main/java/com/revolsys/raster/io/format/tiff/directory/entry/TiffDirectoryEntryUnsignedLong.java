package com.revolsys.raster.io.format.tiff.directory.entry;

import com.revolsys.io.channels.ChannelReader;
import com.revolsys.raster.io.format.tiff.TiffDirectory;
import com.revolsys.raster.io.format.tiff.code.TiffFieldType;
import com.revolsys.raster.io.format.tiff.code.TiffTag;

public class TiffDirectoryEntryUnsignedLong extends AbstractTiffDirectoryEntrySingle {

  private final long value;

  public TiffDirectoryEntryUnsignedLong(final TiffFieldType type, final TiffTag tag,
    final TiffDirectory directory) {
    super(type, tag, directory);
    final ChannelReader in = directory.getIn();
    this.value = in.getLong();
  }

  @Override
  public long getLong() {
    return this.value;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getValue() {
    return (V)(Object)this.value;
  }
}
