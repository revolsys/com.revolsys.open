package com.revolsys.swing.map;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.swing.map.layer.Project;

public class ImageViewport extends Viewport2D {

  private BufferedImage image;

  private Graphics2D graphics;

  public ImageViewport(Project project, int width, int height,
    BoundingBox boundingBox) {
    super(project, width, height, boundingBox);
    image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    graphics = (Graphics2D)image.getGraphics();
  }

  public Graphics2D getGraphics() {
    return graphics;
  }

  public Image getImage() {
    return image;
  }
}
