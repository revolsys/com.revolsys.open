/*
 * Copyright 2005-2014, martin isenburg, rapidlasso - fast tools to catch reality
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

import com.revolsys.io.BaseCloseable;
import com.revolsys.io.channels.ChannelReader;

// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
//                                                                           -
// Fast arithmetic coding implementation                                     -
// -> 32-bit variables, 32-bit product, periodic updates, table decoding     -
//                                                                           -
// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
//                                                                           -
// Version 1.00  -  April 25, 2004                                           -
//                                                                           -
// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
//                                                                           -
//                                  WARNING                                  -
//                                 =========                                 -
//                                                                           -
// The only purpose of this program is to demonstrate the basic principles   -
// of arithmetic coding. It is provided as is, without any express or        -
// implied warranty, without even the warranty of fitness for any particular -
// purpose, or that the implementations are correct.                         -
//                                                                           -
// Permission to copy and redistribute this code is hereby granted, provided -
// that this warning and copyright notices are not removed or altered.       -
//                                                                           -
// Copyright (c) 2004 by Amir Said (said@ieee.org) &                         -
//                       William A. Pearlman (pearlw@ecse.rpi.edu)           -
//                                                                           -
// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
//                                                                           -
// A description of the arithmetic coding method used here is available in   -
//                                                                           -
// Lossless Compression Handbook, ed. K. Sayood                              -
// Chapter 5: Arithmetic Coding (A. Said), pp. 101-152, Academic Press, 2003 -
//                                                                           -
// A. Said, Introduction to Arithetic Coding Theory and Practice             -
// HP Labs report HPL-2004-76  -  http://www.hpl.hp.com/techreports/         -
//                                                                           -
// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

public class ArithmeticDecoder implements ArithmeticConstants, BaseCloseable {

  public static ArithmeticBitModel createBitModel() {
    return new ArithmeticBitModel();
  }

  public static ArithmeticModel createSymbolModel(final int n) {
    return new ArithmeticModel(n, false);
  }

  public static void initBitModel(final ArithmeticBitModel m) {
    m.init();
  }

  private ChannelReader reader;

  private int value;

  private int length;

  public ArithmeticDecoder(final ChannelReader reader) {
    this.reader = reader;
  }

  @Override
  public void close() {
    this.reader = null;
  }

  public int decodeBit(final ArithmeticBitModel model) {
    final int x = model.bit0Prob * (this.length >>> BM_LENGTH_SHIFT); // product l x p0
    final int sym = compareUnsigned(this.value, x) >= 0 ? 1 : 0; // decision
    // update & shift interval
    if (sym == 0) {
      this.length = x;
      ++model.bit0Count;
    } else {
      this.value -= x; // shifted interval base = 0
      this.length -= x;
    }

    if (compareUnsigned(this.length, AC_MIN_LENGTH) < 0) {
      renormDecoderInterval();
    }
    model.updateIfRequired(); // periodic model update

    return sym; // return data bit value
  }

  public int decodeSymbol(final ArithmeticModel model) {
    int symbol;
    int x;
    final int value = this.value;
    int length = this.length;
    int y = length;
    length >>>= DM_LENGTH_SHIFT;
    final int[] decoderTable = model.decoderTable;
    final int[] distribution = model.distribution;
    if (decoderTable == null) {
      symbol = 0;
      x = 0;
      int n = model.symbols;
      int k = n >>> 1;
      // decode via bisection search
      do {
        final int z = length * distribution[k];
        if (compareUnsigned(z, value) > 0) {
          n = k;
          y = z; // value is smaller
        } else {
          symbol = k;
          x = z; // value is larger or equal
        }
        k = symbol + n >>> 1;
      } while (k != symbol);
    } else {
      final int dv = (int)(Integer.toUnsignedLong(value) / length);
      final int t = dv >>> model.tableShift;
      symbol = decoderTable[t]; // initial decision based on table look-up
      int n = decoderTable[t + 1] + 1;

      while (n > symbol + 1) { // finish with bisection search
        final int k = symbol + n >>> 1;
        if (distribution[k] > dv) {
          n = k;
        } else {
          symbol = k;
        }
      }
      // compute products
      x = distribution[symbol] * length;
      if (symbol != model.lastSymbol) {
        y = distribution[symbol + 1] * length;
      }

    }

    this.value -= x; // update interval
    length = y - x;
    this.length = length;

    if (compareUnsigned(length, AC_MIN_LENGTH) < 0) {
      renormDecoderInterval(); // renormalization
    }

    model.update(symbol);

    return symbol;
  }

  public int readBit() {
    final int sym = Integer.divideUnsigned(this.value, this.length >>>= 1); // decode symbol,
                                                                            // change length
    this.value -= this.length * sym; // update interval

    if (compareUnsigned(this.length, AC_MIN_LENGTH) < 0) {
      renormDecoderInterval(); // renormalization
    }

    if (compareUnsigned(sym, 2) >= 0) {
      throw new RuntimeException("4711");
    }

    return sym;
  }

  public int readBits(int bits) {
    assert bits != 0 && bits <= 32;

    if (bits > 19) {
      final int tmp = readShort();
      bits = bits - 16;
      final int tmp1 = readBits(bits) << 16;
      return tmp1 | tmp;
    }

    final int sym = Integer.divideUnsigned(this.value, this.length >>>= bits);// decode
                                                                              // symbol,
                                                                              // change
                                                                              // length
    this.value -= this.length * sym; // update interval

    if (compareUnsigned(this.length, AC_MIN_LENGTH) < 0) {
      renormDecoderInterval(); // renormalization
    }

    if (compareUnsigned(sym, 1 << bits) >= 0) {
      throw new RuntimeException("4711");
    }

    return sym;
  }

  public byte readByte() {
    final int sym = Integer.divideUnsigned(this.value, this.length >>>= 8); // decode symbol,
                                                                            // change length
    this.value -= this.length * sym; // update interval

    if (compareUnsigned(this.length, AC_MIN_LENGTH) < 0) {
      renormDecoderInterval(); // renormalization
    }

    if (compareUnsigned(sym, 1 << 8) >= 0) {
      throw new RuntimeException("4711");
    }

    return (byte)sym;
  }

  public double readDouble() /* danger in float reinterpretation */
  {
    return Double.longBitsToDouble(readInt64());
  }

  public float readFloat() /* danger in float reinterpretation */
  {
    return Float.intBitsToFloat(readInt());
  }

  public int readInt() {
    final int lowerInt = readShort();
    final int upperInt = readShort();
    return upperInt << 16 | lowerInt;
  }

  public long readInt64() {
    final long lowerInt = readInt();
    final long upperInt = readInt();
    return upperInt << 32 | lowerInt;
  }

  public short readShort() {
    final int sym = Integer.divideUnsigned(this.value, this.length >>>= 16); // decode symbol,
                                                                             // change length
    this.value -= this.length * sym; // update interval

    if (compareUnsigned(this.length, AC_MIN_LENGTH) < 0) {
      renormDecoderInterval(); // renormalization
    }

    if (compareUnsigned(sym, 1 << 16) >= 0) {
      throw new RuntimeException("4711");
    }

    return (short)sym;
  }

  private void renormDecoderInterval() {
    final ChannelReader reader = this.reader;
    int value = this.value;
    int length = this.length;
    do { // read least-significant byte
      value = value << 8 | reader.getByte() & 0xff;
      length <<= 8;// multiplied by 256
    } while (Integer.compareUnsigned(length, AC_MIN_LENGTH) < 0);
    this.value = value;
    this.length = length;
  }

  public void reset() {
    this.length = AC_MAX_LENGTH;
    final ChannelReader reader = this.reader;
    int value = (reader.getByte() & 0xff) << 24;
    value |= (reader.getByte() & 0xff) << 16;
    value |= (reader.getByte() & 0xff) << 8;
    value |= reader.getByte() & 0xff;

    this.value = value;
  }
}
