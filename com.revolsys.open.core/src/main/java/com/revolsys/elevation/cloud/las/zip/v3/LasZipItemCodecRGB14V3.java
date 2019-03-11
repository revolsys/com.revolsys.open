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

import com.revolsys.elevation.cloud.las.pointformat.LasPoint;
import com.revolsys.elevation.cloud.las.zip.ArithmeticDecoderByteArray;
import com.revolsys.elevation.cloud.las.zip.ArithmeticEncoderByteArray;
import com.revolsys.elevation.cloud.las.zip.LasZipItemCodec;
import com.revolsys.io.channels.ChannelReader;
import com.revolsys.io.channels.ChannelWriter;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingCodec;
import com.revolsys.math.arithmeticcoding.ArithmeticDecoder;
import com.revolsys.math.arithmeticcoding.ArithmeticEncoder;

public class LasZipItemCodecRGB14V3 implements LasZipItemCodec {

  private ArithmeticDecoder dec;

  private ArithmeticEncoder enc;

  private final ArithmeticEncoderByteArray enc_RGB = new ArithmeticEncoderByteArray();

  private final ArithmeticDecoderByteArray dec_RGB = new ArithmeticDecoderByteArray();

  private boolean changed_RGB;

  private final LAScontextRGB14[] contexts = new LAScontextRGB14[4];

  private int current_context;

  public LasZipItemCodecRGB14V3(final ArithmeticCodingCodec codec) {
    if (codec instanceof ArithmeticDecoder) {
      this.dec = (ArithmeticDecoder)codec;
    } else if (codec instanceof ArithmeticEncoder) {
      this.enc = (ArithmeticEncoder)codec;
    } else {
      throw new IllegalArgumentException("Not supported:" + codec.getClass());
    }

  }

  @Override
  public int getVersion() {
    return 2;
  }

  @Override
  public int init(final LasPoint point, final int contextIndex) {
    if (this.enc != null) {
      return writeInit(point);
    }

    if (this.dec != null) {
      return readInit(point);
    }
    return this.current_context;
  }

  private void initContext(final int contextIndex, final LasPoint point,
    final ArithmeticCodingCodec codecRGB) {
    final LAScontextRGB14 context = this.contexts[contextIndex];
    if (context.m_byte_used == null) {
      context.m_byte_used = codecRGB.createSymbolModel(128);
      context.m_rgb_diff_0 = codecRGB.createSymbolModel(256);
      context.m_rgb_diff_1 = codecRGB.createSymbolModel(256);
      context.m_rgb_diff_2 = codecRGB.createSymbolModel(256);
      context.m_rgb_diff_3 = codecRGB.createSymbolModel(256);
      context.m_rgb_diff_4 = codecRGB.createSymbolModel(256);
      context.m_rgb_diff_5 = codecRGB.createSymbolModel(256);
    }

    codecRGB.initModel(context.m_byte_used);
    codecRGB.initModel(context.m_rgb_diff_0);
    codecRGB.initModel(context.m_rgb_diff_1);
    codecRGB.initModel(context.m_rgb_diff_2);
    codecRGB.initModel(context.m_rgb_diff_3);
    codecRGB.initModel(context.m_rgb_diff_4);
    codecRGB.initModel(context.m_rgb_diff_5);

    context.last_item = point;
    context.unused = false;
  }

  @Override
  public int read(final LasPoint point, final int contextIndex) {

    return this.current_context;
  }

  @Override
  public void readChunkSizes() {
    final ChannelReader in = this.dec.getIn();
    this.dec_RGB.readSize(in);
  }

  public int readInit(final LasPoint point) {
    final ChannelReader in = this.dec.getIn();

    return this.current_context;
  }

  private void readInitContext(final int contextIndex, final LasPoint point) {
    initContext(contextIndex, point, this.dec);
  }

  @Override
  public int write(final LasPoint point, final int contextIndex) {
    return this.current_context;

  }

  @Override
  public void writeChunkBytes() {
    final ChannelWriter writer = this.enc.getWriter();
    this.enc_RGB.writeBytes(writer);
  }

  @Override
  public void writeChunkSizes() {
    final ChannelWriter writer = this.enc.getWriter();
    this.enc_RGB.writeSize(writer, this.changed_RGB);
  }

  public int writeInit(final LasPoint point) {
    return this.current_context;
  }

  private void writeInitContext(final int contextIndex, final LasPoint point) {
    initContext(contextIndex, point, this.enc);
  }
}
