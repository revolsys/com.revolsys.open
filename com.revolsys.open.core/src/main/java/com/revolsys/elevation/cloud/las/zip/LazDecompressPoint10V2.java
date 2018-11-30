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

import static com.revolsys.elevation.cloud.las.zip.Common_v2.number_return_level;
import static com.revolsys.elevation.cloud.las.zip.Common_v2.number_return_map;
import static com.revolsys.elevation.cloud.las.zip.MyDefs.U32_ZERO_BIT_0;
import static com.revolsys.elevation.cloud.las.zip.MyDefs.U8_FOLD;
import static com.revolsys.elevation.cloud.las.zip.StreamingMedian5.newStreamingMedian5;

import java.nio.ByteBuffer;

import com.revolsys.elevation.cloud.las.LasPointCloud;
import com.revolsys.elevation.cloud.las.pointformat.LasPoint;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingDecompressDecoder;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingDecompressInteger;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingDecompressModel;

public class LazDecompressPoint10V2 implements LazDecompress {

  static class LASpoint10 {
    static LASpoint10 wrap(final byte[] data) {
      return new LASpoint10(ByteBuffer.wrap(data));
    }

    private final ByteBuffer bb;

    public LASpoint10() {
      this(ByteBuffer.allocate(20));
    }

    private LASpoint10(final ByteBuffer bb) {
      this.bb = bb;
    }

    char getIntensity() {
      return this.bb.getChar(12);
    }

    int getNumber_of_returns_of_given_pulse() {
      final byte b = this.bb.get(14);
      return b >>> 3 & 0x7;
    }

    char getPoint_source_ID() {
      return this.bb.getChar(18);
    }

    int getReturn_number() {
      final byte b = this.bb.get(14);
      return b & 0x7;
    }

    int getScan_direction_flag() {
      final byte b = this.bb.get(14);
      return b >>> 6 & 0x1;
    }

    int getX() {
      return this.bb.getInt(0);
    }

    int getY() {
      return this.bb.getInt(4);
    }

    int getZ() {
      return this.bb.getInt(8);
    }

    void setIntensity(final char i) {
      this.bb.putChar(12, i);
    }

    void setPoint_source_ID(final char id) {
      this.bb.putChar(18, id);
    }

    void setX(final int x) {
      this.bb.putInt(0, x);
    }

    void setY(final int y) {
      this.bb.putInt(4, y);
    }

    void setZ(final int z) {
      this.bb.putInt(8, z);
    }

    /*
     * int x; // 0 int y; // 4 int z; // 8 char intensity; // 12 byte
     * return_number : 3; // 14 byte number_of_returns_of_given_pulse : 3; // 14
     * byte scan_direction_flag : 1; // 14 byte edge_of_flight_line : 1; // 14
     * byte classification; // 15 byte scan_angle_rank; // 16 byte user_data; //
     * 17 char point_source_ID; // 18
     */
  }

  private final ArithmeticCodingDecompressDecoder dec;

  private final ArithmeticCodingDecompressInteger ic_z;

  private final ArithmeticCodingDecompressInteger ic_dx;

  private final ArithmeticCodingDecompressInteger ic_dy;

  private final ArithmeticCodingDecompressInteger ic_intensity;

  private final ArithmeticCodingDecompressInteger ic_point_source_ID;

  private final int[] last_height = new int[8]; // signed

  private final char[] last_intensity = new char[16];

  private final StreamingMedian5[] last_x_diff_median5 = newStreamingMedian5(16);

  private final StreamingMedian5[] last_y_diff_median5 = newStreamingMedian5(16);

  private final ArithmeticCodingDecompressModel[] m_bit_byte = new ArithmeticCodingDecompressModel[256];

  private final ArithmeticCodingDecompressModel m_changed_values;

  private final ArithmeticCodingDecompressModel[] m_classification = new ArithmeticCodingDecompressModel[256];

  private final ArithmeticCodingDecompressModel[] m_scan_angle_rank = new ArithmeticCodingDecompressModel[2];

  private final ArithmeticCodingDecompressModel[] m_user_data = new ArithmeticCodingDecompressModel[256];

  private final byte[] last_item = new byte[20];

  private final LASpoint10 lp = LASpoint10.wrap(this.last_item);

  public LazDecompressPoint10V2(final LasPointCloud pointCloud,
    final ArithmeticCodingDecompressDecoder dec) {
    int i; // unsigned

    /* set decoder */
    assert dec != null;
    this.dec = dec;

    /* create models and integer compressors */
    this.m_changed_values = new ArithmeticCodingDecompressModel(64);
    this.ic_intensity = new ArithmeticCodingDecompressInteger(dec, 16, 4);
    this.m_scan_angle_rank[0] = new ArithmeticCodingDecompressModel(256);
    this.m_scan_angle_rank[1] = new ArithmeticCodingDecompressModel(256);
    this.ic_point_source_ID = new ArithmeticCodingDecompressInteger(dec, 16);
    for (i = 0; i < 256; i++) {
      this.m_bit_byte[i] = null;
      this.m_classification[i] = null;
      this.m_user_data[i] = null;
    }
    this.ic_dx = new ArithmeticCodingDecompressInteger(dec, 32, 2); // 32 bits,
                                                                    // 2 context
    this.ic_dy = new ArithmeticCodingDecompressInteger(dec, 32, 22); // 32 bits,
                                                                     // 22
                                                                     // contexts
    this.ic_z = new ArithmeticCodingDecompressInteger(dec, 32, 20); // 32 bits,
                                                                    // 20
                                                                    // contexts
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
    this.m_changed_values.reset();
    this.ic_intensity.reset();
    this.m_scan_angle_rank[0].reset();
    this.m_scan_angle_rank[1].reset();
    this.ic_point_source_ID.reset();
    for (i = 0; i < 256; i++) {
      if (this.m_bit_byte[i] != null) {
        this.m_bit_byte[i].reset();
      }
      if (this.m_classification[i] != null) {
        this.m_classification[i].reset();
      }
      if (this.m_user_data[i] != null) {
        this.m_user_data[i].reset();
      }
    }
    this.ic_dx.reset();
    this.ic_dy.reset();
    this.ic_z.reset();

    this.lp.setX(point.getXInt()); // last_item[0]
    this.lp.setY(point.getYInt()); // last_item[4]
    this.lp.setZ(point.getZInt()); // last_item[8]

    /* but set intensity to zero */
    this.last_item[12] = 0;
    this.last_item[13] = 0;
    this.last_item[14] = point.getReturnByte();
    this.last_item[15] = point.getClassificationByte();
    this.last_item[16] = point.getScanAngleRank();
    this.last_item[17] = (byte)point.getUserData();
    this.lp.setPoint_source_ID((char)point.getPointSourceID());
  }

  private void postRead(final LasPoint point) {
    final int x = this.lp.getX();
    final int y = this.lp.getY();
    final int z = this.lp.getZ();
    point.setXYZ(x, y, z);
    point.setIntensity(this.lp.getIntensity());
    point.setReturnByte(this.last_item[14]);

    point.setClassificationByte(this.last_item[15]);
    point.setScanAngleRank(this.last_item[16]);
    point.setUserData(this.last_item[17]);
    point.setPointSourceID(this.lp.getPoint_source_ID());
  }

  @Override
  public void read(final LasPoint point) {
    int r, n, m, l; // unsigned
    int k_bits; // unsigned
    int median, diff; // signed

    // decompress which other values have changed
    final int changed_values = this.dec.decodeSymbol(this.m_changed_values);

    final LASpoint10 lp = this.lp;
    if (changed_values != 0) {
      // decompress the edge_of_flight_line, scan_direction_flag, ... if it has
      // changed
      if ((changed_values & 32) != 0) {
        if (this.m_bit_byte[Byte.toUnsignedInt(this.last_item[14])] == null) {
          this.m_bit_byte[Byte
            .toUnsignedInt(this.last_item[14])] = new ArithmeticCodingDecompressModel(256);
          this.m_bit_byte[Byte.toUnsignedInt(this.last_item[14])].reset();
        }
        this.last_item[14] = (byte)this.dec
          .decodeSymbol(this.m_bit_byte[Byte.toUnsignedInt(this.last_item[14])]);
      }

      r = lp.getReturn_number();
      n = lp.getNumber_of_returns_of_given_pulse();
      m = number_return_map[n][r];
      l = number_return_level[n][r];

      // decompress the intensity if it has changed
      if ((changed_values & 16) != 0) {
        lp.setIntensity((char)this.ic_intensity.decompress(this.last_intensity[m], m < 3 ? m : 3));
        this.last_intensity[m] = lp.getIntensity();
      } else {
        lp.setIntensity(this.last_intensity[m]);
      }

      // decompress the classification ... if it has changed
      if ((changed_values & 8) != 0) {
        if (this.m_classification[Byte.toUnsignedInt(this.last_item[15])] == null) {
          this.m_classification[Byte
            .toUnsignedInt(this.last_item[15])] = new ArithmeticCodingDecompressModel(256);
          this.m_classification[Byte.toUnsignedInt(this.last_item[15])].reset();
        }
        this.last_item[15] = (byte)this.dec
          .decodeSymbol(this.m_classification[Byte.toUnsignedInt(this.last_item[15])]);
      }

      // decompress the scan_angle_rank ... if it has changed
      if ((changed_values & 4) != 0) {
        final int val = this.dec.decodeSymbol(this.m_scan_angle_rank[lp.getScan_direction_flag()]);
        this.last_item[16] = U8_FOLD(val + this.last_item[16]);
      }

      // decompress the user_data ... if it has changed
      if ((changed_values & 2) != 0) {
        if (this.m_user_data[Byte.toUnsignedInt(this.last_item[17])] == null) {
          this.m_user_data[Byte
            .toUnsignedInt(this.last_item[17])] = new ArithmeticCodingDecompressModel(256);
          this.m_user_data[Byte.toUnsignedInt(this.last_item[17])].reset();
        }
        this.last_item[17] = (byte)this.dec
          .decodeSymbol(this.m_user_data[Byte.toUnsignedInt(this.last_item[17])]);
      }

      // decompress the point_source_ID ... if it has changed
      if ((changed_values & 1) != 0) {
        lp.setPoint_source_ID((char)this.ic_point_source_ID.decompress(lp.getPoint_source_ID()));
      }
    } else {
      r = lp.getReturn_number();
      n = lp.getNumber_of_returns_of_given_pulse();
      m = number_return_map[n][r];
      l = number_return_level[n][r];
    }

    // decompress x coordinate
    median = this.last_x_diff_median5[m].get();
    diff = this.ic_dx.decompress(median, n == 1 ? 1 : 0);
    lp.setX(lp.getX() + diff);
    this.last_x_diff_median5[m].add(diff);

    // decompress y coordinate
    median = this.last_y_diff_median5[m].get();
    k_bits = this.ic_dx.getK();
    diff = this.ic_dy.decompress(median,
      (n == 1 ? 1 : 0) + (k_bits < 20 ? U32_ZERO_BIT_0(k_bits) : 20));
    lp.setY(lp.getY() + diff);
    this.last_y_diff_median5[m].add(diff);

    // decompress z coordinate
    k_bits = (this.ic_dx.getK() + this.ic_dy.getK()) / 2;
    lp.setZ(this.ic_z.decompress(this.last_height[l],
      (n == 1 ? 1 : 0) + (k_bits < 18 ? U32_ZERO_BIT_0(k_bits) : 18)));
    this.last_height[l] = lp.getZ();

    postRead(point);
  }

}
