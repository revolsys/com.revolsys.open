package com.revolsys.swing.map.layer.arcgisrest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.revolsys.collection.map.MapEx;
import com.revolsys.datatype.DataType;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.map.MapObjectFactoryRegistry;
import com.revolsys.logging.Logs;
import com.revolsys.record.io.format.esri.rest.map.MapService;
import com.revolsys.record.io.format.esri.rest.map.TileInfo;
import com.revolsys.spring.resource.UrlResource;
import com.revolsys.swing.Icons;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.field.TextField;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.AbstractTiledImageLayer;
import com.revolsys.swing.map.layer.BaseMapLayer;
import com.revolsys.swing.map.layer.BaseMapLayerGroup;
import com.revolsys.swing.map.layer.MapTile;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.menu.Menus;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.Exceptions;
import com.revolsys.util.PasswordUtil;
import com.revolsys.util.Property;

public class ArcGisRestServerTileCacheLayer extends AbstractTiledImageLayer {
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
        final ArcGisRestServerTileCacheLayer layer = new ArcGisRestServerTileCacheLayer();
        layer.setUrl(url);
        layer.setVisible(true);
        parent.addLayer(layer);
      }
    });

    dialog.showDialog();
  }

  public static void mapObjectFactoryInit() {
    MapObjectFactoryRegistry.newFactory("arcGisRestServerTileLayer",
      "Arc GIS REST Server Tile Cache Layer", ArcGisRestServerTileCacheLayer::new);

    MapObjectFactoryRegistry.newFactory("arcgisServerRest", "Arc GIS REST Server Tile Cache Layer",
      ArcGisRestServerTileCacheLayer::new);

    final MenuFactory baseMapsMenu = MenuFactory.getMenu(BaseMapLayerGroup.class);

    Menus.addMenuItem(baseMapsMenu, "group", "Add ArcGIS Tile Cache",
      Icons.getIconWithBadge("map", "add"), ArcGisRestServerTileCacheLayer::actionAddLayer, false);

    final MenuFactory tileInfoMenu = MenuFactory.getMenu(TileInfo.class);

    final Function<TileInfo, BaseMapLayer> baseMapLayerFactory = ArcGisRestServerTileCacheLayer::new;
    BaseMapLayer.addNewLayerMenu(tileInfoMenu, baseMapLayerFactory);
  }

  private String username;

  private String password;

  private GeometryFactory geometryFactory;

  private final Object initSync = new Object();

  private MapService mapServer;

  private String url;

  public ArcGisRestServerTileCacheLayer() {
    super("arcGisRestServerTileLayer");
  }

  public ArcGisRestServerTileCacheLayer(final Map<String, ? extends Object> properties) {
    this();
    setProperties(properties);
  }

  public ArcGisRestServerTileCacheLayer(final TileInfo tileInfo) {
    this();
    final MapService mapServer = tileInfo.getMapServer();
    final UrlResource serviceUrl = mapServer.getServiceUrl();
    setUrl(serviceUrl);
  }

  @Override
  public boolean equals(final Object other) {
    if (other instanceof ArcGisRestServerTileCacheLayer) {
      final ArcGisRestServerTileCacheLayer layer = (ArcGisRestServerTileCacheLayer)other;
      if (DataType.equal(layer.getUrl(), getUrl())) {
        return true;
      }
    }
    return false;
  }

  public MapService getMapServer() {
    return this.mapServer;
  }

  @Override
  public List<MapTile> getOverlappingMapTiles(final Viewport2D viewport) {
    final List<MapTile> tiles = new ArrayList<>();
    final MapService mapServer = getMapServer();
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
                final ArcGisRestServerTileCacheMapTile tile = new ArcGisRestServerTileCacheMapTile(
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
    final MapService mapServer = getMapServer();
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
          // TODO this isn't working
          this.mapServer = MapService.getMapServer(this.url);
          if (this.mapServer == null) {
            return false;
          } else {
            final TileInfo tileInfo = this.mapServer.getTileInfo();
            if (tileInfo == null) {
              Logs.info(this, this.url + " does not contain a tileInfo definition.");
              return false;
            } else {
              this.geometryFactory = tileInfo.getGeometryFactory();
              final BoundingBox boundingBox = this.mapServer.getFullExtent();
              setBoundingBox(boundingBox);
              return true;
            }
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

  public void setPassword(final String password) {
    this.password = PasswordUtil.decrypt(password);
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

  public void setUrl(final UrlResource url) {
    if (url != null) {
      setUrl(url.getUriString());
      this.username = url.getUsername();
      this.password = url.getPassword();
    }
  }

  public void setUsername(final String username) {
    this.username = username;
  }

  @Override
  public MapEx toMap() {
    final MapEx map = super.toMap();
    addToMap(map, "url", this.url);
    addToMap(map, "username", this.username);
    addToMap(map, "password", PasswordUtil.encrypt(this.password));
    return map;
  }

}
