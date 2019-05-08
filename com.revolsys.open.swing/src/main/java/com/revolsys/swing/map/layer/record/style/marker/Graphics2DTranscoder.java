package com.revolsys.swing.map.layer.record.style.marker;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;

import org.apache.batik.ext.awt.RenderingHintsKeyExt;
import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderOutput;
import org.jeometry.common.logging.Logs;
import org.w3c.dom.Document;

public class Graphics2DTranscoder extends SVGAbstractTranscoder {
  private final Graphics2D graphics;

  public Graphics2DTranscoder(final Graphics2D graphics) {
    this.graphics = graphics;
  }

  @Override
  protected void transcode(final Document document, final String uri, final TranscoderOutput output)
    throws TranscoderException {
    super.transcode(document, uri, output);
    this.graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
      RenderingHints.VALUE_ANTIALIAS_ON);
    this.graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
      RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    this.graphics.setRenderingHint(RenderingHintsKeyExt.KEY_TRANSCODING,
      RenderingHintsKeyExt.VALUE_TRANSCODING_PRINTING);

    final AffineTransform t = this.graphics.getTransform();
    final Shape clip = this.graphics.getClip();

    try {
      if (this.curTxf != null) {
        this.graphics.transform(this.curTxf);
      }
      this.root.paint(this.graphics);
    } catch (final Exception e) {
      Logs.error(this, e);
    } finally {
      this.graphics.setTransform(t);
      this.graphics.setClip(clip);
    }
  }
}
