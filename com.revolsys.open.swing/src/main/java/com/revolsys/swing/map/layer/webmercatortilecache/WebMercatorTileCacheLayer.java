package com.revolsys.swing.map.layer.webmercatortilecache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import org.slf4j.LoggerFactory;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.field.TextField;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.AbstractTiledImageLayer;
import com.revolsys.swing.map.layer.BaseMapLayerGroup;
import com.revolsys.swing.map.layer.MapTile;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.menu.Menus;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.Property;

public class WebMercatorTileCacheLayer extends AbstractTiledImageLayer {
  public static final GeometryFactory GEOMETRY_FACTORY = GeometryFactory.floating3(4326);

  private static final BoundingBox MAX_BOUNDING_BOX = new BoundingBoxDoubleGf(GEOMETRY_FACTORY, 2,
    -180, -85, 180, 85);

  private static void actionAddLayer(final BaseMapLayerGroup parent) {
    final ValueField dialog = new ValueField();
    dialog.setTitle("Add Web Mercator Tile Cache Layer");

    SwingUtil.addLabel(dialog, "URL");
    final TextField urlField = new TextField("url", 50);
    dialog.add(urlField);

    GroupLayouts.makeColumns(dialog, 2, true, true);

    dialog.setSaveAction(() -> {
      final WebMercatorTileCacheLayer layer = new WebMercatorTileCacheLayer();
      final String url = urlField.getText();
      layer.setUrl(url);
      layer.setVisible(false);
      parent.addLayer(layer);
    });

    dialog.showDialog();
  }

  public static void mapObjectFactoryInit() {
    final MenuFactory baseMapsMenu = MenuFactory.getMenu(BaseMapLayerGroup.class);

    Menus.addMenuItem(baseMapsMenu, "group", "Add Web Mercator Tile Cache Layer", (Icon)null,
      WebMercatorTileCacheLayer::actionAddLayer);
  }

  private WebMercatorTileCacheClient client;

  private String url;

  public WebMercatorTileCacheLayer() {
    super("webMercatorTileCacheLayer");
  }

  public WebMercatorTileCacheLayer(final Map<String, Object> properties) {
    this();
    setProperties(properties);
  }

  @Override
  public BoundingBox getBoundingBox() {
    return MAX_BOUNDING_BOX;
  }

  public WebMercatorTileCacheClient getClient() {
    return this.client;
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
          final WebMercatorTileCacheMapTile tile = new WebMercatorTileCacheMapTile(this, zoomLevel,
            resolution, tileX, tileY);
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
  public Map<String, Object> toMap() {
    final Map<String, Object> map = super.toMap();
    map.put("type", "openStreetMap");
    addToMap(map, "url", this.url);
    return map;
  }
}
