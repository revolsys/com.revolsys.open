/*
 * Copyright 2007-2014, martin isenburg, rapidlasso - fast tools to catch reality
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

public class LazDecompressGpsTimeV1 extends LazDecompressGpsTime {

  private static final int LASZIP_GPSTIME_MULTIMAX = 512;

  private long gpsTime;

  private int multiExtremeCounter;

  private int gpstimeDiff;

  public LazDecompressGpsTimeV1(final ArithmeticDecoder dec) {
    super(dec);
    this.gpstimeMulti = ArithmeticDecoder.createSymbolModel(LASZIP_GPSTIME_MULTIMAX);
    this.gpstime0Diff = ArithmeticDecoder.createSymbolModel(3);
    this.decompressGpstime = new IntegerCompressor(dec, 32, 6); // 32 bits, 6 contexts
  }

  @Override
  public void init(final LasPoint point) {
    super.init(point);
    this.gpstimeDiff = 0;
    this.multiExtremeCounter = 0;

    this.gpsTime = Double.doubleToLongBits(point.getGpsTime());
  }

  @Override
  public void read(final LasPoint point) {
    int multi;
    if (this.gpstimeDiff == 0) {
      // if the last integer difference was zero
      multi = this.decoder.decodeSymbol(this.gpstime0Diff);
      if (multi == 1) {
        // the difference can be represented with 32 bits
        this.gpstimeDiff = this.decompressGpstime.decompress(0, 0);
        this.gpsTime += this.gpstimeDiff;
      } else if (multi == 2) {
        // the difference is huge
        this.gpsTime = this.decoder.getLong();
      }
    } else {
      multi = this.decoder.decodeSymbol(this.gpstimeMulti);

      if (multi < LASZIP_GPSTIME_MULTIMAX - 2) {
        int gpstime_diff;
        if (multi == 1) {
          gpstime_diff = this.decompressGpstime.decompress(this.gpstimeDiff, 1);
          this.gpstimeDiff = gpstime_diff;
          this.multiExtremeCounter = 0;
        } else if (multi == 0) {
          gpstime_diff = this.decompressGpstime.decompress(this.gpstimeDiff / 4, 2);
          this.multiExtremeCounter++;
          if (this.multiExtremeCounter > 3) {
            this.gpstimeDiff = gpstime_diff;
            this.multiExtremeCounter = 0;
          }
        } else if (multi < 10) {
          gpstime_diff = this.decompressGpstime.decompress(multi * this.gpstimeDiff, 3);
        } else if (multi < 50) {
          gpstime_diff = this.decompressGpstime.decompress(multi * this.gpstimeDiff, 4);
        } else {
          gpstime_diff = this.decompressGpstime.decompress(multi * this.gpstimeDiff, 5);
          if (multi == LASZIP_GPSTIME_MULTIMAX - 3) {
            this.multiExtremeCounter++;
            if (this.multiExtremeCounter > 3) {
              this.gpstimeDiff = gpstime_diff;
              this.multiExtremeCounter = 0;
            }
          }
        }
        this.gpsTime += gpstime_diff;
      } else if (multi < LASZIP_GPSTIME_MULTIMAX - 1) {
        this.gpsTime = this.decoder.getLong();
      }
    }
    point.setGpsTime(Double.longBitsToDouble(this.gpsTime));
  }
}
