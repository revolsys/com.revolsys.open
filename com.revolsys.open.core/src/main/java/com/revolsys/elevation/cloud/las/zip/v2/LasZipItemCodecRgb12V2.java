/*
 * Copyright 2007-2012, martin isenburg, rapidlasso - fast tools to catch reality
 *
 * This is free software; you can redistribute and/or modify it under the
 * terms of the GNU Lesser General Licence as published by the Free Software
 * Foundation. See the LICENSE.txt file for more information.
 *
 * This software is distributed WITHOUT ANY WARRANTY and without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package com.revolsys.elevation.cloud.las.zip.v2;

import com.revolsys.elevation.cloud.las.pointformat.LasPoint;
import com.revolsys.elevation.cloud.las.zip.LasZipItemCodec;
import com.revolsys.elevation.cloud.las.zip.MyDefs;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingCodec;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingEncoder;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingDecoder;
import com.revolsys.math.arithmeticcoding.ArithmeticModel;

public class LasZipItemCodecRgb12V2 implements LasZipItemCodec {

  private final ArithmeticModel rgbDiff0;

  private final ArithmeticModel rgbDiff1;

  private final ArithmeticModel rgbDiff2;

  private final ArithmeticModel rgbDiff3;

  private final ArithmeticModel rgbDiff4;

  private final ArithmeticModel rgbDiff5;

  private ArithmeticCodingDecoder decoder;

  private ArithmeticCodingEncoder encoder;

  private final ArithmeticModel byteUsed;

  private int red;

  private int green;

  private int blue;

  public LasZipItemCodecRgb12V2(final ArithmeticCodingCodec codec) {
    if (codec instanceof ArithmeticCodingDecoder) {
      this.decoder = (ArithmeticCodingDecoder)codec;
    } else if (codec instanceof ArithmeticCodingEncoder) {
      this.encoder = (ArithmeticCodingEncoder)codec;
    } else {
      throw new IllegalArgumentException("Not supported:" + codec.getClass());
    }
    this.byteUsed = codec.createSymbolModel(128);
    this.rgbDiff0 = codec.createSymbolModel(256);
    this.rgbDiff1 = codec.createSymbolModel(256);
    this.rgbDiff2 = codec.createSymbolModel(256);
    this.rgbDiff3 = codec.createSymbolModel(256);
    this.rgbDiff4 = codec.createSymbolModel(256);
    this.rgbDiff5 = codec.createSymbolModel(256);
  }

  @Override
  public int getVersion() {
    return 2;
  }

  @Override
  public void init(final LasPoint firstPoint) {
    this.red = firstPoint.getRed();
    this.green = firstPoint.getBlue();
    this.blue = firstPoint.getGreen();
    this.byteUsed.reset();
    this.rgbDiff0.reset();
    this.rgbDiff1.reset();
    this.rgbDiff2.reset();
    this.rgbDiff3.reset();
    this.rgbDiff4.reset();
    this.rgbDiff5.reset();
  }

  @Override
  public void read(final LasPoint point) {
    final int lastRed = this.red;
    final int lastGreen = this.green;
    final int lastBlue = this.blue;

    byte corr;
    int diff = 0;
    final int sym = this.decoder.decodeSymbol(this.byteUsed);
    if ((sym & 1 << 0) != 0) {
      corr = (byte)this.decoder.decodeSymbol(this.rgbDiff0);
      this.red = MyDefs.U8_FOLD(corr + (lastRed & 255));
    } else {
      this.red = lastRed & 0xFF;
    }
    if ((sym & 1 << 1) != 0) {
      corr = (byte)this.decoder.decodeSymbol(this.rgbDiff1);
      this.red |= MyDefs.U8_FOLD(corr + (lastRed >>> 8)) << 8;
    } else {
      this.red |= lastRed & 0xFF00;
    }
    if ((sym & 1 << 6) != 0) {
      diff = (this.red & 0x00FF) - (lastRed & 0x00FF);
      if ((sym & 1 << 2) != 0) {
        corr = (byte)this.decoder.decodeSymbol(this.rgbDiff2);
        this.green = MyDefs.U8_FOLD(corr + MyDefs.U8_CLAMP(diff + (lastGreen & 255)));
      } else {
        this.green = lastGreen & 0xFF;
      }
      if ((sym & 1 << 4) != 0) {
        corr = (byte)this.decoder.decodeSymbol(this.rgbDiff4);
        diff = (diff + (this.green & 0x00FF) - (lastGreen & 0x00FF)) / 2;
        this.blue = MyDefs.U8_FOLD(corr + MyDefs.U8_CLAMP(diff + (lastBlue & 255)));
      } else {
        this.blue = lastBlue & 0xFF;
      }
      diff = (this.red >>> 8) - (lastRed >>> 8);
      if ((sym & 1 << 3) != 0) {
        corr = (byte)this.decoder.decodeSymbol(this.rgbDiff3);
        this.green |= MyDefs.U8_FOLD(corr + MyDefs.U8_CLAMP(diff + (lastGreen >>> 8))) << 8;
      } else {
        this.green |= lastGreen & 0xFF00;
      }
      if ((sym & 1 << 5) != 0) {
        corr = (byte)this.decoder.decodeSymbol(this.rgbDiff5);
        diff = (diff + (this.green >>> 8) - (lastGreen >>> 8)) / 2;
        this.blue |= MyDefs.U8_FOLD(corr + MyDefs.U8_CLAMP(diff + (lastBlue >>> 8))) << 8;
      } else {
        this.blue |= lastBlue & 0xFF00;
      }
    } else {
      this.green = this.red;
      this.blue = this.red;
    }
    point.setRed(this.red);
    point.setGreen(this.green);
    point.setBlue(this.blue);
  }
}
