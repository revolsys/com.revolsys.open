package com.revolsys.elevation.cloud.las.zip;

import com.revolsys.elevation.cloud.las.LasPoint;

public abstract class LazDecompressGpsTime implements LazDecompress {

  protected ArithmeticDecoder decoder;

  protected ArithmeticModel gpstimeMulti;

  protected ArithmeticModel gpstime0Diff;

  protected IntegerCompressor decompressGpstime;

  public LazDecompressGpsTime(final ArithmeticDecoder dec) {
    this.decoder = dec;
  }

  @Override
  public void init(final LasPoint firstPoint) {
    ArithmeticModel.initSymbolModel(this.gpstimeMulti);
    ArithmeticModel.initSymbolModel(this.gpstime0Diff);
    this.decompressGpstime.initDecompressor();
  }
}
