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
import com.revolsys.math.arithmeticcoding.ArithmeticCodingCodec;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingDecoder;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingEncoder;
import com.revolsys.math.arithmeticcoding.ArithmeticModel;

public class LasZipItemCodecRgb12V2 implements LasZipItemCodec {

  private final ArithmeticModel diffRedLower;

  private final ArithmeticModel diffRedUpper;

  private final ArithmeticModel diffGreenLowe;

  private final ArithmeticModel diffGreenUpper;

  private final ArithmeticModel diffBlueLower;

  private final ArithmeticModel diffBlueUpper;

  private ArithmeticCodingDecoder decoder;

  private ArithmeticCodingEncoder encoder;

  private final ArithmeticModel byteUsed;

  private int red;

  private int green;

  private int blue;

  private int lastRedLower;

  private int lastRedUpper;

  private int lastGreenLower;

  private int lastGreenUpper;

  private int lastBlueLower;

  private int lastBlueUpper;

  public LasZipItemCodecRgb12V2(final ArithmeticCodingCodec codec) {
    if (codec instanceof ArithmeticCodingDecoder) {
      this.decoder = (ArithmeticCodingDecoder)codec;
    } else if (codec instanceof ArithmeticCodingEncoder) {
      this.encoder = (ArithmeticCodingEncoder)codec;
    } else {
      throw new IllegalArgumentException("Not supported:" + codec.getClass());
    }
    this.byteUsed = codec.createSymbolModel(128);
    this.diffRedLower = codec.createSymbolModel(256);
    this.diffRedUpper = codec.createSymbolModel(256);
    this.diffGreenLowe = codec.createSymbolModel(256);
    this.diffGreenUpper = codec.createSymbolModel(256);
    this.diffBlueLower = codec.createSymbolModel(256);
    this.diffBlueUpper = codec.createSymbolModel(256);
  }

  @Override
  public int getVersion() {
    return 2;
  }

  @Override
  public void init(final LasPoint point) {
    this.red = point.getRed();
    this.green = point.getBlue();
    this.blue = point.getGreen();

    final int red = point.getRed();
    final int green = point.getGreen();
    final int blue = point.getBlue();
    this.lastRedLower = red & 0xFF;
    this.lastRedUpper = red >>> 8;
    this.lastGreenLower = green & 0xFF;
    this.lastGreenUpper = green >>> 8;
    this.lastBlueLower = blue & 0xFF;
    this.lastBlueUpper = blue >>> 8;

    this.byteUsed.reset();
    this.diffRedLower.reset();
    this.diffRedUpper.reset();
    this.diffGreenLowe.reset();
    this.diffGreenUpper.reset();
    this.diffBlueLower.reset();
    this.diffBlueUpper.reset();
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
      corr = (byte)this.decoder.decodeSymbol(this.diffRedLower);
      this.red = LasZipItemCodec.U8_FOLD(corr + (lastRed & 255));
    } else {
      this.red = lastRed & 0xFF;
    }
    if ((sym & 1 << 1) != 0) {
      corr = (byte)this.decoder.decodeSymbol(this.diffRedUpper);
      this.red |= LasZipItemCodec.U8_FOLD(corr + (lastRed >>> 8)) << 8;
    } else {
      this.red |= lastRed & 0xFF00;
    }
    if ((sym & 1 << 6) != 0) {
      diff = (this.red & 0x00FF) - (lastRed & 0x00FF);
      if ((sym & 1 << 2) != 0) {
        corr = (byte)this.decoder.decodeSymbol(this.diffGreenLowe);
        this.green = LasZipItemCodec.U8_FOLD(corr + LasZipItemCodec.U8_CLAMP(diff + (lastGreen & 255)));
      } else {
        this.green = lastGreen & 0xFF;
      }
      if ((sym & 1 << 4) != 0) {
        corr = (byte)this.decoder.decodeSymbol(this.diffBlueLower);
        diff = (diff + (this.green & 0x00FF) - (lastGreen & 0x00FF)) / 2;
        this.blue = LasZipItemCodec.U8_FOLD(corr + LasZipItemCodec.U8_CLAMP(diff + (lastBlue & 255)));
      } else {
        this.blue = lastBlue & 0xFF;
      }
      diff = (this.red >>> 8) - (lastRed >>> 8);
      if ((sym & 1 << 3) != 0) {
        corr = (byte)this.decoder.decodeSymbol(this.diffGreenUpper);
        this.green |= LasZipItemCodec.U8_FOLD(corr + LasZipItemCodec.U8_CLAMP(diff + (lastGreen >>> 8))) << 8;
      } else {
        this.green |= lastGreen & 0xFF00;
      }
      if ((sym & 1 << 5) != 0) {
        corr = (byte)this.decoder.decodeSymbol(this.diffBlueUpper);
        diff = (diff + (this.green >>> 8) - (lastGreen >>> 8)) / 2;
        this.blue |= LasZipItemCodec.U8_FOLD(corr + LasZipItemCodec.U8_CLAMP(diff + (lastBlue >>> 8))) << 8;
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

  @Override
  public void write(final LasPoint point) {
    final int red = point.getRed();
    final int green = point.getGreen();
    final int blue = point.getBlue();
    final int redLower = red & 0xFF;
    final int redUpper = red >>> 8;
    final int greenLower = green & 0xFF;
    final int greenUpper = green >>> 8;
    final int blueLower = blue & 0xFF;
    final int blueUpper = blue >>> 8;

    int sym = 0;
    final boolean redLowerChanged = this.lastRedLower != redLower;
    if (redLowerChanged) {
      sym |= 0b1;
    }
    final boolean redUpperChanged = this.lastRedUpper != redUpper;
    if (redUpperChanged) {
      sym |= 0b10;
    }
    final boolean greenLowerChanged = this.lastGreenLower != greenLower;
    if (greenLowerChanged) {
      sym |= 0b100;
    }
    final boolean greenUpperChanged = this.lastGreenUpper != greenUpper;
    if (greenUpperChanged) {
      sym |= 0b1000;
    }
    final boolean blueLowerChanged = this.lastBlueLower != blueLower;
    if (blueLowerChanged) {
      sym |= 0b10000;
    }
    final boolean blueUpperChanged = this.lastBlueUpper != blueUpper;
    if (blueUpperChanged) {
      sym |= 0b100000;
    }
    final boolean redDiffFromGreenAndBlue = redLower != greenLower || redLower != blueLower
      || redUpper != greenUpper || redUpper != blueUpper;
    if (redDiffFromGreenAndBlue) {
      sym |= 0b1000000;
    }

    this.encoder.encodeSymbol(this.byteUsed, sym);
    int diffLower = 0;
    if (redLowerChanged) {
      diffLower = redLower - this.lastRedLower;
      this.encoder.encodeSymbol(this.diffRedLower, LasZipItemCodec.U8_FOLD(diffLower));
      this.lastRedLower = redLower;
    }
    int diffUpper = 0;
    if (redUpperChanged) {
      diffUpper = redUpper - this.lastRedUpper;
      this.encoder.encodeSymbol(this.diffRedUpper, LasZipItemCodec.U8_FOLD(diffUpper));
      this.lastRedUpper = redUpper;
    }
    if (redDiffFromGreenAndBlue) {
      if (greenLowerChanged) {
        final int corr = greenLower - LasZipItemCodec.U8_CLAMP(diffLower + this.lastGreenLower);
        this.encoder.encodeSymbol(this.diffGreenLowe, LasZipItemCodec.U8_FOLD(corr));
      }
      if (blueLowerChanged) {
        diffLower = (diffLower + greenLower - this.lastGreenLower) / 2;
        final int corr = blueLower - LasZipItemCodec.U8_CLAMP(diffLower + this.lastBlueLower);
        this.encoder.encodeSymbol(this.diffBlueLower, LasZipItemCodec.U8_FOLD(corr));
        this.lastBlueLower = blueLower;
      }
      this.lastGreenUpper = greenUpper;
      if (greenUpperChanged) {
        final int corr = greenUpper - LasZipItemCodec.U8_CLAMP(diffUpper + this.lastGreenUpper);
        this.encoder.encodeSymbol(this.diffGreenUpper, LasZipItemCodec.U8_FOLD(corr));
      }
      if (blueUpperChanged) {
        diffUpper = (diffUpper + greenUpper - this.lastGreenUpper) / 2;
        final int corr = blueUpper - LasZipItemCodec.U8_CLAMP(diffUpper + this.lastBlueUpper);
        this.encoder.encodeSymbol(this.diffBlueUpper, LasZipItemCodec.U8_FOLD(corr));
        this.lastBlueUpper = blueUpper;
      }
      this.lastGreenUpper = greenUpper;
    } else {
      this.lastGreenLower = greenLower;
      this.lastGreenUpper = greenUpper;
      this.lastBlueLower = blueLower;
      this.lastBlueUpper = blueUpper;

    }
  }

}
