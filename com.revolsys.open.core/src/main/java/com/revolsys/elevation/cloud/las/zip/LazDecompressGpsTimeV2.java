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

import com.revolsys.elevation.cloud.las.LasPoint;
import com.revolsys.util.Debug;

public class LazDecompressGpsTimeV2 extends LazDecompressGpsTime implements LazDecompress {

  private static int LASZIP_GPSTIME_MULTI = 500;

  private static int LASZIP_GPSTIME_MULTI_MINUS = -10;

  private static int LASZIP_GPSTIME_MULTI_UNCHANGED = LASZIP_GPSTIME_MULTI
    - LASZIP_GPSTIME_MULTI_MINUS + 1;

  private static int LASZIP_GPSTIME_MULTI_CODE_FULL = LASZIP_GPSTIME_MULTI
    - LASZIP_GPSTIME_MULTI_MINUS + 2;

  private static int LASZIP_GPSTIME_MULTI_TOTAL = LASZIP_GPSTIME_MULTI - LASZIP_GPSTIME_MULTI_MINUS
    + 6;

  private int lastIndex;

  int nextIndex;

  private final long[] lastGpsTime = new long[4];

  private final int[] lastGpstimeDiff = new int[4];

  private final int[] multiExtremeCounter = new int[4];

  public LazDecompressGpsTimeV2(final ArithmeticDecoder decoder) {
    super(decoder);
    this.gpstimeMulti = ArithmeticDecoder.createSymbolModel(LASZIP_GPSTIME_MULTI_TOTAL);
    this.gpstime0Diff = ArithmeticDecoder.createSymbolModel(6);
    this.decompressGpstime = new IntegerCompressor(decoder, 32, 9);
  }

  @Override
  public void init(final LasPoint point) {
    super.init(point);

    this.lastIndex = 0;
    this.nextIndex = 0;
    this.lastGpstimeDiff[0] = 0;
    this.lastGpstimeDiff[1] = 0;
    this.lastGpstimeDiff[2] = 0;
    this.lastGpstimeDiff[3] = 0;
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
    final int[] lastGpstimeDiff = this.lastGpstimeDiff;
    final long[] lastGpsTime = this.lastGpsTime;
    final int[] multiExtremeCounter = this.multiExtremeCounter;
    final IntegerCompressor decompressGpstime = this.decompressGpstime;
    final int lastDiff = lastGpstimeDiff[this.lastIndex];
    if (lastDiff == 0) {
      // if the last integer difference was zero
      multi = this.decoder.decodeSymbol(this.gpstime0Diff);
      if (multi == 1) {
        // the difference can be represented with 32 bits
        final int diff = decompressGpstime.decompress(0, 0);
        lastGpstimeDiff[this.lastIndex] = diff;
        lastGpsTime[this.lastIndex] += diff;
        multiExtremeCounter[this.lastIndex] = 0;
      } else if (multi == 2) {
        // the difference is huge
        final int index = this.nextIndex + 1;
        this.nextIndex = index & 3;
        int nextGpsTime = decompressGpstime.decompress((int)(lastGpsTime[this.lastIndex] >>> 32),
          8);
        nextGpsTime = nextGpsTime << 32;
        nextGpsTime |= this.decoder.getInt();
        lastGpsTime[this.nextIndex] = nextGpsTime;
        this.lastIndex = this.nextIndex;
        lastGpstimeDiff[this.lastIndex] = 0;
        multiExtremeCounter[this.lastIndex] = 0;
      } else if (multi > 2) {
        // we switch to another sequence
        final int index = this.lastIndex + multi - 2;
        this.lastIndex = index & 3;
        read(point);
      }
    } else {
      multi = this.decoder.decodeSymbol(this.gpstimeMulti);
      if (multi == 1) {
        lastGpsTime[this.lastIndex] += decompressGpstime.decompress(lastDiff, 1);
        multiExtremeCounter[this.lastIndex] = 0;
      } else if (multi < LASZIP_GPSTIME_MULTI_UNCHANGED) {
        int gpstimeDiff;
        if (multi == 0) {
          gpstimeDiff = decompressGpstime.decompress(0, 7);
          multiExtremeCounter[this.lastIndex]++;
          if (multiExtremeCounter[this.lastIndex] > 3) {
            lastGpstimeDiff[this.lastIndex] = gpstimeDiff;
            multiExtremeCounter[this.lastIndex] = 0;
          }
        } else if (multi < LASZIP_GPSTIME_MULTI) {
          if (multi < 10) {
            gpstimeDiff = decompressGpstime.decompress(multi * lastDiff, 2);
          } else {
            gpstimeDiff = decompressGpstime.decompress(multi * lastDiff, 3);
          }
        } else if (multi == LASZIP_GPSTIME_MULTI) {
          gpstimeDiff = decompressGpstime.decompress(LASZIP_GPSTIME_MULTI * lastDiff, 4);
          multiExtremeCounter[this.lastIndex]++;
          if (multiExtremeCounter[this.lastIndex] > 3) {
            lastGpstimeDiff[this.lastIndex] = gpstimeDiff;
            multiExtremeCounter[this.lastIndex] = 0;
          }
        } else {
          multi = LASZIP_GPSTIME_MULTI - multi;
          if (multi > LASZIP_GPSTIME_MULTI_MINUS) {
            gpstimeDiff = decompressGpstime.decompress(multi * lastDiff, 5);
          } else {
            gpstimeDiff = decompressGpstime.decompress(LASZIP_GPSTIME_MULTI_MINUS * lastDiff, 6);
            multiExtremeCounter[this.lastIndex]++;
            if (multiExtremeCounter[this.lastIndex] > 3) {
              lastGpstimeDiff[this.lastIndex] = gpstimeDiff;
              multiExtremeCounter[this.lastIndex] = 0;
            }
          }
        }
        lastGpsTime[this.lastIndex] += gpstimeDiff;
      } else if (multi == LASZIP_GPSTIME_MULTI_CODE_FULL) {
        this.nextIndex = this.nextIndex + 1 & 3;
        lastGpsTime[this.nextIndex] = decompressGpstime
          .decompress((int)(lastGpsTime[this.lastIndex] >>> 32), 8);
        lastGpsTime[this.nextIndex] = lastGpsTime[this.nextIndex] << 32;
        lastGpsTime[this.nextIndex] += Integer.toUnsignedLong(this.decoder.getInt());
        this.lastIndex = this.nextIndex;
        lastGpstimeDiff[this.lastIndex] = 0;
        multiExtremeCounter[this.lastIndex] = 0;
      } else if (multi >= LASZIP_GPSTIME_MULTI_CODE_FULL) {
        this.lastIndex = this.lastIndex + multi - LASZIP_GPSTIME_MULTI_CODE_FULL & 3;
        read(point);
      }
    }
    final double newGpsTime = Double.longBitsToDouble(lastGpsTime[this.lastIndex]);
    if (!Double.isFinite(newGpsTime)) {
      Debug.noOp();
    }
    point.setGpsTime(newGpsTime);
  }
}
