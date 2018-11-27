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

public class ArithmeticBitModel {

  private static final int BM__LengthShift = 13; // length bits discarded before
                                                 // mult.

  private static final int BM__MaxCount = 1 << BM__LengthShift; // for adaptive
                                                                // models

  private int updateCycle = 4;

  private int bitCountUntilUpdate = 4;

  protected int bit0Probability = 1 << BM__LengthShift - 1;

  protected int bit0Count = 1;

  private int bitCount = 2;

  public ArithmeticBitModel() {
  }

  public void update() {
    if (--this.bitCountUntilUpdate == 0) {
      // halve counts when a threshold is reached
      if ((this.bitCount += this.updateCycle) > BM__MaxCount) {
        this.bitCount = this.bitCount + 1 >>> 1;
        this.bit0Count = this.bit0Count + 1 >>> 1;
        if (this.bit0Count == this.bitCount) {
          ++this.bitCount;
        }
      }

      // compute scaled bit 0 probability
      final int scale = Integer.divideUnsigned(0x80000000, this.bitCount);
      this.bit0Probability = this.bit0Count * scale >>> 31 - BM__LengthShift;

      // set frequency of model updates
      this.updateCycle = 5 * this.updateCycle >>> 2;
      if (this.updateCycle > 64) {
        this.updateCycle = 64;
      }
      this.bitCountUntilUpdate = this.updateCycle;
    }
  }
}
