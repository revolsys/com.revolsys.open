package com.revolsys.swing.map.layer.arcgisrest;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.exception.WrappedException;
import org.jeometry.common.io.PathName;
import org.jeometry.common.logging.Logs;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.record.io.format.esri.rest.ArcGisRestCatalog;
import com.revolsys.record.io.format.esri.rest.map.MapService;
import com.revolsys.record.io.format.esri.rest.map.TileInfo;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.spring.resource.UrlResource;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.swing.map.layer.raster.AbstractTiledGeoreferencedImageLayer;
import com.revolsys.swing.map.layer.tile.AbstractTiledLayerRenderer;
import com.revolsys.swing.map.view.ViewRenderer;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.PasswordUtil;
import com.revolsys.util.Property;
import com.revolsys.webservice.WebService;
import com.revolsys.webservice.WebServiceConnectionManager;
import com.revolsys.webservice.WebServiceResource;

public class ArcGisRestServerTileCacheLayer
  extends AbstractTiledGeoreferencedImageLayer<ArcGisRestServerTileCacheMapTile> {

  static {
    MenuFactory.addMenuInitializer(ArcGisRestServerTileCacheLayer.class, menu -> {

      menu.addGroup(3, "server");

      menu.<ArcGisRestServerTileCacheLayer> addCheckboxMenuItem("server", "Project image on Server",
        "", ArcGisRestServerTileCacheLayer::isExportTilesAllowed,
        ArcGisRestServerTileCacheLayer::toggleUseServerExport,
        ArcGisRestServerTileCacheLayer::isUseServerExport, true);

    });
  }

  private boolean useServerExport = false;

  private String username;

  private String password;

  private final Object initSync = new Object();

  private MapService mapService;

  private String url;

  private PathName servicePath;

  private String connectionName;

  public ArcGisRestServerTileCacheLayer() {
    super("arcGisRestServerTileLayer");
  }

  public ArcGisRestServerTileCacheLayer(final Map<String, ? extends Object> properties) {
    this();
    setProperties(properties);
  }

  public ArcGisRestServerTileCacheLayer(final TileInfo tileInfo) {
    this();
    final MapService mapService = tileInfo.getMapService();
    final WebService<?> webService = mapService.getWebService();
    if (webService instanceof ArcGisRestCatalog) {
      this.connectionName = webService.getName();
      this.servicePath = mapService.getPathName();
    } else {
      final UrlResource serviceUrl = mapService.getServiceUrl();
      setUrl(serviceUrl);
    }
    setName(mapService.getParent().getName());
  }

  @Override
  public boolean equals(final Object other) {
    if (other instanceof ArcGisRestServerTileCacheLayer) {
      final ArcGisRestServerTileCacheLayer layer = (ArcGisRestServerTileCacheLayer)other;
      if (DataType.equal(layer.connectionName, this.connectionName)) {
        if (DataType.equal(layer.servicePath, this.servicePath)) {
          if (DataType.equal(layer.url, this.url)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public String getConnectionName() {
    return this.connectionName;
  }

  public MapService getMapService() {
    return this.mapService;
  }

  @Override
  public List<ArcGisRestServerTileCacheMapTile> getOverlappingMapTiles(
    final AbstractTiledLayerRenderer<?, ?> renderer, final ViewRenderer view) {
    final List<ArcGisRestServerTileCacheMapTile> tiles = new ArrayList<>();
    final MapService mapService = getMapService();
    if (mapService != null) {
      try {
        final double viewResolution = view.getMetresPerPixel();
        final int zoomLevel = mapService.getZoomLevel(viewResolution);
        final double resolution = mapService.getResolution(zoomLevel);
        if (resolution > 0) {
          final BoundingBox viewBoundingBox = view.getBoundingBox();
          final BoundingBox maxBoundingBox = getBoundingBox();
          final BoundingBox boundingBox = viewBoundingBox.bboxToCs(this)
            .bboxIntersection(maxBoundingBox);
          final double minX = boundingBox.getMinX();
          final double minY = boundingBox.getMinY();
          final double maxX = boundingBox.getMaxX();
          final double maxY = boundingBox.getMaxY();

          // Tiles start at the North-West corner of the map
          final int minTileX = mapService.getTileX(zoomLevel, minX);
          final int minTileY = mapService.getTileY(zoomLevel, maxY);
          final int maxTileX = mapService.getTileX(zoomLevel, maxX);
          final int maxTileY = mapService.getTileY(zoomLevel, minY);

          for (int tileY = minTileY; tileY <= maxTileY; tileY++) {
            for (int tileX = minTileX; tileX <= maxTileX; tileX++) {
              final ArcGisRestServerTileCacheMapTile tile = new ArcGisRestServerTileCacheMapTile(
                this, mapService, zoomLevel, resolution, tileX, tileY);
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
  public double getResolution(final ViewRenderer view) {
    final MapService mapService = getMapService();
    if (mapService == null) {
      return 0;
    } else {
      final double metresPerPixel = view.getMetresPerPixel();
      final int zoomLevel = mapService.getZoomLevel(metresPerPixel);
      return mapService.getResolution(zoomLevel);
    }
  }

  public PathName getServicePath() {
    return this.servicePath;
  }

  public String getUrl() {
    return this.url;
  }

  @Override
  protected boolean initializeDo() {
    synchronized (this.initSync) {
      if (this.mapService == null) {
        try {
          if (Property.hasValue(this.connectionName)) {
            final WebService<?> webService = WebServiceConnectionManager
              .getWebService(this.connectionName);
            if (webService instanceof ArcGisRestCatalog) {
              final ArcGisRestCatalog catalog = (ArcGisRestCatalog)webService;
              final WebServiceResource service = catalog.getWebServiceResource(this.servicePath);
              if (service instanceof MapService) {
                this.mapService = (MapService)service;
              } else {
                Logs.error(this,
                  getPath() + ": Web service " + this.connectionName + " is not a ArcGIS service");
                return false;
              }
            } else {
              Logs.error(this, getPath() + ": Web service " + this.connectionName + " "
                + this.servicePath + " is not a ArcGIS Map service");
              return false;
            }
          } else {
            // TODO username/password
            this.mapService = MapService.getMapService(this.url);
          }
          if (this.mapService == null) {
            Logs.error(this, "Unable to connect to ArcGIS rest server");
            return false;
          } else {
            final TileInfo tileInfo = this.mapService.getTileInfo();
            if (tileInfo == null) {
              Logs.info(this, this.url + " does not contain a tileInfo definition.");
              return false;
            } else {
              if (this.useServerExport && !this.mapService.isExportTilesAllowed()) {
                this.useServerExport = false;
              }

              final GeometryFactory geometryFactory = tileInfo.getGeometryFactory();
              setGeometryFactory(geometryFactory);
              final BoundingBox boundingBox = this.mapService.getFullExtent();
              setBoundingBox(boundingBox);
              return true;
            }
          }
        } catch (final WrappedException e) {
          final Throwable cause = Exceptions.unwrap(e);
          if (cause instanceof UnknownHostException) {
            // Logs.error(this, getPath() + "Unknown host: " +
            // cause.getMessage());
            return setNotExists("Unknown host: " + cause.getMessage());
          } else {
            throw e;
          }
        }
      } else {
        return true;
      }
    }
  }

  public boolean isExportTilesAllowed() {
    if (this.mapService == null) {
      return false;
    }
    return this.mapService.isExportTilesAllowed();
  }

  public boolean isUseServerExport() {
    return this.useServerExport;
  }

  @Override
  protected ValueField newPropertiesTabGeneralPanelSource(final BasePanel parent) {
    final ValueField panel = super.newPropertiesTabGeneralPanelSource(parent);

    final String url = getUrl();
    if (Property.hasValue(url)) {
      SwingUtil.addLabelledReadOnlyTextField(panel, "URL", url);
    }
    if (this.mapService != null) {
      SwingUtil.addLabelledReadOnlyTextField(panel, "Service URL", this.mapService.getServiceUrl());
    }
    SwingUtil.addLabelledReadOnlyTextField(panel, "Service Path", this.servicePath);
    GroupLayouts.makeColumns(panel, panel.getComponentCount() / 2, true);
    return panel;
  }

  @Override
  protected ArcGisRestTileCacheLayerRenderer newRenderer() {
    return new ArcGisRestTileCacheLayerRenderer(this);
  }

  @Override
  public void refreshDo() {
    initializeForce();
    super.refreshDo();
  }

  public void setConnectionName(final String connectionName) {
    this.connectionName = connectionName;
  }

  public void setPassword(final String password) {
    this.password = PasswordUtil.decrypt(password);
  }

  public void setServicePath(final PathName servicePath) {
    this.servicePath = servicePath;
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

  public void setUseServerExport(final boolean useServerExport) {
    final boolean oldValue = this.useServerExport;
    this.useServerExport = useServerExport;
    firePropertyChange("useServerExport", oldValue, useServerExport);
  }

  public void toggleUseServerExport() {
    final boolean useServerExport = isUseServerExport();
    setUseServerExport(!useServerExport);
  }

  @Override
  public JsonObject toMap() {
    final JsonObject map = super.toMap();
    if (Property.hasValue(this.connectionName)) {
      addToMap(map, "connectionName", this.connectionName);
      addToMap(map, "servicePath", this.servicePath);
    } else {
      addToMap(map, "url", this.url);
      addToMap(map, "username", this.username);
      addToMap(map, "password", PasswordUtil.encrypt(this.password));
    }
    addToMap(map, "useServerExport", this.useServerExport, false);
    return map;
  }

}
