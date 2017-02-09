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

  private final int bitsHigh;

  private final int contexts;

  private int correctorBits;

  private int correctorMax;

  private int correctorMin;

  private int correctorRange;

  private final ArithmeticDecoder decoder;

  private final ArithmeticEncoder encoder;

  private int k;

  private ArithmeticModel[] bits;

  private ArithmeticModel[] corrector;

  private ArithmeticBitModel corrector0;

  public IntegerCompressor(final ArithmeticDecoder decoder, final int bits) {
    this(decoder, bits, 1);
  }

  public IntegerCompressor(final ArithmeticDecoder decoder, final int bits, final int contexts) {
    this(decoder, bits, contexts, 8, 0);
  }

  IntegerCompressor(final ArithmeticDecoder decoder, final int bits, final int contexts,
    final int bitsHigh, int range) {
    assert decoder != null;
    this.encoder = null;
    this.decoder = decoder;
    this.contexts = contexts;
    this.bitsHigh = bitsHigh;

    if (range != 0) // the corrector's significant bits and range
    {
      this.correctorBits = 0;
      this.correctorRange = range;
      while (range != 0) {
        range = range >>> 1;
        this.correctorBits++;
      }
      if (this.correctorRange == 1 << this.correctorBits - 1) {
        this.correctorBits--;
      }
      // the corrector must fall into this interval
      this.correctorMin = -Integer.divideUnsigned(this.correctorRange, 2);
      this.correctorMax = this.correctorMin + this.correctorRange - 1;
    } else if (bits != 0 && compareUnsigned(bits, 32) < 0) {
      this.correctorBits = bits;
      this.correctorRange = 1 << bits;
      // the corrector must fall into this interval
      this.correctorMin = -Integer.divideUnsigned(this.correctorRange, 2);
      this.correctorMax = this.correctorMin + this.correctorRange - 1;
    } else {
      this.correctorBits = 32;
      this.correctorRange = 0;
      // the corrector must fall into this interval
      this.correctorMin = Integer.MIN_VALUE;
      this.correctorMax = Integer.MAX_VALUE;
    }

    this.k = 0;

    this.bits = null;
    this.corrector = null;
  }

  public IntegerCompressor(final ArithmeticEncoder encoder, final int bits) {
    this(encoder, bits, 1, 8, 0);
  }

  IntegerCompressor(final ArithmeticEncoder encoder, final int bits, final int contexts,
    final int bitsHigh, int range) {
    assert encoder != null;
    this.encoder = encoder;
    this.decoder = null;
    this.contexts = contexts;
    this.bitsHigh = bitsHigh;

    if (range != 0) // the corrector's significant bits and range
    {
      this.correctorBits = 0;
      this.correctorRange = range;
      while (range != 0) {
        range = range >>> 1;
        this.correctorBits++;
      }
      if (this.correctorRange == 1 << this.correctorBits - 1) {
        this.correctorBits--;
      }
      // the corrector must fall into this interval
      this.correctorMin = -Integer.divideUnsigned(this.correctorRange, 2);
      this.correctorMax = this.correctorMin + this.correctorRange - 1;
    } else if (bits != 0 && compareUnsigned(bits, 32) < 0) {
      this.correctorBits = bits;
      this.correctorRange = 1 << bits;
      // the corrector must fall into this interval
      this.correctorMin = -Integer.divideUnsigned(this.correctorRange, 2);
      this.correctorMax = this.correctorMin + this.correctorRange - 1;
    } else {
      this.correctorBits = 32;
      this.correctorRange = 0;
      // the corrector must fall into this interval
      this.correctorMin = Integer.MIN_VALUE;
      this.correctorMax = Integer.MAX_VALUE;
    }

    this.k = 0;

    this.bits = null;
    this.corrector = null;
  }

  public void compress(final int pred, final int real) {
    compress(pred, real, 0);
  }

  void compress(final int pred, final int real, final int context) {
    assert this.encoder != null;
    // the corrector will be within the interval [ - (corr_range - 1) ... + (corr_range - 1) ]
    int corr = real - pred;
    // we fold the corrector into the interval [ corr_min ... corr_max ]
    if (corr < this.correctorMin) {
      corr += this.correctorRange;
    } else if (corr > this.correctorMax) {
      corr -= this.correctorRange;
    }
    writeCorrector(corr, this.bits[context]);
  }

  public int decompress(final int pred) {
    return decompress(pred, 0);
  }

  public int decompress(final int pred, final int context) {
    int real = pred + readCorrector(this.bits[context]);
    if (real < 0) {
      real += this.correctorRange;
    } else if (compareUnsigned(real, this.correctorRange) >= 0) {
      real -= this.correctorRange;
    }
    return real;
  }

  public int getK() {
    return this.k;
  }

  public void initCompressor() {
    int i;

    assert this.encoder != null;

    // maybe create the models
    if (this.bits == null) {
      this.bits = new ArithmeticModel[this.contexts];
      for (i = 0; i < this.contexts; i++) {
        this.bits[i] = this.encoder.createSymbolModel(this.correctorBits + 1);
      }
      this.corrector = new ArithmeticModel[this.correctorBits + 1];
      this.corrector0 = this.encoder.createBitModel();
      for (i = 1; i <= this.correctorBits; i++) {
        if (i <= this.bitsHigh) {
          this.corrector[i] = this.encoder.createSymbolModel(1 << i);
        } else {
          this.corrector[i] = this.encoder.createSymbolModel(1 << this.bitsHigh);
        }
      }
    }

    // certainly init the models
    for (i = 0; i < this.contexts; i++) {
      this.encoder.initSymbolModel(this.bits[i]);
    }

    this.encoder.initBitModel(this.corrector0);
    for (i = 1; i <= this.correctorBits; i++) {
      this.encoder.initSymbolModel(this.corrector[i]);
    }
  }

  public void initDecompressor() {
    int i;

    // maybe create the models
    if (this.bits == null) {
      this.bits = new ArithmeticModel[this.contexts];
      for (i = 0; i < this.contexts; i++) {
        this.bits[i] = ArithmeticDecoder.createSymbolModel(this.correctorBits + 1);
      }
      this.corrector = new ArithmeticModel[this.correctorBits + 1];
      this.corrector0 = ArithmeticDecoder.createBitModel();
      for (i = 1; i <= this.correctorBits; i++) {
        if (i <= this.bitsHigh) {
          this.corrector[i] = ArithmeticDecoder.createSymbolModel(1 << i);
        } else {
          this.corrector[i] = ArithmeticDecoder.createSymbolModel(1 << this.bitsHigh);
        }
      }
    }

    // certainly init the models
    for (i = 0; i < this.contexts; i++) {
      ArithmeticModel.initSymbolModel(this.bits[i]);
    }
    ArithmeticDecoder.initBitModel(this.corrector0);
    for (i = 1; i <= this.correctorBits; i++) {
      ArithmeticModel.initSymbolModel(this.corrector[i]);
    }
  }

  int readCorrector(final ArithmeticModel model) {
    final ArithmeticDecoder decoder = this.decoder;
    // decode within which interval the corrector is falling
    final int k = decoder.decodeSymbol(model);
    this.k = k;
    // decode the exact location of the corrector within the interval

    // TODO: original code: if (k)
    // TODO: how can k be zero or one?
    int corrector;
    if (k == 0) {
      // then c is either 0 or 1
      corrector = decoder.decodeBit(this.corrector0);
    } else {
      // then c is either smaller than 0 or bigger than 1
      if (k < 32) {
        corrector = decoder.decodeSymbol(this.corrector[k]);
        final int bitsHigh = this.bitsHigh;
        if (k > bitsHigh) {
          // for larger k we need to do this in two steps
          final int k1 = k - bitsHigh;
          // read lower bits raw
          final int c1 = decoder.readBits(k1);
          // put the corrector back together
          corrector = corrector << k1 | c1;
        }
        // translate c back into its correct interval
        if (corrector >= 1 << k - 1) {
          // if c is in the interval [ 2^(k-1) ... + 2^k - 1 ]
          // so we translate c back into the interval [ 2^(k-1) + 1 ... 2^k ] by adding 1
          corrector += 1;
        } else {
          // otherwise c is in the interval [ 0 ... + 2^(k-1) - 1 ]
          // so we translate c back into the interval [ - (2^k - 1) ... - (2^(k-1)) ] by
          // subtracting
          // (2^k - 1)
          corrector -= (1 << k) - 1;
        }
      } else {
        corrector = this.correctorMin;
      }
    }

    return corrector;
  }

  void writeCorrector(int c, final ArithmeticModel mBits) {
    int c1;

    // find the tighest interval [ - (2^k - 1) ... + (2^k) ] that contains c

    this.k = 0;

    // do this by checking the absolute value of c (adjusted for the case that c is 2^k)

    c1 = c <= 0 ? -c : c - 1;

    // this loop could be replaced with more efficient code

    while (c1 != 0) {
      c1 = c1 >>> 1;
      this.k = this.k + 1;
    }

    // the number k is between 0 and corr_bits and describes the interval the corrector falls into
    // we can compress the exact location of c within this interval using k bits

    this.encoder.encodeSymbol(mBits, this.k);

    if (this.k != 0) // then c is either smaller than 0 or bigger than 1
    {
      assert c != 0 && c != 1;
      if (compareUnsigned(this.k, 32) < 0) {
        // translate the corrector c into the k-bit interval [ 0 ... 2^k - 1 ]
        if (c < 0) // then c is in the interval [ - (2^k - 1) ... - (2^(k-1)) ]
        {
          // so we translate c into the interval [ 0 ... + 2^(k-1) - 1 ] by adding (2^k - 1)
          c += (1 << this.k) - 1;
        } else // then c is in the interval [ 2^(k-1) + 1 ... 2^k ]
        {
          // so we translate c into the interval [ 2^(k-1) ... + 2^k - 1 ] by subtracting 1
          c -= 1;
        }
        if (this.k <= this.bitsHigh) // for small k we code the interval in one step
        {
          // compress c with the range coder
          this.encoder.encodeSymbol(this.corrector[this.k], c);
        } else // for larger k we need to code the interval in two steps
        {
          // figure out how many lower bits there are
          final int k1 = this.k - this.bitsHigh;
          // c1 represents the lowest k-bitsHigh+1 bits
          c1 = c & (1 << k1) - 1;
          // c represents the highest bitsHigh bits
          c = c >> k1;
          // compress the higher bits using a context table
          this.encoder.encodeSymbol(this.corrector[this.k], c);
          // store the lower k1 bits raw
          this.encoder.writeBits(k1, c1);
        }
      }
    } else // then c is 0 or 1
    {
      assert c == 0 || c == 1;
      this.encoder.encodeBit(this.corrector0, c);
    }
  }
}
