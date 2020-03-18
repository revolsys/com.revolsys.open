package com.revolsys.swing.map.layer.record.style.marker;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.geom.Dimension2D;
import java.awt.image.BufferedImage;

import javax.swing.Icon;

import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.w3c.dom.Document;

/**
 * A Swing Icon that draws an SVG image.
 *
 * @author <a href="mailto:cam%40mcc%2eid%2eau">Cameron McCormack</a>
 */
public class SvgIcon extends UserAgentAdapter implements Icon {
  /**
   * The BufferedImage generated from the SVG document.
   */
  protected BufferedImage image;

  /**
   * The width of the rendered image.
   */
  protected int width;

  /**
   * The height of the rendered image.
   */
  protected int height;

  /**
   * Create a new SvgIcon object.
   * @param doc The SVG document.
   */
  public SvgIcon(final Document doc) throws TranscoderException {
    this(doc, 0, 0);
  }

  /**
   * Create a new SvgIcon object.
   * @param doc The SVG document.
   * @param w The width of the icon.
   * @param h The height of the icon.
   */
  public SvgIcon(final Document doc, final int w, final int h) throws TranscoderException {
    generateBufferedImage(new TranscoderInput(doc), w, h);
  }

  /**
   * Create a new SvgIcon object.
   * @param uri The URI to read the SVG document from.
   */
  public SvgIcon(final String uri) throws TranscoderException {
    this(uri, 0, 0);
  }

  /**
   * Create a new SvgIcon object.
   * @param uri The URI to read the SVG document from.
   * @param w The width of the icon.
   * @param h The height of the icon.
   */
  public SvgIcon(final String uri, final int w, final int h) throws TranscoderException {
    generateBufferedImage(new TranscoderInput(uri), w, h);
  }

  /**
   * Generate the BufferedImage.
   */
  protected void generateBufferedImage(final TranscoderInput input, final int width,
    final int height) throws TranscoderException {
    final SvgBufferedImageTranscoder transcoder = new SvgBufferedImageTranscoder(width, height);
    transcoder.transcode(input, null);
    this.image = transcoder.getImage();
    this.width = this.image.getWidth();
    this.height = this.image.getHeight();
  }

  /**
   * Returns the icon's height.
   */
  @Override
  public int getIconHeight() {
    return this.height;
  }

  /**
   * Returns the icon's width.
   */
  @Override
  public int getIconWidth() {
    return this.width;
  }

  public BufferedImage getImage() {
    return this.image;
  }

  /**
   * Returns the default size of this user agent.
   */
  @Override
  public Dimension2D getViewportSize() {
    return new Dimension(this.width, this.height);
  }

  /**
   * Draw the icon at the specified location.
   */
  @Override
  public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
    g.drawImage(this.image, x, y, null);
  }
}
