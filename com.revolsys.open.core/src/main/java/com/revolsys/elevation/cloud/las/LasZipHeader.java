package com.revolsys.elevation.cloud.las;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.function.BiFunction;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.cloud.las.zip.ArithmeticDecoder;
import com.revolsys.elevation.cloud.las.zip.LazDecompress;
import com.revolsys.elevation.cloud.las.zip.LazDecompressGpsTime11V1;
import com.revolsys.elevation.cloud.las.zip.LazDecompressGpsTime11V2;
import com.revolsys.elevation.cloud.las.zip.LazDecompressPoint10V1;
import com.revolsys.elevation.cloud.las.zip.LazDecompressPoint10V2;
import com.revolsys.elevation.cloud.las.zip.LazDecompressRgb12V1;
import com.revolsys.elevation.cloud.las.zip.LazDecompressRgb12V2;
import com.revolsys.elevation.cloud.las.zip.LazItemType;
import com.revolsys.io.Buffers;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.util.Exceptions;
import com.revolsys.util.Pair;

public class LasZipHeader implements MapSerializer {
  public static final Version VERSION_1_0 = new Version(1, 0);

  public static final byte LASZIP_COMPRESSOR_POINTWISE = 1;

  public static final byte LASZIP_COMPRESSOR_POINTWISE_CHUNKED = 2;

  public static final byte LASZIP_COMPRESSOR_TOTAL_NUMBER_OF = 3;

  private static final String LAS_ZIP = "laszip encoded";

  private static final int LAS_ZIP_TAG = 22204;

  public static void init(
    final Map<Pair<String, Integer>, BiFunction<LasPointCloudHeader, byte[], Object>> vlrfactory) {
    vlrfactory.put(new Pair<>(LAS_ZIP, LAS_ZIP_TAG), LasZipHeader::newLasZipHeader);
  }

  private static LasZipHeader newLasZipHeader(final LasPointCloudHeader header,
    final byte[] bytes) {
    try {
      final LasZipHeader lasZipHeader = new LasZipHeader(bytes);
      header.setLasZipHeader(lasZipHeader);
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

  private LasZipHeader(final byte[] bytes) throws IOException {
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

  public int getVersion(final int i) {
    return this.versions[i];
  }

  public int[] getVersions() {
    return this.versions;
  }

  public boolean isCompressor(final byte compressor) {
    return this.compressor == compressor;
  }

  public LazDecompress[] newLazDecompressors(final LasPointCloud pointCloud,
    final ArithmeticDecoder decoder) {
    final int numItems = this.getNumItems();
    final LazDecompress[] pointDecompressors = new LazDecompress[numItems];
    for (int i = 0; i < numItems; i++) {
      final LazItemType type = this.getType(i);
      final int version = this.getVersion(i);
      if (version < 1 || version > 2) {
        throw new RuntimeException(version + " not yet supported");
      }
      switch (type) {
        case POINT10:
          if (version == 1) {
            pointDecompressors[i] = new LazDecompressPoint10V1(pointCloud, decoder);
          } else {
            pointDecompressors[i] = new LazDecompressPoint10V2(pointCloud, decoder);
          }
        break;
        case GPSTIME11:
          if (version == 1) {
            pointDecompressors[i] = new LazDecompressGpsTime11V1(decoder);
          } else {
            pointDecompressors[i] = new LazDecompressGpsTime11V2(decoder);
          }
        break;
        case RGB12:
          if (version == 1) {
            pointDecompressors[i] = new LazDecompressRgb12V1(decoder);
          } else {
            pointDecompressors[i] = new LazDecompressRgb12V2(decoder);
          }
        break;

        default:
          throw new RuntimeException(type + " not yet supported");
      }
    }
    return pointDecompressors;
  }

  @Override
  public MapEx toMap() {
    final MapEx map = new LinkedHashMapEx();
    addToMap(map, "compressor", this.compressor);
    addToMap(map, "coder", this.coder);
    addToMap(map, "version", this.version);
    addToMap(map, "versionRevision", this.versionRevision);
    addToMap(map, "options", this.options);
    addToMap(map, "chunkSize", this.chunkSize);
    addToMap(map, "numberOfSpecialEvlrs", this.numberOfSpecialEvlrs);
    addToMap(map, "offsetToSpecialEvlrs", this.offsetToSpecialEvlrs);
    addToMap(map, "numItems", this.numItems);
    addToMap(map, "types", this.types);
    addToMap(map, "sizes", this.sizes);
    addToMap(map, "versions", this.versions);

    return map;
  }
}
