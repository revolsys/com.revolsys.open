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

import com.revolsys.io.channels.ChannelWriter;

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

public class ArithmeticEncoder implements ArithmeticConstants {

  private static final byte ZERO = 0;

  private ChannelWriter out;

  private final byte[] outbuffer = new byte[AC_BUFFER_SIZE];

  private final int endbuffer = AC_BUFFER_SIZE;

  private int outbyte;

  private int endbyte;

  private int base;

  private int value;

  private int length;

  public ArithmeticEncoder(final ChannelWriter out) {
    this.out = out;
  }

  public ArithmeticBitModel createBitModel() {
    return new ArithmeticBitModel();
  }

  ArithmeticModel createSymbolModel(final int symbols) {
    return new ArithmeticModel(symbols, true);
  }

  public void done() {
    final int init_base = this.base; // done encoding: set final data bytes
    boolean another_byte = true;

    if (Integer.compareUnsigned(this.length, 2 * AC_MIN_LENGTH) > 0) {
      this.base += AC_MIN_LENGTH; // base offset
      this.length = AC_MIN_LENGTH >>> 1; // set new length for 1 more byte
    } else {
      this.base += AC_MIN_LENGTH >>> 1; // base offset
      this.length = AC_MIN_LENGTH >>> 9; // set new length for 2 more bytes
      another_byte = false;
    }

    if (compareUnsigned(init_base, this.base) > 0) {
      propagate_carry(); // overflow = carry
    }
    renorm_enc_interval(); // renormalization = output last bytes

    /*
     * TODO doesn't make sense. is this really needed? if (endbyte != endbuffer) { assert(outbyte <
     * AC_BUFFER_SIZE); outstream.putBytes(outbuffer + AC_BUFFER_SIZE, AC_BUFFER_SIZE); }
     */
    final int buffer_size = this.outbyte;
    if (buffer_size > 0) {
      this.out.putBytes(this.outbuffer, buffer_size);
    }

    // write two or three zero bytes to be in sync with the decoder's byte reads
    this.out.putByte(ZERO);
    this.out.putByte(ZERO);
    if (another_byte) {
      this.out.putByte(ZERO);
    }

    this.out = null;
  }

  void encodeBit(final ArithmeticBitModel m, final int sym) {
    assert m != null && sym <= 1;

    final int x = m.bit0Prob * (this.length >>> BM_LENGTH_SHIFT); // product l x p0
    // update interval
    if (sym == 0) {
      this.length = x;
      ++m.bit0Count;
    } else {
      final int init_base = this.base;
      this.base += x;
      this.length -= x;
      if (compareUnsigned(init_base, this.base) > 0) {
        propagate_carry(); // overflow = carry
      }
    }

    if (compareUnsigned(this.length, AC_MIN_LENGTH) < 0) {
      renorm_enc_interval(); // renormalization
    }
    m.updateIfRequired();
  }

  void encodeSymbol(final ArithmeticModel m, final int sym) {
    assert m != null && compareUnsigned(sym, m.lastSymbol) <= 0;

    int x;
    final int init_base = this.base;
    // compute products
    if (sym == m.lastSymbol) {
      x = m.distribution[sym] * (this.length >>> DM_LENGTH_SHIFT);
      this.base += x; // update interval
      this.length -= x; // no product needed
    } else {
      x = m.distribution[sym] * (this.length >>>= DM_LENGTH_SHIFT);
      this.base += x; // update interval
      this.length = m.distribution[sym + 1] * this.length - x;
    }

    if (compareUnsigned(init_base, this.base) > 0) {
      propagate_carry(); // overflow = carry
    }
    if (compareUnsigned(this.length, AC_MIN_LENGTH) < 0) {
      renorm_enc_interval(); // renormalization
    }

    m.update(sym);
  }

  public boolean init() {
    this.base = 0;
    this.length = AC_MAX_LENGTH;
    this.outbyte = 0;
    this.endbyte = this.endbuffer;
    return true;
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

  private void manage_outbuffer() {
    if (this.outbyte == this.endbuffer) {
      this.outbyte = 0;
    }
    this.out.putBytes(this.outbuffer, AC_BUFFER_SIZE);
    this.endbyte = this.outbyte + AC_BUFFER_SIZE;
    assert this.endbyte > this.outbyte;
    assert this.outbyte < this.endbuffer;
  }

  private void propagate_carry() {
    int p;
    if (this.outbyte == 0) {
      p = this.endbuffer - 1;
    } else {
      p = this.outbyte - 1;
    }
    while (this.outbuffer[p] == 0xFF) {
      this.outbuffer[p] = 0;
      if (p == 0) {
        p = this.endbuffer - 1;
      } else {
        p--;
      }
      assert 0 <= p;
      assert p < this.endbuffer;
      assert this.outbyte < this.endbuffer;
    }
    ++this.outbuffer[p];
  }

  private void renorm_enc_interval() {
    do { // output and discard top byte
      assert 0 <= this.outbyte;
      assert this.outbyte < this.endbuffer;
      assert this.outbyte < this.endbyte;
      this.outbuffer[this.outbyte++] = (byte)(this.base >>> 24);
      if (this.outbyte == this.endbyte) {
        manage_outbuffer();
      }
      this.base <<= 8;
    } while (Integer.compareUnsigned(this.length <<= 8, AC_MIN_LENGTH) < 0); // length multiplied
                                                                             // by 256
  }

  void writeBit(final int sym) {
    assert sym < 2;

    final int init_base = this.base;
    this.base += sym * (this.length >>>= 1); // new interval base and length

    if (compareUnsigned(init_base, this.base) > 0) {
      propagate_carry(); // overflow = carry
    }
    if (compareUnsigned(this.length, AC_MIN_LENGTH) < 0) {
      renorm_enc_interval(); // renormalization
    }
  }

  void writeBits(int bits, int sym) {
    assert bits != 0 && bits <= 32 && sym < 1 << bits;

    if (bits > 19) {
      writeShort((short)(sym & 0xFFFF));
      sym = sym >>> 16;
      bits = bits - 16;
    }

    final int init_base = this.base;
    this.base += sym * (this.length >>>= bits); // new interval base and length

    if (compareUnsigned(init_base, this.base) > 0) {
      propagate_carry(); // overflow = carry
    }
    if (compareUnsigned(this.length, AC_MIN_LENGTH) < 0) {
      renorm_enc_interval(); // renormalization
    }
  }

  void writeByte(final byte sym) {
    final int init_base = this.base;
    this.base += sym * (this.length >>>= 8); // new interval base and length

    if (compareUnsigned(init_base, this.base) > 0) {
      propagate_carry(); // overflow = carry
    }
    if (compareUnsigned(this.length, AC_MIN_LENGTH) < 0) {
      renorm_enc_interval(); // renormalization
    }
  }

  void writeDouble(final double sym) /* danger in float reinterpretation */
  {
    writeInt64(Double.doubleToLongBits(sym));
  }

  void writeFloat(final float sym) /* danger in float reinterpretation */
  {
    writeInt(Float.floatToIntBits(sym));
  }

  void writeInt(final int sym) {
    writeShort((short)(sym & 0xFFFF)); // lower 16 bits
    writeShort((short)(sym >>> 16)); // UPPER 16 bits
  }

  void writeInt64(final long sym) {
    writeInt((int)(sym & 0xFFFFFFFF)); // lower 32 bits
    writeInt((int)(sym >>> 32)); // UPPER 32 bits
  }

  void writeShort(final short sym) {
    final int init_base = this.base;
    this.base += sym * (this.length >>>= 16); // new interval base and length

    if (compareUnsigned(init_base, this.base) > 0) {
      propagate_carry(); // overflow = carry
    }
    if (compareUnsigned(this.length, AC_MIN_LENGTH) < 0) {
      renorm_enc_interval(); // renormalization
    }
  }
}
