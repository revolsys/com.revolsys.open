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

import java.util.Arrays;

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

  protected static final int DM__LengthShift = 15; // length bits discarded
                                                   // before
  // mult.

  private static final int DM__MaxCount = 1 << DM__LengthShift; // for adaptive
                                                                // models

  protected int[] distribution;

  private final int[] symbolCounts;

  private int totalCount = 0;

  private int updateCycle;

  private int symbolCountUntilUpdate;

  private final int symbolCount;

  protected int lastSymbol;

  public ArithmeticModel(final int symbolCount) {
    if (symbolCount < 2 || symbolCount > 1 << 11) {
      throw new IllegalArgumentException();
    }
    this.symbolCount = symbolCount;
    this.updateCycle = symbolCount;

    this.lastSymbol = symbolCount - 1;
    this.distribution = new int[symbolCount];
    this.symbolCounts = new int[symbolCount];

    Arrays.fill(this.symbolCounts, 1);

    update();
    this.symbolCountUntilUpdate = this.updateCycle = this.symbolCount + 6 >>> 1;
  }

  public void addCount(final int symbol) {
    ++this.symbolCounts[symbol];
    if (--this.symbolCountUntilUpdate == 0) {
      update();
    }
  }

  void update() {
    // halve counts when a threshold is reached
    if ((this.totalCount += this.updateCycle) > DM__MaxCount) {
      this.totalCount = 0;
      for (int n = 0; n < this.symbolCount; n++) {
        this.totalCount += this.symbolCounts[n] = this.symbolCounts[n] + 1 >>> 1;
      }
    }

    // compute cumulative distribution, decoder table
    int sum = 0;
    final int scale = Integer.divideUnsigned(0x80000000, this.totalCount);

    for (int k = 0; k < this.symbolCount; k++) {
      this.distribution[k] = scale * sum >>> 31 - DM__LengthShift;
      sum += this.symbolCounts[k];
    }

    // set frequency of model updates
    this.updateCycle = 5 * this.updateCycle >>> 2;
    final int maxCycle = this.symbolCount + 6 << 3;
    if (Integer.compareUnsigned(this.updateCycle, maxCycle) > 0) {
      this.updateCycle = maxCycle;
    }
    this.symbolCountUntilUpdate = this.updateCycle;
  }
}
