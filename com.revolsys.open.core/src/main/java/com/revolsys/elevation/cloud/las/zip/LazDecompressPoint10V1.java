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

public class LazDecompressPoint10V1 implements LazDecompress {

  protected int classificationByte;

  protected final ArithmeticDecoder dec;

  protected final ArithmeticModel m_changed_values;

  protected IntegerCompressor ic_dx;

  protected IntegerCompressor ic_dy;

  protected IntegerCompressor ic_intensity;

  protected IntegerCompressor ic_point_source_ID;

  protected IntegerCompressor ic_z;

  protected int intensity = 0;

  protected final ArithmeticModel[] m_bit_byte = new ArithmeticModel[256];

  protected final ArithmeticModel[] m_classification = new ArithmeticModel[256];

  protected final ArithmeticModel[] m_user_data = new ArithmeticModel[256];

  private final LasPointCloud pointCloud;

  protected int pointSourceId;

  protected int returnByte;

  protected byte scanAngleRank;

  protected boolean scanDirectionFlag;

  protected int userData;

  protected int x;

  protected int y;

  protected int z;

  private final int[] last_x_diff = new int[3];

  private final int[] last_y_diff = new int[3];

  private int last_incr;

  private final IntegerCompressor ic_scan_angle_rank;

  public LazDecompressPoint10V1(final LasPointCloud pointCloud, final ArithmeticDecoder decoder) {
    this.pointCloud = pointCloud;
    this.dec = decoder;
    this.m_changed_values = decoder.createSymbolModel(64);

    this.ic_dx = new IntegerCompressor(decoder, 32);
    this.ic_dy = new IntegerCompressor(decoder, 32, 20);
    this.ic_z = new IntegerCompressor(decoder, 32, 20);
    this.ic_intensity = new IntegerCompressor(decoder, 16);
    this.ic_scan_angle_rank = new IntegerCompressor(decoder, 8, 2);
    this.ic_point_source_ID = new IntegerCompressor(decoder, 16);
  }

  @Override
  public void init(final LasPoint point) {
    int i;

    /* init state */
    this.last_x_diff[0] = this.last_x_diff[1] = this.last_x_diff[2] = 0;
    this.last_y_diff[0] = this.last_y_diff[1] = this.last_y_diff[2] = 0;
    this.last_incr = 0;

    this.ic_dx.initDecompressor();
    this.ic_dy.initDecompressor();
    this.ic_z.initDecompressor();
    this.ic_intensity.initDecompressor();
    this.ic_scan_angle_rank.initDecompressor();
    this.ic_point_source_ID.initDecompressor();
    this.dec.initSymbolModel(this.m_changed_values);
    for (i = 0; i < 256; i++) {
      if (this.m_bit_byte[i] != null) {
        this.dec.initSymbolModel(this.m_bit_byte[i]);
      }
      if (this.m_classification[i] != null) {
        this.dec.initSymbolModel(this.m_classification[i]);
      }
      if (this.m_user_data[i] != null) {
        this.dec.initSymbolModel(this.m_user_data[i]);
      }
    }

    this.x = point.getXInt();
    this.y = point.getYInt();
    this.z = point.getZInt();

    this.classificationByte = (short)Byte.toUnsignedInt(point.getClassificationByte());
    this.returnByte = (short)Byte.toUnsignedInt(point.getReturnByte());
    this.userData = point.getUserData();
    this.scanDirectionFlag = point.isScanDirectionFlag();
    this.scanAngleRank = point.getScanAngleRank();
    this.pointSourceId = point.getPointSourceID();

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

  protected void postRead(final LasPoint point) {
    point.setXYZ(this.x, this.y, this.z);
    point.setIntensity(this.intensity);
    point.setReturnByte((byte)this.returnByte);

    point.setClassificationByte((byte)this.classificationByte);
    point.setScanAngleRank(this.scanAngleRank);
    point.setUserData((short)this.userData);
    point.setPointSourceID(this.pointSourceId);
  }

  protected int read(final ArithmeticModel[] models, final int lastValue) {
    ArithmeticModel model = models[lastValue];
    if (model == null) {
      model = this.dec.createSymbolModel(256);
      models[lastValue] = model;
      this.dec.initSymbolModel(model);
    }

    final int newValue = this.dec.decodeSymbol(model);
    return newValue;
  }

  @Override
  public void read(final LasPoint point) {
    final int medianX = median(this.last_x_diff);
    final int medianY = median(this.last_y_diff);

    // decompress x y z coordinates
    final int diffX = this.ic_dx.decompress(medianX);
    this.x += diffX;
    // we use the number k of bits corrector bits to switch contexts
    final int kBitsX = this.ic_dx.getK(); // unsigned
    final int diffY = this.ic_dy.decompress(medianY, kBitsX < 19 ? kBitsX : 19);
    this.y += diffY;
    final int kBitsY = (kBitsX + this.ic_dy.getK()) / 2;
    this.z = this.ic_z.decompress(this.z, kBitsY < 19 ? kBitsY : 19);

    final int changedValues = this.dec.decodeSymbol(this.m_changed_values);

    if (changedValues != 0) {
      if ((changedValues & 32) != 0) {
        this.intensity = this.ic_intensity.decompress(this.intensity);
      }

      if ((changedValues & 16) != 0) {
        this.returnByte = read(this.m_bit_byte, this.returnByte);
      }

      if ((changedValues & 8) != 0) {
        this.classificationByte = read(this.m_classification, this.classificationByte);
      }

      if ((changedValues & 4) != 0) {
        this.scanAngleRank = (byte)this.ic_scan_angle_rank.decompress(this.scanAngleRank,
          kBitsY < 3 ? 1 : 0);
      }

      if ((changedValues & 2) != 0) {
        this.userData = read(this.m_user_data, this.userData);
      }

      if ((changedValues & 1) != 0) {
        this.pointSourceId = this.ic_point_source_ID.decompress(this.pointSourceId);
      }
    }

    // record the difference
    this.last_x_diff[this.last_incr] = diffX;
    this.last_y_diff[this.last_incr] = diffY;
    this.last_incr++;
    if (this.last_incr > 2) {
      this.last_incr = 0;
    }

    postRead(point);
  }
}
