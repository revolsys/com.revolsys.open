package com.revolsys.swing.map;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.raster.BufferedGeoreferencedImage;
import com.revolsys.swing.map.layer.Project;

public class ImageViewport extends GraphicsViewport2D {
  private final BufferedImage image;

  public ImageViewport(final Project project, final int width, final int height,
    final BoundingBox boundingBox) {
    this(project, width, height, boundingBox, BufferedImage.TYPE_INT_ARGB_PRE);
  }

  public ImageViewport(final Project project, final int width, final int height,
    final BoundingBox boundingBox, final int imageType) {
    super(project, width, height, boundingBox);
    this.image = new BufferedImage(width, height, imageType);
    setGraphics((Graphics2D)this.image.getGraphics());
  }

  public ImageViewport(final Viewport2D parentViewport) {
    this(parentViewport.getProject(), parentViewport.getViewWidthPixels(),
      parentViewport.getViewHeightPixels(), parentViewport.getBoundingBox());
  }

  public ImageViewport(final Viewport2D parentViewport, final int imageType) {
    this(parentViewport.getProject(), parentViewport.getViewWidthPixels(),
      parentViewport.getViewHeightPixels(), parentViewport.getBoundingBox(), imageType);
  }

  public BufferedGeoreferencedImage getGeoreferencedImage() {
    final BoundingBox boundingBox = getBoundingBox();
    return new BufferedGeoreferencedImage(boundingBox, this.image);
  }

  public BufferedImage getImage() {
    return this.image;
  }
}
