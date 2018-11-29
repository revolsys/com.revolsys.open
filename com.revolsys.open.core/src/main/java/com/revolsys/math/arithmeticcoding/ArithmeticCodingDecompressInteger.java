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

    if (range != 0) // the corrector's significant bits and range
    {
      this.corr_bits = 0;
      this.corr_range = range;
      while (range != 0) {
        range = range >>> 1;
        this.corr_bits++;
      }
      if (this.corr_range == 1 << this.corr_bits - 1) {
        this.corr_bits--;
      }
      // the corrector must fall into this interval
      this.corr_min = -Integer.divideUnsigned(this.corr_range, 2);
    } else if (bits != 0 && compareUnsigned(bits, 32) < 0) {
      this.corr_bits = bits;
      this.corr_range = 1 << bits;
      // the corrector must fall into this interval
      this.corr_min = -Integer.divideUnsigned(this.corr_range, 2);
    } else {
      this.corr_bits = 32;
      this.corr_range = 0;
      // the corrector must fall into this interval
      this.corr_min = Integer.MIN_VALUE;
    }

    this.k = 0;

    this.mBits = new ArithmeticCodingDecompressModel[this.contexts];
    for (int i = 0; i < this.contexts; i++) {
      this.mBits[i] = this.dec.createSymbolModel(this.corr_bits + 1);
    }
    this.mCorrector0 = new ArithmeticCodingBitModel();
    this.mCorrector = new ArithmeticCodingDecompressModel[this.corr_bits + 1];
    for (int i = 1; i <= this.corr_bits; i++) {
      if (i <= this.bits_high) {
        this.mCorrector[i] = this.dec.createSymbolModel(1 << i);
      } else {
        this.mCorrector[i] = this.dec.createSymbolModel(1 << this.bits_high);
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

  int readCorrector(final ArithmeticCodingDecompressModel mBits) {
    int c;

    // decode within which interval the corrector is falling

    this.k = this.dec.decodeSymbol(mBits);

    // decode the exact location of the corrector within the interval

    // TODO: original code: if (k)
    // TODO: how can k be zero or one?
    if (this.k != 0) // then c is either smaller than 0 or bigger than 1
    {
      if (compareUnsigned(this.k, 32) < 0) {
        if (compareUnsigned(this.k, this.bits_high) <= 0) // for small k we
                                                          // can do this in
                                                          // one step
        {
          // decompress c with the range coder
          c = this.dec.decodeSymbol(this.mCorrector[this.k]);
        } else {
          // for larger k we need to do this in two steps
          final int k1 = this.k - this.bits_high;
          // decompress higher bits with table
          c = this.dec.decodeSymbol(this.mCorrector[this.k]);
          // read lower bits raw
          final int c1 = this.dec.readBits(k1);
          // put the corrector back together
          c = c << k1 | c1;
        }
        // translate c back into its correct interval
        if (c >= 1 << this.k - 1) // if c is in the interval [ 2^(k-1) ... +
                                  // 2^k - 1 ]
        {
          // so we translate c back into the interval [ 2^(k-1) + 1 ... 2^k ] by
          // adding 1
          c += 1;
        } else // otherwise c is in the interval [ 0 ... + 2^(k-1) - 1 ]
        {
          // so we translate c back into the interval [ - (2^k - 1) ... -
          // (2^(k-1)) ] by subtracting (2^k - 1)
          c -= (1 << this.k) - 1;
        }
      } else {
        c = this.corr_min;
      }
    } else // then c is either 0 or 1
    {
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
