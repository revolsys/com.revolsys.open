package com.revolsys.swing.map;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.swing.map.layer.Project;

public class ImageViewport extends Viewport2D {

  private final BufferedImage image;

  private final Graphics2D graphics;

  public ImageViewport(final Project project, final int width,
    final int height, final BoundingBox boundingBox) {
    super(project, width, height, boundingBox);
    this.image = new BufferedImage(width, height,
      BufferedImage.TYPE_INT_ARGB_PRE);
    this.graphics = (Graphics2D)this.image.getGraphics();
  }

  public ImageViewport(final Viewport2D parentViewport) {
    this(parentViewport.getProject(), parentViewport.getViewWidthPixels(),
      parentViewport.getViewHeightPixels(), parentViewport.getBoundingBox());
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    if (graphics != null) {
      graphics.dispose();
    }
  }

  public Graphics2D getGraphics() {
    return this.graphics;
  }

  public BufferedImage getImage() {
    return this.image;
  }

}
