package com.revolsys.elevation.cloud.las.zip.context;

import com.revolsys.elevation.cloud.las.pointformat.LasPoint;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingCodec;
import com.revolsys.math.arithmeticcoding.ArithmeticDecoder;
import com.revolsys.math.arithmeticcoding.ArithmeticEncoder;
import com.revolsys.math.arithmeticcoding.ArithmeticModel;
import com.revolsys.util.number.Integers;

public class LasZipContextRgbNir extends LasZipContextRgb {

  private ArithmeticModel nirBytesUsed;

  private ArithmeticModel diffNirLower;

  private ArithmeticModel diffNirUpper;

  private int lastNirLower;

  private int lastNirUpper;

  @Override
  public void initPoint(final ArithmeticCodingCodec codec, final LasPoint point) {
    super.initPoint(codec, point);
    if (this.nirBytesUsed == null) {
      this.nirBytesUsed = codec.createSymbolModel(128);
      this.diffNirLower = codec.createSymbolModel(256);
      this.diffNirUpper = codec.createSymbolModel(256);
    } else {
      this.nirBytesUsed.init();
      this.diffNirLower.init();
      this.diffNirUpper.init();
    }
  }

  public void readNir(final ArithmeticDecoder decoder, final LasPoint point) {
    int nirLower = this.lastNirLower;
    int nirUpper = this.lastNirUpper;

    final int sym = decoder.decodeSymbol(this.nirBytesUsed);
    if ((sym & 0b1) != 0) {
      final int corr = decoder.decodeSymbol(this.diffNirLower);
      nirLower = Integers.U8_FOLD(nirLower + corr);
      this.lastNirLower = nirLower;
    }

    if ((sym & 0b10) != 0) {
      final int corr = decoder.decodeSymbol(this.diffNirUpper);
      nirUpper = Integers.U8_FOLD(nirUpper + corr);
      this.lastNirUpper = nirUpper;
    }

    final int nir = nirUpper << 8 | nirLower;
    point.setNir(nir);
    this.lastPoint = point;
  }

  @Override
  protected void setLastPoint(final LasPoint point) {
    super.setLastPoint(point);
    final int nir = point.getNir();
    this.lastNirLower = nir & 0xFF;
    this.lastNirUpper = nir >>> 8;
  }

  public boolean writeNir(final ArithmeticEncoder encoder, final LasPoint point) {
    final int nir = point.getNir();

    final int nirLower = nir & 0xFF;
    final int nirUpper = nir >>> 8;
    int sym = 0;
    final boolean nirLowerChanged = this.lastNirLower != nirLower;
    if (nirLowerChanged) {
      sym |= 0b1;
    }
    final boolean nirUpperChanged = this.lastNirUpper != nirUpper;
    if (nirUpperChanged) {
      sym |= 0b10;
    }

    encoder.encodeSymbol(this.nirBytesUsed, sym);
    if (nirLowerChanged) {
      final int nirLowerDiff = nirLower - this.lastNirLower;
      encoder.encodeSymbol(this.diffNirLower, Integers.U8_FOLD(nirLowerDiff));
      this.lastNirLower = nirLower;
    }
    if (nirUpperChanged) {
      final int nirUpperDiff = nirUpper - this.lastNirUpper;
      encoder.encodeSymbol(this.diffNirUpper, Integers.U8_FOLD(nirUpperDiff));
      this.lastNirUpper = nirUpper;
    }

    return sym != 0;
  }

}
