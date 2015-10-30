package com.revolsys.swing.map.layer.bing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;
import com.revolsys.io.map.MapSerializerUtil;
import com.revolsys.parallel.ExecutorServiceFactory;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.AbstractTiledImageLayer;
import com.revolsys.swing.map.layer.MapTile;
import com.revolsys.util.Property;

public class BingLayer extends AbstractTiledImageLayer {

  public static final GeometryFactory GEOMETRY_FACTORY = GeometryFactory.floating3(4326);

  private static final BoundingBox MAX_BOUNDING_BOX = new BoundingBoxDoubleGf(GEOMETRY_FACTORY, 2,
    -180, -85, 180, 85);

  public static BingLayer newLayer(final Map<String, Object> properties) {
    return new BingLayer(properties);
  }

  private BingClient client;

  private ImagerySet imagerySet = ImagerySet.Road;

  private MapLayer mapLayer;

  public BingLayer(final Map<String, Object> properties) {
    super(properties);
    setType("bing");
  }

  @Override
  protected boolean doInitialize() {
    ImagerySet imagerySet = ImagerySet.Road;
    final String imagerySetName = getProperty("imagerySet");
    if (Property.hasValue(imagerySetName)) {
      try {
        imagerySet = ImagerySet.valueOf(imagerySetName);
      } catch (final Throwable e) {
        throw new RuntimeException("Unknown Bing imagery set " + imagerySetName, e);
      }
    }
    MapLayer mapLayer = null;
    final String mapLayerName = getProperty("mapLayer");
    if (Property.hasValue(mapLayerName)) {
      try {
        mapLayer = MapLayer.valueOf(mapLayerName);
      } catch (final Throwable e) {
        throw new RuntimeException("Unknown Bing map layer " + mapLayerName, e);
      }
    }
    final String bingMapsKey = getProperty("bingMapsKey");
    this.client = new BingClient(bingMapsKey);
    this.imagerySet = imagerySet;
    this.mapLayer = mapLayer;
    return true;
  }

  @Override
  public BoundingBox getBoundingBox() {
    return MAX_BOUNDING_BOX;
  }

  public BingClient getClient() {
    return this.client;
  }

  public String getImagerySet() {
    return StringConverterRegistry.toString(this.imagerySet.toString());
  }

  public ImagerySet getImagerySetEnum() {
    return this.imagerySet;
  }

  public String getMapLayer() {
    return StringConverterRegistry.toString(this.mapLayer);
  }

  public MapLayer getMapLayerEnum() {
    return this.mapLayer;
  }

  @Override
  public List<MapTile> getOverlappingMapTiles(final Viewport2D viewport) {
    final List<MapTile> tiles = new ArrayList<MapTile>();
    try {
      final double metresPerPixel = viewport.getUnitsPerPixel();
      final int zoomLevel = this.client.getZoomLevel(metresPerPixel);
      final double resolution = getResolution(viewport);
      final BoundingBox geographicBoundingBox = viewport.getBoundingBox()
        .convert(GEOMETRY_FACTORY)
        .intersection(MAX_BOUNDING_BOX);
      final double minX = geographicBoundingBox.getMinX();
      final double minY = geographicBoundingBox.getMinY();
      final double maxX = geographicBoundingBox.getMaxX();
      final double maxY = geographicBoundingBox.getMaxY();

      // Tiles start at the North-West corner of the map
      final int minTileY = this.client.getTileY(zoomLevel, maxY);
      final int maxTileY = this.client.getTileY(zoomLevel, minY);
      final int minTileX = this.client.getTileX(zoomLevel, minX);
      final int maxTileX = this.client.getTileX(zoomLevel, maxX);

      for (int tileY = minTileY; tileY <= maxTileY; tileY++) {
        for (int tileX = minTileX; tileX <= maxTileX; tileX++) {
          final BingMapTile tile = new BingMapTile(this, zoomLevel, resolution, tileX, tileY);
          tiles.add(tile);
        }
      }

    } catch (final Throwable e) {
      LoggerFactory.getLogger(getClass()).error("Error getting tile envelopes", e);
    }
    return tiles;
  }

  @Override
  public double getResolution(final Viewport2D viewport) {
    final double metresPerPixel = viewport.getUnitsPerPixel();
    final int zoomLevel = this.client.getZoomLevel(metresPerPixel);
    return this.client.getResolution(zoomLevel);
  }

  public void setClient(final BingClient client) {
    this.client = client;
    ExecutorServiceFactory.getExecutorService().execute(this::initialize);
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

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = super.toMap();
    MapSerializerUtil.add(map, "imagerySet", this.imagerySet);
    MapSerializerUtil.add(map, "mapLayer", this.mapLayer);
    return map;
  }
}
