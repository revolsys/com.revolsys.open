package com.revolsys.record.io.format.esri.rest.map;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import com.revolsys.collection.Parent;
import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;
import com.revolsys.io.PathName;
import com.revolsys.record.io.format.esri.rest.ArcGisRestCatalog;
import com.revolsys.record.io.format.esri.rest.ArcGisRestService;
import com.revolsys.record.io.format.esri.rest.CatalogElement;
import com.revolsys.util.Debug;
import com.revolsys.util.Property;
import com.revolsys.util.UrlUtil;
import com.revolsys.util.WrappedException;

public class ArcGisRestMapServer extends ArcGisRestService implements Parent<CatalogElement> {
  public static ArcGisRestMapServer getMapServer(String url) {
    url = url.replaceAll("/*MapServer/*(\\?.*)?", "");
    final String baseUrl = UrlUtil.getParent(url);
    final String name = UrlUtil.getFileName(url);
    final ArcGisRestCatalog arcGisRestCatalog = new ArcGisRestCatalog(baseUrl);
    final ArcGisRestMapServer service = arcGisRestCatalog.getService(name,
      ArcGisRestMapServer.class);
    return service;
  }

  private List<CatalogElement> children = Collections.emptyList();

  private Map<String, LayerDescription> rootLayersByName = Collections.emptyMap();

  private TileInfo tileInfo;

  private String capabilities;

  private String copyrightText;

  private String description;

  private MapEx documentInfo;

  private String mapName;

  private String supportedImageFormatTypes;

  private boolean singleFusedMapCache;

  private String units;

  private BoundingBox boundingBox = BoundingBox.EMPTY;

  List<TableDescription> tables = new ArrayList<>();

  protected ArcGisRestMapServer() {
    super("MapServer");
  }

  public ArcGisRestMapServer(final ArcGisRestCatalog catalog, final String servicePath) {
    super(catalog, servicePath, "MapServer");
  }

  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  public BoundingBoxDoubleGf getBoundingBox(final int zoomLevel, final int tileX, final int tileY) {
    final TileInfo tileInfo = getTileInfo();

    final double originX = tileInfo.getOriginX();
    final double tileWidth = tileInfo.getModelWidth(zoomLevel);
    final double x1 = originX + tileWidth * tileX;
    final double x2 = x1 + tileWidth;

    final double originY = tileInfo.getOriginY();
    final double tileHeight = tileInfo.getModelHeight(zoomLevel);
    final double y1 = originY - tileHeight * tileY;
    final double y2 = y1 - tileHeight;

    final GeometryFactory geometryFactory = tileInfo.getGeometryFactory();
    return new BoundingBoxDoubleGf(geometryFactory, 2, x1, y1, x2, y2);
  }

  public String getCapabilities() {
    return this.capabilities;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <C extends CatalogElement> C getChild(final String name) {
    refreshIfNeeded();
    return (C)this.rootLayersByName.get(name);
  }

  @Override
  public List<CatalogElement> getChildren() {
    refreshIfNeeded();
    return this.children;
  }

  public String getCopyrightText() {
    return this.copyrightText;
  }

  public String getDescription() {
    return this.description;
  }

  public MapEx getDocumentInfo() {
    return this.documentInfo;
  }

  @Override
  public String getIconName() {
    return "folder:map";
  }

  @SuppressWarnings("unchecked")
  public <L extends LayerDescription> L getLayer(final PathName pathName) {
    refreshIfNeeded();
    final List<String> elements = pathName.getElements();
    if (!elements.isEmpty()) {
      LayerDescription layer = this.rootLayersByName.get(elements.get(0));
      for (int i = 1; layer != null && i < elements.size(); i++) {
        if (layer instanceof LayerGroupDescription) {
          final LayerGroupDescription layerGroup = (LayerGroupDescription)layer;
          final String childLayerName = elements.get(i);
          layer = layerGroup.getLayer(childLayerName);
        } else {
          return null;
        }

      }
      return (L)layer;
    }
    return null;
  }

  public String getMapName() {
    return this.mapName;
  }

  public double getResolution(final int zoomLevel) {
    final TileInfo tileInfo = getTileInfo();
    final List<LevelOfDetail> levelOfDetails = tileInfo.getLevelOfDetails();
    for (final LevelOfDetail levelOfDetail : levelOfDetails) {
      final int level = levelOfDetail.getLevel();
      if (level == zoomLevel) {
        return levelOfDetail.getResolution();
      }
    }
    return 0;
  }

  public boolean getSingleFusedMapCache() {
    return this.singleFusedMapCache;
  }

  public String getSupportedImageFormatTypes() {
    return this.supportedImageFormatTypes;
  }

  public List<TableDescription> getTables() {
    return this.tables;
  }

  public BufferedImage getTileImage(final int zoomLevel, final int tileX, final int tileY) {
    final String url = getTileUrl(zoomLevel, tileX, tileY);
    try {

      final URLConnection connection = new URL(url).openConnection();
      final InputStream in = connection.getInputStream();
      return ImageIO.read(in);
    } catch (final FileNotFoundException e) {
      return null;
    } catch (final IOException e) {
      throw new WrappedException(e);
    }
  }

  public TileInfo getTileInfo() {
    refreshIfNeeded();
    return this.tileInfo;
  }

  public String getTileUrl(final int zoomLevel, final int tileX, final int tileY) {
    return getServiceUrl() + getPath() + "/tile/" + zoomLevel + "/" + tileY + "/" + tileX;
  }

  public int getTileX(final int zoomLevel, final double x) {
    final TileInfo tileInfo = getTileInfo();
    final double modelTileSize = tileInfo.getModelWidth(zoomLevel);
    final double originX = tileInfo.getOriginX();
    final double deltaX = x - originX;
    final double ratio = deltaX / modelTileSize;
    int tileX = (int)Math.floor(ratio);
    if (tileX > 0 && ratio - tileX < 0.0001) {
      tileX--;
    }
    return tileX;
  }

  public int getTileY(final int zoomLevel, final double y) {
    final TileInfo tileInfo = getTileInfo();
    final double modelTileSize = tileInfo.getModelHeight(zoomLevel);

    final double originY = tileInfo.getOriginY();
    final double deltaY = originY - y;

    final double ratio = deltaY / modelTileSize;
    final int tileY = (int)Math.floor(ratio);
    return tileY;
  }

  public String getUnits() {
    return this.units;
  }

  public int getZoomLevel(final double metresPerPixel) {
    final TileInfo tileInfo = getTileInfo();
    final List<LevelOfDetail> levelOfDetails = tileInfo.getLevelOfDetails();
    LevelOfDetail previousLevel = levelOfDetails.get(0);
    for (final LevelOfDetail levelOfDetail : levelOfDetails) {
      final double levelMetresPerPixel = levelOfDetail.getResolution();
      if (metresPerPixel > levelMetresPerPixel) {
        if (levelOfDetail == previousLevel) {
          return levelOfDetail.getLevel();
        } else {
          final double previousLevelMetresPerPixel = previousLevel.getResolution();
          final double range = levelMetresPerPixel - previousLevelMetresPerPixel;
          final double ratio = (metresPerPixel - previousLevelMetresPerPixel) / range;
          if (ratio < 0.8) {
            return previousLevel.getLevel();
          } else {
            return levelOfDetail.getLevel();
          }
        }
      }
      previousLevel = levelOfDetail;
    }
    return previousLevel.getLevel();
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void initialize(final MapEx properties) {
    this.boundingBox = newBoundingBox(properties, "fullExtent");
    final List<CatalogElement> children = new ArrayList<>();
    final Map<String, LayerDescription> rootLayersByName = new HashMap<>();

    this.tileInfo = newObject(TileInfo.class, properties, "tileInfo");
    if (this.tileInfo != null) {
      this.tileInfo.setMapServer(this);
      children.add(this.tileInfo);
    }
    final Map<Integer, LayerDescription> layersById = new HashMap<>();
    final Map<LayerGroupDescription, List<Number>> layerGroups = new HashMap<>();
    for (final MapEx layerProperties : (List<MapEx>)properties.getValue("layers")) {
      final Integer parentLayerId = layerProperties.getInteger("parentLayerId");
      final Integer id = layerProperties.getInteger("id");
      final String name = layerProperties.getString("name");
      LayerDescription layer;
      final List<Number> subLayerIds = (List<Number>)layerProperties.getValue("subLayerIds");
      if (Property.hasValue(subLayerIds)) {
        final LayerGroupDescription layerGroup = new LayerGroupDescription(this, id, name);
        layerGroups.put(layerGroup, subLayerIds);
        layer = layerGroup;
      } else {
        layer = new RecordLayerDescription(this, id, name);
      }
      layersById.put(id, layer);
      if (parentLayerId == -1) {
        children.add(layer);
        rootLayersByName.put(name, layer);
      }
    }
    for (final Entry<LayerGroupDescription, List<Number>> entry : layerGroups.entrySet()) {
      final LayerGroupDescription layerGroup = entry.getKey();
      final List<Number> subLayerIds = entry.getValue();
      final List<LayerDescription> layers = new ArrayList<>();
      for (final Number layerId : subLayerIds) {
        final LayerDescription layer = layersById.get(layerId.intValue());
        if (layer == null) {
          Debug.noOp();
        } else {
          layer.setParent(layerGroup);
          layers.add(layer);
        }
      }
      layerGroup.setLayers(layers);
    }
    this.children = Collections.unmodifiableList(children);
    this.rootLayersByName = Collections.unmodifiableMap(rootLayersByName);
    super.initialize(properties);
  }

  public void setCapabilities(final String capabilities) {
    this.capabilities = capabilities;
  }

  public void setCopyrightText(final String copyrightText) {
    this.copyrightText = copyrightText;
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  public void setMapName(final String mapName) {
    this.mapName = mapName;
  }

  @Override
  public void setProperties(final Map<String, ? extends Object> values) {
    super.setProperties(values);
    this.tables = newList(TableDescription.class, (MapEx)values, "tables");
  }

  public void setSupportedImageFormatTypes(final String supportedImageFormatTypes) {
    this.supportedImageFormatTypes = supportedImageFormatTypes;
  }

  public void setUnits(final String units) {
    this.units = units;
  }
}
