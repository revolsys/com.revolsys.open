package com.revolsys.swing.map.layer.arcgisrest;

import java.awt.image.BufferedImage;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.raster.BufferedGeoreferencedImage;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.record.io.format.esri.rest.map.MapService;
import com.revolsys.swing.map.layer.raster.TiledGeoreferencedImageLayerRenderer;
import com.revolsys.swing.map.layer.tile.AbstractTiledLayer;
import com.revolsys.swing.map.view.ViewRenderer;

public class ArcGisRestTileCacheLayerRenderer
  extends TiledGeoreferencedImageLayerRenderer<ArcGisRestServerTileCacheMapTile> {

  public ArcGisRestTileCacheLayerRenderer(final ArcGisRestServerTileCacheLayer layer) {
    super(layer);
  }

  @Override
  public void render(final ViewRenderer view,
    final AbstractTiledLayer<GeoreferencedImage, ArcGisRestServerTileCacheMapTile> layer) {
    if (layer.isSameCoordinateSystem(view)) {
      super.render(view, layer);
    } else {
      final ArcGisRestServerTileCacheLayer restLayer = (ArcGisRestServerTileCacheLayer)getLayer();
      final MapService mapService = restLayer.getMapService();
      if (mapService.isExportTilesAllowed()) {
        super.render(view, layer);
        if (1 == 1) {
          final BoundingBox boundingBox = view.getBoundingBox();
          final BufferedImage image = mapService.getExportImage(boundingBox,
            (int)view.getViewWidthPixels(), (int)view.getViewHeightPixels());
          final BufferedGeoreferencedImage georeferencedImage = new BufferedGeoreferencedImage(
            boundingBox, image);
          view.drawImage(georeferencedImage, false);
        }
      } else {
        super.render(view, layer);
      }
    }
  }

}
