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
package com.revolsys.elevation.cloud.las.zip.v1;

import com.revolsys.elevation.cloud.las.pointformat.LasPoint;
import com.revolsys.elevation.cloud.las.zip.LasZipItemCodec;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingCodec;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingEncoder;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingDecoder;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingInteger;
import com.revolsys.math.arithmeticcoding.ArithmeticModel;

public class LasZipItemCodecRgb12V1 implements LasZipItemCodec {

  private final ArithmeticCodingInteger intDecompressor;

  private ArithmeticCodingDecoder decoder;

  private ArithmeticCodingEncoder encoder;

  private final ArithmeticModel byteUsed;

  private int red;

  private int green;

  private int blue;

  public LasZipItemCodecRgb12V1(final ArithmeticCodingCodec codec) {
    if (codec instanceof ArithmeticCodingDecoder) {
      this.decoder = (ArithmeticCodingDecoder)codec;
    } else if (codec instanceof ArithmeticCodingEncoder) {
      this.encoder = (ArithmeticCodingEncoder)codec;
    } else {
      throw new IllegalArgumentException("Not supported:" + codec.getClass());
    }
    this.byteUsed = codec.createSymbolModel(64);
    this.intDecompressor = codec.newCodecInteger(8, 6);
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
    this.red = firstPoint.getRed();
    this.green = firstPoint.getBlue();
    this.blue = firstPoint.getGreen();
    this.byteUsed.reset();
    this.intDecompressor.init();
  }

  @Override
  public void read(final LasPoint point) {
    final int sym = this.decoder.decodeSymbol(this.byteUsed);

    this.red = decompress(this.red, (sym & 1) != 0, (sym & 1 << 1) != 0);
    this.green = decompress(this.green, (sym & 1 << 2) != 0, (sym & 1 << 3) != 0);
    this.blue = decompress(this.blue, (sym & 1 << 4) != 0, (sym & 1 << 5) != 0);

    point.setRed(this.red);
    point.setGreen(this.green);
    point.setBlue(this.blue);
  }
}
