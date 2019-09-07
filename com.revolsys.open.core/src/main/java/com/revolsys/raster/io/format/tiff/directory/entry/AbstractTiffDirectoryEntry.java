package com.revolsys.raster.io.format.tiff.directory.entry;

import org.jeometry.common.data.type.DataTypes;

import com.revolsys.io.channels.ChannelReader;
import com.revolsys.raster.io.format.tiff.TiffDirectory;
import com.revolsys.raster.io.format.tiff.TiffDirectoryEntry;
import com.revolsys.raster.io.format.tiff.code.TiffFieldType;
import com.revolsys.raster.io.format.tiff.code.TiffTag;

public abstract class AbstractTiffDirectoryEntry<A> implements TiffDirectoryEntry {

  protected A value;

  private final TiffTag tag;

  protected final long count;

  protected final long offset;

  public AbstractTiffDirectoryEntry(final TiffTag tag, final TiffDirectory directory,
    final ChannelReader in) {
    this.tag = tag;
    this.count = directory.readOffsetOrCount(in);
    final int maxInlineSize = directory.getMaxInlineSize();
    final long size = this.count * getValueSizeBytes();
    if (size <= maxInlineSize) {
      this.value = loadValueDo(in, (int)this.count);
      in.skipBytes((int)(maxInlineSize - size));
      this.offset = -1;
    } else {
      this.offset = directory.readOffsetOrCount(in);
    }
  }

  @Override
  public long getCount() {
    return this.count;
  }

  @Override
  public String getString() {
    final Object value = getValue();
    return DataTypes.toString(value);
  }

  @Override
  public TiffTag getTag() {
    return this.tag;
  }

  @SuppressWarnings("unchecked")
  @Override
  public final <V> V getValue() {
    return (V)this.value;
  }

  @Override
  public boolean isLoaded() {
    return this.value != null;
  }

  @Override
  public void loadValue(final ChannelReader in) {
    if (!isLoaded()) {
      in.seek(this.offset);
      this.value = loadValueDo(in, (int)this.count);
    }
  }

  protected abstract A loadValueDo(ChannelReader in, int count);

  @Override
  public String toString() {
    final StringBuilder s = new StringBuilder();
    final TiffTag tag = getTag();
    s.append(tag.name());
    s.append(" (");
    s.append(tag.getId());
    s.append(") ");

    final TiffFieldType type = getType();
    s.append(type);
    s.append(" (");
    s.append(type.getType());
    s.append(") ");

    final long count = getCount();
    s.append(count);
    s.append('<');
    if (isLoaded()) {
      if (isArray()) {
        for (int i = 0; i < count; i++) {
          if (i > 0) {
            s.append(' ');
          }
          if (type == TiffFieldType.UNDEFINED) {
            final byte b = getByte(i);
            final String hexString = Integer.toHexString(b & 0xff);
            s.append("0x");
            s.append(hexString);
          } else {
            final Number number = getNumber(i);
            s.append(DataTypes.toString(number));
          }
        }
      } else {
        final String string = getString();
        s.append(string);
      }
    } else {
      s.append("...");
    }
    s.append('>');
    return s.toString();
  }
}
