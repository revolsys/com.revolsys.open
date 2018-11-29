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

import java.nio.ByteBuffer;

import com.revolsys.elevation.cloud.las.LasPointCloud;
import com.revolsys.elevation.cloud.las.pointformat.LasPoint;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingDecompressDecoder;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingDecompressInteger;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingDecompressModel;

public class LazDecompressPoint10V1 implements LazDecompress {

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

    char getPoint_source_ID() {
      return this.bb.getChar(18);
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

    // int x; 0
    // int y; 4
    // int z; 8
    // char intensity; 12
    // byte return_number = 3; 14
    // byte number_of_returns_of_given_pulse = 3; 14
    // byte scan_direction_flag = 1; 14
    // byte edge_of_flight_line = 1; 14
    // byte classification; 15
    // byte scan_angle_rank; 16
    // byte user_data; 17
    // char point_source_ID; 18
  };

  private final ArithmeticCodingDecompressDecoder dec;

  private final ArithmeticCodingDecompressModel m_changed_values;

  private final ArithmeticCodingDecompressInteger ic_dx;

  private final ArithmeticCodingDecompressInteger ic_dy;

  private final ArithmeticCodingDecompressInteger ic_intensity;

  private final ArithmeticCodingDecompressInteger ic_point_source_ID;

  private final ArithmeticCodingDecompressInteger ic_z;

  private final ArithmeticCodingDecompressModel[] m_bit_byte = new ArithmeticCodingDecompressModel[256];

  private final ArithmeticCodingDecompressModel[] m_classification = new ArithmeticCodingDecompressModel[256];

  private final ArithmeticCodingDecompressModel[] m_user_data = new ArithmeticCodingDecompressModel[256];

  private final int[] last_x_diff = new int[3];

  private final int[] last_y_diff = new int[3];

  private int last_incr;

  private final ArithmeticCodingDecompressInteger ic_scan_angle_rank;

  private final byte[] last_item = new byte[20];

  private final LASpoint10 lp = LASpoint10.wrap(this.last_item);

  public LazDecompressPoint10V1(final LasPointCloud pointCloud, final ArithmeticCodingDecompressDecoder decoder) {
    this.dec = decoder;
    this.m_changed_values = decoder.createSymbolModel(64);

    this.ic_dx = new ArithmeticCodingDecompressInteger(decoder, 32);
    this.ic_dy = new ArithmeticCodingDecompressInteger(decoder, 32, 20);
    this.ic_z = new ArithmeticCodingDecompressInteger(decoder, 32, 20);
    this.ic_intensity = new ArithmeticCodingDecompressInteger(decoder, 16);
    this.ic_scan_angle_rank = new ArithmeticCodingDecompressInteger(decoder, 8, 2);
    this.ic_point_source_ID = new ArithmeticCodingDecompressInteger(decoder, 16);
  }

  @Override
  public void init(final LasPoint point) {
    int i;

    /* init state */
    this.last_x_diff[0] = this.last_x_diff[1] = this.last_x_diff[2] = 0;
    this.last_y_diff[0] = this.last_y_diff[1] = this.last_y_diff[2] = 0;
    this.last_incr = 0;

    /* init models and integer compressors */
    this.ic_dx.reset();
    this.ic_dy.reset();
    this.ic_z.reset();
    this.ic_intensity.reset();
    this.ic_scan_angle_rank.reset();
    this.ic_point_source_ID.reset();
    ArithmeticCodingDecompressDecoder r = this.dec;
    this.m_changed_values.reset();
    for (i = 0; i < 256; i++) {
      if (this.m_bit_byte[i] != null) {
        ArithmeticCodingDecompressDecoder r1 = this.dec;
        this.m_bit_byte[i].reset();
      }
      if (this.m_classification[i] != null) {
        ArithmeticCodingDecompressDecoder r1 = this.dec;
        this.m_classification[i].reset();
      }
      if (this.m_user_data[i] != null) {
        ArithmeticCodingDecompressDecoder r1 = this.dec;
        this.m_user_data[i].reset();
      }
    }

    this.lp.setX(point.getXInt()); // last_item[0]
    this.lp.setY(point.getYInt()); // last_item[4]
    this.lp.setZ(point.getZInt()); // last_item[8]

    /* but set intensity to zero */
    this.lp.setIntensity((char)point.getIntensity());
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
    // find median difference for x and y from 3 preceding differences
    int median_x;
    if (this.last_x_diff[0] < this.last_x_diff[1]) {
      if (this.last_x_diff[1] < this.last_x_diff[2]) {
        median_x = this.last_x_diff[1];
      } else if (this.last_x_diff[0] < this.last_x_diff[2]) {
        median_x = this.last_x_diff[2];
      } else {
        median_x = this.last_x_diff[0];
      }
    } else {
      if (this.last_x_diff[0] < this.last_x_diff[2]) {
        median_x = this.last_x_diff[0];
      } else if (this.last_x_diff[1] < this.last_x_diff[2]) {
        median_x = this.last_x_diff[2];
      } else {
        median_x = this.last_x_diff[1];
      }
    }

    int median_y;
    if (this.last_y_diff[0] < this.last_y_diff[1]) {
      if (this.last_y_diff[1] < this.last_y_diff[2]) {
        median_y = this.last_y_diff[1];
      } else if (this.last_y_diff[0] < this.last_y_diff[2]) {
        median_y = this.last_y_diff[2];
      } else {
        median_y = this.last_y_diff[0];
      }
    } else {
      if (this.last_y_diff[0] < this.last_y_diff[2]) {
        median_y = this.last_y_diff[0];
      } else if (this.last_y_diff[1] < this.last_y_diff[2]) {
        median_y = this.last_y_diff[2];
      } else {
        median_y = this.last_y_diff[1];
      }
    }

    // decompress x y z coordinates
    final int x_diff = this.ic_dx.decompress(median_x);
    final LASpoint10 lp = this.lp;
    lp.setX(lp.getX() + x_diff);
    // we use the number k of bits corrector bits to switch contexts
    int k_bits = this.ic_dx.getK(); // unsigned
    final int y_diff = this.ic_dy.decompress(median_y, k_bits < 19 ? k_bits : 19);
    lp.setY(lp.getY() + y_diff);
    k_bits = (k_bits + this.ic_dy.getK()) / 2;
    lp.setZ(this.ic_z.decompress(lp.getZ(), k_bits < 19 ? k_bits : 19));

    // decompress which other values have changed
    final int changed_values = this.dec.decodeSymbol(this.m_changed_values);

    if (changed_values != 0) {
      // decompress the intensity if it has changed
      if ((changed_values & 32) != 0) {
        lp.setIntensity((char)this.ic_intensity.decompress(lp.getIntensity()));
      }

      // decompress the edge_of_flight_line, scan_direction_flag, ... if it has
      // changed
      if ((changed_values & 16) != 0) {
        if (this.m_bit_byte[this.last_item[14]] == null) {
          this.m_bit_byte[this.last_item[14]] = this.dec.createSymbolModel(256);
          ArithmeticCodingDecompressDecoder r = this.dec;
          this.m_bit_byte[this.last_item[14]].reset();
        }
        this.last_item[14] = (byte)this.dec.decodeSymbol(this.m_bit_byte[this.last_item[14]]);
      }

      // decompress the classification ... if it has changed
      if ((changed_values & 8) != 0) {
        if (this.m_classification[this.last_item[15]] == null) {
          this.m_classification[this.last_item[15]] = this.dec.createSymbolModel(256);
          ArithmeticCodingDecompressDecoder r = this.dec;
          this.m_classification[this.last_item[15]].reset();
        }
        this.last_item[15] = (byte)this.dec.decodeSymbol(this.m_classification[this.last_item[15]]);
      }

      // decompress the scan_angle_rank ... if it has changed
      if ((changed_values & 4) != 0) {
        this.last_item[16] = (byte)this.ic_scan_angle_rank.decompress(this.last_item[16],
          k_bits < 3 ? 1 : 0);
      }

      // decompress the user_data ... if it has changed
      if ((changed_values & 2) != 0) {
        if (this.m_user_data[this.last_item[17]] == null) {
          this.m_user_data[this.last_item[17]] = this.dec.createSymbolModel(256);
          ArithmeticCodingDecompressDecoder r = this.dec;
          this.m_user_data[this.last_item[17]].reset();
        }
        this.last_item[17] = (byte)this.dec.decodeSymbol(this.m_user_data[this.last_item[17]]);
      }

      // decompress the point_source_ID ... if it has changed
      if ((changed_values & 1) != 0) {
        lp.setPoint_source_ID((char)this.ic_point_source_ID.decompress(lp.getPoint_source_ID()));
      }
    }

    // record the difference
    this.last_x_diff[this.last_incr] = x_diff;
    this.last_y_diff[this.last_incr] = y_diff;
    this.last_incr++;
    if (this.last_incr > 2) {
      this.last_incr = 0;
    }

    postRead(point);
  }
}
