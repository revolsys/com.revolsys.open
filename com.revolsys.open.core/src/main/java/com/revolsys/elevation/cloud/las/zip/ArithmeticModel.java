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

public class ArithmeticModel implements ArithmeticConstants {

  public static void initSymbolModel(final ArithmeticModel m) {
    ArithmeticModel.initSymbolModel(m, null);
  }

  public static void initSymbolModel(final ArithmeticModel model, final int[] table) {
    if (model != null) {
      model.init(table);
    }
  }

  public static void initSymbolModels(final ArithmeticModel... models) {
    for (final ArithmeticModel model : models) {
      if (model != null) {
        model.init(null);
      }
    }
  }

  private final boolean compress;

  int[] distribution;

  int[] symbolCount;

  int[] decoderTable;

  int totalCount;

  int updateCycle;

  int symbolsUntilUpdate;

  final int symbols;

  int lastSymbol;

  int tableSize;

  int tableShift;

  public ArithmeticModel(final int symbols, final boolean compress) {
    this.symbols = symbols;
    this.compress = compress;
  }

  public int init(final int[] table) {
    if (this.distribution == null) {
      if (this.symbols < 2 || this.symbols > 1 << 11) {
        return -1; // invalid number of symbols
      }
      this.lastSymbol = this.symbols - 1;
      if (!this.compress && this.symbols > 16) {
        int table_bits = 3;
        while (this.symbols > 1 << table_bits + 2) {
          ++table_bits;
        }
        this.tableSize = 1 << table_bits;
        this.tableShift = DM_LENGTH_SHIFT - table_bits;
        this.distribution = new int[this.symbols];
        this.decoderTable = new int[this.tableSize + 2];
      } else {
        // small alphabet: no table needed
        this.decoderTable = null;
        this.tableSize = this.tableShift = 0;
        this.distribution = new int[this.symbols];
      }
      this.symbolCount = new int[this.symbols];
    }

    this.totalCount = 0;
    this.updateCycle = this.symbols;
    if (table != null) {
      for (int k = 0; k < this.symbols; k++) {
        this.symbolCount[k] = table[k];
      }
    } else {
      for (int k = 0; k < this.symbols; k++) {
        this.symbolCount[k] = 1;
      }
    }

    update();
    this.symbolsUntilUpdate = this.updateCycle = this.symbols + 6 >>> 1;

    return 0;
  }

  void update() {
    // halve counts when a threshold is reached
    final int[] symbolCount = this.symbolCount;
    if ((this.totalCount += this.updateCycle) > DM_MAX_COUNT) {
      this.totalCount = 0;
      for (int n = 0; n < this.symbols; n++) {
        this.totalCount += symbolCount[n] = symbolCount[n] + 1 >>> 1;
      }
    }

    // compute cumulative distribution, decoder table
    int k, sum = 0, s = 0;
    final int scale = Integer.divideUnsigned(0x80000000, this.totalCount);

    final int[] distribution = this.distribution;
    if (this.compress || this.tableSize == 0) {
      for (k = 0; k < this.symbols; k++) {
        distribution[k] = scale * sum >>> 31 - DM_LENGTH_SHIFT;
        sum += symbolCount[k];
      }
    } else {
      final int[] decoderTable2 = this.decoderTable;
      for (k = 0; k < this.symbols; k++) {
        distribution[k] = scale * sum >>> 31 - DM_LENGTH_SHIFT;
        sum += symbolCount[k];
        final int w = distribution[k] >>> this.tableShift;
        while (s < w) {
          decoderTable2[++s] = k - 1;
        }
      }
      decoderTable2[0] = 0;
      while (s <= this.tableSize) {
        decoderTable2[++s] = this.symbols - 1;
      }
    }

    // set frequency of model updates
    this.updateCycle = 5 * this.updateCycle >>> 2;
    final int max_cycle = this.symbols + 6 << 3;
    if (Integer.compareUnsigned(this.updateCycle, max_cycle) > 0) {
      this.updateCycle = max_cycle;
    }
    this.symbolsUntilUpdate = this.updateCycle;
  }

  void update(final int symbol) {
    this.symbolCount[symbol]++;
    if (--this.symbolsUntilUpdate == 0) {
      update();
    }
  }
}
