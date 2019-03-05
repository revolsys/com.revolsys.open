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
package com.revolsys.elevation.cloud.las.zip.v1;

import com.revolsys.elevation.cloud.las.pointformat.LasPoint;
import com.revolsys.elevation.cloud.las.zip.LasZipItemCodec;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingCodec;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingEncoder;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingDecoder;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingInteger;
import com.revolsys.math.arithmeticcoding.ArithmeticModel;

public class LasZipItemCodecGpsTime11V1 implements LasZipItemCodec {

  private static final int LASZIP_GPSTIME_MULTIMAX = 512;

  private long gpsTime;

  private int multiExtremeCounter;

  private int gpstimeDiff;

  private ArithmeticCodingDecoder decoder;

  private ArithmeticCodingEncoder encoder;

  private final ArithmeticModel gpsTimeMulti;

  private final ArithmeticModel gpsTime0Diff;

  private final ArithmeticCodingInteger decompressGpsTime;

  public LasZipItemCodecGpsTime11V1(final ArithmeticCodingCodec codec) {
    if (codec instanceof ArithmeticCodingDecoder) {
      this.decoder = (ArithmeticCodingDecoder)codec;
    } else if (codec instanceof ArithmeticCodingEncoder) {
      this.encoder = (ArithmeticCodingEncoder)codec;
    } else {
      throw new IllegalArgumentException("Not supported:" + codec.getClass());
    }
    this.gpsTimeMulti = codec.createSymbolModel(LASZIP_GPSTIME_MULTIMAX);
    this.gpsTime0Diff = codec.createSymbolModel(3);
    this.decompressGpsTime = codec.newCodecInteger(32, 6);
  }

  public LasZipItemCodecGpsTime11V1(final ArithmeticCodingDecoder decoder) {
    this((ArithmeticCodingCodec)decoder);
    this.decoder = decoder;
  }

  @Override
  public void init(final LasPoint point) {
    this.gpsTimeMulti.reset();
    this.gpsTime0Diff.reset();
    this.decompressGpsTime.init();
    this.gpstimeDiff = 0;
    this.multiExtremeCounter = 0;

    this.gpsTime = Double.doubleToLongBits(point.getGpsTime());
  }

  @Override
  public void read(final LasPoint point) {
    int multi;
    if (this.gpstimeDiff == 0) {
      // if the last integer difference was zero
      multi = this.decoder.decodeSymbol(this.gpsTime0Diff);
      if (multi == 1) {
        // the difference can be represented with 32 bits
        this.gpstimeDiff = this.decompressGpsTime.decompress(0, 0);
        this.gpsTime += this.gpstimeDiff;
      } else if (multi == 2) {
        // the difference is huge
        this.gpsTime = this.decoder.readInt64();
      }
    } else {
      multi = this.decoder.decodeSymbol(this.gpsTimeMulti);

      if (multi < LASZIP_GPSTIME_MULTIMAX - 2) {
        int gpstime_diff;
        if (multi == 1) {
          gpstime_diff = this.decompressGpsTime.decompress(this.gpstimeDiff, 1);
          this.gpstimeDiff = gpstime_diff;
          this.multiExtremeCounter = 0;
        } else if (multi == 0) {
          gpstime_diff = this.decompressGpsTime.decompress(this.gpstimeDiff / 4, 2);
          this.multiExtremeCounter++;
          if (this.multiExtremeCounter > 3) {
            this.gpstimeDiff = gpstime_diff;
            this.multiExtremeCounter = 0;
          }
        } else if (multi < 10) {
          gpstime_diff = this.decompressGpsTime.decompress(multi * this.gpstimeDiff, 3);
        } else if (multi < 50) {
          gpstime_diff = this.decompressGpsTime.decompress(multi * this.gpstimeDiff, 4);
        } else {
          gpstime_diff = this.decompressGpsTime.decompress(multi * this.gpstimeDiff, 5);
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
        this.gpsTime = this.decoder.readInt64();
      }
    }
    point.setGpsTime(Double.longBitsToDouble(this.gpsTime));
  }
}
