package com.revolsys.raster.io.format.tiff.directory.entry;

import com.revolsys.io.channels.ChannelReader;
import com.revolsys.raster.io.format.tiff.TiffDirectory;
import com.revolsys.raster.io.format.tiff.code.TiffFieldType;
import com.revolsys.raster.io.format.tiff.code.TiffTag;

public class TiffDirectoryEntryAscii extends AbstractTiffDirectoryEntryCountOffset {

  private String value;

  public TiffDirectoryEntryAscii(final TiffFieldType type, final TiffTag tag,
    final TiffDirectory directory) {
    super(type, tag, directory);
  }

  @Override
  public String getString() {
    loadValue();
    return this.value;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getValue() {
    return (V)getString();
  }

  @Override
  public void loadValueDo(final ChannelReader in, final int count) {
    this.value = in.getUsAsciiString(count);
  }
}
