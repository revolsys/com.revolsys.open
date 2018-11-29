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

public class ArithmeticCodingCompressInteger {

  private final int bits_high;

  private final ArithmeticCodingCompressEncoder enc;

  private final ArithmeticCodingCompressModel mBits;

  private final ArithmeticCodingBitModel mCorrector0;

  private final ArithmeticCodingCompressModel[] mCorrector;

  public ArithmeticCodingCompressInteger(final ArithmeticCodingCompressEncoder enc, final int bits) {
    this.enc = enc;
    this.bits_high = 8;

    final int corr_bits = 32;

    this.mBits = new ArithmeticCodingCompressModel(corr_bits + 1, true);
    this.mCorrector = new ArithmeticCodingCompressModel[corr_bits + 1];
    this.mCorrector0 = new ArithmeticCodingBitModel();
    for (int i = 1; i <= corr_bits; i++) {
      if (i <= this.bits_high) {
        this.mCorrector[i] = new ArithmeticCodingCompressModel(1 << i, true);
      } else {
        this.mCorrector[i] = new ArithmeticCodingCompressModel(1 << this.bits_high, true);
      }
    }

  }

  public void compress(final int pred, final int real) {
    final int corr = real - pred;
    writeCorrector(corr, this.mBits);
  }

  void writeCorrector(int c, final ArithmeticCodingCompressModel mBits) {
    // find the tighest interval [ - (2^k - 1) ... + (2^k) ] that contains c

    int k = 0;

    // do this by checking the absolute value of c (adjusted for the case that c
    // is 2^k)

    long c1 = c <= 0 ? -c : c - 1;

    // this loop could be replaced with more efficient code

    while (c1 != 0) {
      c1 = c1 >>> 1;
      k = k + 1;
    }

    // the number k is between 0 and corr_bits and describes the interval the
    // corrector falls into
    // we can compress the exact location of c within this interval using k bits

    this.enc.encodeSymbol(mBits, k);

    if (k != 0) {// then c is either smaller than 0 or bigger than 1
      if (k < 32) {
        if (k < 32) {
          // translate the corrector c into the k-bit interval [ 0 ... 2^k - 1 ]
          if (c < 0) // then c is in the interval [ - (2^k - 1) ... - (2^(k-1))
                     // ]
          {
            // so we translate c into the interval [ 0 ... + 2^(k-1) - 1 ] by
            // adding (2^k - 1)
            c += (1 << k) - 1;
          } else // then c is in the interval [ 2^(k-1) + 1 ... 2^k ]
          {
            // so we translate c into the interval [ 2^(k-1) ... + 2^k - 1 ] by
            // subtracting 1
            c -= 1;
          }
          if (k <= this.bits_high) // for small k we code the interval in
                                   // one step
          {
            // compress c with the range coder
            this.enc.encodeSymbol(this.mCorrector[k], c);
          } else // for larger k we need to code the interval in two steps
          {
            // figure out how many lower bits there are
            final long k1 = k - this.bits_high;
            // c1 represents the lowest k-bits_high+1 bits
            c1 = c & (1 << k1) - 1;
            // c represents the highest bits_high bits
            c = c >> k1;
            // compress the higher bits using a context table
            this.enc.encodeSymbol(this.mCorrector[k], c);
            // store the lower k1 bits raw
            this.enc.writeBits(k1, c1);
          }
        }
      }
    } else {// then c is 0 or 1
      this.enc.encodeBit(this.mCorrector0, c);
    }
  }
}
