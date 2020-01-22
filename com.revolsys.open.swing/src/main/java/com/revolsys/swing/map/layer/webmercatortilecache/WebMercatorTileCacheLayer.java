package com.revolsys.swing.map.layer.webmercatortilecache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jeometry.common.data.type.DataType;
import org.jeometry.coordinatesystem.model.systems.EpsgId;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.swing.map.layer.raster.AbstractTiledGeoreferencedImageLayer;
import com.revolsys.swing.map.layer.tile.AbstractTiledLayerRenderer;
import com.revolsys.swing.map.view.ViewRenderer;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.Property;

public class WebMercatorTileCacheLayer
  extends AbstractTiledGeoreferencedImageLayer<WebMercatorTileCacheMapTile> {
  public static final GeometryFactory GEOMETRY_FACTORY = GeometryFactory.floating3d(EpsgId.WGS84);

  private static final BoundingBox MAX_BOUNDING_BOX = GEOMETRY_FACTORY.newBoundingBox(-180, -85,
    180, 85);

  private WebMercatorTileCacheClient client;

  private String url;

  public WebMercatorTileCacheLayer() {
    super("webMercatorTileCacheLayer");
    setGeometryFactory(GeometryFactory.worldMercator());
  }

  public WebMercatorTileCacheLayer(final Map<String, ? extends Object> properties) {
    this();
    setProperties(properties);
    setGeometryFactory(GeometryFactory.worldMercator());
  }

  @Override
  public boolean equals(final Object other) {
    if (other instanceof WebMercatorTileCacheLayer) {
      final WebMercatorTileCacheLayer layer = (WebMercatorTileCacheLayer)other;
      if (DataType.equal(layer.getUrl(), getUrl())) {
        return true;
      }
    }
    return false;
  }

  public WebMercatorTileCacheClient getClient() {
    return this.client;
  }

  @Override
  public List<WebMercatorTileCacheMapTile> getOverlappingMapTiles(
    final AbstractTiledLayerRenderer<?, ?> renderer, final ViewRenderer view) {
    final List<WebMercatorTileCacheMapTile> tiles = new ArrayList<>();
    try {
      final double metresPerPixel = view.getMetresPerPixel();
      final int zoomLevel = this.client.getZoomLevel(metresPerPixel);
      final double resolution = this.client.getResolution(zoomLevel);
      final BoundingBox geographicBoundingBox = view.getBoundingBox()
        .bboxToCs(GEOMETRY_FACTORY)
        .bboxIntersection(MAX_BOUNDING_BOX);
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
          final WebMercatorTileCacheMapTile tile = new WebMercatorTileCacheMapTile(this, zoomLevel,
            resolution, tileX, tileY);
          tiles.add(tile);
        }
      }

    } catch (final Throwable e) {
      setError(e);
    }
    return tiles;
  }

  @Override
  public double getResolution(final ViewRenderer view) {
    final double metresPerPixel = view.getMetresPerPixel();
    final int zoomLevel = this.client.getZoomLevel(metresPerPixel);
    return this.client.getResolution(zoomLevel);
  }

  public String getUrl() {
    return this.url;
  }

  @Override
  protected boolean initializeDo() {
    this.client = new WebMercatorTileCacheClient(this.url);
    return true;
  }

  @Override
  protected ValueField newPropertiesTabGeneralPanelSource(final BasePanel parent) {
    final ValueField panel = super.newPropertiesTabGeneralPanelSource(parent);

    final String url = getUrl();
    SwingUtil.addLabelledReadOnlyTextField(panel, "URL", url);
    GroupLayouts.makeColumns(panel, 2, true);
    return panel;
  }

  public void setUrl(final String url) {
    final Object oldValue = this.url;
    if (Property.hasValue(url)) {
      if (url.endsWith("/")) {
        this.url = url;
      } else {
        this.url = url + "/";
      }
      if (getName() == null) {
        setName(
          CaseConverter.toCapitalizedWords(url.replaceAll("http(s)?://", "").replaceAll("/", " ")));
      }
    }
    firePropertyChange("url", oldValue, url);
  }

  @Override
  public JsonObject toMap() {
    final JsonObject map = super.toMap();
    map.put("type", "webMercatorTileCacheLayer");
    addToMap(map, "url", this.url);
    return map;
  }
}
