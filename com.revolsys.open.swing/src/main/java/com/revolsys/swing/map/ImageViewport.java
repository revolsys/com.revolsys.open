package com.revolsys.swing.map;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.swing.map.layer.LayerGroup;

public class ImageViewport extends Viewport2D {

  private final BufferedImage image;

  private final Graphics2D graphics;

  public ImageViewport(final LayerGroup project, final int width,
    final int height, final BoundingBox boundingBox) {
    super(project, width, height, boundingBox);
    image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    graphics = (Graphics2D)image.getGraphics();
  }

  public Graphics2D getGraphics() {
    return graphics;
  }

  public BufferedImage getImage() {
    return image;
  }
}
