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
package com.revolsys.elevation.cloud.las.decoder;

import static java.lang.Integer.compareUnsigned;

import java.nio.ByteOrder;

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

public class ArithmeticDecoder implements ArithmeticConstants {

  private ChannelReader reader;

  private int value;

  private int length;

  public ArithmeticDecoder(final ChannelReader reader) {
    this.reader = reader;
  }

  ArithmeticBitModel createBitModel() {
    return new ArithmeticBitModel();
  }

  ArithmeticModel createSymbolModel(final int n) {
    return new ArithmeticModel(n, false);
  }

  int decodeBit(final ArithmeticBitModel m) {
    assert m != null;

    final int x = m.bit0Prob * (this.length >>> BM_LENGTH_SHIFT); // product l x p0
    final int sym = compareUnsigned(this.value, x) >= 0 ? 1 : 0; // decision
    // update & shift interval
    if (sym == 0) {
      this.length = x;
      ++m.bit0Count;
    } else {
      this.value -= x; // shifted interval base = 0
      this.length -= x;
    }

    if (compareUnsigned(this.length, AC_MIN_LENGTH) < 0) {
      renorm_dec_interval(); // renormalization
    }
    m.updateIfRequired(); // periodic model update

    return sym; // return data bit value
  }

  int decodeSymbol(final ArithmeticModel m) {
    int n, sym, x, y = this.length;

    if (m.decoderTable != null) { // use table look-up for faster decoding

      final int dv = Integer.divideUnsigned(this.value, this.length >>>= DM_LENGTH_SHIFT);
      final int t = dv >>> m.tableShift;

      sym = m.decoderTable[t]; // initial decision based on table look-up
      n = m.decoderTable[t + 1] + 1;

      while (compareUnsigned(n, sym + 1) > 0) { // finish with bisection search
        final int k = sym + n >>> 1;
        if (compareUnsigned(m.distribution[k], dv) > 0) {
          n = k;
        } else {
          sym = k;
        }
      }
      // compute products
      x = m.distribution[sym] * this.length;
      if (sym != m.lastSymbol) {
        y = m.distribution[sym + 1] * this.length;
      }
    }

    else { // decode using only multiplications

      x = sym = 0;
      this.length >>>= DM_LENGTH_SHIFT;
      int k = (n = m.symbols) >>> 1;
      // decode via bisection search
      do {
        final int z = this.length * m.distribution[k];
        if (compareUnsigned(z, this.value) > 0) {
          n = k;
          y = z; // value is smaller
        } else {
          sym = k;
          x = z; // value is larger or equal
        }
      } while ((k = sym + n >>> 1) != sym);
    }

    this.value -= x; // update interval
    this.length = y - x;

    if (compareUnsigned(this.length, AC_MIN_LENGTH) < 0) {
      renorm_dec_interval(); // renormalization
    }

    ++m.symbolCount[sym];
    if (--m.symbolsUntilUpdate == 0) {
      m.update(); // periodic model update
    }

    assert compareUnsigned(sym, m.symbols) < 0;

    return sym;
  }

  public void done() {
    this.reader = null;
  }

  void initBitModel(final ArithmeticBitModel m) {
    m.init();
  }

  void initSymbolModel(final ArithmeticModel m) {
    initSymbolModel(m, null);
  }

  void initSymbolModel(final ArithmeticModel m, final int[] table) {
    m.init(table);
  }

  int readBit() {
    final int sym = Integer.divideUnsigned(this.value, this.length >>>= 1); // decode symbol,
                                                                            // change length
    this.value -= this.length * sym; // update interval

    if (compareUnsigned(this.length, AC_MIN_LENGTH) < 0) {
      renorm_dec_interval(); // renormalization
    }

    if (compareUnsigned(sym, 2) >= 0) {
      throw new RuntimeException("4711");
    }

    return sym;
  }

  int readBits(int bits) {
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
      renorm_dec_interval(); // renormalization
    }

    if (compareUnsigned(sym, 1 << bits) >= 0) {
      throw new RuntimeException("4711");
    }

    return sym;
  }

  byte readByte() {
    final int sym = Integer.divideUnsigned(this.value, this.length >>>= 8); // decode symbol,
                                                                            // change length
    this.value -= this.length * sym; // update interval

    if (compareUnsigned(this.length, AC_MIN_LENGTH) < 0) {
      renorm_dec_interval(); // renormalization
    }

    if (compareUnsigned(sym, 1 << 8) >= 0) {
      throw new RuntimeException("4711");
    }

    return (byte)sym;
  }

  double readDouble() /* danger in float reinterpretation */
  {
    return Double.longBitsToDouble(readInt64());
  }

  float readFloat() /* danger in float reinterpretation */
  {
    return Float.intBitsToFloat(readInt());
  }

  int readInt() {
    final int lowerInt = readShort();
    final int upperInt = readShort();
    return upperInt << 16 | lowerInt;
  }

  long readInt64() {
    final long lowerInt = readInt();
    final long upperInt = readInt();
    return upperInt << 32 | lowerInt;
  }

  char readShort() {
    final int sym = Integer.divideUnsigned(this.value, this.length >>>= 16); // decode symbol,
                                                                             // change length
    this.value -= this.length * sym; // update interval

    if (compareUnsigned(this.length, AC_MIN_LENGTH) < 0) {
      renorm_dec_interval(); // renormalization
    }

    if (compareUnsigned(sym, 1 << 16) >= 0) {
      throw new RuntimeException("4711");
    }

    return (char)sym;
  }

  private void renorm_dec_interval() {
    do { // read least-significant byte
      this.value = this.value << 8 | this.reader.getByte() & 0xff;
    } while (Integer.compareUnsigned(this.length <<= 8, AC_MIN_LENGTH) < 0); // length multiplied
                                                                             // by 256
  }

  public void reset() {
    this.reader.setByteOrder(ByteOrder.BIG_ENDIAN);
    this.length = AC_MAX_LENGTH;
    this.value = this.reader.getInt();
  }
}
