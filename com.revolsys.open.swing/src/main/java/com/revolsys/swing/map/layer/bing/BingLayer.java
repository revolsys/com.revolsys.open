package com.revolsys.swing.map.layer.bing;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.revolsys.gis.bing.BingClient;
import com.revolsys.gis.bing.BingClient.ImagerySet;
import com.revolsys.gis.bing.BingClient.MapLayer;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.parallel.ExecutorServiceFactory;
import com.revolsys.parallel.process.InvokeMethodRunnable;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.AbstractTiledImageLayer;
import com.revolsys.swing.map.layer.MapTile;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.TileLoaderProcess;
import com.revolsys.swing.map.layer.TiledImageLayerRenderer;

public class BingLayer extends AbstractTiledImageLayer {
  public static final GeometryFactory GEOMETRY_FACTORY = GeometryFactory.getFactory(4326);

  private static final BoundingBox MAX_BOUNDING_BOX = new BoundingBox(
    GEOMETRY_FACTORY, -180, -85, 180, 85);

  private BingClient client;

  private final ImagerySet imagerySet;

  private MapLayer mapLayer;

  public BingLayer(final BingClient client, final ImagerySet imagerySet) {
    this.client = client;
    this.imagerySet = imagerySet;
    setRenderer(new TiledImageLayerRenderer(this));
    setName("Bing " + imagerySet);
  }

  public BingLayer(final String bingMapKey, final ImagerySet imagerySet) {
    this(new BingClient(bingMapKey), imagerySet);
  }

  @Override
  public BoundingBox getBoundingBox() {
    return MAX_BOUNDING_BOX;
  }

  public BingClient getClient() {
    return client;
  }

  public ImagerySet getImagerySet() {
    return imagerySet;
  }

  public MapLayer getMapLayer() {
    return mapLayer;
  }

  @Override
  public List<MapTile> getOverlappingEnvelopes(final Viewport2D viewport) {
    final List<MapTile> tiles = new ArrayList<MapTile>();
    try {
      double metresPerPixel = viewport.getMetresPerPixel();
      int zoomLevel = BingClient.getZoomLevel(metresPerPixel);
      BoundingBox geographicBoundingBox = viewport.getBoundingBox()
        .convert(GEOMETRY_FACTORY)
        .intersection(MAX_BOUNDING_BOX);
      double minX = geographicBoundingBox.getMinX();
      double minY = geographicBoundingBox.getMinY();
      double maxX = geographicBoundingBox.getMaxX();
      double maxY = geographicBoundingBox.getMaxY();

      // Tiles start at the North-West corner of the map
      int minTileY = BingClient.getTileY(zoomLevel, maxY);
      int maxTileY = BingClient.getTileY(zoomLevel, minY);
      int minTileX = BingClient.getTileX(zoomLevel, minX);
      int maxTileX = BingClient.getTileX(zoomLevel, maxX);

      for (int tileY = minTileY; tileY <= maxTileY; tileY++) {
        for (int tileX = minTileX; tileX <= maxTileX; tileX++) {
          tiles.add(new BingMapTile(zoomLevel, tileX, tileY));
        }
      }

    } catch (final OutOfMemoryError e) {
      LoggerFactory.getLogger(getClass()).error("Out of memory", e);
    }
    return tiles;
  }

  @Override
  public TileLoaderProcess getTileLoaderProcess() {
    return new BingTileImageLoaderProcess(this);
  }

  @Override
  public boolean isVisible() {
    if (!super.isVisible()) {
      return false;
    } else {
      final Project project = getProject();
      int srid = project.getGeometryFactory().getSRID();
      if (srid == 102100 || srid == 3857) {
        return true;
      }
      return false;
    }
  }

  public void setClient(final BingClient client) {
    this.client = client;
    ExecutorServiceFactory.getExecutorService().execute(
      new InvokeMethodRunnable(this, "init"));
  }

}
