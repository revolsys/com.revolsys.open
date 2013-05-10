package com.revolsys.swing.map.layer.bing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.revolsys.gis.bing.BingClient;
import com.revolsys.gis.bing.ImagerySet;
import com.revolsys.gis.bing.MapLayer;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.parallel.ExecutorServiceFactory;
import com.revolsys.parallel.process.InvokeMethodRunnable;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.AbstractTiledImageLayer;
import com.revolsys.swing.map.layer.InvokeMethodLayerFactory;
import com.revolsys.swing.map.layer.LayerFactory;
import com.revolsys.swing.map.layer.MapTile;
import com.revolsys.swing.map.layer.Project;

public class BingLayer extends AbstractTiledImageLayer {

  public static final LayerFactory<BingLayer> FACTORY = new InvokeMethodLayerFactory<BingLayer>(
    "bing", "Bing Tiles", BingLayer.class, "create");

  public static final GeometryFactory GEOMETRY_FACTORY = GeometryFactory.getFactory(4326);

  private static final BoundingBox MAX_BOUNDING_BOX = new BoundingBox(
    GEOMETRY_FACTORY, -180, -85, 180, 85);

  public static BingLayer create(final Map<String, Object> properties) {
    ImagerySet imagerySet = ImagerySet.Road;
    final String imagerySetName = (String)properties.remove("imagerySet");
    if (StringUtils.hasText(imagerySetName)) {
      try {
        imagerySet = ImagerySet.valueOf(imagerySetName);
      } catch (final Throwable e) {
        LoggerFactory.getLogger(BingLayer.class).error(
          "Unknown Bing imagery set " + imagerySetName, e);
      }
    }
    MapLayer mapLayer = null;
    final String mapLayerName = (String)properties.remove("mapLayer");
    if (StringUtils.hasText(mapLayerName)) {
      try {
        mapLayer = MapLayer.valueOf(mapLayerName);
      } catch (final Throwable e) {
        LoggerFactory.getLogger(BingLayer.class).error(
          "Unknown Bing map layer " + mapLayerName, e);
      }
    }
    final BingLayer layer = new BingLayer(imagerySet, mapLayer);
    layer.setProperties(properties);
    return layer;
  }

  private BingClient client;

  private ImagerySet imagerySet;

  private MapLayer mapLayer;

  public BingLayer() {
    this(ImagerySet.Road);
  }

  public BingLayer(final BingClient client, final ImagerySet imagerySet) {
    this(client, imagerySet, null);
  }

  public BingLayer(final BingClient client, final ImagerySet imagerySet,
    final MapLayer mapLayer) {
    this.client = client;
    this.imagerySet = imagerySet;
    this.mapLayer = mapLayer;
    setName("Bing " + imagerySet);
    setVisible(true);
  }

  public BingLayer(final ImagerySet imagerySet) {
    this(new BingClient(), imagerySet);
  }

  public BingLayer(final ImagerySet imagerySet, final MapLayer mapLayer) {
    this(new BingClient(), imagerySet, mapLayer);
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
      final double metresPerPixel = viewport.getMetresPerPixel();
      final int zoomLevel = BingClient.getZoomLevel(metresPerPixel);
      final BoundingBox geographicBoundingBox = viewport.getBoundingBox()
        .convert(GEOMETRY_FACTORY)
        .intersection(MAX_BOUNDING_BOX);
      final double minX = geographicBoundingBox.getMinX();
      final double minY = geographicBoundingBox.getMinY();
      final double maxX = geographicBoundingBox.getMaxX();
      final double maxY = geographicBoundingBox.getMaxY();

      // Tiles start at the North-West corner of the map
      final int minTileY = BingClient.getTileY(zoomLevel, maxY);
      final int maxTileY = BingClient.getTileY(zoomLevel, minY);
      final int minTileX = BingClient.getTileX(zoomLevel, minX);
      final int maxTileX = BingClient.getTileX(zoomLevel, maxX);

      for (int tileY = minTileY; tileY <= maxTileY; tileY++) {
        for (int tileX = minTileX; tileX <= maxTileX; tileX++) {
          tiles.add(new BingMapTile(this, zoomLevel, tileX, tileY));
        }
      }

    } catch (final OutOfMemoryError e) {
      LoggerFactory.getLogger(getClass()).error("Out of memory", e);
    }
    return tiles;
  }

  @Override
  public boolean isVisible() {
    if (!super.isVisible()) {
      return false;
    } else {
      final Project project = getProject();
      if (project != null) {
        final int srid = project.getGeometryFactory().getSRID();
        if (srid == 102100 || srid == 3857) {
          return true;
        }
      }
      return false;
    }
  }

  public void setClient(final BingClient client) {
    this.client = client;
    ExecutorServiceFactory.getExecutorService().execute(
      new InvokeMethodRunnable(this, "init"));
  }

  public void setImagerySet(final ImagerySet imagerySet) {
    this.imagerySet = imagerySet;
  }

  public void setImagerySet(final String imagerySet) {
    this.imagerySet = ImagerySet.valueOf(imagerySet);
  }

  public void setMapLayer(final MapLayer mapLayer) {
    this.mapLayer = mapLayer;
  }

  public void setMapLayer(final String mapLayer) {
    this.mapLayer = MapLayer.valueOf(mapLayer);
  }

}
