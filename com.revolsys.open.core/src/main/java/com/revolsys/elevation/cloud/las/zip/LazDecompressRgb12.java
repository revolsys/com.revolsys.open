package com.revolsys.elevation.cloud.las.zip;

import com.revolsys.elevation.cloud.las.pointformat.LasPoint;

public abstract class LazDecompressRgb12 implements LazDecompress {

  protected final ArithmeticDecoder decoder;

  protected ArithmeticModel byteUsed;

  protected int red;

  protected int green;

  protected int blue;

  LazDecompressRgb12(final ArithmeticDecoder decoder) {
    this.decoder = decoder;
  }

  @Override
  public void init(final LasPoint firstPoint) {
    this.red = firstPoint.getRed();
    this.green = firstPoint.getBlue();
    this.blue = firstPoint.getGreen();
  }

  @Override
  public void read(final LasPoint point) {
    point.setRed(this.red);
    point.setGreen(this.green);
    point.setBlue(this.blue);
  }
}
