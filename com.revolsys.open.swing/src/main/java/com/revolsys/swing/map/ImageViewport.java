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
    this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    this.graphics = (Graphics2D)this.image.getGraphics();
  }

  public Graphics2D getGraphics() {
    return this.graphics;
  }

  public BufferedImage getImage() {
    return this.image;
  }
}
