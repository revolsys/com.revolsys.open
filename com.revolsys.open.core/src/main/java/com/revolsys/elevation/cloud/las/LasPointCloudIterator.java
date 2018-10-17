package com.revolsys.elevation.cloud.las;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.revolsys.elevation.cloud.las.pointformat.LasPoint;
import com.revolsys.elevation.cloud.las.pointformat.LasPointFormat;
import com.revolsys.io.channels.ChannelReader;

public class LasPointCloudIterator implements Iterator<LasPoint>, Iterable<LasPoint> {

  private long index = 0;

  private long pointCount = 0;

  private final LasPointFormat pointFormat;

  private final ChannelReader reader;

  private final LasPointCloud pointCloud;

  public LasPointCloudIterator(final LasPointCloud pointCloud, final ChannelReader reader) {
    this.pointCloud = pointCloud;
    this.reader = reader;
    this.pointCount = pointCloud.getPointCount();
    this.pointFormat = pointCloud.getPointFormat();
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
      final LasPoint point = this.pointFormat.readLasPoint(this.pointCloud, this.reader);
      this.index++;
      return point;
    } else {
      throw new NoSuchElementException();
    }
  }

}
