package com.revolsys.swing.map.layer.arcgisrest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.io.esri.map.rest.ArcGisServerRestClient;
import com.revolsys.io.esri.map.rest.MapServer;
import com.revolsys.io.esri.map.rest.map.TileInfo;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.AbstractTiledImageLayer;
import com.revolsys.swing.map.layer.MapTile;
import com.revolsys.swing.map.layer.Project;

public class ArcGisServerRestLayer extends AbstractTiledImageLayer {
  public static ArcGisServerRestLayer create(
    final Map<String, Object> properties) {
    final String url = (String)properties.get("url");
    final ArcGisServerRestLayer layer = new ArcGisServerRestLayer(url);
    layer.setProperties(properties);
    return layer;
  }

  private final MapServer mapServer;

  private final GeometryFactory geometryFactory;

  public ArcGisServerRestLayer(final String url) {
    this(null, url);
  }

  public ArcGisServerRestLayer(final String name, final String url) {
    this.mapServer = ArcGisServerRestClient.getMapServer(url);
    if (StringUtils.hasText(name)) {
      setName(name);
    }
    final TileInfo tileInfo = mapServer.getTileInfo();
    this.geometryFactory = tileInfo.getSpatialReference();

  }

  @Override
  public BoundingBox getBoundingBox() {
    return mapServer.getFullExtent();
  }

  public MapServer getMapServer() {
    return mapServer;
  }

  @Override
  public List<MapTile> getOverlappingEnvelopes(final Viewport2D viewport) {
    final List<MapTile> tiles = new ArrayList<MapTile>();
    try {
      final double metresPerPixel = viewport.getMetresPerPixel();
      final int zoomLevel = mapServer.getZoomLevel(metresPerPixel);
      final BoundingBox viewBoundingBox = viewport.getBoundingBox();
      final BoundingBox maxBoundingBox = getBoundingBox();
      final BoundingBox boundingBox = viewBoundingBox.convert(geometryFactory)
        .intersection(maxBoundingBox);
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
          tiles.add(new ArcGisServerRestMapTile(mapServer, zoomLevel, tileX,
            tileY));
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
      final int srid = project.getGeometryFactory().getSRID();
      if (srid == geometryFactory.getSRID()) {
        return true;
      }
      return false;
    }
  }

}
