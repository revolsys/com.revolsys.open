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

import com.revolsys.elevation.cloud.las.LasPointCloud;
import com.revolsys.elevation.cloud.las.pointformat.LasPoint;

public class LazDecompressPoint10V1 extends LazDecompressPoint10 {

  private final int[] lastDiffX = new int[3];

  private final int[] lastDiffY = new int[3];

  private int lastIncrement;

  private final IntegerCompressor decompressScanAngleRank;

  public LazDecompressPoint10V1(final LasPointCloud pointCloud, final ArithmeticDecoder decoder) {
    super(pointCloud, decoder);

    this.decompressDeltaX = new IntegerCompressor(decoder, 32);
    this.decompressDeltaY = new IntegerCompressor(decoder, 32, 20);
    this.decompressZ = new IntegerCompressor(decoder, 32, 20);
    this.decompressIntensity = new IntegerCompressor(decoder, 16);
    this.decompressScanAngleRank = new IntegerCompressor(decoder, 8, 2);
    this.decompressPointSourceId = new IntegerCompressor(decoder, 16);
  }

  @Override
  public void init(final LasPoint point) {
    super.init(point);

    this.lastDiffX[0] = this.lastDiffX[1] = this.lastDiffX[2] = 0;
    this.lastDiffY[0] = this.lastDiffY[1] = this.lastDiffY[2] = 0;
    this.lastIncrement = 0;

    this.decompressScanAngleRank.initDecompressor();

    this.intensity = point.getIntensity();
  }

  private int median(final int[] lastDiffs) {
    int medianX;
    final int diff0 = lastDiffs[0];
    final int diff1 = lastDiffs[1];
    final int diff2 = lastDiffs[2];
    if (diff0 < diff1) {
      if (diff1 < diff2) {
        medianX = diff1;
      } else if (diff0 < diff2) {
        medianX = diff2;
      } else {
        medianX = diff0;
      }
    } else {
      if (diff0 < diff2) {
        medianX = diff0;
      } else if (diff1 < diff2) {
        medianX = diff2;
      } else {
        medianX = diff1;
      }
    }
    return medianX;
  }

  @Override
  public void read(final LasPoint point) {
    final int medianX = median(this.lastDiffX);
    final int medianY = median(this.lastDiffY);

    // decompress x y z coordinates
    final int diffX = this.decompressDeltaX.decompress(medianX);
    this.x += diffX;
    // we use the number k of bits corrector bits to switch contexts
    final int kBitsX = this.decompressDeltaX.getK(); // unsigned
    final int diffY = this.decompressDeltaY.decompress(medianY, kBitsX < 19 ? kBitsX : 19);
    this.y += diffY;
    final int kBitsY = (kBitsX + this.decompressDeltaY.getK()) / 2;
    this.z = this.decompressZ.decompress(this.z, kBitsY < 19 ? kBitsY : 19);

    final int changedValues = this.decoder.decodeSymbol(this.decompressChangedValues);

    if (changedValues != 0) {
      if ((changedValues & 32) != 0) {
        this.intensity = this.decompressIntensity.decompress(this.intensity);
      }

      if ((changedValues & 16) != 0) {
        this.returnByte = read(this.decompressBitByte, this.returnByte);
      }

      if ((changedValues & 8) != 0) {
        this.classificationByte = read(this.decompressClassification, this.classificationByte);
      }

      if ((changedValues & 4) != 0) {
        this.scanAngleRank = (byte)this.decompressScanAngleRank.decompress(this.scanAngleRank,
          kBitsY < 3 ? 1 : 0);
      }

      if ((changedValues & 2) != 0) {
        this.userData = read(this.decompressUserData, this.userData);
      }

      if ((changedValues & 1) != 0) {
        this.pointSourceId = this.decompressPointSourceId.decompress(this.pointSourceId);
      }
    }

    // record the difference
    this.lastDiffX[this.lastIncrement] = diffX;
    this.lastDiffY[this.lastIncrement] = diffY;
    this.lastIncrement++;
    if (this.lastIncrement > 2) {
      this.lastIncrement = 0;
    }

    postRead(point);
  }
}
