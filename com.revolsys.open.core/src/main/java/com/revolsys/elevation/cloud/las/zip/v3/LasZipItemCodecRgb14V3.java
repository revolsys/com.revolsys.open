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
import com.revolsys.elevation.cloud.las.zip.LasZipDecompressSelective;
import com.revolsys.elevation.cloud.las.zip.LasZipItemCodec;
import com.revolsys.elevation.cloud.las.zip.context.LasZipContextRgb;
import com.revolsys.io.channels.ChannelReader;
import com.revolsys.io.channels.ChannelWriter;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingCodec;
import com.revolsys.math.arithmeticcoding.ArithmeticDecoder;
import com.revolsys.math.arithmeticcoding.ArithmeticEncoder;

public class LasZipItemCodecRgb14V3 implements LasZipItemCodec {

  private ArithmeticDecoder dec;

  private ArithmeticEncoder enc;

  private final ArithmeticEncoderByteArray enc_RGB = new ArithmeticEncoderByteArray();

  private final ArithmeticDecoderByteArray dec_RGB = new ArithmeticDecoderByteArray();

  private boolean changed_RGB;

  private final LasZipContextRgb[] contexts = new LasZipContextRgb[4];

  private int currentContextIndex;

  public LasZipItemCodecRgb14V3(final ArithmeticCodingCodec codec) {
    if (codec instanceof ArithmeticDecoder) {
      this.dec = (ArithmeticDecoder)codec;
    } else if (codec instanceof ArithmeticEncoder) {
      this.enc = (ArithmeticEncoder)codec;
    } else {
      throw new IllegalArgumentException("Not supported:" + codec.getClass());
    }

    for (int i = 0; i < this.contexts.length; i++) {
      this.contexts[i] = new LasZipContextRgb();
    }
    final int decompressSelective = LasZipDecompressSelective.LASZIP_DECOMPRESS_SELECTIVE_ALL;
    this.dec_RGB.setEnabled(decompressSelective,
      LasZipDecompressSelective.LASZIP_DECOMPRESS_SELECTIVE_RGB);
  }

  @Override
  public int getVersion() {
    return 3;
  }

  @Override
  public int init(final LasPoint point, final int contextIndex) {
    this.currentContextIndex = contextIndex;
    if (this.enc != null) {
      return writeInit(point);
    }

    if (this.dec != null) {
      return readInit(point);
    }
    return this.currentContextIndex;
  }

  private void initContext(final int contextIndex, final LasPoint point,
    final ArithmeticCodingCodec codecRGB) {
    final LasZipContextRgb context = this.contexts[contextIndex];
    context.initPoint(codecRGB, point);
  }

  @Override
  public int read(final LasPoint point, final int contextIndex) {
    if (this.changed_RGB) {
      LasZipContextRgb context = this.contexts[this.currentContextIndex];
      if (this.currentContextIndex != contextIndex) {
        this.currentContextIndex = contextIndex;
        if (context.unused) {
          readInitContext(this.currentContextIndex, context.lastPoint);
          context = this.contexts[this.currentContextIndex];
        }
      }
      context.readRgb(this.dec_RGB, point);
    }
    return contextIndex;
  }

  @Override
  public void readChunkSizes() {
    final ChannelReader in = this.dec.getIn();
    this.dec_RGB.readSize(in);
  }

  public int readInit(final LasPoint point) {
    final ChannelReader in = this.dec.getIn();

    this.changed_RGB = this.dec_RGB.readBytes(in);

    for (final LasZipContextRgb context : this.contexts) {
      context.unused = true;
    }

    readInitContext(this.currentContextIndex, point);

    return this.currentContextIndex;
  }

  private void readInitContext(final int contextIndex, final LasPoint point) {
    initContext(contextIndex, point, this.dec);
  }

  @Override
  public int write(final LasPoint point, final int contextIndex) {
    LasZipContextRgb context = this.contexts[this.currentContextIndex];
    if (this.currentContextIndex != contextIndex) {
      this.currentContextIndex = contextIndex;
      if (context.unused) {
        writeInitContext(this.currentContextIndex, context.lastPoint);
        context = this.contexts[this.currentContextIndex];
      }
    }

    if (context.writeRgb(this.enc_RGB, point)) {
      this.changed_RGB = true;
    }

    return contextIndex;

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
    this.enc_RGB.init();
    this.changed_RGB = false;
    for (final LasZipContextRgb context : this.contexts) {
      context.unused = true;
    }
    writeInitContext(this.currentContextIndex, point);
    return this.currentContextIndex;
  }

  private void writeInitContext(final int contextIndex, final LasPoint point) {
    initContext(contextIndex, point, this.enc);
  }
}
