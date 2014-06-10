package com.revolsys.swing.map;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.Project;

public class ImageViewport extends Viewport2D {

  private final BufferedImage image;

  private final Graphics2D graphics;

  private final Viewport2D parentViewport;

  public ImageViewport(final Viewport2D parentViewport, final Project project,
    final int width, final int height, final BoundingBox boundingBox) {
    super(project, width, height, boundingBox);
    this.parentViewport = parentViewport;
    this.image = new BufferedImage(width, height,
      BufferedImage.TYPE_INT_ARGB_PRE);
    this.graphics = (Graphics2D)this.image.getGraphics();
  }

  @Override
  public void clearLayerCache(final Layer layer) {
    parentViewport.clearLayerCache(layer);
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

  @Override
  public <V> V getLayerCacheValue(final Layer layer, final String name) {
    return parentViewport.getLayerCacheValue(layer, name);
  }

  @Override
  public void setLayerCacheValue(final Layer layer, final String name,
    final Object value) {
    parentViewport.setLayerCacheValue(layer, name, value);
  }

}
