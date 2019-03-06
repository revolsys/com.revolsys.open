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
import com.revolsys.math.arithmeticcoding.ArithmeticCodingDecoder;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingEncoder;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingInteger;
import com.revolsys.math.arithmeticcoding.ArithmeticModel;
import com.revolsys.util.Debug;

public class LasZipItemCodecGpsTime11V1 implements LasZipItemCodec {

  private static final int LASZIP_GPSTIME_MULTIMAX = 512;

  private long lastGpsTime;

  private int multiExtremeCounter;

  private int lastGpstimeDiff;

  private ArithmeticCodingDecoder decoder;

  private ArithmeticCodingEncoder encoder;

  private final ArithmeticModel gpsTimeMulti;

  private final ArithmeticModel gpsTime0Diff;

  private final ArithmeticCodingInteger ic_gpstime;

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
    this.ic_gpstime = codec.newCodecInteger(32, 6);
  }

  public LasZipItemCodecGpsTime11V1(final ArithmeticCodingDecoder decoder) {
    this((ArithmeticCodingCodec)decoder);
    this.decoder = decoder;
  }

  @Override
  public void init(final LasPoint point) {
    this.gpsTimeMulti.reset();
    this.gpsTime0Diff.reset();
    this.ic_gpstime.init();
    this.lastGpstimeDiff = 0;
    this.multiExtremeCounter = 0;

    this.lastGpsTime = point.getGpsTimeLong();
  }

  @Override
  public void read(final LasPoint point) {
    int multi;
    if (this.lastGpstimeDiff == 0) {
      // if the last integer difference was zero
      multi = this.decoder.decodeSymbol(this.gpsTime0Diff);
      if (multi == 1) {
        // the difference can be represented with 32 bits
        this.lastGpstimeDiff = this.ic_gpstime.decompress(0, 0);
        this.lastGpsTime += this.lastGpstimeDiff;
      } else if (multi == 2) {
        // the difference is huge
        this.lastGpsTime = this.decoder.readInt64();
      }
    } else {
      multi = this.decoder.decodeSymbol(this.gpsTimeMulti);

      if (multi < LASZIP_GPSTIME_MULTIMAX - 2) {
        int gpstime_diff;
        if (multi == 1) {
          gpstime_diff = this.ic_gpstime.decompress(this.lastGpstimeDiff, 1);
          this.lastGpstimeDiff = gpstime_diff;
          this.multiExtremeCounter = 0;
        } else if (multi == 0) {
          gpstime_diff = this.ic_gpstime.decompress(this.lastGpstimeDiff / 4, 2);
          this.multiExtremeCounter++;
          if (this.multiExtremeCounter > 3) {
            this.lastGpstimeDiff = gpstime_diff;
            this.multiExtremeCounter = 0;
          }
        } else if (multi < 10) {
          gpstime_diff = this.ic_gpstime.decompress(multi * this.lastGpstimeDiff, 3);
        } else if (multi < 50) {
          gpstime_diff = this.ic_gpstime.decompress(multi * this.lastGpstimeDiff, 4);
        } else {
          gpstime_diff = this.ic_gpstime.decompress(multi * this.lastGpstimeDiff, 5);
          if (multi == LASZIP_GPSTIME_MULTIMAX - 3) {
            this.multiExtremeCounter++;
            if (this.multiExtremeCounter > 3) {
              this.lastGpstimeDiff = gpstime_diff;
              this.multiExtremeCounter = 0;
            }
          }
        }
        this.lastGpsTime += gpstime_diff;
      } else if (multi < LASZIP_GPSTIME_MULTIMAX - 1) {
        this.lastGpsTime = this.decoder.readInt64();
      }
    }
    final double gpsTime = Double.longBitsToDouble(this.lastGpsTime);
    if (!Double.isFinite(gpsTime)) {
      Debug.noOp();
    }
    point.setGpsTime(gpsTime);
  }

  @Override
  public void write(final LasPoint point) {
    final long gpsTime = point.getGpsTimeLong();

    if (this.lastGpstimeDiff == 0) // if the last integer difference was zero
    {
      if (gpsTime == this.lastGpsTime) {
        this.encoder.encodeSymbol(this.gpsTime0Diff, 0); // the doubles have not
        // changed
      } else {
        // calculate the difference between the two doubles as an integer
        final long curr_gpstime_diff_64 = gpsTime - this.lastGpsTime;
        final int curr_gpstime_diff = (int)curr_gpstime_diff_64;
        if (curr_gpstime_diff_64 == curr_gpstime_diff) {
          // the difference can be represented with 32 bits
          this.encoder.encodeSymbol(this.gpsTime0Diff, 1);
          this.ic_gpstime.compress(0, curr_gpstime_diff, 0);
          this.lastGpstimeDiff = curr_gpstime_diff;
        } else {
          // the difference is huge
          this.encoder.encodeSymbol(this.gpsTime0Diff, 2);
          this.encoder.writeInt64(gpsTime);
        }
        this.lastGpsTime = gpsTime;
      }
    } else {
      // the last integer difference was *not* zero
      if (gpsTime == this.lastGpsTime) {
        // if the doubles have not changed use a special symbol
        this.encoder.encodeSymbol(this.gpsTimeMulti, LASZIP_GPSTIME_MULTIMAX - 1);
      } else {
        // calculate the difference between the two doubles as an integer
        final long curr_gpstime_diff_64 = gpsTime - this.lastGpsTime;
        final int curr_gpstime_diff = (int)curr_gpstime_diff_64;
        // if the current gpstime difference can be represented with 32 bits
        if (curr_gpstime_diff_64 == curr_gpstime_diff) {
          // compute multiplier between current and last integer difference
          int multi = (int)((float)curr_gpstime_diff / (float)this.lastGpstimeDiff + 0.5f);

          // limit the multiplier into some bounds
          if (multi >= LASZIP_GPSTIME_MULTIMAX - 3) {
            multi = LASZIP_GPSTIME_MULTIMAX - 3;
          } else if (multi <= 0) {
            multi = 0;
          }
          // compress this multiplier
          this.encoder.encodeSymbol(this.gpsTimeMulti, multi);
          // compress the residual curr_gpstime_diff in dependance on the
          // multiplier
          if (multi == 1) {
            // this is the case we assume we get most often
            this.ic_gpstime.compress(this.lastGpstimeDiff, curr_gpstime_diff, 1);
            this.lastGpstimeDiff = curr_gpstime_diff;
            this.multiExtremeCounter = 0;
          } else {
            if (multi == 0) {
              this.ic_gpstime.compress(this.lastGpstimeDiff / 4, curr_gpstime_diff, 2);
              this.multiExtremeCounter++;
              if (this.multiExtremeCounter > 3) {
                this.lastGpstimeDiff = curr_gpstime_diff;
                this.multiExtremeCounter = 0;
              }
            } else if (multi < 10) {
              this.ic_gpstime.compress(multi * this.lastGpstimeDiff, curr_gpstime_diff, 3);
            } else if (multi < 50) {
              this.ic_gpstime.compress(multi * this.lastGpstimeDiff, curr_gpstime_diff, 4);
            } else {
              this.ic_gpstime.compress(multi * this.lastGpstimeDiff, curr_gpstime_diff, 5);
              if (multi == LASZIP_GPSTIME_MULTIMAX - 3) {
                this.multiExtremeCounter++;
                if (this.multiExtremeCounter > 3) {
                  this.lastGpstimeDiff = curr_gpstime_diff;
                  this.multiExtremeCounter = 0;
                }
              }
            }
          }
        } else {
          // if difference is so huge ... we simply write the double
          this.encoder.encodeSymbol(this.gpsTimeMulti, LASZIP_GPSTIME_MULTIMAX - 2);
          this.encoder.writeInt64(gpsTime);
        }
        this.lastGpsTime = gpsTime;
      }
    }
  }
}
