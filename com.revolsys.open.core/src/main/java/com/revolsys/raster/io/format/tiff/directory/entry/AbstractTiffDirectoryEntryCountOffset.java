package com.revolsys.raster.io.format.tiff.directory.entry;

import com.revolsys.io.channels.ChannelReader;
import com.revolsys.raster.io.format.tiff.TiffDirectory;
import com.revolsys.raster.io.format.tiff.code.TiffFieldType;
import com.revolsys.raster.io.format.tiff.code.TiffTag;

public abstract class AbstractTiffDirectoryEntryCountOffset extends AbstractTiffDirectoryEntry {

  protected long count;

  protected long offset;

  private final ChannelReader in;

  public AbstractTiffDirectoryEntryCountOffset(final TiffFieldType type, final TiffTag tag,
    final ChannelReader in, final long count, final long offset) {
    super(type, tag);
    this.in = in;
    this.count = count;
    this.offset = offset;
  }

  public AbstractTiffDirectoryEntryCountOffset(final TiffFieldType type, final TiffTag tag,
    final long count) {
    this(type, tag, null, count, -1);
  }

  public AbstractTiffDirectoryEntryCountOffset(final TiffFieldType type, final TiffTag tag,
    final TiffDirectory directory) {
    super(type, tag);
    this.in = directory.getIn();
    if (directory.isBigTiff()) {
      this.count = this.in.getLong();
      this.offset = this.in.getLong();
    } else {
      this.count = this.in.getUnsignedInt();
      this.offset = this.in.getUnsignedInt();
    }
  }

  public AbstractTiffDirectoryEntryCountOffset(final TiffFieldType type, final TiffTag tag,
    final TiffDirectory directory, final long count) {
    super(type, tag);
    this.in = directory.getIn();
    this.count = count;
    this.offset = directory.readOffsetOrCount();
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
  public void loadValue() {
    if (!isLoaded()) {
      this.in.seek(this.offset);
      loadValueDo(this.in, (int)this.count);
      this.offset = -1;
    }
  }

  protected void loadValueDo(final ChannelReader in, final int count) {
  }
}
