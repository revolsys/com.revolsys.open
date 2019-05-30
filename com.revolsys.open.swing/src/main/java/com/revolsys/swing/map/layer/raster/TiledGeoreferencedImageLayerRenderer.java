package com.revolsys.swing.map.layer.raster;

import java.awt.Graphics2D;
import java.beans.PropertyChangeListener;
import java.util.List;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.swing.map.ImageViewport;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.tile.AbstractTiledLayer;
import com.revolsys.swing.map.layer.tile.AbstractTiledLayerRenderer;
import com.revolsys.util.Cancellable;

public class TiledGeoreferencedImageLayerRenderer<T extends GeoreferencedImageMapTile>
  extends AbstractTiledLayerRenderer<GeoreferencedImage, T> implements PropertyChangeListener {

  public TiledGeoreferencedImageLayerRenderer(final AbstractTiledImageLayer<T> layer) {
    super("tiledImage", layer);
  }

  public TiledGeoreferencedImageLayerRenderer(final String type, final String name) {
    super(type, name);
  }

  @Override
  protected void renderTile(final Viewport2D viewport, final Cancellable cancellable,
    final T tile) {
    final GeoreferencedImage image = tile.getData();
    final Graphics2D graphics = viewport.getGraphics();
    if (graphics != null) {
      viewport.drawImage(image, false);
    }
  }

  @Override
  protected void renderTiles(final Viewport2D viewport, final Cancellable cancellable,
    final List<T> mapTiles) {
    final AbstractTiledLayer<GeoreferencedImage, T> layer = getLayer();
    if (layer.isProjectionRequired(viewport)) {
      final GeometryFactory geometryFactory = layer.getGeometryFactory();
      final BoundingBox boundingBox = geometryFactory.bboxEditor()
        .addAllBbox(mapTiles)
        .getBoundingBox();
      final double resolution = getLayerResolution();
      final int width = (int)Math.round(boundingBox.getWidth() / resolution);
      final int height = (int)Math.round(boundingBox.getHeight() / resolution);
      try (
        final ImageViewport imageViewport = new ImageViewport(layer.getProject(), width, height,
          boundingBox)) {
        super.renderTiles(imageViewport, cancellable, mapTiles);
        final GeoreferencedImage mergedImage = imageViewport.getGeoreferencedImage();
        final GeoreferencedImage projectedImage = mergedImage.imageToCs(viewport);
        final Graphics2D graphics = viewport.getGraphics();
        if (graphics != null) {
          viewport.drawImage(projectedImage, false);
        }
      }
    } else {
      super.renderTiles(viewport, cancellable, mapTiles);
    }
  }

  @Override
  public MapEx toMap() {
    return MapEx.EMPTY;
  }
}
