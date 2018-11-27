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
package com.revolsys.elevation.gridded.scaledint.compressed;

import static java.lang.Integer.compareUnsigned;

public class IntegerCompressor {

  private int k = 0;

  private final ArithmeticEncoder encoder;

  private final ArithmeticModel bits;

  private final ArithmeticBitModel corrector0;

  private final ArithmeticModel[] corrector;

  public IntegerCompressor(final ArithmeticEncoder encoder, final int u_bits) {
    this.encoder = encoder;

    this.bits = new ArithmeticModel(33);
    this.corrector0 = new ArithmeticBitModel();
    this.corrector = new ArithmeticModel[33];
    for (int i = 1; i <= 32; i++) {
      if (i <= 8) {
        this.corrector[i] = new ArithmeticModel(1 << i);
      } else {
        this.corrector[i] = new ArithmeticModel(1 << 8);
      }
    }
  }

  public void compress(final int expectedValue, final int actualValue) {
    int corrector = actualValue - expectedValue;

    // find the tighest interval [ - (2^k - 1) ... + (2^k) ] that contains c

    this.k = 0;

    // do this by checking the absolute value of c (adjusted for the case that c
    // is 2^k)

    int u_c1 = corrector <= 0 ? -corrector : corrector - 1;

    // this loop could be replaced with more efficient code

    while (u_c1 != 0) {
      u_c1 = u_c1 >>> 1;
      this.k = this.k + 1;
    }

    // the number k is between 0 and corr_bits and describes the interval the
    // corrector falls into
    // we can compress the exact location of c within this interval using k bits

    this.encoder.encodeSymbol(this.bits, this.k);

    if (this.k != 0) { // then c is either smaller than 0 or bigger than 1
      if (compareUnsigned(this.k, 32) < 0) {
        // translate the corrector c into the k-bit interval [ 0 ... 2^k - 1 ]
        if (corrector < 0) {// then c is in the interval [ - (2^k - 1) ... -
                            // (2^(k-1)) ]
          // so we translate c into the interval [ 0 ... + 2^(k-1) - 1 ] by
          // adding (2^k - 1)
          corrector += (1 << this.k) - 1;
        } else { // then c is in the interval [ 2^(k-1) + 1 ... 2^k ]
          // so we translate c into the interval [ 2^(k-1) ... + 2^k - 1 ] by
          // subtracting 1
          corrector -= 1;
        }
        if (this.k <= 8) {// for small k we code the interval in one step
          // compress c with the range coder
          this.encoder.encodeSymbol(this.corrector[this.k], corrector);
        } else {// for larger k we need to code the interval in two steps
          // figure out how many lower bits there are
          final int k1 = this.k - 8;
          // c1 represents the lowest k-bits_high+1 bits
          u_c1 = corrector & (1 << k1) - 1;
          // c represents the highest bits_high bits
          corrector = corrector >> k1;
          // compress the higher bits using a context table
          this.encoder.encodeSymbol(this.corrector[this.k], corrector);
          // store the lower k1 bits raw
          this.encoder.writeBits(k1, u_c1);
        }
      }
    } else {// then c is 0 or 1
      this.encoder.encodeBit(this.corrector0, corrector);
    }
  }
}
