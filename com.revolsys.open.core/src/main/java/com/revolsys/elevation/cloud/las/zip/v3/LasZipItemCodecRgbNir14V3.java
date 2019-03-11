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
import com.revolsys.elevation.cloud.las.zip.context.LasZipContextRgbNir;
import com.revolsys.io.channels.ChannelReader;
import com.revolsys.io.channels.ChannelWriter;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingCodec;
import com.revolsys.math.arithmeticcoding.ArithmeticDecoder;
import com.revolsys.math.arithmeticcoding.ArithmeticEncoder;

public class LasZipItemCodecRgbNir14V3 implements LasZipItemCodec {

  private ArithmeticDecoder decoder;

  private ArithmeticEncoder encoder;

  private final ArithmeticEncoderByteArray rgbEncoder = new ArithmeticEncoderByteArray();

  private final ArithmeticDecoderByteArray rgbDecoder = new ArithmeticDecoderByteArray();

  private final ArithmeticEncoderByteArray nirEncoder = new ArithmeticEncoderByteArray();

  private final ArithmeticDecoderByteArray nirDecoder = new ArithmeticDecoderByteArray();

  private boolean rgbChanged;

  private boolean nirChanged;

  private final LasZipContextRgbNir[] contexts = new LasZipContextRgbNir[4];

  private int currentContextIndex;

  public LasZipItemCodecRgbNir14V3(final ArithmeticCodingCodec codec) {
    if (codec instanceof ArithmeticDecoder) {
      this.decoder = (ArithmeticDecoder)codec;
    } else if (codec instanceof ArithmeticEncoder) {
      this.encoder = (ArithmeticEncoder)codec;
    } else {
      throw new IllegalArgumentException("Not supported:" + codec.getClass());
    }

    for (int i = 0; i < this.contexts.length; i++) {
      this.contexts[i] = new LasZipContextRgbNir();
    }
    final int decompressSelective = LasZipDecompressSelective.LASZIP_DECOMPRESS_SELECTIVE_ALL;
    this.rgbDecoder.setEnabled(decompressSelective,
      LasZipDecompressSelective.LASZIP_DECOMPRESS_SELECTIVE_RGB);
    this.nirDecoder.setEnabled(decompressSelective,
      LasZipDecompressSelective.LASZIP_DECOMPRESS_SELECTIVE_NIR);
  }

  @Override
  public int getVersion() {
    return 3;
  }

  @Override
  public int init(final LasPoint point, final int contextIndex) {
    this.currentContextIndex = contextIndex;
    if (this.encoder != null) {
      return writeInit(point);
    }

    if (this.decoder != null) {
      return readInit(point);
    }
    return this.currentContextIndex;
  }

  private void initContext(final int contextIndex, final LasPoint point,
    final ArithmeticCodingCodec codec) {
    final LasZipContextRgbNir context = this.contexts[contextIndex];
    context.initPoint(codec, point);
  }

  @Override
  public int read(final LasPoint point, final int contextIndex) {
    LasZipContextRgbNir context = this.contexts[this.currentContextIndex];
    if (this.currentContextIndex != contextIndex) {
      this.currentContextIndex = contextIndex;
      if (context.unused) {
        readInitContext(this.currentContextIndex, context.lastPoint);
        context = this.contexts[this.currentContextIndex];
      }
    }
    if (this.rgbChanged) {
      context.readRgb(this.rgbDecoder, point);
    }
    if (this.nirChanged) {
      context.readNir(this.nirDecoder, point);
    }
    return contextIndex;
  }

  @Override
  public void readChunkSizes() {
    final ChannelReader in = this.decoder.getIn();
    this.rgbDecoder.readSize(in);
    this.nirDecoder.readSize(in);
  }

  public int readInit(final LasPoint point) {
    final ChannelReader in = this.decoder.getIn();

    this.rgbChanged = this.rgbDecoder.readBytes(in);
    this.nirChanged = this.nirDecoder.readBytes(in);

    for (final LasZipContextRgbNir context : this.contexts) {
      context.unused = true;
    }

    readInitContext(this.currentContextIndex, point);

    return this.currentContextIndex;
  }

  private void readInitContext(final int contextIndex, final LasPoint point) {
    initContext(contextIndex, point, this.decoder);
  }

  @Override
  public int write(final LasPoint point, final int contextIndex) {
    LasZipContextRgbNir context = this.contexts[this.currentContextIndex];
    if (this.currentContextIndex != contextIndex) {
      this.currentContextIndex = contextIndex;
      if (context.unused) {
        writeInitContext(this.currentContextIndex, context.lastPoint);
        context = this.contexts[this.currentContextIndex];
      }
    }

    if (context.writeRgb(this.rgbEncoder, point)) {
      this.rgbChanged = true;
    }
    if (context.writeNir(this.nirEncoder, point)) {
      this.nirChanged = true;
    }

    return contextIndex;

  }

  @Override
  public void writeChunkBytes() {
    final ChannelWriter writer = this.encoder.getWriter();
    this.rgbEncoder.writeBytes(writer);
    this.nirEncoder.writeBytes(writer);
  }

  @Override
  public void writeChunkSizes() {
    final ChannelWriter writer = this.encoder.getWriter();
    this.rgbEncoder.writeSize(writer, this.rgbChanged);
    this.nirEncoder.writeSize(writer, this.nirChanged);
  }

  public int writeInit(final LasPoint point) {
    this.rgbEncoder.init();
    this.nirEncoder.init();
    this.rgbChanged = false;
    this.nirChanged = false;
    for (final LasZipContextRgbNir context : this.contexts) {
      context.unused = true;
    }
    writeInitContext(this.currentContextIndex, point);
    return this.currentContextIndex;
  }

  private void writeInitContext(final int contextIndex, final LasPoint point) {
    initContext(contextIndex, point, this.encoder);
  }
}
