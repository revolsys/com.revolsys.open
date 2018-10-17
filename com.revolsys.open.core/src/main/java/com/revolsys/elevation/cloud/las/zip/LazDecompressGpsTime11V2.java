/*
 * Copyright 2007-2012, martin isenburg, rapidlasso - fast tools to catch reality
 *
 * This is free software; you can redistribute and/or modify it under the
 * terms of the GNU Lesser General Licence as published by the Free Software
 * Foundation. See the LICENSE.txt file for more information.
 *
 * This software is distributed WITHOUT ANY WARRANTY and without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package com.revolsys.elevation.cloud.las.zip;

import com.revolsys.elevation.cloud.las.pointformat.LasPoint;

public class LazDecompressGpsTime11V2 extends LazDecompressGpsTime11 implements LazDecompress {

  private static int LASZIP_GPSTIME_MULTI = 500;

  private static int LASZIP_GPSTIME_MULTI_MINUS = -10;

  private static int LASZIP_GPSTIME_MULTI_UNCHANGED = LASZIP_GPSTIME_MULTI
    - LASZIP_GPSTIME_MULTI_MINUS + 1;

  private static int LASZIP_GPSTIME_MULTI_CODE_FULL = LASZIP_GPSTIME_MULTI
    - LASZIP_GPSTIME_MULTI_MINUS + 2;

  private static int LASZIP_GPSTIME_MULTI_TOTAL = LASZIP_GPSTIME_MULTI - LASZIP_GPSTIME_MULTI_MINUS
    + 6;

  private int last;

  private int next;

  private final long[] lastGpsTime = new long[4];

  private final int[] lastGpsTimeDiff = new int[4];

  private final int[] multiExtremeCounter = new int[4];

  public LazDecompressGpsTime11V2(final ArithmeticDecoder decoder) {
    super(decoder);
    this.gpsTimeMulti = decoder.createSymbolModel(LASZIP_GPSTIME_MULTI_TOTAL);
    this.gpsTime0Diff = decoder.createSymbolModel(6);
    this.decompressGpsTime = new IntegerCompressor(decoder, 32, 9);
  }

  @Override
  public void init(final LasPoint point) {
    super.init(point);

    this.last = 0;
    this.next = 0;
    this.lastGpsTimeDiff[0] = 0;
    this.lastGpsTimeDiff[1] = 0;
    this.lastGpsTimeDiff[2] = 0;
    this.lastGpsTimeDiff[3] = 0;
    this.multiExtremeCounter[0] = 0;
    this.multiExtremeCounter[1] = 0;
    this.multiExtremeCounter[2] = 0;
    this.multiExtremeCounter[3] = 0;

    this.lastGpsTime[0] = Double.doubleToLongBits(point.getGpsTime());
    this.lastGpsTime[1] = 0;
    this.lastGpsTime[2] = 0;
    this.lastGpsTime[3] = 0;
  }

  @Override
  public void read(final LasPoint point) {
    int multi;
    final int[] lastGpsTimeDiff = this.lastGpsTimeDiff;
    final int lastDiff = lastGpsTimeDiff[this.last];
    final ArithmeticDecoder decoder = this.decoder;
    final long[] lastGpsTime = this.lastGpsTime;
    final int[] multiExtremeCounter = this.multiExtremeCounter;
    int last = this.last;
    int next = this.next;
    final IntegerCompressor decompressGpsTime = this.decompressGpsTime;
    if (lastDiff == 0) { // if the last integer difference was zero
      multi = decoder.decodeSymbol(this.gpsTime0Diff);
      if (multi == 1) {// the difference can be represented with 32 bits
        final int gpsTimeDiff = decompressGpsTime.decompress(0, 0);
        lastGpsTimeDiff[last] = gpsTimeDiff;
        lastGpsTime[last] += gpsTimeDiff;
        multiExtremeCounter[last] = 0;
      } else if (multi == 2) {// the difference is huge
        long gpsTime = decompressGpsTime.decompress((int)(lastGpsTime[last] >>> 32), 8);
        gpsTime <<= 32;
        gpsTime |= Integer.toUnsignedLong(decoder.readInt());

        next = this.next + 1;
        next &= 3;
        this.next = next;
        lastGpsTime[next] = gpsTime;

        last = next;
        this.last = next;
        lastGpsTimeDiff[last] = 0;
        multiExtremeCounter[last] = 0;
      } else if (multi > 2) {// we switch to another sequence
        last = last + multi - 2;
        this.last = last & 3;
        read(point);
      }
    } else {
      multi = decoder.decodeSymbol(this.gpsTimeMulti);
      if (multi == 1) {
        final int gpsTimeDiff = lastGpsTimeDiff[last];
        lastGpsTime[last] += decompressGpsTime.decompress(gpsTimeDiff, 1);
        multiExtremeCounter[last] = 0;
      } else if (multi < LASZIP_GPSTIME_MULTI_UNCHANGED) {
        int gpsTimeDiff;
        if (multi == 0) {
          gpsTimeDiff = decompressGpsTime.decompress(0, 7);
          multiExtremeCounter[last]++;
          if (multiExtremeCounter[last] > 3) {
            lastGpsTimeDiff[last] = gpsTimeDiff;
            multiExtremeCounter[last] = 0;
          }
        } else if (multi < LASZIP_GPSTIME_MULTI) {
          gpsTimeDiff = lastGpsTimeDiff[last];
          if (multi < 10) {
            gpsTimeDiff = decompressGpsTime.decompress(multi * gpsTimeDiff, 2);
          } else {
            gpsTimeDiff = decompressGpsTime.decompress(multi * gpsTimeDiff, 3);
          }
        } else if (multi == LASZIP_GPSTIME_MULTI) {
          gpsTimeDiff = lastGpsTimeDiff[last];
          gpsTimeDiff = decompressGpsTime.decompress(LASZIP_GPSTIME_MULTI * gpsTimeDiff, 4);
          multiExtremeCounter[last]++;
          if (multiExtremeCounter[last] > 3) {
            lastGpsTimeDiff[last] = gpsTimeDiff;
            multiExtremeCounter[last] = 0;
          }
        } else {
          gpsTimeDiff = lastGpsTimeDiff[last];
          multi = LASZIP_GPSTIME_MULTI - multi;
          if (multi > LASZIP_GPSTIME_MULTI_MINUS) {
            gpsTimeDiff = decompressGpsTime.decompress(multi * gpsTimeDiff, 5);
          } else {
            gpsTimeDiff = decompressGpsTime.decompress(LASZIP_GPSTIME_MULTI_MINUS * gpsTimeDiff, 6);
            multiExtremeCounter[last]++;
            if (multiExtremeCounter[last] > 3) {
              lastGpsTimeDiff[last] = gpsTimeDiff;
              multiExtremeCounter[last] = 0;
            }
          }
        }
        lastGpsTime[last] += gpsTimeDiff;
      } else if (multi == LASZIP_GPSTIME_MULTI_CODE_FULL) {
        next += 1;
        next &= 3;
        this.next = next;
        long gpsTime = lastGpsTime[last];
        gpsTime >>= 32;
        gpsTime = decompressGpsTime.decompress((int)gpsTime, 8);
        gpsTime <<= 32;
        gpsTime |= Integer.toUnsignedLong(decoder.readInt());
        lastGpsTime[next] = gpsTime;

        last = next;
        this.last = last;
        lastGpsTimeDiff[last] = 0;
        multiExtremeCounter[last] = 0;
      } else if (multi >= LASZIP_GPSTIME_MULTI_CODE_FULL) {
        last += multi - LASZIP_GPSTIME_MULTI_CODE_FULL;
        this.last = last & 3;
        read(point);
        return;
      }
    }
    point.setGpsTime(Double.longBitsToDouble(lastGpsTime[this.last]));
  }
}
