package com.revolsys.swing.map;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleXY;
import com.revolsys.raster.BufferedGeoreferencedImage;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.view.graphics.Graphics2DViewport;

public class ImageViewport extends Graphics2DViewport {
  private BufferedImage image;

  public ImageViewport(final int width, final int height) {
    this(null, width, height, new BoundingBoxDoubleXY(0, 0, width, height));
  }

  public ImageViewport(final Project project, final int width, final int height,
    final BoundingBox boundingBox) {
    this(project, width, height, boundingBox, BufferedImage.TYPE_INT_ARGB_PRE);
  }

  public ImageViewport(final Project project, final int width, final int height,
    final BoundingBox boundingBox, final int imageType) {
    super(project, width, height, boundingBox);
    newImage(width, height, imageType);
  }

  public ImageViewport(final Viewport2D parentViewport) {
    this(parentViewport, BufferedImage.TYPE_INT_ARGB_PRE);
  }

  public ImageViewport(final Viewport2D parentViewport, final int imageType) {
    super(parentViewport);
    final int viewWidthPixels = this.cacheBoundingBox.getViewWidthPixels();
    final int viewHeightPixels = this.cacheBoundingBox.getViewHeightPixels();
    newImage(viewWidthPixels, viewHeightPixels, BufferedImage.TYPE_INT_ARGB_PRE);
  }

  public BufferedGeoreferencedImage getGeoreferencedImage() {
    final BufferedImage image = this.image;
    final BoundingBox boundingBox = getBoundingBox();
    if (image == null) {
      return null;
    } else {
      return new BufferedGeoreferencedImage(boundingBox, image);
    }
  }

  public BufferedImage getImage() {
    return this.image;
  }

  private void newImage(final int viewWidthPixels, final int viewHeightPixels,
    final int imageType) {
    if (viewWidthPixels > 0 && viewHeightPixels > 0) {
      this.image = new BufferedImage(viewWidthPixels, viewHeightPixels, imageType);
      final Graphics2D graphics = (Graphics2D)this.image.getGraphics();
      setGraphics(graphics);
    }
  }
}
