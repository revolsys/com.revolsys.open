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

public class ArithmeticCodingCompressModel {

  static final int AC_BUFFER_SIZE = 1024;

  static final int AC__MinLength = 0x01000000; // threshold for renormalization

  static final int AC__MaxLength = 0xFFFFFFFF; // maximum AC interval length

  static final int BM__LengthShift = 13; // length bits discarded before mult.

  static final int BM__MaxCount = 1 << BM__LengthShift; // for adaptive models

  static final int DM__LengthShift = 15; // length bits discarded before mult.

  static final int DM__MaxCount = 1 << DM__LengthShift; // for adaptive models

  private final boolean compress;

  public int[] distribution, symbol_count, decoder_table;

  public int total_count;

  public int update_cycle;

  public int symbols_until_update;

  public int symbols;

  public int last_symbol;

  public int table_size;

  public int table_shift;

  public ArithmeticCodingCompressModel(final int symbols, final boolean compress) {
    this.symbols = symbols;
    this.compress = compress;
    if (symbols < 2 || symbols > 1 << 11) {
      throw new IllegalArgumentException();
    }
    this.last_symbol = symbols - 1;
    if (!this.compress && symbols > 16) {
      int table_bits = 3;
      while (symbols > 1 << table_bits + 2) {
        ++table_bits;
      }
      this.table_size = 1 << table_bits;
      this.table_shift = DM__LengthShift - table_bits;
      this.distribution = new int[symbols];
      this.decoder_table = new int[this.table_size + 2];
    } else // small alphabet: no table needed
    {
      this.decoder_table = null;
      this.table_size = this.table_shift = 0;
      this.distribution = new int[symbols];
    }
    this.symbol_count = new int[symbols];

    this.total_count = 0;
    this.update_cycle = symbols;
    for (int k = 0; k < symbols; k++) {
      this.symbol_count[k] = 1;
    }

    update();
    this.symbols_until_update = this.update_cycle = symbols + 6 >>> 1;
  }

  public void update() {
    // halve counts when a threshold is reached
    if ((this.total_count += this.update_cycle) > DM__MaxCount) {
      this.total_count = 0;
      for (int n = 0; n < this.symbols; n++) {
        this.total_count += this.symbol_count[n] = this.symbol_count[n] + 1 >>> 1;
      }
    }

    // compute cumulative distribution, decoder table
    int sum = 0;
    int s = 0;
    final int scale = Integer.divideUnsigned(0x80000000, this.total_count);

    if (this.compress || this.table_size == 0) {
      for (int k = 0; k < this.symbols; k++) {
        this.distribution[k] = scale * sum >>> 31 - DM__LengthShift;
        sum += this.symbol_count[k];
      }
    } else {
      for (int k = 0; k < this.symbols; k++) {
        this.distribution[k] = scale * sum >>> 31 - DM__LengthShift;
        sum += this.symbol_count[k];
        final int w = this.distribution[k] >>> this.table_shift;
        while (s < w) {
          this.decoder_table[++s] = k - 1;
        }
      }
      this.decoder_table[0] = 0;
      while (s <= this.table_size) {
        this.decoder_table[++s] = this.symbols - 1;
      }
    }

    // set frequency of model updates
    this.update_cycle = 5 * this.update_cycle >>> 2;
    final int max_cycle = this.symbols + 6 << 3;
    if (Integer.compareUnsigned(this.update_cycle, max_cycle) > 0) {
      this.update_cycle = max_cycle;
    }
    this.symbols_until_update = this.update_cycle;
  }
}
