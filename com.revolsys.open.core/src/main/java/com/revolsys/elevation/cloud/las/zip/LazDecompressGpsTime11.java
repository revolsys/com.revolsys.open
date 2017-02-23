package com.revolsys.elevation.cloud.las.zip;

import com.revolsys.elevation.cloud.las.pointformat.LasPoint;

public abstract class LazDecompressGpsTime11 implements LazDecompress {

  protected ArithmeticDecoder decoder;

  protected ArithmeticModel gpsTimeMulti;

  protected ArithmeticModel gpsTime0Diff;

  protected IntegerCompressor decompressGpsTime;

  public LazDecompressGpsTime11(final ArithmeticDecoder decoder) {
    this.decoder = decoder;
  }

  @Override
  public void init(final LasPoint firstPoint) {
    ArithmeticModel.initSymbolModel(this.gpsTimeMulti);
    ArithmeticModel.initSymbolModel(this.gpsTime0Diff);
    this.decompressGpsTime.initDecompressor();
  }
}
