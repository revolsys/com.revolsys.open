package com.revolsys.swing.map.layer.record.style.marker;

import java.awt.image.BufferedImage;

import org.apache.batik.gvt.renderer.ConcreteImageRendererFactory;
import org.apache.batik.gvt.renderer.ImageRenderer;
import org.apache.batik.gvt.renderer.ImageRendererFactory;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.jeometry.common.exception.Exceptions;
import org.w3c.dom.Document;

public class SvgBufferedImageTranscoder extends ImageTranscoder {

  private static final ImageRendererFactory RENDERER_FACTORY = new ConcreteImageRendererFactory();

  public static BufferedImage newImage(final Document document, final String uri, final int width,
    final int height) {
    final SvgBufferedImageTranscoder transcoder = new SvgBufferedImageTranscoder(width, height);
    return transcoder.newImage(document, uri);
  }

  private BufferedImage image;

  public SvgBufferedImageTranscoder(final int width, final int height) {
    if (width > 0 && height > 0) {
      super.setImageSize(width, height);
    }
  }

  @Override
  public BufferedImage createImage(final int width, final int height) {
    return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
  }

  @Override
  protected ImageRenderer createRenderer() {
    return RENDERER_FACTORY.createStaticImageRenderer();
  }

  public BufferedImage getImage() {
    return this.image;
  }

  public BufferedImage newImage(final Document document, final String uri) {
    try {
      final TranscoderInput transcoderInput = new TranscoderInput(document);
      transcoderInput.setURI(uri);
      transcode(transcoderInput, null);
      return this.image;
    } catch (final TranscoderException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public void writeImage(final BufferedImage image, final TranscoderOutput output)
    throws TranscoderException {
    this.image = image;
  }
}
