package com.revolsys.elevation.cloud.las;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.function.BiFunction;

import com.revolsys.elevation.cloud.las.zip.LazItemType;
import com.revolsys.io.Buffers;
import com.revolsys.util.Exceptions;
import com.revolsys.util.Pair;

public class LasZipHeader {
  public static final Version VERSION_1_0 = new Version(1, 0);

  public static final byte LASZIP_COMPRESSOR_POINTWISE = 1;

  public static final byte LASZIP_COMPRESSOR_POINTWISE_CHUNKED = 2;

  public static final byte LASZIP_COMPRESSOR_TOTAL_NUMBER_OF = 3;

  private static final String LAS_ZIP = "laszip encoded";

  private static final int LAS_ZIP_TAG = 22204;

  public static void init(
    final Map<Pair<String, Integer>, BiFunction<LasPointCloud, byte[], Object>> vlrfactory) {
    vlrfactory.put(new Pair<>(LAS_ZIP, LAS_ZIP_TAG), LasZipHeader::newLasZipHeader);
  }

  private static LasZipHeader newLasZipHeader(final LasPointCloud lasPointCloud,
    final byte[] bytes) {
    try {
      final LasZipHeader lasZipHeader = new LasZipHeader(lasPointCloud, bytes);
      lasPointCloud.setLasZipHeader(lasZipHeader);
      return lasZipHeader;
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  private final int compressor;

  private final int coder;

  private final int versionRevision;

  private final long options;

  private final long chunkSize;

  private final long numberOfSpecialEvlrs;

  private final long offsetToSpecialEvlrs;

  private final int numItems;

  private final LazItemType[] types;

  private final int[] sizes;

  private final int[] versions;

  private final Version version;

  private LasZipHeader(final LasPointCloud lasPointCloud, final byte[] bytes) throws IOException {
    final ByteBuffer buffer = ByteBuffer.wrap(bytes);
    this.compressor = Buffers.getLEUnsignedShort(buffer);
    this.coder = Buffers.getLEUnsignedShort(buffer);
    final short versionMajor = Buffers.getUnsignedByte(buffer);
    final short versionMinor = Buffers.getUnsignedByte(buffer);
    this.version = new Version(versionMajor, versionMinor);
    this.versionRevision = Buffers.getLEUnsignedShort(buffer);
    this.options = Buffers.getLEUnsignedInt(buffer);
    this.chunkSize = Buffers.getLEUnsignedInt(buffer);
    this.numberOfSpecialEvlrs = Buffers.getLEUnsignedLong(buffer);
    this.offsetToSpecialEvlrs = Buffers.getLEUnsignedLong(buffer);
    this.numItems = Buffers.getLEUnsignedShort(buffer);
    this.types = new LazItemType[this.numItems];
    this.sizes = new int[this.numItems];
    this.versions = new int[this.numItems];
    for (int i = 0; i < this.numItems; i++) {
      this.types[i] = LazItemType.fromOrdinal(Buffers.getLEUnsignedShort(buffer));
      this.sizes[i] = Buffers.getLEUnsignedShort(buffer);
      this.versions[i] = Buffers.getLEUnsignedShort(buffer);
    }
  }

  public long getChunkSize() {
    return this.chunkSize;
  }

  public int getCompressor() {
    return this.compressor;
  }

  public int getNumItems() {
    return this.numItems;
  }

  public int getSize(final int i) {
    return this.sizes[i];
  }

  public int[] getSizes() {
    return this.sizes;
  }

  public LazItemType getType(final int i) {
    return this.types[i];
  }

  public LazItemType[] getTypes() {
    return this.types;
  }

  public Version getVersion() {
    return this.version;
  }

  public int[] getVersions() {
    return this.versions;
  }

  public int getVersion(final int i) {
    return this.versions[i];
  }

  public boolean isCompressor(final byte compressor) {
    return this.compressor == compressor;
  }
}
