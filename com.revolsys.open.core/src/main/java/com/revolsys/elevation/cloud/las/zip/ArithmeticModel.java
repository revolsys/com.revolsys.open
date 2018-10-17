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

public class ArithmeticModel {

  static final int AC_BUFFER_SIZE = 1024;

  static final int AC__MinLength = 0x01000000; // threshold for renormalization

  static final int AC__MaxLength = 0xFFFFFFFF; // maximum AC interval length

  static final int BM__LengthShift = 13; // length bits discarded before mult.

  static final int BM__MaxCount = 1 << BM__LengthShift; // for adaptive models

  static final int DM__LengthShift = 15; // length bits discarded before mult.

  static final int DM__MaxCount = 1 << DM__LengthShift; // for adaptive models

  private final boolean compress;

  int[] u_distribution, u_symbol_count, u_decoder_table;

  int u_total_count, u_update_cycle, u_symbols_until_update;

  int u_symbols, u_last_symbol, u_table_size, u_table_shift;

  public ArithmeticModel(final int u_symbols, final boolean compress) {
    this.u_symbols = u_symbols;
    this.compress = compress;
  }

  public int init(final int[] u_table) {
    if (this.u_distribution == null) {
      if (this.u_symbols < 2 || this.u_symbols > 1 << 11) {
        return -1; // invalid number of symbols
      }
      this.u_last_symbol = this.u_symbols - 1;
      if (!this.compress && this.u_symbols > 16) {
        int table_bits = 3;
        while (this.u_symbols > 1 << table_bits + 2) {
          ++table_bits;
        }
        this.u_table_size = 1 << table_bits;
        this.u_table_shift = DM__LengthShift - table_bits;
        this.u_distribution = new int[this.u_symbols];
        this.u_decoder_table = new int[this.u_table_size + 2];
      } else // small alphabet: no table needed
      {
        this.u_decoder_table = null;
        this.u_table_size = this.u_table_shift = 0;
        this.u_distribution = new int[this.u_symbols];
      }
      this.u_symbol_count = new int[this.u_symbols];
    }

    this.u_total_count = 0;
    this.u_update_cycle = this.u_symbols;
    if (u_table != null) {
      for (int k = 0; k < this.u_symbols; k++) {
        this.u_symbol_count[k] = u_table[k];
      }
    } else {
      for (int k = 0; k < this.u_symbols; k++) {
        this.u_symbol_count[k] = 1;
      }
    }

    update();
    this.u_symbols_until_update = this.u_update_cycle = this.u_symbols + 6 >>> 1;

    return 0;
  }

  void update() {
    // halve counts when a threshold is reached
    if ((this.u_total_count += this.u_update_cycle) > DM__MaxCount) {
      this.u_total_count = 0;
      for (int n = 0; n < this.u_symbols; n++) {
        this.u_total_count += this.u_symbol_count[n] = this.u_symbol_count[n] + 1 >>> 1;
      }
    }

    // compute cumulative distribution, decoder table
    int k, sum = 0, s = 0;
    final int scale = Integer.divideUnsigned(0x80000000, this.u_total_count);

    if (this.compress || this.u_table_size == 0) {
      for (k = 0; k < this.u_symbols; k++) {
        this.u_distribution[k] = scale * sum >>> 31 - DM__LengthShift;
        sum += this.u_symbol_count[k];
      }
    } else {
      for (k = 0; k < this.u_symbols; k++) {
        this.u_distribution[k] = scale * sum >>> 31 - DM__LengthShift;
        sum += this.u_symbol_count[k];
        final int w = this.u_distribution[k] >>> this.u_table_shift;
        while (s < w) {
          this.u_decoder_table[++s] = k - 1;
        }
      }
      this.u_decoder_table[0] = 0;
      while (s <= this.u_table_size) {
        this.u_decoder_table[++s] = this.u_symbols - 1;
      }
    }

    // set frequency of model updates
    this.u_update_cycle = 5 * this.u_update_cycle >>> 2;
    final int max_cycle = this.u_symbols + 6 << 3;
    if (Integer.compareUnsigned(this.u_update_cycle, max_cycle) > 0) {
      this.u_update_cycle = max_cycle;
    }
    this.u_symbols_until_update = this.u_update_cycle;
  }
}
