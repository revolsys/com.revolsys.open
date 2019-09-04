package com.revolsys.raster.io.format.tiff.directory.entry;

import org.jeometry.common.data.type.DataTypes;

import com.revolsys.raster.io.format.tiff.TiffDirectoryEntry;
import com.revolsys.raster.io.format.tiff.code.TiffFieldType;
import com.revolsys.raster.io.format.tiff.code.TiffTag;

public abstract class AbstractTiffDirectoryEntry implements TiffDirectoryEntry {

  private final TiffTag tag;

  private final TiffFieldType type;

  public AbstractTiffDirectoryEntry(final TiffFieldType type, final TiffTag tag) {
    this.type = type;
    this.tag = tag;
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

  @Override
  public TiffFieldType getType() {
    return this.type;
  }

  @Override
  public String toString() {
    final StringBuilder s = new StringBuilder();
    s.append(this.tag.name());
    s.append(" (");
    s.append(this.tag.getId());
    s.append(") ");
    s.append(this.type);
    s.append(" (");
    s.append(this.type.getType());
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
          if (this.type == TiffFieldType.UNDEFINED) {
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
