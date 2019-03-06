/*
  CONTENTS:

    A modular C++ wrapper for an adapted version of Amir Said's FastAC Code.
    see: http://www.cipr.rpi.edu/~said/FastAC.html

  PROGRAMMERS:

    martin.isenburg@rapidlasso.com  -  http://rapidlasso.com

  COPYRIGHT:

    (c) 2007-2017, martin isenburg, rapidlasso - fast tools to catch reality

    This is free software; you can redistribute and/or modify it under the
    terms of the GNU Lesser General Licence as published by the Free Software
    Foundation. See the COPYING file for more information.

    This software is distributed WITHOUT ANY WARRANTY and without even the
    implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
*/
package com.revolsys.math.arithmeticcoding;

import com.revolsys.io.BaseCloseable;
import com.revolsys.io.channels.ChannelWriter;
import com.revolsys.util.Debug;

public class ArithmeticCodingEncoder implements ArithmeticCodingCodec, BaseCloseable {

  private static final int AC_BUFFER_SIZE = 1024;

  private static final int AC__MinLength = 0x01000000; // threshold for
                                                       // renormalization

  private static final int AC__MaxLength = 0xFFFFFFFF; // maximum AC interval
                                                       // length

  // Maximum values for binary models
  private static final int BM__LengthShift = 13; // length bits discarded
                                                 // before mult.

  // Maximum values for general models
  private static final int DM__LengthShift = 15; // length bits discarded
                                                 // before mult.

  private ChannelWriter writer;

  private final byte[] outBuffer = new byte[2 * AC_BUFFER_SIZE];

  private final int endBufferIndex = 2 * AC_BUFFER_SIZE;

  private int outByteIndex = 0;

  private int endByteIndex = this.endBufferIndex;

  private int base = 0;

  private int length = AC__MaxLength;

  public ArithmeticCodingEncoder() {
  }

  public ArithmeticCodingEncoder(final ChannelWriter writer) {
    this.writer = writer;
  }

  @Override
  public void close() {
    boolean another_byte = true;

    if (Integer.compareUnsigned(this.length, 2 * AC__MinLength) > 0) {
      this.length = AC__MinLength >>> 1; // set new length for 1 more byte
      setBase(this.base + AC__MinLength);
    } else {
      this.length = AC__MinLength >>> 9; // set new length for 2 more bytes
      another_byte = false;
      setBase(this.base + (AC__MinLength >>> 1));
    }

    renorm_enc_interval(); // renormalization = output last bytes

    if (this.endByteIndex != this.endBufferIndex) {

      this.writer.putBytes(this.outBuffer, AC_BUFFER_SIZE, AC_BUFFER_SIZE);
    }
    final int buffer_size = this.outByteIndex;
    if (buffer_size != 0) {
      this.writer.putBytes(this.outBuffer, buffer_size);
    }

    // write two or three zero bytes to be in sync with the decoder's byte reads
    this.writer.putByte((byte)0);
    this.writer.putByte((byte)0);
    if (another_byte) {
      this.writer.putByte((byte)0);
    }

    this.writer = null;
  }

  @Override
  public ArithmeticModel createSymbolModel(final int symbolCount) {
    return new ArithmeticModel(symbolCount, true);
  }

  /* Encode a bit with modelling */
  public void encodeBit(final ArithmeticBitModel m, final int sym) {
    final int x = m.bit0Probability * (this.length >>> BM__LengthShift);
    int length;
    if (sym == 0) {
      length = x;
      ++m.bit0Count;
    } else {
      length = this.length - x;
      setBase(this.base + x);
    }

    setLength(length);
    m.update();
  }

  /* Encode a symbol with modelling */
  public void encodeSymbol(final ArithmeticModel m, final int sym) {
    int x;
    int length = this.length;
    if (sym == m.lastSymbol) {
      x = m.distribution[sym] * (length >>> DM__LengthShift);
      length -= x; // no product needed
    } else {
      x = m.distribution[sym] * (length >>>= DM__LengthShift);
      length = m.distribution[sym + 1] * length - x;
    }
    setBase(this.base + x);
    setLength(length);

    ++m.symbolCounts[sym];
    if (--m.symbolsUntilUpdate == 0) {
      m.update(); // periodic model update
    }
  }

  private void manage_outbuffer() {
    if (this.outByteIndex == this.endBufferIndex) {
      this.outByteIndex = 0;
    }
    this.writer.putBytes(this.outBuffer, this.outByteIndex, AC_BUFFER_SIZE);
    this.endByteIndex = this.outByteIndex + AC_BUFFER_SIZE;
  }

  private void renorm_enc_interval() {
    do { // output and discard top byte
      final int outValue = this.base >>> 24;
      this.outBuffer[this.outByteIndex++] = (byte)outValue;
      if (this.outByteIndex == this.endByteIndex) {
        manage_outbuffer();
      }
      this.base <<= 8;
      this.length <<= 8;// length multiplied by 256
    } while (Integer.compareUnsigned(this.length, AC__MinLength) < 0);
  }

  private void setBase(final int base) {
    final int init_base = this.base;
    this.base = base;
    if (Integer.compareUnsigned(init_base, this.base) > 0) {
      int index;
      if (this.outByteIndex == 0) {
        index = this.endBufferIndex - 1;
      } else {
        index = this.outByteIndex - 1;
      }
      while (this.outBuffer[index] == (byte)0xFF) {
        this.outBuffer[index] = 0;
        if (index == 0) {
          index = this.endBufferIndex - 1;
        } else {
          index--;
        }
      }
      ++this.outBuffer[index]; // overflow = carry
    }
  }

  private void setLength(final int length) {
    this.length = length;
    if (Integer.compareUnsigned(length, AC__MinLength) < 0) {
      renorm_enc_interval();
    }
  }

  /* Encode bits without modelling */
  public void writeBits(int bits, int sym) {
    if (bits > 19) {
      writeShort(sym & 0xFFFF);
      sym = sym >>> 16;
      bits = bits - 16;
    }

    final int length = this.length >>> bits;
    setBase(this.base + sym * length); // new interval base and
    setLength(length);
  }

  public void writeInt(final int sym) {
    writeShort(sym & 0xFFFF); // lower 16 bits
    writeShort(sym >>> 16); // UPPER 16 bits
  }

  public void writeInt64(final long sym) {
    final long lower = sym & 0xFFFFFFFFL;
    final long upper = sym >>> 32;
    Debug.println(sym + "\t" + lower + "\t" + upper);
    writeInt((int)lower); // lower 32 bits
    writeInt((int)upper); // UPPER 32 bits
  }

  /* Encode an unsigned short without modelling */
  public void writeShort(final int sym) {
    final int length = this.length >>> 16;
    setBase(this.base + sym * length);
    setLength(length);
  }

}
