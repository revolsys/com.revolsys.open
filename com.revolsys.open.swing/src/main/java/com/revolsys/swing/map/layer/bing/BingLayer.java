package com.revolsys.swing.map.layer.bing;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.slf4j.LoggerFactory;

import com.revolsys.gis.bing.BingClient;
import com.revolsys.gis.bing.ImagerySet;
import com.revolsys.gis.bing.MapLayer;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.parallel.ExecutorServiceFactory;
import com.revolsys.parallel.process.InvokeMethodRunnable;
import com.revolsys.swing.component.TabbedValuePanel;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.AbstractTiledImageLayer;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.MapTile;

public class BingLayer extends AbstractTiledImageLayer {

  public static final GeometryFactory GEOMETRY_FACTORY = GeometryFactory.getFactory(4326);

  private static final BoundingBox MAX_BOUNDING_BOX = new BoundingBox(
    GEOMETRY_FACTORY, -180, -85, 180, 85);

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

  @Override
  public ValueField<Layer> addPropertiesTabGeneral(TabbedValuePanel<Layer> tabPanel) {
    return super.addPropertiesTabGeneral(tabPanel);
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
      final int zoomLevel = client.getZoomLevel(metresPerPixel);
      final BoundingBox geographicBoundingBox = viewport.getBoundingBox()
        .convert(GEOMETRY_FACTORY)
        .intersection(MAX_BOUNDING_BOX);
      final double minX = geographicBoundingBox.getMinX();
      final double minY = geographicBoundingBox.getMinY();
      final double maxX = geographicBoundingBox.getMaxX();
      final double maxY = geographicBoundingBox.getMaxY();

      // Tiles start at the North-West corner of the map
      final int minTileY = client.getTileY(zoomLevel, maxY);
      final int maxTileY = client.getTileY(zoomLevel, minY);
      final int minTileX = client.getTileX(zoomLevel, minX);
      final int maxTileX = client.getTileX(zoomLevel, maxX);

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
      return true;
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
