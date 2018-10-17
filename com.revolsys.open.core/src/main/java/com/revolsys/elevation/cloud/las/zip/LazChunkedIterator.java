package com.revolsys.elevation.cloud.las.zip;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.revolsys.elevation.cloud.las.LasPointCloud;
import com.revolsys.elevation.cloud.las.LasZipHeader;
import com.revolsys.elevation.cloud.las.pointformat.LasPoint;
import com.revolsys.elevation.cloud.las.pointformat.LasPointFormat;
import com.revolsys.io.channels.ChannelReader;

public class LazChunkedIterator implements Iterator<LasPoint>, Iterable<LasPoint> {

  private long index = 0;

  private long pointCount = 0;

  private final ArithmeticDecoder decoder;

  private final LazDecompress[] pointDecompressors;

  private final LasPointFormat pointFormat;

  private final ChannelReader reader;

  private final LasPointCloud pointCloud;

  private final long chunkTableOffset;

  private final long chunkSize;

  private long chunkReadCount;

  public LazChunkedIterator(final LasPointCloud pointCloud, final ChannelReader reader) {
    this.pointCloud = pointCloud;
    this.reader = reader;
    this.pointCount = pointCloud.getPointCount();
    this.decoder = new ArithmeticDecoder();
    final LasZipHeader lasZipHeader = pointCloud.getLasZipHeader();
    this.pointDecompressors = lasZipHeader.newLazDecompressors(pointCloud, this.decoder);
    this.pointFormat = pointCloud.getPointFormat();

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
  public LasPoint next() {
    if (this.index < this.pointCount) {
      LasPoint point;
      if (this.chunkSize == this.chunkReadCount) {
        point = this.pointFormat.readLasPoint(this.pointCloud, this.reader);
        for (final LazDecompress pointDecompressor : this.pointDecompressors) {
          pointDecompressor.init(point);
        }
        this.decoder.reset();
        this.chunkReadCount = 0;
      } else {
        point = this.pointFormat.newLasPoint(this.pointCloud);
        for (final LazDecompress pointDecompressor : this.pointDecompressors) {
          pointDecompressor.read(point);
        }
      }
      this.chunkReadCount++;
      this.index++;
      return point;
    } else {
      throw new NoSuchElementException();
    }
  }

}
