package com.revolsys.swing.map.layer.bing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.logging.Logs;
import org.jeometry.coordinatesystem.model.systems.EpsgId;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.parallel.ExecutorServiceFactory;
import com.revolsys.swing.Icons;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.raster.AbstractTiledImageLayer;
import com.revolsys.util.CaseConverter;

public class BingLayer extends AbstractTiledImageLayer<BingMapTile> {
  public static final GeometryFactory GEOMETRY_FACTORY = GeometryFactory.floating3d(EpsgId.WGS84);

  private static final BoundingBox MAX_BOUNDING_BOX = GEOMETRY_FACTORY.newBoundingBox(-180, -85,
    180, 85);

  private BingClient client;

  private ImagerySet imagerySet = ImagerySet.Road;

  private MapLayer mapLayer;

  BingLayer() {
    super("bing");
    setIcon(Icons.getIcon("bing"));
    setGeometryFactory(GeometryFactory.worldMercator());
  }

  public BingLayer(final Map<String, ? extends Object> properties) {
    this();
    setProperties(properties);
    setGeometryFactory(GeometryFactory.worldMercator());
  }

  @Override
  public boolean equals(final Object other) {
    if (other instanceof BingLayer) {
      final BingLayer layer = (BingLayer)other;
      if (DataType.equal(layer.getImagerySet(), getImagerySet())) {
        if (DataType.equal(layer.getMapLayer(), getMapLayer())) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public BoundingBox getBoundingBox() {
    return MAX_BOUNDING_BOX;
  }

  public BingClient getClient() {
    return this.client;
  }

  public ImagerySet getImagerySet() {
    return this.imagerySet;
  }

  public MapLayer getMapLayer() {
    return this.mapLayer;
  }

  @Override
  public List<BingMapTile> getOverlappingMapTiles(final Viewport2D view) {
    final List<BingMapTile> tiles = new ArrayList<>();
    try {
      final double metresPerPixel = view.getUnitsPerPixel();
      BingClient client = this.client;
      final int zoomLevel = client.getZoomLevel(metresPerPixel);
      final double resolution = getResolution(view);
      final BoundingBox geographicBoundingBox = view.getBoundingBox()
        .bboxToCs(GEOMETRY_FACTORY)
        .bboxIntersection(MAX_BOUNDING_BOX);
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
          final String quadKey = client.getQuadKey(zoomLevel, tileX, tileY);
          final BoundingBox boundingBox = client.getBoundingBox(zoomLevel, tileX, tileY);
          final BingMapTile tile = new BingMapTile(this, boundingBox, quadKey, resolution);
          tiles.add(tile);
        }
      }

    } catch (final Throwable e) {
      Logs.error(this, "Error getting tile envelopes", e);
    }
    return tiles;
  }

  @Override
  public double getResolution(final Viewport2D viewport) {
    final double metresPerPixel = viewport.getUnitsPerPixel();
    final int zoomLevel = this.client.getZoomLevel(metresPerPixel);
    return this.client.getResolution(zoomLevel);
  }

  @Override
  protected boolean initializeDo() {
    final String bingMapsKey = getProperty("bingMapsKey");
    this.client = new BingClient(bingMapsKey);
    return true;
  }

  public void setClient(final BingClient client) {
    this.client = client;
    ExecutorServiceFactory.getExecutorService().execute(this::initialize);
  }

  public void setImagerySet(final ImagerySet imagerySet) {
    this.imagerySet = imagerySet;
    if (getName() == null) {
      setName("Bing " + CaseConverter.toCapitalizedWords(imagerySet.toString()));
    }
  }

  public void setMapLayer(final MapLayer mapLayer) {
    this.mapLayer = mapLayer;
  }

  @Override
  public MapEx toMap() {
    final MapEx map = super.toMap();
    addToMap(map, "imagerySet", this.imagerySet);
    addToMap(map, "mapLayer", this.mapLayer);
    return map;
  }
}
