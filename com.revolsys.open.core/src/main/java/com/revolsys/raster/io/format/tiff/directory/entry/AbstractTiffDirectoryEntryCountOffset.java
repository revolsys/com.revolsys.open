package com.revolsys.raster.io.format.tiff.directory.entry;

import com.revolsys.io.channels.ChannelReader;
import com.revolsys.raster.io.format.tiff.TiffDirectory;
import com.revolsys.raster.io.format.tiff.code.TiffFieldType;
import com.revolsys.raster.io.format.tiff.code.TiffTag;

public abstract class AbstractTiffDirectoryEntryCountOffset extends AbstractTiffDirectoryEntry {

  protected long count;

  protected long offset;

  public AbstractTiffDirectoryEntryCountOffset(final TiffFieldType type, final TiffTag tag,
    final long count, final long offset) {
    super(type, tag);
    this.count = count;
    this.offset = offset;
  }

  public AbstractTiffDirectoryEntryCountOffset(final TiffFieldType type, final TiffTag tag,
    final long count) {
    this(type, tag, count, -1);
  }

  public AbstractTiffDirectoryEntryCountOffset(final TiffFieldType type, final TiffTag tag,
    final TiffDirectory directory, final ChannelReader in) {
    super(type, tag);
    this.count = directory.readOffsetOrCount(in);
    this.offset = directory.readOffsetOrCount(in);
  }

  public AbstractTiffDirectoryEntryCountOffset(final TiffFieldType type, final TiffTag tag,
    final TiffDirectory directory, final ChannelReader in, final long count) {
    super(type, tag);
    this.count = count;
    this.offset = directory.readOffsetOrCount(in);
  }

  @Override
  public long getCount() {
    return this.count;
  }

  @Override
  public boolean isLoaded() {
    return this.offset == -1;
  }

  @Override
  public void loadValue(final ChannelReader in) {
    if (!isLoaded()) {
      in.seek(this.offset);
      loadValueDo(in, (int)this.count);
      this.offset = -1;
    }
  }

  protected void loadValueDo(final ChannelReader in, final int count) {
  }
}
