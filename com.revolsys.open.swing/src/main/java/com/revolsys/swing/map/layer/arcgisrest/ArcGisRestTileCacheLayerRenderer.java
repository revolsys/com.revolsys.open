package com.revolsys.swing.map.layer.arcgisrest;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.raster.BufferedGeoreferencedImage;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.record.io.format.esri.rest.map.MapService;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.raster.TiledGeoreferencedImageLayerRenderer;
import com.revolsys.swing.map.layer.tile.AbstractTiledLayer;
import com.revolsys.util.Cancellable;

public class ArcGisRestTileCacheLayerRenderer
  extends TiledGeoreferencedImageLayerRenderer<ArcGisRestServerTileCacheMapTile> {

  public ArcGisRestTileCacheLayerRenderer(final ArcGisRestServerTileCacheLayer layer) {
    super(layer);
  }

  @Override
  public void render(final Viewport2D viewport, final Cancellable cancellable,
    final AbstractTiledLayer<GeoreferencedImage, ArcGisRestServerTileCacheMapTile> layer) {
    if (layer.isSameCoordinateSystem(viewport)) {
      super.render(viewport, cancellable, layer);
    } else {
      final ArcGisRestServerTileCacheLayer restLayer = (ArcGisRestServerTileCacheLayer)getLayer();
      final MapService mapService = restLayer.getMapService();
      if (mapService.isExportTilesAllowed() && restLayer.isUseServerExport()) {
        try {
          final BoundingBox boundingBox = viewport.getBoundingBox();
          final int width = viewport.getViewWidthPixels();
          final int height = viewport.getViewHeightPixels();
          final BufferedImage image = mapService.getExportImage(boundingBox, width, height);
          final BufferedGeoreferencedImage georeferencedImage = new BufferedGeoreferencedImage(
            boundingBox, image);
          final Graphics2D graphics = viewport.getGraphics();
          if (graphics != null) {
            viewport.drawImage(georeferencedImage, false);
          }
        } catch (final Exception e) {
          super.render(viewport, cancellable, layer);
        }
      } else {
        super.render(viewport, cancellable, layer);
      }
    }
  }

}
