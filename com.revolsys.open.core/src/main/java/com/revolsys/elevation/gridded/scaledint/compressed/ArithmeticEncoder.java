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
package com.revolsys.elevation.gridded.scaledint.compressed;

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

public class ArithmeticEncoder {

  private static final byte ZERO = 0;

  private static final int AC_BUFFER_SIZE = 1024;

  private static final int AC__MinLength = 0x01000000; // threshold for
                                                       // renormalization

  private static final int AC__MaxLength = 0xFFFFFFFF; // maximum AC interval
                                                       // length

  private static final int BM__LengthShift = 13; // length bits discarded
                                                 // before
                                                 // mult.

  private ChannelWriter writer;

  private final byte[] outbuffer;

  private final int endbuffer;

  private int outbyte;

  private int endbyte;

  private int u_base;

  private int u_length;

  public ArithmeticEncoder(final ChannelWriter writer) {
    this.outbuffer = new byte[AC_BUFFER_SIZE];
    this.endbuffer = this.outbuffer.length;
    this.writer = writer;
    this.u_base = 0;
    this.u_length = AC__MaxLength;
    this.outbyte = 0;
    this.endbyte = this.endbuffer;
  }

  public void done() {
    final int u_init_base = this.u_base; // done encoding: set final data bytes
    boolean another_byte = true;

    if (Integer.compareUnsigned(this.u_length, 2 * AC__MinLength) > 0) {
      this.u_base += AC__MinLength; // base offset
      this.u_length = AC__MinLength >>> 1; // set new length for 1 more byte
    } else {
      this.u_base += AC__MinLength >>> 1; // base offset
      this.u_length = AC__MinLength >>> 9; // set new length for 2 more bytes
      another_byte = false;
    }

    if (compareUnsigned(u_init_base, this.u_base) > 0) {
      propagate_carry(); // overflow = carry
    }
    renorm_enc_interval(); // renormalization = output last bytes

    /*
     * TODO doesn't make sense. is this really needed? if (endbyte != endbuffer)
     * { assert(outbyte < AC_BUFFER_SIZE); writer.putBytes(outbuffer +
     * AC_BUFFER_SIZE, AC_BUFFER_SIZE); }
     */
    final int buffer_size = this.outbyte;
    if (buffer_size > 0) {
      this.writer.putBytes(this.outbuffer, buffer_size);
    }

    // write two or three zero bytes to be in sync with the decoder's byte reads
    this.writer.putByte(ZERO);
    this.writer.putByte(ZERO);
    if (another_byte) {
      this.writer.putByte(ZERO);
    }

    this.writer = null;
  }

  public void encodeBit(final ArithmeticBitModel m, final int sym) {
    final int u_x = m.bit0Probability * (this.u_length >>> BM__LengthShift); // product
    // l x
    // p0
    // update interval
    if (sym == 0) {
      this.u_length = u_x;
      ++m.bit0Count;
    } else {
      final int u_init_base = this.u_base;
      this.u_base += u_x;
      this.u_length -= u_x;
      if (compareUnsigned(u_init_base, this.u_base) > 0) {
        propagate_carry(); // overflow = carry
      }
    }

    if (compareUnsigned(this.u_length, AC__MinLength) < 0) {
      renorm_enc_interval(); // renormalization
    }
    m.update();
  }

  public void encodeSymbol(final ArithmeticModel m, final int symbol) {
    int x;
    final int initialBase = this.u_base;
    // compute products
    if (symbol == m.lastSymbol) {
      x = m.distribution[symbol] * (this.u_length >>> ArithmeticModel.DM__LengthShift);
      this.u_base += x; // update interval
      this.u_length -= x; // no product needed
    } else {
      x = m.distribution[symbol] * (this.u_length >>>= ArithmeticModel.DM__LengthShift);
      this.u_base += x; // update interval
      this.u_length = m.distribution[symbol + 1] * this.u_length - x;
    }

    if (compareUnsigned(initialBase, this.u_base) > 0) {
      propagate_carry(); // overflow = carry
    }
    if (compareUnsigned(this.u_length, AC__MinLength) < 0) {
      renorm_enc_interval(); // renormalization
    }

    m.addCount(symbol);
  }

  private void manage_outbuffer() {
    if (this.outbyte == this.endbuffer) {
      this.outbyte = 0;
    }
    this.writer.putBytes(this.outbuffer, AC_BUFFER_SIZE);
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
      this.outbuffer[this.outbyte++] = (byte)(this.u_base >>> 24);
      if (this.outbyte == this.endbyte) {
        manage_outbuffer();
      }
      this.u_base <<= 8;
    } while (Integer.compareUnsigned(this.u_length <<= 8, AC__MinLength) < 0); // length
                                                                               // multiplied
                                                                               // by
                                                                               // 256
  }

  public void writeBits(int bits, int symbol) {
    if (bits > 19) {
      writeShort((short)(symbol & 0xFFFF));
      symbol = symbol >>> 16;
      bits = bits - 16;
    }

    final int u_init_base = this.u_base;
    this.u_base += symbol * (this.u_length >>>= bits); // new interval base and
                                                       // length

    if (compareUnsigned(u_init_base, this.u_base) > 0) {
      propagate_carry(); // overflow = carry
    }
    if (compareUnsigned(this.u_length, AC__MinLength) < 0) {
      renorm_enc_interval(); // renormalization
    }
  }

  private void writeShort(final short u_sym) {
    final int u_init_base = this.u_base;
    this.u_base += u_sym * (this.u_length >>>= 16); // new interval base and
                                                    // length

    if (compareUnsigned(u_init_base, this.u_base) > 0) {
      propagate_carry(); // overflow = carry
    }
    if (compareUnsigned(this.u_length, AC__MinLength) < 0) {
      renorm_enc_interval(); // renormalization
    }
  }
}
