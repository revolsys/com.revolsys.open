package com.revolsys.swing.map.layer.raster;

import java.beans.PropertyChangeListener;
import java.util.List;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.swing.map.ImageViewport;
import com.revolsys.swing.map.layer.tile.AbstractTiledLayer;
import com.revolsys.swing.map.layer.tile.AbstractTiledLayerRenderer;
import com.revolsys.swing.map.view.ViewRenderer;
import com.revolsys.swing.map.view.graphics.Graphics2DViewRenderer;
import com.revolsys.util.BooleanCancellable;
import com.revolsys.util.Cancellable;

public class TiledImageLayerRenderer<T extends GeoreferencedImageMapTile>
  extends AbstractTiledLayerRenderer<GeoreferencedImage, T> implements PropertyChangeListener {

  public TiledImageLayerRenderer(final AbstractTiledImageLayer<T> layer) {
    super("tiledImage", layer);
  }

  public TiledImageLayerRenderer(final String type, final String name) {
    super(type, name);
  }

  @Override
  protected void renderTile(final ViewRenderer view, final Cancellable cancellable, final T tile) {
    final GeoreferencedImage image = tile.getData();
    view.drawImage(image, false);
  }

  @Override
  protected void renderTiles(final ViewRenderer view, final BooleanCancellable cancellable,
    final List<T> mapTiles) {
    final AbstractTiledLayer<GeoreferencedImage, T> layer = getLayer();
    if (layer.isProjectionRequired(view)) {
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
        final Graphics2DViewRenderer imageView = imageViewport.newViewRenderer();
        super.renderTiles(imageView, cancellable, mapTiles);
        final GeoreferencedImage mergedImage = imageViewport.getGeoreferencedImage();
        final double viewResolution = getViewResolution();
        final GeoreferencedImage projectedImage = mergedImage.getImage(view, viewResolution);
        view.drawImage(projectedImage, false);
      }
    } else {
      super.renderTiles(view, cancellable, mapTiles);
    }
  }

  @Override
  public MapEx toMap() {
    return MapEx.EMPTY;
  }
}
