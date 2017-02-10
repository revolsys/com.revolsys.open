package com.revolsys.elevation.cloud.las.zip;

import com.revolsys.elevation.cloud.las.LasPoint;

public abstract class LazDecompressGpsTime implements LazDecompress {

  protected ArithmeticDecoder decoder;

  protected ArithmeticModel gpsTimeMulti;

  protected ArithmeticModel gpsTime0Diff;

  protected IntegerCompressor decompressGpsTime;

  public LazDecompressGpsTime(final ArithmeticDecoder decoder) {
    this.decoder = decoder;
  }

  @Override
  public void init(final LasPoint firstPoint) {
    ArithmeticModel.initSymbolModel(this.gpsTimeMulti);
    ArithmeticModel.initSymbolModel(this.gpsTime0Diff);
    this.decompressGpsTime.initDecompressor();
  }
}
