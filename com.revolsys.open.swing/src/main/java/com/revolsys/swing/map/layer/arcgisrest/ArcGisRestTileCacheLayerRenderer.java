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
      if (mapService.isExportTilesAllowed() && restLayer.isUseServerExport()) {
        try {
          final BoundingBox boundingBox = view.getBoundingBox();
          final double resolution = restLayer.getResolution(view);
          final double width = boundingBox.getWidth();
          final double height = boundingBox.getHeight();
          final int viewWidth = (int)(width / resolution);
          final int viewHeight = (int)(height / resolution);
          final BufferedImage image = mapService.getExportImage(boundingBox, viewWidth, viewHeight);
          final BufferedGeoreferencedImage georeferencedImage = new BufferedGeoreferencedImage(
            boundingBox, image);
          view.drawImage(georeferencedImage, false);
        } catch (final Exception e) {
          super.render(view, layer);
        }
      } else {
        super.render(view, layer);
      }
    }
  }

}
