package com.revolsys.swing.map.layer.record.style.marker;

import java.awt.Image;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.revolsys.raster.BufferedImages;
import com.revolsys.spring.resource.Resource;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.swing.map.view.ViewRenderer;

public class ImageMarker extends AbstractMarker {

  private final Image image;

  public ImageMarker(final Image image) {
    this.image = image;
  }

  public ImageMarker(final Resource resource) {
    this.image = BufferedImages.readBufferedImage(resource);
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
