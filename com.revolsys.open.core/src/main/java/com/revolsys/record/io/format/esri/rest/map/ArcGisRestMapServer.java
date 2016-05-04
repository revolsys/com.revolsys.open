package com.revolsys.record.io.format.esri.rest.map;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import com.revolsys.collection.Parent;
import com.revolsys.collection.map.Maps;
import com.revolsys.geometry.model.BoundingBox;
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

  private List<CatalogElement> children = null;

  private final Map<String, LayerDescription> rootLayersByName = new HashMap<>();

  private TileInfo tileInfo;

  private final Map<Integer, LayerDescription> layersById = new HashMap<>();

  protected ArcGisRestMapServer() {
    super("MapServer");
  }

  public ArcGisRestMapServer(final ArcGisRestCatalog catalog, final String servicePath) {
    super(catalog, servicePath, "MapServer");
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

    return new BoundingBoxDoubleGf(tileInfo.getSpatialReference(), 2, x1, y1, x2, y2);
  }

  public String getCapabilities() {
    return getValue("capabilities");
  }

  @Override
  @SuppressWarnings("unchecked")
  public <C extends CatalogElement> C getChild(final String name) {
    initChildren();
    return (C)this.rootLayersByName.get(name);
  }

  @Override
  public List<CatalogElement> getChildren() {
    initChildren();
    return this.children;
  }

  public String getCopyrightText() {
    return getValue("copyrightText");
  }

  public String getDescription() {
    return getValue("description");
  }

  public Map<String, String> getDocumentInfo() {
    return getValue("documentInfo");
  }

  public BoundingBox getFullExtent() {
    return getBoundingBox("fullExtent");
  }

  @Override
  public String getIconName() {
    return "folder:map";
  }

  public BoundingBox getInitialExtent() {
    return getBoundingBox("initialExtent");
  }

  @SuppressWarnings("unchecked")
  public <L extends LayerDescription> L getLayer(final PathName pathName) {
    initChildren();
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
    return getValue("mapName");
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

  public Boolean getSingleFusedMapCache() {
    return getValue("singleFusedMapCache");
  }

  public String getSupportedImageFormatTypes() {
    return getValue("supportedImageFormatTypes");
  }

  public List<TableDescription> getTables() {
    return getList(TableDescription.class, "tables");
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
    if (this.tileInfo == null) {
      this.tileInfo = getObject(TileInfo.class, "tileInfo");
      if (this.tileInfo != null) {
        this.tileInfo.setMapServer(this);
      }
    }
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

  public TimeInfo getTimeInfo() {
    return getObject(TimeInfo.class, "timeInfo");
  }

  public String getUnits() {
    return getValue("units");
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
  private void initChildren() {
    synchronized (this.rootLayersByName) {
      if (this.children == null) {
        final List<CatalogElement> children = new ArrayList<>();

        final TileInfo tileCache = getTileInfo();
        if (tileCache != null) {
          children.add(tileCache);
        }
        Map<Integer, LayerDescription> layersById = this.layersById;
        layersById = new HashMap<>();
        final Map<LayerGroupDescription, List<Number>> layerGroups = new HashMap<>();
        for (final Map<String, Object> layerProperties : (List<Map<String, Object>>)getValue(
          "layers")) {
          final Integer parentLayerId = Maps.getInteger(layerProperties, "parentLayerId");
          final Integer id = Maps.getInteger(layerProperties, "id");
          final String name = (String)layerProperties.get("name");
          LayerDescription layer;
          final List<Number> subLayerIds = (List<Number>)layerProperties.get("subLayerIds");
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
            this.rootLayersByName.put(name, layer);
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
        this.children = children;
      }
    }
  }
}
