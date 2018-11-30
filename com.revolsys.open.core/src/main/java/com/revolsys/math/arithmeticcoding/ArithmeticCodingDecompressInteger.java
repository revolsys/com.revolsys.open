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
package com.revolsys.math.arithmeticcoding;

import static java.lang.Integer.compareUnsigned;

public class ArithmeticCodingDecompressInteger {

  private int k;

  private final int contexts;

  private final int bits_high;

  private int corr_bits;

  private int corr_range;

  private int corr_min;

  private final ArithmeticCodingDecompressDecoder dec;

  private final ArithmeticCodingDecompressModel[] mBits;

  private final ArithmeticCodingBitModel mCorrector0;

  private final ArithmeticCodingDecompressModel[] mCorrector;

  public ArithmeticCodingDecompressInteger(final ArithmeticCodingDecompressDecoder dec,
    final int bits) {
    this(dec, bits, 1);
  }

  public ArithmeticCodingDecompressInteger(final ArithmeticCodingDecompressDecoder dec,
    final int bits, final int contexts) {
    this(dec, bits, contexts, 8, 0);
  }

  private ArithmeticCodingDecompressInteger(final ArithmeticCodingDecompressDecoder dec,
    final int bits, final int contexts, final int bits_high, int range) {
    assert dec != null;
    this.dec = dec;
    this.contexts = contexts;
    this.bits_high = bits_high;

    if (range != 0) {
      this.corr_bits = 0;
      this.corr_range = range;
      while (range != 0) {
        range = range >>> 1;
        this.corr_bits++;
      }
      if (this.corr_range == 1 << this.corr_bits - 1) {
        this.corr_bits--;
      }
      this.corr_min = -Integer.divideUnsigned(this.corr_range, 2);
    } else if (bits != 0 && compareUnsigned(bits, 32) < 0) {
      this.corr_bits = bits;
      this.corr_range = 1 << bits;
      this.corr_min = -Integer.divideUnsigned(this.corr_range, 2);
    } else {
      this.corr_bits = 32;
      this.corr_range = 0;
      this.corr_min = Integer.MIN_VALUE;
    }

    this.k = 0;

    this.mBits = new ArithmeticCodingDecompressModel[this.contexts];
    for (int i = 0; i < this.contexts; i++) {
      this.mBits[i] = new ArithmeticCodingDecompressModel(this.corr_bits + 1);
    }
    this.mCorrector0 = new ArithmeticCodingBitModel();
    this.mCorrector = new ArithmeticCodingDecompressModel[this.corr_bits + 1];
    for (int i = 1; i <= this.corr_bits; i++) {
      if (i <= this.bits_high) {
        this.mCorrector[i] = new ArithmeticCodingDecompressModel(1 << i);
      } else {
        this.mCorrector[i] = new ArithmeticCodingDecompressModel(1 << this.bits_high);
      }
    }

  }

  public int decompress(final int pred) {
    return decompress(pred, 0);
  }

  public int decompress(final int pred, final int context) {
    assert this.dec != null;
    int real = pred + readCorrector(this.mBits[context]);
    if (real < 0) {
      real += this.corr_range;
    } else if (compareUnsigned(real, this.corr_range) >= 0) {
      real -= this.corr_range;
    }
    return real;
  }

  public int getK() {
    return this.k;
  }

  private int readCorrector(final ArithmeticCodingDecompressModel mBits) {
    int c;

    this.k = this.dec.decodeSymbol(mBits);

    if (this.k != 0) {
      if (compareUnsigned(this.k, 32) < 0) {
        if (compareUnsigned(this.k, this.bits_high) <= 0) {
          c = this.dec.decodeSymbol(this.mCorrector[this.k]);
        } else {
          final int k1 = this.k - this.bits_high;
          c = this.dec.decodeSymbol(this.mCorrector[this.k]);
          final int c1 = this.dec.readBits(k1);
          c = c << k1 | c1;
        }
        if (c >= 1 << this.k - 1) {
          c += 1;
        } else {
          c -= (1 << this.k) - 1;
        }
      } else {
        c = this.corr_min;
      }
    } else {
      c = this.dec.decodeBit(this.mCorrector0);
    }

    return c;
  }

  public void reset() {
    for (final ArithmeticCodingDecompressModel model : this.mBits) {
      model.reset();
    }
    this.mCorrector0.reset();
    for (int i = 1; i <= this.corr_bits; i++) {
      final ArithmeticCodingDecompressModel model = this.mCorrector[i];
      model.reset();
    }
  }
}
