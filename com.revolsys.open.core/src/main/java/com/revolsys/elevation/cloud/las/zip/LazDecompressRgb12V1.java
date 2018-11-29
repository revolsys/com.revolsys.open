/*
 * Copyright 2007-2014, martin isenburg, rapidlasso - fast tools to catch reality
 *
 * This is free software; you can redistribute and/or modify it under the
 * terms of the GNU Lesser General Licence as published by the Free Software
 * Foundation. See the LICENSE.txt file for more information.
 *
 * This software is distributed WITHOUT ANY WARRANTY and without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package com.revolsys.elevation.cloud.las.zip;

import com.revolsys.elevation.cloud.las.pointformat.LasPoint;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingDecompressDecoder;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingDecompressInteger;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingDecompressModel;

public class LazDecompressRgb12V1 extends LazDecompressRgb12 {

  private final ArithmeticCodingDecompressInteger intDecompressor;

  public LazDecompressRgb12V1(final ArithmeticCodingDecompressDecoder decoder) {
    super(decoder);
    this.byteUsed = decoder.createSymbolModel(64);
    this.intDecompressor = new ArithmeticCodingDecompressInteger(decoder, 8, 6);
  }

  private int decompress(final int lastValue, final boolean lowChanged, final boolean highChanged) {
    int value;
    if (lowChanged) {
      value = this.intDecompressor.decompress(lastValue & 0xFF, 0);
    } else {
      value = lastValue & 0xFF;
    }
    if (highChanged) {
      value = this.intDecompressor.decompress(lastValue >> 8, 1) << 8;
    } else {
      value += lastValue & 0xFF00;
    }
    return value;
  }

  @Override
  public void init(final LasPoint firstPoint) {
    super.init(firstPoint);
    final ArithmeticCodingDecompressModel m = this.byteUsed;
    ArithmeticCodingDecompressDecoder r = this.decoder;
    m.reset();
    this.intDecompressor.reset();
  }

  @Override
  public void read(final LasPoint point) {
    final int sym = this.decoder.decodeSymbol(this.byteUsed);

    this.red = decompress(this.red, (sym & 1) != 0, (sym & 1 << 1) != 0);
    this.green = decompress(this.green, (sym & 1 << 2) != 0, (sym & 1 << 3) != 0);
    this.blue = decompress(this.blue, (sym & 1 << 4) != 0, (sym & 1 << 5) != 0);

    super.read(point);
  }
}
