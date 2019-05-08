package com.revolsys.swing.map.layer.record.style.marker;

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.revolsys.io.FileUtil;
import com.revolsys.spring.resource.Resource;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.swing.map.view.ViewRenderer;

public class ImageMarker extends AbstractMarker {

  private Image image;

  public ImageMarker(final Image image) {
    this.image = image;
  }

  public ImageMarker(final Resource resource) {
    final InputStream in = resource.getInputStream();
    try {
      this.image = ImageIO.read(in);
    } catch (final IOException e) {
      throw new IllegalArgumentException("Unable to read file: " + resource);
    } finally {
      FileUtil.closeSilent(in);
    }
  }

  public Image getImage() {
    return this.image;
  }

  @Override
  public String getTypeName() {
    return "markerImage";
  }

  @Override
  public Icon newIcon(final MarkerStyle style) {
    final Image newImage = this.image.getScaledInstance(16, 16, Image.SCALE_SMOOTH);
    return new ImageIcon(newImage);
  }

  @Override
  public MarkerRenderer newMarkerRenderer(final ViewRenderer view, final MarkerStyle style) {
    return view.newMarkerRendererImage(this, style);
  }

}
