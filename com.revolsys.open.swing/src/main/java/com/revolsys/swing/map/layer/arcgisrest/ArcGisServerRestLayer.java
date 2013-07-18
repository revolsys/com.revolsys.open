package com.revolsys.swing.map.layer.arcgisrest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.io.esri.map.rest.ArcGisServerRestClient;
import com.revolsys.io.esri.map.rest.MapServer;
import com.revolsys.io.esri.map.rest.map.TileInfo;
import com.revolsys.io.map.MapSerializerUtil;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.AbstractTiledImageLayer;
import com.revolsys.swing.map.layer.MapTile;

public class ArcGisServerRestLayer extends AbstractTiledImageLayer {
  public static ArcGisServerRestLayer create(
    final Map<String, Object> properties) {
    final String url = (String)properties.get("url");
    final ArcGisServerRestLayer layer = new ArcGisServerRestLayer(url);
    layer.setProperties(properties);
    return layer;
  }

  private MapServer mapServer;

  private GeometryFactory geometryFactory;

  private final String url;

  public ArcGisServerRestLayer(final String url) {
    this(null, url);
  }

  public ArcGisServerRestLayer(final String name, final String url) {
    this.url = url;
    setType("arcgisServerRest");
    if (StringUtils.hasText(name)) {
      setName(name);
    }
  }

  @Override
  public BoundingBox getBoundingBox() {
    final MapServer mapServer = getMapServer();
    if (mapServer == null) {
      return new BoundingBox();
    } else {
      return mapServer.getFullExtent();
    }
  }

  public synchronized MapServer getMapServer() {
    if (mapServer == null) {
      try {
        // TODO initialize in background?
        this.mapServer = ArcGisServerRestClient.getMapServer(url);
        final TileInfo tileInfo = mapServer.getTileInfo();
        this.geometryFactory = tileInfo.getSpatialReference();
      } catch (final Throwable e) {
        setError(e);
        return null;
      }
    }
    return mapServer;
  }

  @Override
  public List<MapTile> getOverlappingMapTiles(final Viewport2D viewport) {
    final MapServer mapServer = getMapServer();
    final List<MapTile> tiles = new ArrayList<MapTile>();
    if (!isHasError()) {
      try {
        final double metresPerPixel = viewport.getMetresPerPixel();
        final int zoomLevel = mapServer.getZoomLevel(metresPerPixel);
        final double resolution = getResolution(viewport);
        if (resolution > 0) {
          final BoundingBox viewBoundingBox = viewport.getBoundingBox();
          final BoundingBox maxBoundingBox = getBoundingBox();
          final BoundingBox boundingBox = viewBoundingBox.convert(
            geometryFactory).intersection(maxBoundingBox);
          final double minX = boundingBox.getMinX();
          final double minY = boundingBox.getMinY();
          final double maxX = boundingBox.getMaxX();
          final double maxY = boundingBox.getMaxY();

          // Tiles start at the North-West corner of the map
          final int minTileX = mapServer.getTileX(zoomLevel, minX);
          final int minTileY = mapServer.getTileY(zoomLevel, maxY);
          final int maxTileX = mapServer.getTileX(zoomLevel, maxX);
          final int maxTileY = mapServer.getTileY(zoomLevel, minY);

          for (int tileY = minTileY; tileY <= maxTileY; tileY++) {
            for (int tileX = minTileX; tileX <= maxTileX; tileX++) {
              final ArcGisServerRestMapTile tile = new ArcGisServerRestMapTile(
                this, zoomLevel, resolution, tileX, tileY);
              tiles.add(tile);
            }
          }
        }
      } catch (final Throwable e) {
        setError(e);
      }
    }
    return tiles;
  }

  @Override
  public double getResolution(final Viewport2D viewport) {
    final MapServer mapServer = getMapServer();
    if (mapServer == null) {
      return 0;
    } else {
      final double metresPerPixel = viewport.getMetresPerPixel();
      final int zoomLevel = mapServer.getZoomLevel(metresPerPixel);
      return mapServer.getResolution(zoomLevel);
    }
  }

  @Override
  public boolean isVisible() {
    if (!super.isVisible()) {
      return false;
    } else {
      return true;
    }
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = super.toMap();
    MapSerializerUtil.add(map, "url", url);
    return map;
  }

}
