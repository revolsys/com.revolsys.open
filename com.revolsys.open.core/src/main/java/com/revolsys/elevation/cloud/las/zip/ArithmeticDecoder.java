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

import static com.revolsys.elevation.cloud.las.zip.ArithmeticModel.AC__MaxLength;
import static com.revolsys.elevation.cloud.las.zip.ArithmeticModel.AC__MinLength;
import static com.revolsys.elevation.cloud.las.zip.ArithmeticModel.BM__LengthShift;
import static com.revolsys.elevation.cloud.las.zip.ArithmeticModel.DM__LengthShift;
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

public class ArithmeticDecoder {

  private ChannelReader instream;

  private int u_value, u_length;

  public ArithmeticDecoder() {
    this.instream = null;
  }

  ArithmeticBitModel createBitModel() {
    return new ArithmeticBitModel();
  }

  ArithmeticModel createSymbolModel(final int n) {
    return new ArithmeticModel(n, false);
  }

  int decodeBit(final ArithmeticBitModel m) {
    assert m != null;

    final int u_x = m.u_bit_0_prob * (this.u_length >>> BM__LengthShift); // product
                                                                          // l x
                                                                          // p0
    final int u_sym = compareUnsigned(this.u_value, u_x) >= 0 ? 1 : 0; // decision
    // update & shift interval
    if (u_sym == 0) {
      this.u_length = u_x;
      ++m.u_bit_0_count;
    } else {
      this.u_value -= u_x; // shifted interval base = 0
      this.u_length -= u_x;
    }

    if (compareUnsigned(this.u_length, AC__MinLength) < 0) {
      renorm_dec_interval(); // renormalization
    }
    if (--m.u_bits_until_update == 0) {
      m.update(); // periodic model update
    }

    return u_sym; // return data bit value
  }

  int decodeSymbol(final ArithmeticModel m) {
    int u_n, u_sym, u_x, u_y = this.u_length;

    if (m.u_decoder_table != null) { // use table look-up for faster decoding

      final int u_dv = Integer.divideUnsigned(this.u_value, this.u_length >>>= DM__LengthShift);
      final int t = u_dv >>> m.u_table_shift;

      u_sym = m.u_decoder_table[t]; // initial decision based on table look-up
      u_n = m.u_decoder_table[t + 1] + 1;

      while (compareUnsigned(u_n, u_sym + 1) > 0) { // finish with bisection
                                                    // search
        final int u_k = u_sym + u_n >>> 1;
        if (compareUnsigned(m.u_distribution[u_k], u_dv) > 0) {
          u_n = u_k;
        } else {
          u_sym = u_k;
        }
      }
      // compute products
      u_x = m.u_distribution[u_sym] * this.u_length;
      if (u_sym != m.u_last_symbol) {
        u_y = m.u_distribution[u_sym + 1] * this.u_length;
      }
    }

    else { // decode using only multiplications

      u_x = u_sym = 0;
      this.u_length >>>= DM__LengthShift;
      int u_k = (u_n = m.u_symbols) >>> 1;
      // decode via bisection search
      do {
        final int u_z = this.u_length * m.u_distribution[u_k];
        if (compareUnsigned(u_z, this.u_value) > 0) {
          u_n = u_k;
          u_y = u_z; // value is smaller
        } else {
          u_sym = u_k;
          u_x = u_z; // value is larger or equal
        }
      } while ((u_k = u_sym + u_n >>> 1) != u_sym);
    }

    this.u_value -= u_x; // update interval
    this.u_length = u_y - u_x;

    if (compareUnsigned(this.u_length, AC__MinLength) < 0) {
      renorm_dec_interval(); // renormalization
    }

    ++m.u_symbol_count[u_sym];
    if (--m.u_symbols_until_update == 0) {
      m.update(); // periodic model update
    }

    assert compareUnsigned(u_sym, m.u_symbols) < 0;

    return u_sym;
  }

  public void done() {
    this.instream = null;
  }

  public boolean init(final ChannelReader instream) {
    if (instream == null) {
      return false;
    }
    this.instream = instream;
    this.u_length = AC__MaxLength;
    this.u_value = (instream.getByte() & 0xff) << 24;
    this.u_value |= (instream.getByte() & 0xff) << 16;
    this.u_value |= (instream.getByte() & 0xff) << 8;
    this.u_value |= instream.getByte() & 0xff;
    return true;
  }

  void initBitModel(final ArithmeticBitModel m) {
    m.init();
  }

  void initSymbolModel(final ArithmeticModel m) {
    initSymbolModel(m, null);
  }

  void initSymbolModel(final ArithmeticModel m, final int[] u_table) {
    m.init(u_table);
  }

  int readBit() {
    final int u_sym = Integer.divideUnsigned(this.u_value, this.u_length >>>= 1); // decode
                                                                                  // symbol,
                                                                                  // change
                                                                                  // length
    this.u_value -= this.u_length * u_sym; // update interval

    if (compareUnsigned(this.u_length, AC__MinLength) < 0) {
      renorm_dec_interval(); // renormalization
    }

    if (compareUnsigned(u_sym, 2) >= 0) {
      throw new IllegalStateException("Error decompressing LAZ file");
    }

    return u_sym;
  }

  int readBits(int u_bits) {
    assert u_bits != 0 && u_bits <= 32;

    if (u_bits > 19) {
      final int u_tmp = readShort();
      u_bits = u_bits - 16;
      final int u_tmp1 = readBits(u_bits) << 16;
      return u_tmp1 | u_tmp;
    }

    final int u_sym = Integer.divideUnsigned(this.u_value, this.u_length >>>= u_bits);// decode
                                                                                      // symbol,
                                                                                      // change
                                                                                      // length
    this.u_value -= this.u_length * u_sym; // update interval

    if (compareUnsigned(this.u_length, AC__MinLength) < 0) {
      renorm_dec_interval(); // renormalization
    }

    if (compareUnsigned(u_sym, 1 << u_bits) >= 0) {
      throw new IllegalStateException("Error decompressing LAZ file");
    }

    return u_sym;
  }

  byte readByte() {
    final int u_sym = Integer.divideUnsigned(this.u_value, this.u_length >>>= 8); // decode
                                                                                  // symbol,
                                                                                  // change
                                                                                  // length
    this.u_value -= this.u_length * u_sym; // update interval

    if (compareUnsigned(this.u_length, AC__MinLength) < 0) {
      renorm_dec_interval(); // renormalization
    }

    if (compareUnsigned(u_sym, 1 << 8) >= 0) {
      throw new IllegalStateException("Error decompressing LAZ file");
    }

    return (byte)u_sym;
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
    final int u_lowerInt = readShort();
    final int u_upperInt = readShort();
    return u_upperInt << 16 | u_lowerInt;
  }

  long readInt64() {
    final long u_lowerInt = readInt();
    final long u_upperInt = readInt();
    return u_upperInt << 32 | u_lowerInt;
  }

  char readShort() {
    final int u_sym = Integer.divideUnsigned(this.u_value, this.u_length >>>= 16); // decode
                                                                                   // symbol,
                                                                                   // change
                                                                                   // length
    this.u_value -= this.u_length * u_sym; // update interval

    if (compareUnsigned(this.u_length, AC__MinLength) < 0) {
      renorm_dec_interval(); // renormalization
    }

    if (compareUnsigned(u_sym, 1 << 16) >= 0) {
      throw new IllegalStateException("Error decompressing LAZ file");
    }

    return (char)u_sym;
  }

  private void renorm_dec_interval() {
    do { // read least-significant byte
      this.u_value = this.u_value << 8 | this.instream.getByte() & 0xff;
    } while (Integer.compareUnsigned(this.u_length <<= 8, AC__MinLength) < 0); // length
                                                                               // multiplied
                                                                               // by
                                                                               // 256
  }
}
