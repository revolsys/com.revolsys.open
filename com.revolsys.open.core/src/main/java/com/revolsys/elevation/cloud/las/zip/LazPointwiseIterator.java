package com.revolsys.elevation.cloud.las.zip;

import com.revolsys.elevation.cloud.las.LasPointCloud;
import com.revolsys.elevation.cloud.las.LasPointCloudIterator;
import com.revolsys.elevation.cloud.las.LasZipHeader;
import com.revolsys.elevation.cloud.las.pointformat.LasPoint;
import com.revolsys.io.channels.ChannelReader;
import com.revolsys.util.Exceptions;

public class LazPointwiseIterator extends LasPointCloudIterator {

  private final ArithmeticDecoder decoder;

  private final LazDecompress[] pointDecompressors;

  public LazPointwiseIterator(final LasPointCloud pointCloud, final ChannelReader reader) {
    super(pointCloud, reader);
    this.decoder = new ArithmeticDecoder();
    final LasZipHeader lasZipHeader = pointCloud.getLasZipHeader();
    this.pointDecompressors = lasZipHeader.newLazDecompressors(pointCloud, this.decoder);
  }

  @Override
  protected LasPoint readNext() {
    try {
      LasPoint point;
      if (this.index == 0) {
        final ChannelReader reader = this.reader;
        point = this.pointFormat.readLasPoint(this.pointCloud, reader);
        for (final LazDecompress pointDecompressor : this.pointDecompressors) {
          pointDecompressor.init(point);
        }
        this.decoder.init(reader);
      } else {
        point = this.pointFormat.newLasPoint(this.pointCloud);
        for (final LazDecompress pointDecompressor : this.pointDecompressors) {
          pointDecompressor.read(point);
        }
      }
      return point;
    } catch (final Exception e) {
      close();
      throw Exceptions.wrap("Error decompressing: " + this.pointCloud.getResource(), e);
    }
  }

}
