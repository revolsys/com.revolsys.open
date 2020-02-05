package com.revolsys.swing.map.layer.raster;

import java.beans.PropertyChangeListener;
import java.util.List;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.raster.GeoreferencedImageMapTile;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.swing.map.ImageViewport;
import com.revolsys.swing.map.layer.tile.AbstractTiledLayer;
import com.revolsys.swing.map.layer.tile.AbstractTiledLayerRenderer;
import com.revolsys.swing.map.view.ViewRenderer;
import com.revolsys.swing.map.view.graphics.Graphics2DViewRenderer;
import com.revolsys.util.BooleanCancellable;
import com.revolsys.util.Cancellable;

public class TiledGeoreferencedImageLayerRenderer<T extends GeoreferencedImageMapTile>
  extends AbstractTiledLayerRenderer<GeoreferencedImage, T> implements PropertyChangeListener {

  public TiledGeoreferencedImageLayerRenderer(final AbstractTiledGeoreferencedImageLayer<T> layer) {
    super("tiledImage", "Tiles", null);
    setLayer(layer);
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
    if (layer != null) {
      if (layer.isProjectionRequired(view)) {
        final GeometryFactory geometryFactory = layer.getGeometryFactory();
        final BoundingBox boundingBox = geometryFactory.bboxEditor()
          .addAllBbox(mapTiles)
          .getBoundingBox();
        final double resolution = getLayerResolution();
        final int width = (int)Math.round(boundingBox.getWidth() / resolution);
        final int height = (int)Math.round(boundingBox.getHeight() / resolution);
        if (width > 0 && height > 0) {
          try (
            final ImageViewport imageViewport = new ImageViewport(layer.getProject(), width, height,
              boundingBox)) {
            final Graphics2DViewRenderer imageView = imageViewport.newViewRenderer();
            super.renderTiles(imageView, cancellable, mapTiles);
            final GeoreferencedImage mergedImage = imageViewport.getGeoreferencedImage();
            final GeoreferencedImage projectedImage = mergedImage.imageToCs(view, view);
            if (!view.isCancelled()) {
              view.drawImage(projectedImage, false);
            }
          } catch (final OutOfMemoryError e) {
          }
        }
      } else {
        super.renderTiles(view, cancellable, mapTiles);
      }
    }
  }

  @Override
  public JsonObject toMap() {
    return JsonObject.EMPTY;
  }
}
