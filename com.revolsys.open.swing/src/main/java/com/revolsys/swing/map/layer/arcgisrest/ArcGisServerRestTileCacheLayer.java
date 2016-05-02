package com.revolsys.swing.map.layer.arcgisrest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.record.io.format.esri.map.rest.ArcGisServerRestClient;
import com.revolsys.record.io.format.esri.map.rest.MapServer;
import com.revolsys.record.io.format.esri.map.rest.map.TileInfo;
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
import com.revolsys.util.Exceptions;
import com.revolsys.util.Property;

public class ArcGisServerRestTileCacheLayer extends AbstractTiledImageLayer {
  private static void actionAddLayer(final BaseMapLayerGroup parent) {
    final ValueField dialog = new ValueField();
    dialog.setTitle("Add ArcGIS Tile Cache");

    SwingUtil.addLabel(dialog, "URL");
    final TextField urlField = new TextField("url", 50);
    dialog.add(urlField);

    GroupLayouts.makeColumns(dialog, 2, true, true);

    dialog.setSaveAction(() -> {
      final String url = urlField.getText();
      if (Property.hasValue(url)) {
        final ArcGisServerRestTileCacheLayer layer = new ArcGisServerRestTileCacheLayer();
        layer.setUrl(url);
        layer.setVisible(false);
        parent.addLayer(layer);
      }
    });

    dialog.showDialog();
  }

  public static void mapObjectFactoryInit() {
    final MenuFactory baseMapsMenu = MenuFactory.getMenu(BaseMapLayerGroup.class);

    Menus.addMenuItem(baseMapsMenu, "group", "Add ArcGIS Tile Cache", (Icon)null,
      ArcGisServerRestTileCacheLayer::actionAddLayer);
  }

  private GeometryFactory geometryFactory;

  private final Object initSync = new Object();

  private MapServer mapServer;

  private String url;

  public ArcGisServerRestTileCacheLayer() {
    super("arcgisRestServerTileLayer");
  }

  public ArcGisServerRestTileCacheLayer(final Map<String, Object> properties) {
    this();
    setProperties(properties);
  }

  @Override
  public BoundingBox getBoundingBox() {
    final MapServer mapServer = getMapServer();
    if (mapServer == null) {
      return BoundingBox.EMPTY;
    } else {
      return mapServer.getFullExtent();
    }
  }

  public MapServer getMapServer() {
    return this.mapServer;
  }

  @Override
  public List<MapTile> getOverlappingMapTiles(final Viewport2D viewport) {
    final List<MapTile> tiles = new ArrayList<MapTile>();
    final MapServer mapServer = getMapServer();
    if (mapServer != null) {
      if (!isHasError()) {
        try {
          final double metresPerPixel = viewport.getUnitsPerPixel();
          final int zoomLevel = mapServer.getZoomLevel(metresPerPixel);
          final double resolution = getResolution(viewport);
          if (resolution > 0) {
            final BoundingBox viewBoundingBox = viewport.getBoundingBox();
            final BoundingBox maxBoundingBox = getBoundingBox();
            final BoundingBox boundingBox = viewBoundingBox.convert(this.geometryFactory)
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
                final ArcGisServerRestTileCacheMapTile tile = new ArcGisServerRestTileCacheMapTile(
                  this, mapServer, zoomLevel, resolution, tileX, tileY);
                tiles.add(tile);
              }
            }
          }
        } catch (final Throwable e) {
          setError(e);
        }
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
      final double metresPerPixel = viewport.getUnitsPerPixel();
      final int zoomLevel = mapServer.getZoomLevel(metresPerPixel);
      return mapServer.getResolution(zoomLevel);
    }
  }

  public String getUrl() {
    return this.url;
  }

  @Override
  protected boolean initializeDo() {
    synchronized (this.initSync) {
      if (this.mapServer == null) {
        try {
          this.mapServer = ArcGisServerRestClient.getMapServer(this.url);
          if (this.mapServer == null) {
            return false;
          } else {
            final TileInfo tileInfo = this.mapServer.getTileInfo();
            this.geometryFactory = tileInfo.getSpatialReference();
            return true;
          }
        } catch (final Throwable e) {
          throw Exceptions.wrap("Error connecting to ArcGIS rest server " + this.url, e);
        }
      } else {
        return true;
      }
    }
  }

  @Override
  protected ValueField newPropertiesTabGeneralPanelSource(final BasePanel parent) {
    final ValueField panel = super.newPropertiesTabGeneralPanelSource(parent);

    final String url = getUrl();
    SwingUtil.addLabelledReadOnlyTextField(panel, "URL", url);
    GroupLayouts.makeColumns(panel, 2, true);
    return panel;
  }

  @Override
  public void refreshDo() {
    initializeDo();
    super.refreshDo();
  }

  public void setUrl(final String url) {
    final Object oldValue = this.url;
    this.url = url;
    if (getName() == null) {
      final String name = CaseConverter.toCapitalizedWords(url.replaceAll(".+/rest/services/", "")//
        .replaceAll("/MapServer", " ")//
        .replaceAll("/", " "));
      setName(name);
    }
    firePropertyChange("url", oldValue, url);
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = super.toMap();
    addToMap(map, "url", this.url);
    return map;
  }

}
