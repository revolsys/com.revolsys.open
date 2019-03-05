package com.revolsys.elevation.cloud.las.zip;

import java.util.Iterator;

import com.revolsys.elevation.cloud.las.LasPointCloud;
import com.revolsys.elevation.cloud.las.LasPointCloudIterator;
import com.revolsys.elevation.cloud.las.pointformat.LasPoint;
import com.revolsys.io.channels.ChannelReader;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingDecoder;
import com.revolsys.util.Exceptions;

public class LasZipChunkedIterator extends LasPointCloudIterator {

  private final ArithmeticCodingDecoder decoder;

  private final LasZipItemCodec[] codecs;

  private final long chunkTableOffset;

  private final long chunkSize;

  private long chunkReadCount;

  public LasZipChunkedIterator(final LasPointCloud pointCloud, final ChannelReader reader) {
    super(pointCloud, reader);
    this.decoder = new ArithmeticCodingDecoder();
    final LasZipHeader lasZipHeader = LasZipHeader.getLasZipHeader(pointCloud);
    this.codecs = lasZipHeader.newLazCodecs(this.decoder);

    this.chunkTableOffset = reader.getLong();
    this.chunkSize = lasZipHeader.getChunkSize();
    this.chunkReadCount = this.chunkSize;
  }

  @Override
  public boolean hasNext() {
    return this.index < this.pointCount;
  }

  @Override
  public Iterator<LasPoint> iterator() {
    return this;
  }

  @Override
  protected LasPoint readNext() {
    try {
      LasPoint point;
      if (this.chunkSize == this.chunkReadCount) {
        point = this.pointFormat.readLasPoint(this.pointCloud, this.reader);
        for (final LasZipItemCodec pointDecompressor : this.codecs) {
          pointDecompressor.init(point);
        }
        this.decoder.init(this.reader);
        this.chunkReadCount = 0;
      } else {
        point = this.pointFormat.newLasPoint(this.pointCloud);
        for (final LasZipItemCodec pointDecompressor : this.codecs) {
          pointDecompressor.read(point);
        }
      }
      this.chunkReadCount++;
      return point;
    } catch (final Exception e) {
      close();
      throw Exceptions.wrap("Error decompressing: " + this.pointCloud.getResource(), e);
    }
  }

}
