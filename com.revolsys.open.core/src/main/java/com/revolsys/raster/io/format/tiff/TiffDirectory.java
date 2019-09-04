package com.revolsys.raster.io.format.tiff;

import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.nio.ByteOrder;
import java.util.LinkedHashMap;
import java.util.Map;

import com.revolsys.io.channels.ChannelReader;
import com.revolsys.raster.io.format.tiff.code.TiffBaselineTag;
import com.revolsys.raster.io.format.tiff.code.TiffCompression;
import com.revolsys.raster.io.format.tiff.code.TiffFieldType;
import com.revolsys.raster.io.format.tiff.code.TiffPhotogrametricInterpretation;
import com.revolsys.raster.io.format.tiff.code.TiffTag;
import com.revolsys.raster.io.format.tiff.code.TiffTags;
import com.revolsys.raster.io.format.tiff.image.TiffBilevelImage;
import com.revolsys.raster.io.format.tiff.image.TiffCmykImage;
import com.revolsys.raster.io.format.tiff.image.TiffGrayscaleImage;
import com.revolsys.raster.io.format.tiff.image.TiffImage;
import com.revolsys.raster.io.format.tiff.image.TiffJpegImage;
import com.revolsys.raster.io.format.tiff.image.TiffPaletteColorImage;
import com.revolsys.raster.io.format.tiff.image.TiffRgbFullColorImage;
import com.revolsys.raster.io.format.tiff.image.TiffTransparencyMaskImage;
import com.revolsys.raster.io.format.tiff.image.TiffYCbCrImage;
import com.revolsys.spring.resource.Resource;

public class TiffDirectory {

  private final Map<TiffTag, TiffDirectoryEntry> entryByTag = new LinkedHashMap<>();

  private final int index;

  private final long offset;

  private final long nextOffset;

  private final ChannelReader in;

  private WeakReference<TiffImage> imageReference = new WeakReference<TiffImage>(null);

  private final Resource resource;

  private final boolean bigTiff;

  public TiffDirectory(final boolean bigTiff, final Resource resource, final ChannelReader in,
    final int index, final long offset) {
    this.resource = resource;
    this.bigTiff = bigTiff;
    this.in = in;
    this.index = index;
    this.offset = offset;
    in.seek(offset);
    if (bigTiff) {
      final long recordCount = in.getLong();
      for (int i = 0; i < recordCount; i++) {
        final int tag = in.getUnsignedShort();
        final int type = in.getUnsignedShort();

        final TiffFieldType fieldType = TiffFieldType.valueByType(type);
        if (fieldType == null) {
          in.skipBytes(16);
        } else {
          final TiffTag tiffTag = TiffTags.getTag(tag);
          final TiffDirectoryEntry entry;
          if (tiffTag.isArray()) {
            entry = fieldType.newDirectoryEntryArray(tiffTag, this);
          } else {
            entry = fieldType.newDirectoryEntry(tiffTag, this);
          }

          this.entryByTag.put(tiffTag, entry);
        }
      }
      this.nextOffset = in.getLong();
    } else {
      final int recordCount = in.getUnsignedShort();
      for (int i = 0; i < recordCount; i++) {
        final int tag = in.getUnsignedShort();
        final int type = in.getUnsignedShort();

        final TiffFieldType fieldType = TiffFieldType.valueByType(type);
        if (fieldType == null) {
          in.skipBytes(8);
        } else {
          final TiffTag tiffTag = TiffTags.getTag(tag);
          final TiffDirectoryEntry entry;
          if (tiffTag.isArray()) {
            entry = fieldType.newDirectoryEntryArray(tiffTag, this);
          } else {
            entry = fieldType.newDirectoryEntry(tiffTag, this);
          }

          this.entryByTag.put(tiffTag, entry);
        }
      }
      this.nextOffset = in.getUnsignedInt();
    }
  }

  public void dump(final PrintStream out) {
    out.print("Directory ");
    out.print(this.index);
    out.print(": offset ");
    out.print(this.offset);
    out.print(" (0x");
    out.print(Long.toHexString(this.offset));
    out.print(") next ");
    out.print(this.nextOffset);
    out.print(" (");
    if (this.nextOffset != 0) {
      out.print("0x");
    }
    out.print(Long.toHexString(this.nextOffset));
    out.println(")");

    for (final TiffDirectoryEntry entry : this.entryByTag.values()) {
      entry.dump(out);
    }
  }

  public byte getByte(final TiffTag tag) {
    final TiffDirectoryEntry entry = getEntryRequired(tag);
    return entry.getByte();
  }

  public byte getByte(final TiffTag tag, final byte defaultValue) {
    final TiffDirectoryEntry entry = getEntry(tag);
    if (entry == null) {
      return defaultValue;
    } else {
      return entry.getByte();
    }
  }

  public byte[] getByteArray(final TiffTag tag) {
    final TiffDirectoryEntry entry = getEntryRequired(tag);
    return entry.getByteArray();
  }

  public byte[] getByteArray(final TiffTag tag, final byte[] defaultValue) {
    final TiffDirectoryEntry entry = getEntry(tag);
    if (entry == null) {
      return defaultValue;
    } else {
      return entry.getByteArray();
    }
  }

  public ByteOrder getByteOrder() {
    return this.in.getByteOrder();
  }

  private TiffCompression getCompression() {
    final int id = getInt(TiffBaselineTag.Compression, 1);
    return TiffCompression.getById(id);
  }

  public double getDouble(final TiffTag tag) {
    final TiffDirectoryEntry entry = getEntryRequired(tag);
    return entry.getDouble();
  }

  public double getDouble(final TiffTag tag, final double defaultValue) {
    final TiffDirectoryEntry entry = getEntry(tag);
    if (entry == null) {
      return defaultValue;
    } else {
      return entry.getDouble();
    }
  }

  public double[] getDoubleArray(final TiffTag tag) {
    final TiffDirectoryEntry entry = getEntryRequired(tag);
    return entry.getDoubleArray();
  }

  public double[] getDoubleArray(final TiffTag tag, final double[] defaultValue) {
    final TiffDirectoryEntry entry = getEntry(tag);
    if (entry == null) {
      return defaultValue;
    } else {
      return entry.getDoubleArray();
    }
  }

  public TiffDirectoryEntry getEntry(final TiffTag tag) {
    return this.entryByTag.get(tag);
  }

  public TiffDirectoryEntry getEntryRequired(final TiffTag tag) {
    final TiffDirectoryEntry entry = getEntry(tag);
    if (entry == null) {
      throw new IllegalArgumentException(tag + " not found in file: " + this.resource);
    }
    return entry;
  }

  public float getFloat(final TiffTag tag, final float defaultValue) {
    final TiffDirectoryEntry entry = getEntry(tag);
    if (entry == null) {
      return defaultValue;
    } else {
      return entry.getFloat();
    }
  }

  public TiffImage getImage() {
    TiffImage image = this.imageReference.get();
    if (image == null) {
      image = newImage();
      this.imageReference = new WeakReference<>(image);
    }
    return image;
  }

  public ChannelReader getIn() {
    return this.in;
  }

  public int getIndex() {
    return this.index;
  }

  public int getInt(final TiffTag tag) {
    final TiffDirectoryEntry entry = getEntryRequired(tag);
    return entry.getInt();
  }

  public int getInt(final TiffTag tag, final int defaultValue) {
    final TiffDirectoryEntry entry = getEntry(tag);
    if (entry == null) {
      return defaultValue;
    } else {
      return entry.getInt();
    }
  }

  public int[] getIntArray(final TiffTag tag) {
    final TiffDirectoryEntry entry = getEntryRequired(tag);
    return entry.getIntArray();
  }

  public int[] getIntArray(final TiffTag tag, final int[] defaultValue) {
    final TiffDirectoryEntry entry = getEntry(tag);
    if (entry == null) {
      return defaultValue;
    } else {
      return entry.getIntArray();
    }
  }

  public long getLong(final TiffTag tag) {
    final TiffDirectoryEntry entry = getEntryRequired(tag);
    return entry.getLong();
  }

  public long getLong(final TiffTag tag, final long defaultValue) {
    final TiffDirectoryEntry entry = getEntry(tag);
    if (entry == null) {
      return defaultValue;
    } else {
      return entry.getLong();
    }
  }

  public long[] getLongArray(final TiffTag tag) {
    final TiffDirectoryEntry entry = getEntryRequired(tag);
    return entry.getLongArray();
  }

  public long[] getLongArray(final TiffTag tag, final long[] defaultValue) {
    final TiffDirectoryEntry entry = getEntry(tag);
    if (entry == null) {
      return defaultValue;
    } else {
      return entry.getLongArray();
    }
  }

  public int getMaxInlineCount(final int dataSize) {
    if (this.bigTiff) {
      return 8 / dataSize;
    } else {
      return 4 / dataSize;
    }
  }

  public long getNextOffset() {
    return this.nextOffset;
  }

  public long getOffset() {
    return this.offset;
  }

  public TiffPhotogrametricInterpretation getPhotogrametricInterpretation() {
    final int id = getInt(TiffBaselineTag.PhotometricInterpretation);
    return TiffPhotogrametricInterpretation.getById(id);
  }

  public Resource getResource() {
    return this.resource;
  }

  public short getShort(final TiffTag tag, final short defaultValue) {
    final TiffDirectoryEntry entry = getEntry(tag);
    if (entry == null) {
      return defaultValue;
    } else {
      return entry.getShort();
    }
  }

  public String getString(final TiffTag tag, final String defaultValue) {
    final TiffDirectoryEntry entry = getEntry(tag);
    if (entry == null) {
      return defaultValue;
    } else {
      return entry.getString();
    }
  }

  public boolean isBigTiff() {
    return this.bigTiff;
  }

  public void loadValues() {
    for (final TiffDirectoryEntry entry : this.entryByTag.values()) {
      entry.loadValue();
    }
  }

  public TiffImage newImage() {

    final TiffPhotogrametricInterpretation photometricInterpretation = getPhotogrametricInterpretation();
    final TiffCompression compression = getCompression();
    if (compression == TiffCompression.JPEG) {
      return new TiffJpegImage(this);
    } else {
      switch (photometricInterpretation) {
        case MIN_IS_WHITE: // 0
        case MIN_IS_BLACK: // 1
          final int bitsPerSample = getInt(TiffBaselineTag.BitsPerSample);
          if (bitsPerSample == 1) {
            return new TiffBilevelImage(this);
          } else {
            return new TiffGrayscaleImage(this);
          }
        case RGB: // 2
          return new TiffRgbFullColorImage(this);
        case PALETTE: // 3
          return new TiffPaletteColorImage(this);
        case MASK: // 4
          return new TiffTransparencyMaskImage(this);
        case CMYK: // 5
          return new TiffCmykImage(this);
        case YCBCR: // 6
          return new TiffYCbCrImage(this);
        default:
          throw new IllegalArgumentException(
            "PhotometricInterpretation=" + photometricInterpretation + " not yet supported");
      }
    }
  }

  public long readOffsetOrCount() {
    if (this.bigTiff) {
      return this.in.getUnsignedLong();
    } else {
      return this.in.getUnsignedInt();
    }
  }

  protected RuntimeException throwRequired(final TiffTag tag) {
    return new IllegalArgumentException(tag + " not found in file");
  }

  @Override
  public String toString() {
    return "TiffDirectory " + this.index + ": " + this.resource;
  }
}
