package com.revolsys.elevation.cloud.las.zip.context;

import com.revolsys.elevation.cloud.las.pointformat.LasPoint;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingCodec;
import com.revolsys.math.arithmeticcoding.ArithmeticDecoder;
import com.revolsys.math.arithmeticcoding.ArithmeticEncoder;
import com.revolsys.math.arithmeticcoding.ArithmeticModel;
import com.revolsys.util.number.Integers;

public class LasZipContextRgb {
  public boolean unused;

  public LasPoint lastPoint;

  private ArithmeticModel rgbBytesUsed;

  private ArithmeticModel diffRedLower;

  private ArithmeticModel diffRedUpper;

  private ArithmeticModel diffGreenLower;

  private ArithmeticModel diffGreenUpper;

  private ArithmeticModel diffBlueUpper;

  private ArithmeticModel diffBlueLower;

  private int lastRedLower;

  private int lastRedUpper;

  private int lastGreenLower;

  private int lastGreenUpper;

  private int lastBlueLower;

  private int lastBlueUpper;

  public void initPoint(final ArithmeticCodingCodec codec, final LasPoint point) {
    this.unused = false;
    if (this.rgbBytesUsed == null) {
      this.rgbBytesUsed = codec.createSymbolModel(128);
      this.diffRedLower = codec.createSymbolModel(256);
      this.diffRedUpper = codec.createSymbolModel(256);
      this.diffGreenLower = codec.createSymbolModel(256);
      this.diffGreenUpper = codec.createSymbolModel(256);
      this.diffBlueLower = codec.createSymbolModel(256);
      this.diffBlueUpper = codec.createSymbolModel(256);
    } else {
      this.rgbBytesUsed.init();
      this.diffRedLower.init();
      this.diffRedUpper.init();
      this.diffGreenLower.init();
      this.diffGreenUpper.init();
      this.diffBlueLower.init();
      this.diffBlueUpper.init();
    }

    setLastPoint(point);
  }

  public void readRgb(final ArithmeticDecoder decoder, final LasPoint point) {
    int redLower = this.lastRedLower;
    int redUpper = this.lastRedUpper;
    int greenLower = this.lastGreenLower;
    int greenUpper = this.lastGreenUpper;
    int blueLower = this.lastBlueLower;
    int blueUpper = this.lastBlueUpper;

    int redLowerDiff = 0;
    final int sym = decoder.decodeSymbol(this.rgbBytesUsed);
    if ((sym & 0b1) != 0) {
      final int corr = decoder.decodeSymbol(this.diffRedLower);
      redLower = Integers.U8_FOLD(redLower + corr);
      redLowerDiff = redLower - this.lastRedLower;
      this.lastRedLower = redLower;
    }

    int redUpperDiff = 0;
    if ((sym & 0b10) != 0) {
      final int corr = decoder.decodeSymbol(this.diffRedUpper);
      redUpper = Integers.U8_FOLD(redUpper + corr);
      redUpperDiff = redUpper - this.lastRedUpper;
      this.lastRedUpper = redUpper;
    }
    final int red = redUpper << 8 | redLower;

    if ((sym & 0b1000000) != 0) {
      int greenLowerDiff = 0;
      if ((sym & 0b100) != 0) {
        final int corr = decoder.decodeSymbol(this.diffGreenLower);
        greenLower = Integers.U8_FOLD(Integers.U8_CLAMP(redLowerDiff + greenLower) + corr);
        greenLowerDiff = (redLowerDiff + greenLower - this.lastGreenLower) / 2;
        this.lastGreenLower = greenLower;
      }

      if ((sym & 0b10000) != 0) {
        final int corr = decoder.decodeSymbol(this.diffBlueLower);
        blueLower = Integers.U8_FOLD(Integers.U8_CLAMP(greenLowerDiff + blueLower) + corr);
        this.lastBlueLower = blueLower;
      }

      int greenUpperDiff = 0;
      if ((sym & 0b1000) != 0) {
        final int corr = decoder.decodeSymbol(this.diffGreenUpper);
        greenUpper = Integers.U8_FOLD(Integers.U8_CLAMP(redUpperDiff + greenUpper) + corr);
        greenUpperDiff = (redUpperDiff + greenUpper - this.lastGreenUpper) / 2;
        this.lastGreenUpper = greenUpper;
      }

      if ((sym & 0b100000) != 0) {
        final int corr = decoder.decodeSymbol(this.diffBlueUpper);
        blueUpper = Integers.U8_FOLD(Integers.U8_CLAMP(greenUpperDiff + blueUpper) + corr);
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
    this.lastPoint = point;
  }

  protected void setLastPoint(final LasPoint point) {
    this.lastPoint = point;
    final int red = point.getRed();
    final int green = point.getGreen();
    final int blue = point.getBlue();
    this.lastRedLower = red & 0xFF;
    this.lastRedUpper = red >>> 8;
    this.lastGreenLower = green & 0xFF;
    this.lastGreenUpper = green >>> 8;
    this.lastBlueLower = blue & 0xFF;
    this.lastBlueUpper = blue >>> 8;
  }

  public boolean writeRgb(final ArithmeticEncoder encoder, final LasPoint point) {
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

    encoder.encodeSymbol(this.rgbBytesUsed, sym);
    int redLowerDiff = 0;
    if (redLowerChanged) {
      redLowerDiff = redLower - this.lastRedLower;
      encoder.encodeSymbol(this.diffRedLower, Integers.U8_FOLD(redLowerDiff));
      this.lastRedLower = redLower;
    }
    int redUpperDiff = 0;
    if (redUpperChanged) {
      redUpperDiff = redUpper - this.lastRedUpper;
      encoder.encodeSymbol(this.diffRedUpper, Integers.U8_FOLD(redUpperDiff));
      this.lastRedUpper = redUpper;
    }
    if (redDiffFromGreenAndBlue) {
      int greenLowerDiff = 0;
      if (greenLowerChanged) {
        greenLowerDiff = (redLowerDiff + greenLower - this.lastGreenLower) / 2;
        final int corr = greenLower - Integers.U8_CLAMP(redLowerDiff + this.lastGreenLower);
        encoder.encodeSymbol(this.diffGreenLower, Integers.U8_FOLD(corr));
        this.lastGreenLower = greenLower;
      }
      if (blueLowerChanged) {
        final int corr = blueLower - Integers.U8_CLAMP(greenLowerDiff + this.lastBlueLower);
        encoder.encodeSymbol(this.diffBlueLower, Integers.U8_FOLD(corr));
        this.lastBlueLower = blueLower;
      }
      int greenUpperDiff = 0;
      if (greenUpperChanged) {
        greenUpperDiff = (redUpperDiff + greenUpper - this.lastGreenUpper) / 2;
        final int corr = greenUpper - Integers.U8_CLAMP(redUpperDiff + this.lastGreenUpper);
        encoder.encodeSymbol(this.diffGreenUpper, Integers.U8_FOLD(corr));
        this.lastGreenUpper = greenUpper;
      }
      if (blueUpperChanged) {
        final int corr = blueUpper - Integers.U8_CLAMP(greenUpperDiff + this.lastBlueUpper);
        encoder.encodeSymbol(this.diffBlueUpper, Integers.U8_FOLD(corr));
        this.lastBlueUpper = blueUpper;
      }
    } else {
      this.lastGreenLower = redLower;
      this.lastGreenUpper = redUpper;
      this.lastBlueLower = redLower;
      this.lastBlueUpper = redUpper;
    }
    this.lastPoint = point;
    return sym != 0;

  }

}
