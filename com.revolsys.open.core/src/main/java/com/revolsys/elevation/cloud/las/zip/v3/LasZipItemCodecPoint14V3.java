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
package com.revolsys.elevation.cloud.las.zip.v3;

import java.util.Arrays;

import com.revolsys.elevation.cloud.las.pointformat.LasPoint;
import com.revolsys.elevation.cloud.las.zip.ArithmeticDecoderByteArray;
import com.revolsys.elevation.cloud.las.zip.ArithmeticEncoderByteArray;
import com.revolsys.elevation.cloud.las.zip.LasZipItemCodec;
import com.revolsys.io.channels.ChannelReader;
import com.revolsys.io.channels.ChannelWriter;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingCodec;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingInteger;
import com.revolsys.math.arithmeticcoding.ArithmeticDecoder;
import com.revolsys.math.arithmeticcoding.ArithmeticEncoder;
import com.revolsys.math.arithmeticcoding.ArithmeticModel;
import com.revolsys.util.number.Longs;

public class LasZipItemCodecPoint14V3 implements LasZipItemCodec {
  public static final byte[][] number_return_map_6ctx = {
    {
      0, 1, 2, 3, 4, 5, 3, 4, 4, 5, 5, 5, 5, 5, 5, 5
    }, {
      1, 0, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3
    }, {
      2, 1, 2, 4, 4, 4, 4, 4, 4, 4, 4, 3, 3, 3, 3, 3
    }, {
      3, 3, 4, 5, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4
    }, {
      4, 3, 4, 4, 5, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4
    }, {
      5, 3, 4, 4, 4, 5, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4
    }, {
      3, 3, 4, 4, 4, 4, 5, 4, 4, 4, 4, 4, 4, 4, 4, 4
    }, {
      4, 3, 4, 4, 4, 4, 4, 5, 4, 4, 4, 4, 4, 4, 4, 4
    }, {
      4, 3, 4, 4, 4, 4, 4, 4, 5, 4, 4, 4, 4, 4, 4, 4
    }, {
      5, 3, 4, 4, 4, 4, 4, 4, 4, 5, 4, 4, 4, 4, 4, 4
    }, {
      5, 3, 4, 4, 4, 4, 4, 4, 4, 4, 5, 4, 4, 4, 4, 4
    }, {
      5, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 4, 4, 4
    }, {
      5, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 4, 4
    }, {
      5, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 4
    }, {
      5, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5
    }, {
      5, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5
    }
  };

  public static final byte[][] number_return_level_8ctx = {
    {
      0, 1, 2, 3, 4, 5, 6, 7, 7, 7, 7, 7, 7, 7, 7, 7
    }, {
      1, 0, 1, 2, 3, 4, 5, 6, 7, 7, 7, 7, 7, 7, 7, 7
    }, {
      2, 1, 0, 1, 2, 3, 4, 5, 6, 7, 7, 7, 7, 7, 7, 7
    }, {
      3, 2, 1, 0, 1, 2, 3, 4, 5, 6, 7, 7, 7, 7, 7, 7
    }, {
      4, 3, 2, 1, 0, 1, 2, 3, 4, 5, 6, 7, 7, 7, 7, 7
    }, {
      5, 4, 3, 2, 1, 0, 1, 2, 3, 4, 5, 6, 7, 7, 7, 7
    }, {
      6, 5, 4, 3, 2, 1, 0, 1, 2, 3, 4, 5, 6, 7, 7, 7
    }, {
      7, 6, 5, 4, 3, 2, 1, 0, 1, 2, 3, 4, 5, 6, 7, 7
    }, {
      7, 7, 6, 5, 4, 3, 2, 1, 0, 1, 2, 3, 4, 5, 6, 7
    }, {
      7, 7, 7, 6, 5, 4, 3, 2, 1, 0, 1, 2, 3, 4, 5, 6
    }, {
      7, 7, 7, 7, 6, 5, 4, 3, 2, 1, 0, 1, 2, 3, 4, 5
    }, {
      7, 7, 7, 7, 7, 6, 5, 4, 3, 2, 1, 0, 1, 2, 3, 4
    }, {
      7, 7, 7, 7, 7, 7, 6, 5, 4, 3, 2, 1, 0, 1, 2, 3
    }, {
      7, 7, 7, 7, 7, 7, 7, 6, 5, 4, 3, 2, 1, 0, 1, 2
    }, {
      7, 7, 7, 7, 7, 7, 7, 7, 6, 5, 4, 3, 2, 1, 0, 1
    }, {
      7, 7, 7, 7, 7, 7, 7, 7, 7, 6, 5, 4, 3, 2, 1, 0
    }
  };

  public static final int LASZIP_DECOMPRESS_SELECTIVE_ALL = 0xFFFFFFFF;

  public static final int LASZIP_DECOMPRESS_SELECTIVE_CHANNEL_RETURNS_XY = 0x00000000;

  public static final int LASZIP_DECOMPRESS_SELECTIVE_Z = 0x00000001;

  public static final int LASZIP_DECOMPRESS_SELECTIVE_CLASSIFICATION = 0x00000002;

  public static final int LASZIP_DECOMPRESS_SELECTIVE_FLAGS = 0x00000004;

  public static final int LASZIP_DECOMPRESS_SELECTIVE_INTENSITY = 0x00000008;

  public static final int LASZIP_DECOMPRESS_SELECTIVE_SCAN_ANGLE = 0x00000010;

  public static final int LASZIP_DECOMPRESS_SELECTIVE_USER_DATA = 0x00000020;

  public static final int LASZIP_DECOMPRESS_SELECTIVE_POINT_SOURCE = 0x00000040;

  public static final int LASZIP_DECOMPRESS_SELECTIVE_GPS_TIME = 0x00000080;

  public static final int LASZIP_DECOMPRESS_SELECTIVE_RGB = 0x00000100;

  public static final int LASZIP_DECOMPRESS_SELECTIVE_NIR = 0x00000200;

  public static final int LASZIP_DECOMPRESS_SELECTIVE_WAVEPACKET = 0x00000400;

  public static final int LASZIP_DECOMPRESS_SELECTIVE_BYTE0 = 0x00010000;

  public static final int LASZIP_DECOMPRESS_SELECTIVE_BYTE1 = 0x00020000;

  public static final int LASZIP_DECOMPRESS_SELECTIVE_BYTE2 = 0x00040000;

  public static final int LASZIP_DECOMPRESS_SELECTIVE_BYTE3 = 0x00080000;

  public static final int LASZIP_DECOMPRESS_SELECTIVE_BYTE4 = 0x00100000;

  public static final int LASZIP_DECOMPRESS_SELECTIVE_BYTE5 = 0x00200000;

  public static final int LASZIP_DECOMPRESS_SELECTIVE_BYTE6 = 0x00400000;

  public static final int LASZIP_DECOMPRESS_SELECTIVE_BYTE7 = 0x00800000;

  public static final int LASZIP_DECOMPRESS_SELECTIVE_EXTRA_BYTES = 0xFFFF0000;

  private static final int LASZIP_GPSTIME_MULTI = 500;

  private static final int LASZIP_GPSTIME_MULTI_MINUS = -10;

  private static final int LASZIP_GPSTIME_MULTI_CODE_FULL = LASZIP_GPSTIME_MULTI
    - LASZIP_GPSTIME_MULTI_MINUS + 1;

  private static final int LASZIP_GPSTIME_MULTI_TOTAL = LASZIP_GPSTIME_MULTI
    - LASZIP_GPSTIME_MULTI_MINUS + 5;

  private ArithmeticDecoder dec;

  private ArithmeticEncoder enc;

  private final ArithmeticEncoderByteArray enc_channel_returns_XY = new ArithmeticEncoderByteArray();

  private final ArithmeticEncoderByteArray enc_Z = new ArithmeticEncoderByteArray();

  private final ArithmeticEncoderByteArray enc_classification = new ArithmeticEncoderByteArray();

  private final ArithmeticEncoderByteArray enc_flags = new ArithmeticEncoderByteArray();

  private final ArithmeticEncoderByteArray enc_intensity = new ArithmeticEncoderByteArray();

  private final ArithmeticEncoderByteArray enc_scan_angle = new ArithmeticEncoderByteArray();

  private final ArithmeticEncoderByteArray enc_user_data = new ArithmeticEncoderByteArray();

  private final ArithmeticEncoderByteArray enc_point_source = new ArithmeticEncoderByteArray();

  private final ArithmeticEncoderByteArray enc_gps_time = new ArithmeticEncoderByteArray();

  private final ArithmeticDecoderByteArray dec_channel_returns_XY = new ArithmeticDecoderByteArray();

  private final ArithmeticDecoderByteArray dec_Z = new ArithmeticDecoderByteArray();

  private final ArithmeticDecoderByteArray dec_classification = new ArithmeticDecoderByteArray();

  private final ArithmeticDecoderByteArray dec_flags = new ArithmeticDecoderByteArray();

  private final ArithmeticDecoderByteArray dec_intensity = new ArithmeticDecoderByteArray();

  private final ArithmeticDecoderByteArray dec_scan_angle = new ArithmeticDecoderByteArray();

  private final ArithmeticDecoderByteArray dec_user_data = new ArithmeticDecoderByteArray();

  private final ArithmeticDecoderByteArray dec_point_source = new ArithmeticDecoderByteArray();

  private final ArithmeticDecoderByteArray dec_gps_time = new ArithmeticDecoderByteArray();

  private boolean changed_Z;

  private boolean changed_classification;

  private boolean changed_flags;

  private boolean changed_intensity;

  private boolean changed_scan_angle;

  private boolean changed_user_data;

  private boolean changed_point_source;

  private boolean changed_gps_time;

  private final boolean requested_Z;

  private final boolean requested_classification;

  private final boolean requested_flags;

  private final boolean requested_intensity;

  private final boolean requested_scan_angle;

  private final boolean requested_user_data;

  private final boolean requested_point_source;

  private final boolean requested_gps_time;

  private int current_context;

  private final LAScontextPOINT14[] contexts = new LAScontextPOINT14[4];

  public LasZipItemCodecPoint14V3(final ArithmeticCodingCodec codec) {
    if (codec instanceof ArithmeticDecoder) {
      this.dec = (ArithmeticDecoder)codec;
    } else if (codec instanceof ArithmeticEncoder) {
      this.enc = (ArithmeticEncoder)codec;
    } else {
      throw new IllegalArgumentException("Not supported:" + codec.getClass());
    }

    for (int i = 0; i < this.contexts.length; i++) {
      this.contexts[i] = new LAScontextPOINT14();
    }
    final int decompress_selective = LASZIP_DECOMPRESS_SELECTIVE_ALL;
    this.requested_Z = (decompress_selective & LASZIP_DECOMPRESS_SELECTIVE_Z) != 0;
    this.requested_classification = (decompress_selective
      & LASZIP_DECOMPRESS_SELECTIVE_CLASSIFICATION) != 0;
    this.requested_flags = (decompress_selective & LASZIP_DECOMPRESS_SELECTIVE_FLAGS) != 0;
    this.requested_intensity = (decompress_selective & LASZIP_DECOMPRESS_SELECTIVE_INTENSITY) != 0;
    this.requested_scan_angle = (decompress_selective
      & LASZIP_DECOMPRESS_SELECTIVE_SCAN_ANGLE) != 0;
    this.requested_user_data = (decompress_selective & LASZIP_DECOMPRESS_SELECTIVE_USER_DATA) != 0;
    this.requested_point_source = (decompress_selective
      & LASZIP_DECOMPRESS_SELECTIVE_POINT_SOURCE) != 0;
    this.requested_gps_time = (decompress_selective & LASZIP_DECOMPRESS_SELECTIVE_GPS_TIME) != 0;

  }

  @Override
  public void readChunkSizes() {
    final ChannelReader in = this.dec.getIn();
    this.dec_channel_returns_XY.readSize(in);
    this.dec_Z.readSize(in);
    this.dec_classification.readSize(in);
    this.dec_flags.readSize(in);
    this.dec_intensity.readSize(in);
    this.dec_scan_angle.readSize(in);
    this.dec_user_data.readSize(in);
    this.dec_point_source.readSize(in);
    this.dec_gps_time.readSize(in);
  }

  private void createAndInitModelsAndCompressors(final int contextIndex, final LasPoint item) {

    /* first create all entropy models and integer compressors (if needed) */

    final LAScontextPOINT14 context = this.contexts[contextIndex];
    if (context.m_changed_values[0] == null) {
      /* for the channel_returns_XY layer */

      for (int i = 0; i < context.m_changed_values.length; i++) {
        context.m_changed_values[i] = this.enc_channel_returns_XY.createSymbolModel(128);
      }
      context.m_scanner_channel = this.enc_channel_returns_XY.createSymbolModel(3);
      Arrays.fill(context.m_number_of_returns, null);
      Arrays.fill(context.m_return_number, null);
      context.m_return_number_gps_same = this.enc_channel_returns_XY.createSymbolModel(13);

      context.ic_dX = new ArithmeticCodingInteger(this.enc_channel_returns_XY, 32, 2);
      context.ic_dY = new ArithmeticCodingInteger(this.enc_channel_returns_XY, 32, 22);
      context.ic_Z = new ArithmeticCodingInteger(this.enc_Z, 32, 20);

      Arrays.fill(context.m_classification, null);
      Arrays.fill(context.m_flags, null);
      Arrays.fill(context.m_user_data, null);

      context.ic_intensity = new ArithmeticCodingInteger(this.enc_intensity, 16, 4);

      context.ic_scan_angle = new ArithmeticCodingInteger(this.enc_scan_angle, 16, 2);

      context.ic_point_source_ID = new ArithmeticCodingInteger(this.enc_point_source, 16);

      context.m_gpstime_multi = this.enc_gps_time.createSymbolModel(LASZIP_GPSTIME_MULTI_TOTAL);
      context.m_gpstime_0diff = this.enc_gps_time.createSymbolModel(5);
      context.ic_gpstime = new ArithmeticCodingInteger(this.enc_gps_time, 32, 9);
    }

    ArithmeticModel.initModels(context.m_changed_values);
    ArithmeticModel.initModel(context.m_scanner_channel);
    ArithmeticModel.initModels(context.m_number_of_returns);
    ArithmeticModel.initModels(context.m_return_number);
    ArithmeticModel.initModel(context.m_return_number_gps_same);
    context.ic_dX.initCompressor();
    context.ic_dY.initCompressor();
    for (int i = 0; i < 12; i++) {
      context.last_X_diff_median5[i].init();
      context.last_Y_diff_median5[i].init();
    }

    context.ic_Z.initCompressor();
    for (int i = 0; i < 8; i++) {
      context.last_Z[i] = item.getZInt();
    }

    ArithmeticModel.initModels(context.m_classification);
    ArithmeticModel.initModels(context.m_flags);
    ArithmeticModel.initModels(context.m_user_data);

    context.ic_intensity.initCompressor();
    for (int i = 0; i < 8; i++) {
      context.last_intensity[i] = item.getIntensity();
    }

    context.ic_scan_angle.initCompressor();

    context.ic_point_source_ID.initCompressor();

    ArithmeticModel.initModel(context.m_gpstime_multi);
    ArithmeticModel.initModel(context.m_gpstime_0diff);
    context.ic_gpstime.initCompressor();
    context.last = 0;
    context.next = 0;
    Arrays.fill(context.last_gpstime_diff, 0);
    Arrays.fill(context.multi_extreme_counter, 0);
    context.last_gpstime[0] = item.getGpsTimeLong();

    context.last_item = item;
    context.gps_time_change = false;

    context.unused = false;
  }

  private void createAndInitModelsAndDecompressors(final int contextIndex, final LasPoint point) {
    final LAScontextPOINT14 context = this.contexts[contextIndex];

    if (context.m_changed_values[0] == null) {
      /* for the channel_returns_XY layer */
      for (int i = 0; i <= 7; i++) {
        context.m_changed_values[i] = this.dec_channel_returns_XY.createSymbolModel(128);
      }
      context.m_scanner_channel = this.dec_channel_returns_XY.createSymbolModel(3);
      Arrays.fill(context.m_number_of_returns, null);
      Arrays.fill(context.m_return_number, null);

      context.m_return_number_gps_same = this.dec_channel_returns_XY.createSymbolModel(13);

      context.ic_dX = new ArithmeticCodingInteger(this.dec_channel_returns_XY, 32, 2);
      context.ic_dY = new ArithmeticCodingInteger(this.dec_channel_returns_XY, 32, 22);
      context.ic_Z = new ArithmeticCodingInteger(this.dec_Z, 32, 20);

      Arrays.fill(context.m_classification, null);
      Arrays.fill(context.m_flags, null);
      Arrays.fill(context.m_user_data, null);

      context.ic_intensity = new ArithmeticCodingInteger(this.dec_intensity, 16, 4);

      context.ic_scan_angle = new ArithmeticCodingInteger(this.dec_scan_angle, 16, 2);

      context.ic_point_source_ID = new ArithmeticCodingInteger(this.dec_point_source, 16);

      context.m_gpstime_multi = this.dec_gps_time.createSymbolModel(LASZIP_GPSTIME_MULTI_TOTAL);
      context.m_gpstime_0diff = this.dec_gps_time.createSymbolModel(5);
      context.ic_gpstime = new ArithmeticCodingInteger(this.dec_gps_time, 32, 9);
    }

    ArithmeticModel.initModels(context.m_changed_values);
    ArithmeticModel.initModel(context.m_scanner_channel);
    ArithmeticModel.initModels(context.m_number_of_returns);
    ArithmeticModel.initModels(context.m_return_number);
    ArithmeticModel.initModel(context.m_return_number_gps_same);
    context.ic_dX.initDecompressor();
    context.ic_dY.initDecompressor();
    for (int i = 0; i < 12; i++) {
      context.last_X_diff_median5[i].init();
      context.last_Y_diff_median5[i].init();
    }
    context.ic_Z.initDecompressor();
    for (int i = 0; i < 8; i++) {
      context.last_Z[i] = point.getZInt();
    }

    ArithmeticModel.initModels(context.m_classification);
    ArithmeticModel.initModels(context.m_flags);
    ArithmeticModel.initModels(context.m_user_data);

    context.ic_intensity.initDecompressor();
    for (int i = 0; i < 8; i++) {
      context.last_intensity[i] = point.getIntensity();
    }

    context.ic_scan_angle.initDecompressor();

    context.ic_point_source_ID.initDecompressor();

    ArithmeticModel.initModel(context.m_gpstime_multi);
    ArithmeticModel.initModel(context.m_gpstime_0diff);
    context.ic_gpstime.initDecompressor();
    context.last = 0;
    context.next = 0;
    context.last_gpstime_diff[0] = 0;
    context.last_gpstime_diff[1] = 0;
    context.last_gpstime_diff[2] = 0;
    context.last_gpstime_diff[3] = 0;
    context.multi_extreme_counter[0] = 0;
    context.multi_extreme_counter[1] = 0;
    context.multi_extreme_counter[2] = 0;
    context.multi_extreme_counter[3] = 0;
    context.last_gpstime[0] = point.getGpsTimeLong();
    context.last_gpstime[1] = 0;
    context.last_gpstime[2] = 0;
    context.last_gpstime[3] = 0;

    context.last_item = point;
    context.gps_time_change = false;

    context.unused = false;
  }

  @Override
  public int getVersion() {
    return 2;
  }

  @Override
  public int init(final LasPoint point, final int contextIndex) {

    /* on the first init create ins and decoders */

    if (this.enc != null) {
      return initCompress(point);
    }

    if (this.dec != null) {

      return initDecompress(point);
    }
    return this.current_context;
  }

  public int initCompress(final LasPoint point) {
    this.enc_channel_returns_XY.init();
    this.enc_Z.init();
    this.enc_classification.init();
    this.enc_flags.init();
    this.enc_intensity.init();
    this.enc_scan_angle.init();
    this.enc_user_data.init();
    this.enc_point_source.init();
    this.enc_gps_time.init();

    this.changed_classification = false;
    this.changed_flags = false;
    this.changed_intensity = false;
    this.changed_scan_angle = false;
    this.changed_user_data = false;
    this.changed_point_source = false;
    this.changed_gps_time = false;

    /* mark the four scanner channel contexts as unused */

    for (final LAScontextPOINT14 context : this.contexts) {
      context.unused = true;
    }

    this.current_context = point.getScannerChannel();

    createAndInitModelsAndCompressors(this.current_context, point);
    return this.current_context;
  }

  public int initDecompress(final LasPoint point) {
    final ChannelReader in = this.dec.getIn();

    this.dec_channel_returns_XY.initializeBytes(in, true);

    this.changed_Z = this.dec_Z.initializeBytes(in, this.requested_Z) != 0;

    this.changed_classification = this.dec_classification.initializeBytes(in,
      this.requested_classification) != 0;

    this.changed_flags = this.dec_flags.initializeBytes(in, this.requested_flags) != 0;

    this.changed_intensity = this.dec_intensity.initializeBytes(in, this.requested_intensity) != 0;

    this.changed_scan_angle = this.dec_scan_angle.initializeBytes(in,
      this.requested_scan_angle) != 0;

    this.changed_user_data = this.dec_user_data.initializeBytes(in, this.requested_user_data) != 0;

    this.changed_point_source = this.dec_point_source.initializeBytes(in,
      this.requested_point_source) != 0;

    this.changed_gps_time = this.dec_gps_time.initializeBytes(in, this.requested_gps_time) != 0;

    for (final LAScontextPOINT14 context : this.contexts) {
      context.unused = true;
    }

    this.current_context = point.getScannerChannel();

    createAndInitModelsAndDecompressors(this.current_context, point);

    return this.current_context;
  }

  @Override
  public int read(final LasPoint point, final int contextIndex) {
    LAScontextPOINT14 context = this.contexts[this.current_context];
    LasPoint lastPoint = context.last_item;

    int lpr = 0;
    final byte lastReturnNumber = lastPoint.getReturnNumber();
    if (lastReturnNumber == 1) {
      lpr |= 1;
    }
    final byte lastNumberOfReturns = lastPoint.getNumberOfReturns();
    if (lastReturnNumber >= lastNumberOfReturns) {
      lpr |= 0b10;
    }
    if (context.gps_time_change) {
      lpr |= 0b100;
    }

    final int changedValues = this.dec_channel_returns_XY
      .decodeSymbol(context.m_changed_values[lpr]);

    if ((changedValues & 1 << 6) != 0) {
      final int diff = this.dec_channel_returns_XY.decodeSymbol(context.m_scanner_channel);

      final int scanner_channel = (this.current_context + diff + 1) % 4;
      if (this.contexts[scanner_channel].unused) {
        createAndInitModelsAndDecompressors(scanner_channel, context.last_item);
      }
      this.current_context = scanner_channel;
      context = this.contexts[this.current_context];
      lastPoint = context.last_item;
      point.setScannerChannel((byte)scanner_channel);
    }

    final boolean gps_time_change = (changedValues & 1 << 4) != 0;

    byte numberOfReturns;
    if ((changedValues & 0b100) != 0) {
      numberOfReturns = (byte)this.dec_channel_returns_XY.decodeSymbol(context.m_number_of_returns,
        lastNumberOfReturns, 16);
    } else {
      numberOfReturns = lastNumberOfReturns;
    }
    point.setNumberOfReturns(numberOfReturns);

    byte returnNumber;
    switch (changedValues & 3) {
      case 0:
        returnNumber = lastReturnNumber;
      break;
      case 1:
        returnNumber = (byte)((lastReturnNumber + 1) % 16);
      break;
      case 2:
        returnNumber = (byte)((lastReturnNumber + 15) % 16);
      break;
      default:
        if (gps_time_change) {
          returnNumber = (byte)this.dec_channel_returns_XY.decodeSymbol(context.m_return_number,
            lastReturnNumber, 16);
        } else {
          final int sym = this.dec_channel_returns_XY
            .decodeSymbol(context.m_return_number_gps_same);
          returnNumber = (byte)((lastReturnNumber + sym + 2) % 16);
        }
      break;
    }
    point.setReturnNumber(returnNumber);

    final int m = number_return_map_6ctx[numberOfReturns][returnNumber];
    final int l = number_return_level_8ctx[numberOfReturns][returnNumber];

    int cpr = returnNumber == 1 ? 2 : 0;
    cpr += returnNumber >= numberOfReturns ? 1 : 0;

    final int gpsTimeChangeBit = gps_time_change ? 1 : 0;
    final int medianX = context.last_X_diff_median5[m << 1 | gpsTimeChangeBit].get();
    final int diffX = context.ic_dX.decompress(medianX, numberOfReturns == 1 ? 1 : 0);
    point.setXInt(lastPoint.getXInt() + diffX);
    context.last_X_diff_median5[m << 1 | gpsTimeChangeBit].add(diffX);

    final int medianY = context.last_Y_diff_median5[m << 1 | gpsTimeChangeBit].get();
    int k_bits = context.ic_dX.getK();
    final int diffY = context.ic_dY.decompress(medianY,
      (numberOfReturns == 1 ? 1 : 0) + (k_bits < 20 ? U32_ZERO_BIT_0(k_bits) : 20));
    point.setYInt(lastPoint.getYInt() + diffY);
    context.last_Y_diff_median5[m << 1 | gpsTimeChangeBit].add(diffY);

    if (this.changed_Z) {
      k_bits = (context.ic_dX.getK() + context.ic_dY.getK()) / 2;
      final int z = context.ic_Z.decompress(context.last_Z[l],
        (numberOfReturns == 1 ? 1 : 0) + (k_bits < 18 ? U32_ZERO_BIT_0(k_bits) : 18));
      point.setZInt(z);
      context.last_Z[l] = z;
    }

    if (this.changed_classification) {
      final int last_classification = lastPoint.getClassification();
      final int ccc = ((last_classification & 0x1F) << 1) + (cpr == 3 ? 1 : 0);
      final short classification = (short)this.dec_classification
        .decodeSymbol(context.m_classification, ccc, 256);
      point.setClassification(classification);

      if (classification < 32) {
        context.legacy_classification = classification;
      }
    }

    if (this.changed_flags) {
      final int last_flags = lastPoint.isEdgeOfFlightLine() ? 1 << 5
        : 0 | (lastPoint.isScanDirectionFlag() ? 1 << 4 : 0) | context.classification_flags;
      final int flags = this.dec_flags.decodeSymbol(context.m_flags, last_flags, 64);
      point.setEdgeOfFlightLine((flags & 1 << 5) != 0);
      point.setScanDirectionFlag((flags & 1 << 4) != 0);
      context.classification_flags = flags & 0x0F;

      context.legacy_flags = flags & 0x07;
    }

    if (this.changed_intensity) {
      final int intensity = context.ic_intensity
        .decompress(context.last_intensity[cpr << 1 | gpsTimeChangeBit], cpr);
      context.last_intensity[cpr << 1 | gpsTimeChangeBit] = intensity;
      point.setIntensity(intensity);
    }

    if (this.changed_scan_angle) {
      if ((changedValues & 0b1000) != 0) {
        final short lastScanAngle = lastPoint.getScanAngle();
        final short scan_angle = (short)context.ic_scan_angle.decompress(lastScanAngle,
          gpsTimeChangeBit);
        point.setScanAngle(scan_angle);
        context.legacy_scan_angle_rank = I8_CLAMP(I16_QUANTIZE(0.006f * scan_angle));
      }
    }

    if (this.changed_user_data) {
      final int lastUserDataDiv4 = lastPoint.getUserData() / 4;
      final short userData = (short)this.dec_user_data.decodeSymbol(context.m_user_data,
        lastUserDataDiv4, 256);
      point.setUserData(userData);
    }

    if (this.changed_point_source) {
      int pointSourceId;
      if ((changedValues & 0b100000) != 0) {
        pointSourceId = context.ic_point_source_ID.decompress(lastPoint.getPointSourceID());
      } else {
        pointSourceId = lastPoint.getPointSourceID();
      }
      point.setPointSourceID(pointSourceId);
    }

    if (this.changed_gps_time) {
      long gpsTime;
      if (gps_time_change) {
        read_gps_time();
        gpsTime = context.last_gpstime[context.last];
      } else {
        gpsTime = lastPoint.getGpsTimeLong();
      }
      point.setGpsTimeLong(gpsTime);
    }

    context.last_item = point;
    context.gps_time_change = gps_time_change;
    return this.current_context;
  }

  private void read_gps_time() {
    final LAScontextPOINT14 context = this.contexts[this.current_context];
    if (context.last_gpstime_diff[context.last] == 0) {
      final int multi = this.dec_gps_time.decodeSymbol(context.m_gpstime_0diff);
      if (multi == 0) {
        final int timeDiff = context.ic_gpstime.decompress(0, 0);
        context.last_gpstime_diff[context.last] = timeDiff;
        context.last_gpstime[context.last] += timeDiff;
        context.multi_extreme_counter[context.last] = 0;
      } else if (multi == 1) {
        context.next = context.next + 1 & 3;
        final int lastTimeUpper = (int)(context.last_gpstime[context.last] >>> 32);
        final int timeUpper = context.ic_gpstime.decompress(lastTimeUpper, 8);
        final int timeLower = this.dec_gps_time.readInt();
        context.last_gpstime[context.next] = Longs.toLong(timeUpper, timeLower);
        context.last = context.next;
        context.last_gpstime_diff[context.last] = 0;
        context.multi_extreme_counter[context.last] = 0;
      } else {
        context.last = context.last + multi - 1 & 3;
        read_gps_time();
      }
    } else {
      int multi = this.dec_gps_time.decodeSymbol(context.m_gpstime_multi);
      if (multi == 1) {
        context.last_gpstime[context.last] += context.ic_gpstime
          .decompress(context.last_gpstime_diff[context.last], 1);
        context.multi_extreme_counter[context.last] = 0;
      } else if (multi < LASZIP_GPSTIME_MULTI_CODE_FULL) {
        int gpstime_diff;
        if (multi == 0) {
          gpstime_diff = context.ic_gpstime.decompress(0, 7);
          context.multi_extreme_counter[context.last]++;
          if (context.multi_extreme_counter[context.last] > 3) {
            context.last_gpstime_diff[context.last] = gpstime_diff;
            context.multi_extreme_counter[context.last] = 0;
          }
        } else if (multi < LASZIP_GPSTIME_MULTI) {
          if (multi < 10) {
            gpstime_diff = context.ic_gpstime
              .decompress(multi * context.last_gpstime_diff[context.last], 2);
          } else {
            gpstime_diff = context.ic_gpstime
              .decompress(multi * context.last_gpstime_diff[context.last], 3);
          }
        } else if (multi == LASZIP_GPSTIME_MULTI) {
          gpstime_diff = context.ic_gpstime
            .decompress(LASZIP_GPSTIME_MULTI * context.last_gpstime_diff[context.last], 4);
          context.multi_extreme_counter[context.last]++;
          if (context.multi_extreme_counter[context.last] > 3) {
            context.last_gpstime_diff[context.last] = gpstime_diff;
            context.multi_extreme_counter[context.last] = 0;
          }
        } else {
          multi = LASZIP_GPSTIME_MULTI - multi;
          if (multi > LASZIP_GPSTIME_MULTI_MINUS) {
            gpstime_diff = context.ic_gpstime
              .decompress(multi * context.last_gpstime_diff[context.last], 5);
          } else {
            gpstime_diff = context.ic_gpstime
              .decompress(LASZIP_GPSTIME_MULTI_MINUS * context.last_gpstime_diff[context.last], 6);
            context.multi_extreme_counter[context.last]++;
            if (context.multi_extreme_counter[context.last] > 3) {
              context.last_gpstime_diff[context.last] = gpstime_diff;
              context.multi_extreme_counter[context.last] = 0;
            }
          }
        }
        context.last_gpstime[context.last] += gpstime_diff;
      } else if (multi == LASZIP_GPSTIME_MULTI_CODE_FULL) {
        context.next = context.next + 1 & 3;
        final int lastTimeUpper = (int)(context.last_gpstime[context.last] >>> 32);
        final int timeUpper = context.ic_gpstime.decompress(lastTimeUpper, 8);
        final int timeLower = this.dec_gps_time.readInt();
        context.last_gpstime[context.next] = Longs.toLong(timeUpper, timeLower);
        context.last = context.next;
        context.last_gpstime_diff[context.last] = 0;
        context.multi_extreme_counter[context.last] = 0;
      } else if (multi >= LASZIP_GPSTIME_MULTI_CODE_FULL) {
        context.last = context.last + multi - LASZIP_GPSTIME_MULTI_CODE_FULL & 3;
        read_gps_time();
      }
    }
  }

  @Override
  public int write(final LasPoint item, final int contextIndex) {
    LAScontextPOINT14 context = this.contexts[this.current_context];
    LasPoint lastPoint = context.last_item;

    final byte lastNumberOfReturns = lastPoint.getNumberOfReturns();
    final byte lastReturnNumber = lastPoint.getReturnNumber();
    int lpr = 0;
    if (lastReturnNumber == 1) {
      lpr |= 1;
    }
    if (lastReturnNumber >= lastReturnNumber) {
      lpr |= 2;
    }
    if (context.gps_time_change) {
      lpr |= 2;
    }

    final int scannerChannel = item.getScannerChannel();
    if (scannerChannel != this.current_context) {
      if (this.contexts[scannerChannel].unused == false) {
        lastPoint = this.contexts[scannerChannel].last_item;
      }
    }

    final int lasPointSourceID = lastPoint.getPointSourceID();
    final int pointSourceID = item.getPointSourceID();
    final boolean point_source_change = pointSourceID != lasPointSourceID;
    final boolean gps_time_change = item.getGpsTimeLong() != lastPoint.getGpsTimeLong();
    final short lastScanAngle = lastPoint.getScanAngle();
    final short scanAngle = item.getScanAngle();
    final boolean scan_angle_change = scanAngle != lastScanAngle;

    final int numberOfReturns = item.getNumberOfReturns();
    final int returnNumber = item.getReturnNumber();

    int changed_values = (scannerChannel != this.current_context ? 1 : 0) << 6 | //
      (point_source_change ? 1 : 0) << 5 | //
      (gps_time_change ? 1 : 0) << 4 | //
      (scan_angle_change ? 1 : 0) << 3 | (numberOfReturns != lastNumberOfReturns ? 1 : 0) << 2;

    if (returnNumber != lastReturnNumber) {
      if (returnNumber == (lastReturnNumber + 1) % 16) {
        changed_values |= 1;
      } else if (returnNumber == (lastReturnNumber + 15) % 16) {
        changed_values |= 2;
      } else {
        changed_values |= 3;
      }
    }

    this.enc_channel_returns_XY.encodeSymbol(context.m_changed_values[lpr], changed_values);

    if ((changed_values & 0b1000000) != 0) {
      final int diff = scannerChannel - this.current_context;
      if (diff > 0) {
        this.enc_channel_returns_XY.encodeSymbol(context.m_scanner_channel, diff - 1);
      } else {
        this.enc_channel_returns_XY.encodeSymbol(context.m_scanner_channel, diff + 4 - 1);
      }
      if (this.contexts[scannerChannel].unused) {
        createAndInitModelsAndCompressors(scannerChannel, context.last_item);
        lastPoint = this.contexts[scannerChannel].last_item;
      }
      this.current_context = scannerChannel;
      context = this.contexts[this.current_context];

    }

    if ((changed_values & 0b100) != 0) {
      this.enc_channel_returns_XY.encodeSymbol(context.m_number_of_returns, lastNumberOfReturns, 16,
        numberOfReturns);
    }

    if ((changed_values & 3) == 3) {
      if (gps_time_change) {
        this.enc_channel_returns_XY.encodeSymbol(context.m_return_number, lastReturnNumber, 16,
          returnNumber);
      } else {
        final int diff = returnNumber - lastReturnNumber;
        if (diff > 1) {
          this.enc_channel_returns_XY.encodeSymbol(context.m_return_number_gps_same, diff - 2);
        } else {
          this.enc_channel_returns_XY.encodeSymbol(context.m_return_number_gps_same, diff + 16 - 2);
        }
      }
    }

    final int m = number_return_map_6ctx[numberOfReturns][returnNumber];
    final int l = number_return_level_8ctx[numberOfReturns][returnNumber];

    int cpr = returnNumber == 1 ? 2 : 0;
    cpr += returnNumber >= numberOfReturns ? 1 : 0;

    final int medianX = context.last_X_diff_median5[m << 1 | (gps_time_change ? 1 : 0)].get();
    final int diffX = item.getXInt() - lastPoint.getXInt();
    context.ic_dX.compress(medianX, diffX, numberOfReturns == 1 ? 1 : 0);
    context.last_X_diff_median5[m << 1 | (gps_time_change ? 1 : 0)].add(diffX);

    int k_bits = context.ic_dX.getK();
    final int medianY = context.last_Y_diff_median5[m << 1 | (gps_time_change ? 1 : 0)].get();
    final int diffY = item.getYInt() - lastPoint.getYInt();
    context.ic_dY.compress(medianY, diffY,
      (numberOfReturns == 1 ? 1 : 0) + (k_bits < 20 ? U32_ZERO_BIT_0(k_bits) : 20));
    context.last_Y_diff_median5[m << 1 | (gps_time_change ? 1 : 0)].add(diffY);

    k_bits = (context.ic_dX.getK() + context.ic_dY.getK()) / 2;
    context.ic_Z.compress(context.last_Z[l], item.getZInt(),
      (numberOfReturns == 1 ? 1 : 0) + (k_bits < 18 ? U32_ZERO_BIT_0(k_bits) : 18));
    context.last_Z[l] = item.getZInt();

    final int lastClassification = lastPoint.getClassification();
    final int classification = item.getClassification();

    if (classification != lastClassification) {
      this.changed_classification = true;
    }

    final int ccc = ((lastClassification & 0x1F) << 1) + (cpr == 3 ? 1 : 0);
    this.enc_classification.encodeSymbol(context.m_classification, ccc, 256, classification);

    final int last_flags = (lastPoint.isEdgeOfFlightLine() ? 1 : 0) << 5
      | (lastPoint.isScanDirectionFlag() ? 1 : 0) << 4 | context.classification_flags;
    final int flags = (item.isEdgeOfFlightLine() ? 1 : 0) << 5
      | (item.isScanDirectionFlag() ? 1 : 0) << 4 | context.classification_flags;

    if (flags != last_flags) {
      this.changed_flags = true;
    }

    this.enc_flags.encodeSymbol(context.m_flags, last_flags, 64, flags);

    final int intensity = item.getIntensity();
    final int lastIntensity = lastPoint.getIntensity();
    if (intensity != lastIntensity) {
      this.changed_intensity = true;
    }
    context.ic_intensity.compress(context.last_intensity[cpr << 1 | (gps_time_change ? 1 : 0)],
      intensity, cpr);
    context.last_intensity[cpr << 1 | (gps_time_change ? 1 : 0)] = intensity;

    if (scan_angle_change) {
      this.changed_scan_angle = true;
      context.ic_scan_angle.compress(lastScanAngle, scanAngle, gps_time_change ? 1 : 0);
    }

    final short lastUserData = lastPoint.getUserData();
    final short userData = item.getUserData();
    if (userData != lastUserData) {
      this.changed_user_data = true;
    }
    this.enc_user_data.encodeSymbol(context.m_user_data, lastUserData / 4, 256, userData);

    if (point_source_change) {
      this.changed_point_source = true;
      context.ic_point_source_ID.compress(lasPointSourceID, pointSourceID);
    }

    if (gps_time_change) {
      this.changed_gps_time = true;
      final long gpsTime = item.getGpsTimeLong();
      write_gps_time(gpsTime);
    }

    context.last_item = item;
    context.gps_time_change = gps_time_change;

    return this.current_context;

  }

  private void write_gps_time(final long gps_time) {
    final LAScontextPOINT14 context = this.contexts[this.current_context];
    if (context.last_gpstime_diff[context.last] == 0) {
      // if the last integer difference was zero
      // calculate the difference between the two doubles as an integer
      final long curr_gpstime_diff_64 = gps_time - context.last_gpstime[context.last];
      final int curr_gpstime_diff = (int)curr_gpstime_diff_64;
      if (curr_gpstime_diff_64 == curr_gpstime_diff) {
        this.enc_gps_time.encodeSymbol(context.m_gpstime_0diff, 0); // the
                                                                    // difference
                                                                    // can
                                                                    // be
                                                                    // represented
                                                                    // with
                                                                    // 32
                                                                    // bits
        context.ic_gpstime.compress(0, curr_gpstime_diff, 0);
        context.last_gpstime_diff[context.last] = curr_gpstime_diff;
        context.multi_extreme_counter[context.last] = 0;
      } else {
        // the difference is huge
        // maybe the double belongs to another time sequence
        for (int i = 1; i < 4; i++) {
          final long other_gpstime_diff_64 = gps_time - context.last_gpstime[context.last + i & 3];
          final int other_gpstime_diff = (int)other_gpstime_diff_64;
          if (other_gpstime_diff_64 == other_gpstime_diff) {
            this.enc_gps_time.encodeSymbol(context.m_gpstime_0diff, i + 1); // it
                                                                            // belongs
                                                                            // to
                                                                            // another
                                                                            // sequence
            context.last = context.last + i & 3;
            write_gps_time(gps_time);
            return;
          }
        }
        // no other sequence found. start new sequence.
        this.enc_gps_time.encodeSymbol(context.m_gpstime_0diff, 1);
        context.ic_gpstime.compress((int)(context.last_gpstime[context.last] >>> 32),
          (int)(gps_time >>> 32), 8);
        this.enc_gps_time.writeInt((int)gps_time);
        context.next = context.next + 1 & 3;
        context.last = context.next;
        context.last_gpstime_diff[context.last] = 0;
        context.multi_extreme_counter[context.last] = 0;
      }
      context.last_gpstime[context.last] = gps_time;
    } else {
      // the last integer difference was *not* zero
      // calculate the difference between the two doubles as an integer
      final long curr_gpstime_diff_64 = gps_time - context.last_gpstime[context.last];
      final int curr_gpstime_diff = (int)curr_gpstime_diff_64;

      // if the current gpstime difference can be represented with 32 bits
      if (curr_gpstime_diff_64 == curr_gpstime_diff) {
        // compute multiplier between current and last integer difference
        final float multi_f = (float)curr_gpstime_diff
          / (float)context.last_gpstime_diff[context.last];
        final int multi = I32_QUANTIZE(multi_f);

        // compress the residual curr_gpstime_diff in dependance on the
        // multiplier
        if (multi == 1) {
          // this is the case we assume we get most often for regular spaced
          // pulses
          this.enc_gps_time.encodeSymbol(context.m_gpstime_multi, 1);
          context.ic_gpstime.compress(context.last_gpstime_diff[context.last], curr_gpstime_diff,
            1);
          context.multi_extreme_counter[context.last] = 0;
        } else if (multi > 0) {
          if (multi < LASZIP_GPSTIME_MULTI) // positive multipliers up to
                                            // LASZIP_GPSTIME_MULTI are
                                            // compressed directly
          {
            this.enc_gps_time.encodeSymbol(context.m_gpstime_multi, multi);
            if (multi < 10) {
              context.ic_gpstime.compress(multi * context.last_gpstime_diff[context.last],
                curr_gpstime_diff, 2);
            } else {
              context.ic_gpstime.compress(multi * context.last_gpstime_diff[context.last],
                curr_gpstime_diff, 3);
            }
          } else {
            this.enc_gps_time.encodeSymbol(context.m_gpstime_multi, LASZIP_GPSTIME_MULTI);
            context.ic_gpstime.compress(
              LASZIP_GPSTIME_MULTI * context.last_gpstime_diff[context.last], curr_gpstime_diff, 4);
            context.multi_extreme_counter[context.last]++;
            if (context.multi_extreme_counter[context.last] > 3) {
              context.last_gpstime_diff[context.last] = curr_gpstime_diff;
              context.multi_extreme_counter[context.last] = 0;
            }
          }
        } else if (multi < 0) {
          if (multi > LASZIP_GPSTIME_MULTI_MINUS) {
            // negative multipliers larger
            // than
            // LASZIP_GPSTIME_MULTI_MINUS
            // are compressed directly
            this.enc_gps_time.encodeSymbol(context.m_gpstime_multi, LASZIP_GPSTIME_MULTI - multi);
            context.ic_gpstime.compress(multi * context.last_gpstime_diff[context.last],
              curr_gpstime_diff, 5);
          } else {
            this.enc_gps_time.encodeSymbol(context.m_gpstime_multi,
              LASZIP_GPSTIME_MULTI - LASZIP_GPSTIME_MULTI_MINUS);
            context.ic_gpstime.compress(
              LASZIP_GPSTIME_MULTI_MINUS * context.last_gpstime_diff[context.last],
              curr_gpstime_diff, 6);
            context.multi_extreme_counter[context.last]++;
            if (context.multi_extreme_counter[context.last] > 3) {
              context.last_gpstime_diff[context.last] = curr_gpstime_diff;
              context.multi_extreme_counter[context.last] = 0;
            }
          }
        } else {
          this.enc_gps_time.encodeSymbol(context.m_gpstime_multi, 0);
          context.ic_gpstime.compress(0, curr_gpstime_diff, 7);
          context.multi_extreme_counter[context.last]++;
          if (context.multi_extreme_counter[context.last] > 3) {
            context.last_gpstime_diff[context.last] = curr_gpstime_diff;
            context.multi_extreme_counter[context.last] = 0;
          }
        }
      } else {
        // the difference is huge
        // maybe the double belongs to another time sequence
        for (int i = 1; i < 4; i++) {
          final long other_gpstime_diff_64 = gps_time - context.last_gpstime[context.last + i & 3];
          final int other_gpstime_diff = (int)other_gpstime_diff_64;
          if (other_gpstime_diff_64 == other_gpstime_diff) {
            // it belongs to this sequence
            this.enc_gps_time.encodeSymbol(context.m_gpstime_multi,
              LASZIP_GPSTIME_MULTI_CODE_FULL + i);
            context.last = context.last + i & 3;
            write_gps_time(gps_time);
            return;
          }
        }
        // no other sequence found. start new sequence.
        this.enc_gps_time.encodeSymbol(context.m_gpstime_multi, LASZIP_GPSTIME_MULTI_CODE_FULL);
        context.ic_gpstime.compress((int)(context.last_gpstime[context.last] >>> 32),
          (int)(gps_time >>> 32), 8);
        this.enc_gps_time.writeInt((int)gps_time);
        context.next = context.next + 1 & 3;
        context.last = context.next;
        context.last_gpstime_diff[context.last] = 0;
        context.multi_extreme_counter[context.last] = 0;
      }
      context.last_gpstime[context.last] = gps_time;
    }
  }

  @Override
  public void writeChunkBytes() {
    final ChannelWriter writer = this.enc.getWriter();

    this.enc_channel_returns_XY.writeBytes(writer);

    this.enc_Z.writeBytes(writer);

    this.enc_classification.writeBytes(writer, this.changed_classification);

    this.enc_flags.writeBytes(writer, this.changed_flags);

    this.enc_intensity.writeBytes(writer, this.changed_intensity);

    this.enc_scan_angle.writeBytes(writer, this.changed_scan_angle);

    this.enc_user_data.writeBytes(writer, this.changed_user_data);

    this.enc_point_source.writeBytes(writer, this.changed_point_source);

    this.enc_gps_time.writeBytes(writer, this.changed_gps_time);
  }

  @Override
  public void writeChunkSizes() {
    final ChannelWriter writer = this.enc.getWriter();

    this.enc_channel_returns_XY.writeNumBytes(writer, true);

    this.enc_Z.writeNumBytes(writer, true);

    this.enc_classification.writeNumBytes(writer, this.changed_classification);

    this.enc_flags.writeNumBytes(writer, this.changed_flags);

    this.enc_intensity.writeNumBytes(writer, this.changed_intensity);

    this.enc_scan_angle.writeNumBytes(writer, this.changed_scan_angle);

    this.enc_user_data.writeNumBytes(writer, this.changed_user_data);

    this.enc_point_source.writeNumBytes(writer, this.changed_point_source);

    this.enc_gps_time.writeNumBytes(writer, this.changed_gps_time);
  }

}
