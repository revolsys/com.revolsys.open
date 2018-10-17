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

import static com.revolsys.elevation.cloud.las.zip.StreamingMedian5.newStreamingMedian5;

import com.revolsys.elevation.cloud.las.LasPointCloud;
import com.revolsys.elevation.cloud.las.pointformat.LasPoint;

public class LazDecompressPoint10V2 implements LazDecompress {

  protected int classificationByte;

  protected final ArithmeticDecoder dec;

  protected IntegerCompressor i_z;

  protected IntegerCompressor ic_dx;

  protected IntegerCompressor ic_dy;

  protected IntegerCompressor ic_intensity;

  protected IntegerCompressor ic_point_source_ID;

  protected int intensity = 0;

  private final int[] last_height = new int[8]; // signed

  private final int[] last_intensity = new int[16];

  private final StreamingMedian5[] last_x_diff_median5 = newStreamingMedian5(16);

  private final StreamingMedian5[] last_y_diff_median5 = newStreamingMedian5(16);

  protected final ArithmeticModel[] m_bit_byte = new ArithmeticModel[256];

  protected final ArithmeticModel m_changed_values;

  protected final ArithmeticModel[] m_classification = new ArithmeticModel[256];

  private final ArithmeticModel m_scan_angle_rank_false;

  private final ArithmeticModel m_scan_angle_rank_true;

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

  public LazDecompressPoint10V2(final LasPointCloud pointCloud, final ArithmeticDecoder decoder) {
    this.pointCloud = pointCloud;
    this.dec = decoder;
    this.m_changed_values = decoder.createSymbolModel(64);
    this.m_scan_angle_rank_false = decoder.createSymbolModel(256);

    this.m_scan_angle_rank_true = decoder.createSymbolModel(256);

    this.ic_dx = new IntegerCompressor(decoder, 32, 2);
    this.ic_dy = new IntegerCompressor(decoder, 32, 22);
    this.i_z = new IntegerCompressor(decoder, 32, 20);
    this.ic_intensity = new IntegerCompressor(decoder, 16, 4);
    this.ic_point_source_ID = new IntegerCompressor(decoder, 16);
  }

  @Override
  public void init(final LasPoint point) {
    int i; // unsigned

    /* init state */
    for (i = 0; i < 16; i++) {
      this.last_x_diff_median5[i].init();
      this.last_y_diff_median5[i].init();
      this.last_intensity[i] = 0;
      this.last_height[i / 2] = 0;
    }

    /* init models and integer compressors */
    this.dec.initSymbolModel(this.m_changed_values);
    this.ic_intensity.initDecompressor();
    this.dec.initSymbolModel(this.m_scan_angle_rank_false);
    this.dec.initSymbolModel(this.m_scan_angle_rank_true);
    this.ic_point_source_ID.initDecompressor();
    for (int i1 = 0; i1 < 256; i1++) {
      if (this.m_bit_byte[i1] != null) {
        this.dec.initSymbolModel(this.m_bit_byte[i1]);
      }
      if (this.m_classification[i1] != null) {
        this.dec.initSymbolModel(this.m_classification[i1]);
      }
      if (this.m_user_data[i1] != null) {
        this.dec.initSymbolModel(this.m_user_data[i1]);
      }
    }
    this.ic_dx.initDecompressor();
    this.ic_dy.initDecompressor();
    this.i_z.initDecompressor();

    this.x = point.getXInt();
    this.y = point.getYInt();
    this.z = point.getZInt();

    this.classificationByte = (short)Byte.toUnsignedInt(point.getClassificationByte());
    this.returnByte = (short)Byte.toUnsignedInt(point.getReturnByte());
    this.userData = point.getUserData();
    this.scanDirectionFlag = point.isScanDirectionFlag();
    this.scanAngleRank = point.getScanAngleRank();
    this.pointSourceId = point.getPointSourceID();

    this.intensity = 0;
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

    System.out.println("changedValues");
    final int changedValues = this.dec.decodeSymbol(this.m_changed_values);
    final int m;
    int returnNumber;
    int returnCount;
    final int l;
    if (changedValues == 0) {
      returnNumber = this.returnByte & 0b111;
      returnCount = this.returnByte >> 3 & 0b111;
      m = Common_v2.NUMBER_RETURN_MAP[returnCount][returnNumber];
      l = Common_v2.NUMBER_RETURN_LEVEL[returnCount][returnNumber];
    } else {
      // decompress the edge_of_flight_line, scan_direction_flag, ... if it has
      // changed
      if ((changedValues & 32) != 0) {
        System.out.println("return");
        this.returnByte = read(this.m_bit_byte, this.returnByte);
      }

      returnNumber = this.returnByte & 0b111;
      returnCount = this.returnByte >> 3 & 0b111;
      m = Common_v2.NUMBER_RETURN_MAP[returnCount][returnNumber];
      l = Common_v2.NUMBER_RETURN_LEVEL[returnCount][returnNumber];

      // decompress the intensity if it has changed
      if ((changedValues & 16) != 0) {
        System.out.println("intensity");
        this.intensity = this.ic_intensity.decompress(this.last_intensity[m], m < 3 ? m : 3);
        this.last_intensity[m] = this.intensity;
      } else {
        this.intensity = this.last_intensity[m];
      }

      if ((changedValues & 8) != 0) {
        System.out.println("classification");
        this.classificationByte = read(this.m_classification, this.classificationByte);
      }

      // decompress the scan_angle_rank ... if it has changed
      if ((changedValues & 4) != 0) {
        System.out.println("scan angle");
        ArithmeticModel model;
        if (this.scanDirectionFlag) {
          model = this.m_scan_angle_rank_true;
        } else {
          model = this.m_scan_angle_rank_false;
        }
        final int val = this.dec.decodeSymbol(model);
        this.scanAngleRank = (byte)Byte.toUnsignedInt(MyDefs.U8_FOLD(val + this.scanAngleRank));
      }

      if ((changedValues & 2) != 0) {
        System.out.println("user data");
        this.userData = read(this.m_user_data, this.userData);
      }

      // decompress the point_source_ID ... if it has changed
      if ((changedValues & 1) != 0) {
        System.out.println("pointSourceId");
        this.pointSourceId = this.ic_point_source_ID.decompress(this.pointSourceId);
      }
    }

    // decompress x coordinate
    final int medianX = this.last_x_diff_median5[m].get();
    System.out.println("x");
    final int diffX = this.ic_dx.decompress(medianX, returnCount == 1 ? 1 : 0);
    this.x += diffX;
    this.last_x_diff_median5[m].add(diffX);

    // decompress y coordinate
    final int medianY = this.last_y_diff_median5[m].get();
    final int kBitsY = this.ic_dx.getK();
    System.out.println("y");
    final int diffY = this.ic_dy.decompress(medianY,
      (returnCount == 1 ? 1 : 0) + (kBitsY < 20 ? MyDefs.U32_ZERO_BIT_0(kBitsY) : 20));
    this.y += diffY;
    this.last_y_diff_median5[m].add(diffY);

    // decompress z coordinate
    System.out.println("z");
    final int kBitsZ = (this.ic_dx.getK() + this.ic_dy.getK()) / 2;
    this.z = this.i_z.decompress(this.last_height[l],
      (returnCount == 1 ? 1 : 0) + (kBitsZ < 18 ? MyDefs.U32_ZERO_BIT_0(kBitsZ) : 18));
    this.last_height[l] = this.z;

    postRead(point);
    this.scanDirectionFlag = point.isScanDirectionFlag();
  }

}
