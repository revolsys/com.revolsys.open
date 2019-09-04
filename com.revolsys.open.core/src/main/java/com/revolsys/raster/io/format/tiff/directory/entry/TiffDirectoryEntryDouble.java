package com.revolsys.raster.io.format.tiff.directory.entry;

import com.revolsys.io.channels.ChannelReader;
import com.revolsys.raster.io.format.tiff.TiffDirectory;
import com.revolsys.raster.io.format.tiff.code.TiffFieldType;
import com.revolsys.raster.io.format.tiff.code.TiffTag;

public class TiffDirectoryEntryDouble extends AbstractTiffDirectoryEntrySingleOffset {

  private double value = Double.NaN;

  public TiffDirectoryEntryDouble(final TiffFieldType type, final TiffTag tag,
    final TiffDirectory directory) {
    super(type, tag, directory);
  }

  @Override
  public double getDouble() {
    loadValue();
    return this.value;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getValue() {
    return (V)(Object)getDouble();
  }

  @Override
  protected void loadValueDo(final ChannelReader in, final int count) {
    this.value = in.getDouble();
  }
}
