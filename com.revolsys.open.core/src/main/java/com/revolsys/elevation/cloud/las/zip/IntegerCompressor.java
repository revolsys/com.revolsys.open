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

import static java.lang.Integer.compareUnsigned;

public class IntegerCompressor {

  private int u_k;

  private final int u_contexts;

  private final int u_bits_high;

  private final int u_bits;

  private final int u_range;

  private int u_corr_bits;

  private int u_corr_range;

  private int corr_min;

  private int corr_max;

  private final ArithmeticEncoder enc;

  private final ArithmeticDecoder dec;

  private ArithmeticModel[] mBits;

  private ArithmeticBitModel mCorrector0;

  private ArithmeticModel[] mCorrector;

  private int[][] corr_histogram;

  public IntegerCompressor(final ArithmeticDecoder dec, final int u_bits) {
    this(dec, u_bits, 1);
  }

  public IntegerCompressor(final ArithmeticDecoder dec, final int u_bits, final int u_contexts) {
    this(dec, u_bits, u_contexts, 8, 0);
  }

  IntegerCompressor(final ArithmeticDecoder dec, final int u_bits, final int u_contexts,
    final int u_bits_high, int u_range) {
    assert dec != null;
    this.enc = null;
    this.dec = dec;
    this.u_bits = u_bits;
    this.u_contexts = u_contexts;
    this.u_bits_high = u_bits_high;
    this.u_range = u_range;

    if (u_range != 0) // the corrector's significant bits and range
    {
      this.u_corr_bits = 0;
      this.u_corr_range = u_range;
      while (u_range != 0) {
        u_range = u_range >>> 1;
        this.u_corr_bits++;
      }
      if (this.u_corr_range == 1 << this.u_corr_bits - 1) {
        this.u_corr_bits--;
      }
      // the corrector must fall into this interval
      this.corr_min = -Integer.divideUnsigned(this.u_corr_range, 2);
      this.corr_max = this.corr_min + this.u_corr_range - 1;
    } else if (u_bits != 0 && compareUnsigned(u_bits, 32) < 0) {
      this.u_corr_bits = u_bits;
      this.u_corr_range = 1 << u_bits;
      // the corrector must fall into this interval
      this.corr_min = -Integer.divideUnsigned(this.u_corr_range, 2);
      this.corr_max = this.corr_min + this.u_corr_range - 1;
    } else {
      this.u_corr_bits = 32;
      this.u_corr_range = 0;
      // the corrector must fall into this interval
      this.corr_min = Integer.MIN_VALUE;
      this.corr_max = Integer.MAX_VALUE;
    }

    this.u_k = 0;

    this.mBits = null;
    this.mCorrector = null;
  }

  public IntegerCompressor(final ArithmeticEncoder enc, final int u_bits) {
    this(enc, u_bits, 1, 8, 0);
  }

  IntegerCompressor(final ArithmeticEncoder enc, final int u_bits, final int u_contexts,
    final int u_bits_high, int u_range) {
    assert enc != null;
    this.enc = enc;
    this.dec = null;
    this.u_bits = u_bits;
    this.u_contexts = u_contexts;
    this.u_bits_high = u_bits_high;
    this.u_range = u_range;

    if (u_range != 0) // the corrector's significant bits and range
    {
      this.u_corr_bits = 0;
      this.u_corr_range = u_range;
      while (u_range != 0) {
        u_range = u_range >>> 1;
        this.u_corr_bits++;
      }
      if (this.u_corr_range == 1 << this.u_corr_bits - 1) {
        this.u_corr_bits--;
      }
      // the corrector must fall into this interval
      this.corr_min = -Integer.divideUnsigned(this.u_corr_range, 2);
      this.corr_max = this.corr_min + this.u_corr_range - 1;
    } else if (u_bits != 0 && compareUnsigned(u_bits, 32) < 0) {
      this.u_corr_bits = u_bits;
      this.u_corr_range = 1 << u_bits;
      // the corrector must fall into this interval
      this.corr_min = -Integer.divideUnsigned(this.u_corr_range, 2);
      this.corr_max = this.corr_min + this.u_corr_range - 1;
    } else {
      this.u_corr_bits = 32;
      this.u_corr_range = 0;
      // the corrector must fall into this interval
      this.corr_min = Integer.MIN_VALUE;
      this.corr_max = Integer.MAX_VALUE;
    }

    this.u_k = 0;

    this.mBits = null;
    this.mCorrector = null;
  }

  public void compress(final int pred, final int real) {
    compress(pred, real, 0);
  }

  void compress(final int pred, final int real, final int u_context) {
    assert this.enc != null;
    // the corrector will be within the interval [ - (corr_range - 1) ... +
    // (corr_range - 1) ]
    int corr = real - pred;
    // we fold the corrector into the interval [ corr_min ... corr_max ]
    if (corr < this.corr_min) {
      corr += this.u_corr_range;
    } else if (corr > this.corr_max) {
      corr -= this.u_corr_range;
    }
    writeCorrector(corr, this.mBits[u_context]);
  }

  public int decompress(final int pred) {
    return decompress(pred, 0);
  }

  public int decompress(final int pred, final int u_context) {
    assert this.dec != null;
    int real = pred + readCorrector(this.mBits[u_context]);
    if (real < 0) {
      real += this.u_corr_range;
    } else if (compareUnsigned(real, this.u_corr_range) >= 0) {
      real -= this.u_corr_range;
    }
    return real;
  }

  public int getK() {
    return this.u_k;
  }

  public void initCompressor() {
    int u_i;

    assert this.enc != null;

    // maybe create the models
    if (this.mBits == null) {
      this.mBits = new ArithmeticModel[this.u_contexts];
      for (u_i = 0; u_i < this.u_contexts; u_i++) {
        this.mBits[u_i] = this.enc.createSymbolModel(this.u_corr_bits + 1);
      }
      this.mCorrector = new ArithmeticModel[this.u_corr_bits + 1];
      this.mCorrector0 = this.enc.createBitModel();
      for (u_i = 1; u_i <= this.u_corr_bits; u_i++) {
        if (u_i <= this.u_bits_high) {
          this.mCorrector[u_i] = this.enc.createSymbolModel(1 << u_i);
        } else {
          this.mCorrector[u_i] = this.enc.createSymbolModel(1 << this.u_bits_high);
        }
      }
    }

    // certainly init the models
    for (u_i = 0; u_i < this.u_contexts; u_i++) {
      this.enc.initSymbolModel(this.mBits[u_i]);
    }

    this.enc.initBitModel(this.mCorrector0);
    for (u_i = 1; u_i <= this.u_corr_bits; u_i++) {
      this.enc.initSymbolModel(this.mCorrector[u_i]);
    }
  }

  public void initDecompressor() {
    int u_i;

    assert this.dec != null;

    // maybe create the models
    if (this.mBits == null) {
      this.mBits = new ArithmeticModel[this.u_contexts];
      for (u_i = 0; u_i < this.u_contexts; u_i++) {
        this.mBits[u_i] = this.dec.createSymbolModel(this.u_corr_bits + 1);
      }
      this.mCorrector = new ArithmeticModel[this.u_corr_bits + 1];
      this.mCorrector0 = this.dec.createBitModel();
      for (u_i = 1; u_i <= this.u_corr_bits; u_i++) {
        if (u_i <= this.u_bits_high) {
          this.mCorrector[u_i] = this.dec.createSymbolModel(1 << u_i);
        } else {
          this.mCorrector[u_i] = this.dec.createSymbolModel(1 << this.u_bits_high);
        }
      }
    }

    // certainly init the models
    for (u_i = 0; u_i < this.u_contexts; u_i++) {
      this.dec.initSymbolModel(this.mBits[u_i]);
    }
    this.dec.initBitModel(this.mCorrector0);
    for (u_i = 1; u_i <= this.u_corr_bits; u_i++) {
      this.dec.initSymbolModel(this.mCorrector[u_i]);
    }
  }

  int readCorrector(final ArithmeticModel mBits) {
    int c;

    // decode within which interval the corrector is falling

    this.u_k = this.dec.decodeSymbol(mBits);

    // decode the exact location of the corrector within the interval

    // TODO: original code: if (k)
    // TODO: how can k be zero or one?
    if (this.u_k != 0) // then c is either smaller than 0 or bigger than 1
    {
      if (compareUnsigned(this.u_k, 32) < 0) {
        if (compareUnsigned(this.u_k, this.u_bits_high) <= 0) // for small k we
                                                              // can do this in
                                                              // one step
        {
          // decompress c with the range coder
          c = this.dec.decodeSymbol(this.mCorrector[this.u_k]);
        } else {
          // for larger k we need to do this in two steps
          final int k1 = this.u_k - this.u_bits_high;
          // decompress higher bits with table
          c = this.dec.decodeSymbol(this.mCorrector[this.u_k]);
          // read lower bits raw
          final int c1 = this.dec.readBits(k1);
          // put the corrector back together
          c = c << k1 | c1;
        }
        // translate c back into its correct interval
        if (c >= 1 << this.u_k - 1) // if c is in the interval [ 2^(k-1) ... +
                                    // 2^k - 1 ]
        {
          // so we translate c back into the interval [ 2^(k-1) + 1 ... 2^k ] by
          // adding 1
          c += 1;
        } else // otherwise c is in the interval [ 0 ... + 2^(k-1) - 1 ]
        {
          // so we translate c back into the interval [ - (2^k - 1) ... -
          // (2^(k-1)) ] by subtracting (2^k - 1)
          c -= (1 << this.u_k) - 1;
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

  void writeCorrector(int c, final ArithmeticModel mBits) {
    int u_c1;

    // find the tighest interval [ - (2^k - 1) ... + (2^k) ] that contains c

    this.u_k = 0;

    // do this by checking the absolute value of c (adjusted for the case that c
    // is 2^k)

    u_c1 = c <= 0 ? -c : c - 1;

    // this loop could be replaced with more efficient code

    while (u_c1 != 0) {
      u_c1 = u_c1 >>> 1;
      this.u_k = this.u_k + 1;
    }

    // the number k is between 0 and corr_bits and describes the interval the
    // corrector falls into
    // we can compress the exact location of c within this interval using k bits

    this.enc.encodeSymbol(mBits, this.u_k);

    if (this.u_k != 0) // then c is either smaller than 0 or bigger than 1
    {
      assert c != 0 && c != 1;
      if (compareUnsigned(this.u_k, 32) < 0) {
        // translate the corrector c into the k-bit interval [ 0 ... 2^k - 1 ]
        if (c < 0) // then c is in the interval [ - (2^k - 1) ... - (2^(k-1)) ]
        {
          // so we translate c into the interval [ 0 ... + 2^(k-1) - 1 ] by
          // adding (2^k - 1)
          c += (1 << this.u_k) - 1;
        } else // then c is in the interval [ 2^(k-1) + 1 ... 2^k ]
        {
          // so we translate c into the interval [ 2^(k-1) ... + 2^k - 1 ] by
          // subtracting 1
          c -= 1;
        }
        if (this.u_k <= this.u_bits_high) // for small k we code the interval in
                                          // one step
        {
          // compress c with the range coder
          this.enc.encodeSymbol(this.mCorrector[this.u_k], c);
        } else // for larger k we need to code the interval in two steps
        {
          // figure out how many lower bits there are
          final int k1 = this.u_k - this.u_bits_high;
          // c1 represents the lowest k-bits_high+1 bits
          u_c1 = c & (1 << k1) - 1;
          // c represents the highest bits_high bits
          c = c >> k1;
          // compress the higher bits using a context table
          this.enc.encodeSymbol(this.mCorrector[this.u_k], c);
          // store the lower k1 bits raw
          this.enc.writeBits(k1, u_c1);
        }
      }
    } else // then c is 0 or 1
    {
      assert c == 0 || c == 1;
      this.enc.encodeBit(this.mCorrector0, c);
    }
  }
}
