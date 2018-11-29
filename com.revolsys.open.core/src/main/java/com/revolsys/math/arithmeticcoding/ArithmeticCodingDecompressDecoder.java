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
package com.revolsys.math.arithmeticcoding;

import static com.revolsys.math.arithmeticcoding.ArithmeticCodingDecompressModel.AC__MaxLength;
import static com.revolsys.math.arithmeticcoding.ArithmeticCodingDecompressModel.AC__MinLength;
import static com.revolsys.math.arithmeticcoding.ArithmeticCodingDecompressModel.BM__LengthShift;
import static com.revolsys.math.arithmeticcoding.ArithmeticCodingDecompressModel.DM__LengthShift;
import static java.lang.Integer.compareUnsigned;

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

public class ArithmeticCodingDecompressDecoder {

  private ChannelReader reader;

  private int value;

  private int length;

  public ArithmeticCodingDecompressDecoder() {
    this.reader = null;
  }

  public ArithmeticCodingDecompressModel createSymbolModel(final int n) {
    return new ArithmeticCodingDecompressModel(n);
  }

  public int decodeBit(final ArithmeticCodingBitModel model) {
    final int u_x = model.bit0Probability * (this.length >>> BM__LengthShift); // product
    // l x
    // p0
    final int u_sym = compareUnsigned(this.value, u_x) >= 0 ? 1 : 0; // decision
    // update & shift interval
    if (u_sym == 0) {
      this.length = u_x;
      ++model.bit0Count;
    } else {
      this.value -= u_x; // shifted interval base = 0
      this.length -= u_x;
    }

    if (compareUnsigned(this.length, AC__MinLength) < 0) {
      renorm_dec_interval(); // renormalization
    }
    model.update();

    return u_sym; // return data bit value
  }

  public int decodeSymbol(final ArithmeticCodingDecompressModel model) {
    int u_n, u_sym, u_x, u_y = this.length;

    if (model.decoderTable != null) { // use table look-up for faster decoding

      final int u_dv = Integer.divideUnsigned(this.value, this.length >>>= DM__LengthShift);
      final int t = u_dv >>> model.tableShift;

      u_sym = model.decoderTable[t]; // initial decision based on table look-up
      u_n = model.decoderTable[t + 1] + 1;

      while (compareUnsigned(u_n, u_sym + 1) > 0) { // finish with bisection
                                                    // search
        final int u_k = u_sym + u_n >>> 1;
        if (compareUnsigned(model.distribution[u_k], u_dv) > 0) {
          u_n = u_k;
        } else {
          u_sym = u_k;
        }
      }
      // compute products
      u_x = model.distribution[u_sym] * this.length;
      if (u_sym != model.lastSymbol) {
        u_y = model.distribution[u_sym + 1] * this.length;
      }
    } else { // decode using only multiplications

      u_x = u_sym = 0;
      this.length >>>= DM__LengthShift;
      int u_k = (u_n = model.symbolCount) >>> 1;
      // decode via bisection search
      do {
        final int u_z = this.length * model.distribution[u_k];
        if (compareUnsigned(u_z, this.value) > 0) {
          u_n = u_k;
          u_y = u_z; // value is smaller
        } else {
          u_sym = u_k;
          u_x = u_z; // value is larger or equal
        }
      } while ((u_k = u_sym + u_n >>> 1) != u_sym);
    }

    this.value -= u_x; // update interval
    this.length = u_y - u_x;

    if (compareUnsigned(this.length, AC__MinLength) < 0) {
      renorm_dec_interval(); // renormalization
    }

    ++model.symbolCounts[u_sym];
    if (--model.symbolsUntilUpdate == 0) {
      model.update(); // periodic model update
    }

    return u_sym;
  }

  public boolean init(final ChannelReader reader) {
    if (reader == null) {
      return false;
    }
    this.reader = reader;
    this.length = AC__MaxLength;
    this.value = (reader.getByte() & 0xff) << 24;
    this.value |= (reader.getByte() & 0xff) << 16;
    this.value |= (reader.getByte() & 0xff) << 8;
    this.value |= reader.getByte() & 0xff;
    return true;
  }

  public int readBit() {
    final int u_sym = Integer.divideUnsigned(this.value, this.length >>>= 1); // decode
                                                                              // symbol,
                                                                              // change
                                                                              // length
    this.value -= this.length * u_sym; // update interval

    if (compareUnsigned(this.length, AC__MinLength) < 0) {
      renorm_dec_interval(); // renormalization
    }

    if (compareUnsigned(u_sym, 2) >= 0) {
      throw new IllegalStateException("Error decompressing file");
    }

    return u_sym;
  }

  public int readBits(int u_bits) {

    if (u_bits > 19) {
      final int u_tmp = readShort();
      u_bits = u_bits - 16;
      final int u_tmp1 = readBits(u_bits) << 16;
      return u_tmp1 | u_tmp;
    }

    final int u_sym = Integer.divideUnsigned(this.value, this.length >>>= u_bits);// decode
                                                                                  // symbol,
                                                                                  // change
                                                                                  // length
    this.value -= this.length * u_sym; // update interval

    if (compareUnsigned(this.length, AC__MinLength) < 0) {
      renorm_dec_interval(); // renormalization
    }

    if (compareUnsigned(u_sym, 1 << u_bits) >= 0) {
      throw new IllegalStateException("Error decompressing LAZ file");
    }

    return u_sym;
  }

  public byte readByte() {
    final int u_sym = Integer.divideUnsigned(this.value, this.length >>>= 8); // decode
                                                                              // symbol,
                                                                              // change
                                                                              // length
    this.value -= this.length * u_sym; // update interval

    if (compareUnsigned(this.length, AC__MinLength) < 0) {
      renorm_dec_interval(); // renormalization
    }

    if (compareUnsigned(u_sym, 1 << 8) >= 0) {
      throw new IllegalStateException("Error decompressing LAZ file");
    }

    return (byte)u_sym;
  }

  public double readDouble() {
    // danger in float reinterpretation
    return Double.longBitsToDouble(readInt64());
  }

  public float readFloat() {
    // danger in float reinterpretation
    return Float.intBitsToFloat(readInt());
  }

  public int readInt() {
    final int u_lowerInt = readShort();
    final int u_upperInt = readShort();
    return u_upperInt << 16 | u_lowerInt;
  }

  public long readInt64() {
    final long u_lowerInt = readInt();
    final long u_upperInt = readInt();
    return u_upperInt << 32 | u_lowerInt;
  }

  public char readShort() {
    final int u_sym = Integer.divideUnsigned(this.value, this.length >>>= 16); // decode
                                                                               // symbol,
                                                                               // change
                                                                               // length
    this.value -= this.length * u_sym; // update interval

    if (compareUnsigned(this.length, AC__MinLength) < 0) {
      renorm_dec_interval(); // renormalization
    }

    if (compareUnsigned(u_sym, 1 << 16) >= 0) {
      throw new IllegalStateException("Error decompressing file");
    }

    return (char)u_sym;
  }

  private void renorm_dec_interval() {
    do { // read least-significant byte
      this.value = this.value << 8 | this.reader.getByte() & 0xff;
    } while (Integer.compareUnsigned(this.length <<= 8, AC__MinLength) < 0); // length
                                                                             // multiplied
                                                                             // by
                                                                             // 256
  }
}
