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
import com.revolsys.math.arithmeticcoding.ArithmeticDecoder;
import com.revolsys.math.arithmeticcoding.ArithmeticEncoder;
import com.revolsys.math.arithmeticcoding.ArithmeticModel;

public class LasZipItemCodecRgb12V2 implements LasZipItemCodec {

  private final ArithmeticModel diffRedLower;

  private final ArithmeticModel diffRedUpper;

  private final ArithmeticModel diffGreenLower;

  private final ArithmeticModel diffGreenUpper;

  private final ArithmeticModel diffBlueLower;

  private final ArithmeticModel diffBlueUpper;

  private ArithmeticDecoder decoder;

  private ArithmeticEncoder encoder;

  private final ArithmeticModel byteUsed;

  private int lastRedLower;

  private int lastRedUpper;

  private int lastGreenLower;

  private int lastGreenUpper;

  private int lastBlueLower;

  private int lastBlueUpper;

  public LasZipItemCodecRgb12V2(final ArithmeticCodingCodec codec) {
    if (codec instanceof ArithmeticDecoder) {
      this.decoder = (ArithmeticDecoder)codec;
    } else if (codec instanceof ArithmeticEncoder) {
      this.encoder = (ArithmeticEncoder)codec;
    } else {
      throw new IllegalArgumentException("Not supported:" + codec.getClass());
    }
    this.byteUsed = codec.createSymbolModel(128);
    this.diffRedLower = codec.createSymbolModel(256);
    this.diffRedUpper = codec.createSymbolModel(256);
    this.diffGreenLower = codec.createSymbolModel(256);
    this.diffGreenUpper = codec.createSymbolModel(256);
    this.diffBlueLower = codec.createSymbolModel(256);
    this.diffBlueUpper = codec.createSymbolModel(256);
  }

  @Override
  public int getVersion() {
    return 2;
  }

  @Override
  public int init(final LasPoint point, final int context) {
    final int red = point.getRed();
    final int green = point.getGreen();
    final int blue = point.getBlue();
    this.lastRedLower = red & 0xFF;
    this.lastRedUpper = red >>> 8;
    this.lastGreenLower = green & 0xFF;
    this.lastGreenUpper = green >>> 8;
    this.lastBlueLower = blue & 0xFF;
    this.lastBlueUpper = blue >>> 8;

    this.byteUsed.init();
    this.diffRedLower.init();
    this.diffRedUpper.init();
    this.diffGreenLower.init();
    this.diffGreenUpper.init();
    this.diffBlueLower.init();
    this.diffBlueUpper.init();
    return context;
  }

  @Override
  public int read(final LasPoint point, final int context) {
    int redLower = this.lastRedLower;
    int redUpper = this.lastRedUpper;
    int greenLower = this.lastGreenLower;
    int greenUpper = this.lastGreenUpper;
    int blueLower = this.lastBlueLower;
    int blueUpper = this.lastBlueUpper;

    int redLowerDiff = 0;
    final ArithmeticDecoder decoder = this.decoder;
    final int sym = decoder.decodeSymbol(this.byteUsed);
    if ((sym & 0b1) != 0) {
      final int corr = decoder.decodeSymbol(this.diffRedLower);
      redLower = U8_FOLD(redLower + corr);
      redLowerDiff = redLower - this.lastRedLower;
      this.lastRedLower = redLower;
    }

    int redUpperDiff = 0;
    if ((sym & 0b10) != 0) {
      final int corr = decoder.decodeSymbol(this.diffRedUpper);
      redUpper = U8_FOLD(redUpper + corr);
      redUpperDiff = redUpper - this.lastRedUpper;
      this.lastRedUpper = redUpper;
    }
    final int red = redUpper << 8 | redLower;

    if ((sym & 0b1000000) != 0) {
      int greenLowerDiff = 0;
      if ((sym & 0b100) != 0) {
        final int corr = decoder.decodeSymbol(this.diffGreenLower);
        greenLower = U8_FOLD(U8_CLAMP(redLowerDiff + greenLower) + corr);
        greenLowerDiff = (redLowerDiff + greenLower - this.lastGreenLower) / 2;
        this.lastGreenLower = greenLower;
      }

      if ((sym & 0b10000) != 0) {
        final int corr = decoder.decodeSymbol(this.diffBlueLower);
        blueLower = U8_FOLD(U8_CLAMP(greenLowerDiff + blueLower) + corr);
        this.lastBlueLower = blueLower;
      }

      int greenUpperDiff = 0;
      if ((sym & 0b1000) != 0) {
        final int corr = decoder.decodeSymbol(this.diffGreenUpper);
        greenUpper = U8_FOLD(U8_CLAMP(redUpperDiff + greenUpper) + corr);
        greenUpperDiff = (redUpperDiff + greenUpper - this.lastGreenUpper) / 2;
        this.lastGreenUpper = greenUpper;
      }

      if ((sym & 0b100000) != 0) {
        final int corr = decoder.decodeSymbol(this.diffBlueUpper);
        blueUpper = U8_FOLD(U8_CLAMP(greenUpperDiff + blueUpper) + corr);
        this.lastBlueUpper = blueUpper;
      }
      final int green = greenUpper << 8 | greenLower;
      final int blue = blueUpper << 8 | blueLower;
      point.setGreen(green);
      point.setBlue(blue);
    } else {
      point.setGreen(red);
      point.setBlue(red);
      this.lastGreenLower = redLower;
      this.lastGreenUpper = redUpper;
      this.lastBlueLower = redLower;
      this.lastBlueUpper = redUpper;
    }

    point.setRed(red);
    return context;
  }

  @Override
  public int write(final LasPoint point, final int context) {
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

    final ArithmeticEncoder encoder = this.encoder;
    encoder.encodeSymbol(this.byteUsed, sym);
    int redLowerDiff = 0;
    if (redLowerChanged) {
      redLowerDiff = redLower - this.lastRedLower;
      encoder.encodeSymbol(this.diffRedLower, U8_FOLD(redLowerDiff));
      this.lastRedLower = redLower;
    }
    int redUpperDiff = 0;
    if (redUpperChanged) {
      redUpperDiff = redUpper - this.lastRedUpper;
      encoder.encodeSymbol(this.diffRedUpper, U8_FOLD(redUpperDiff));
      this.lastRedUpper = redUpper;
    }
    if (redDiffFromGreenAndBlue) {
      int greenLowerDiff = 0;
      if (greenLowerChanged) {
        greenLowerDiff = (redLowerDiff + greenLower - this.lastGreenLower) / 2;
        final int corr = greenLower - U8_CLAMP(redLowerDiff + this.lastGreenLower);
        encoder.encodeSymbol(this.diffGreenLower, U8_FOLD(corr));
        this.lastGreenLower = greenLower;
      }
      if (blueLowerChanged) {
        final int corr = blueLower - U8_CLAMP(greenLowerDiff + this.lastBlueLower);
        encoder.encodeSymbol(this.diffBlueLower, U8_FOLD(corr));
        this.lastBlueLower = blueLower;
      }
      int greenUpperDiff = 0;
      if (greenUpperChanged) {
        greenUpperDiff = (redUpperDiff + greenUpper - this.lastGreenUpper) / 2;
        final int corr = greenUpper - U8_CLAMP(redUpperDiff + this.lastGreenUpper);
        encoder.encodeSymbol(this.diffGreenUpper, U8_FOLD(corr));
        this.lastGreenUpper = greenUpper;
      }
      if (blueUpperChanged) {
        final int corr = blueUpper - U8_CLAMP(greenUpperDiff + this.lastBlueUpper);
        encoder.encodeSymbol(this.diffBlueUpper, U8_FOLD(corr));
        this.lastBlueUpper = blueUpper;
      }
    } else {
      this.lastGreenLower = redLower;
      this.lastGreenUpper = redUpper;
      this.lastBlueLower = redLower;
      this.lastBlueUpper = redUpper;
    }
    return context;
  }

}
