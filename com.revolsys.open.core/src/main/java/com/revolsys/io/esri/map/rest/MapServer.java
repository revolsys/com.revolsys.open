package com.revolsys.io.esri.map.rest;

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.io.esri.map.rest.map.LayerDescription;
import com.revolsys.io.esri.map.rest.map.LevelOfDetail;
import com.revolsys.io.esri.map.rest.map.TableDescription;
import com.revolsys.io.esri.map.rest.map.TileInfo;
import com.revolsys.io.esri.map.rest.map.TimeInfo;

public class MapServer extends Service {

  protected MapServer() {
    super("MapServer");
  }

  public String getCapabilities() {
    return getValue("capabilities");
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

  public BoundingBox getInitialExtent() {
    return getBoundingBox("initialExtent");
  }

  public List<LayerDescription> getLayers() {
    return getList(LayerDescription.class, "layers");
  }

  public String getMapName() {
    return getValue("mapName");
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

  public TileInfo getTileInfo() {
    return getObject(TileInfo.class, "tileInfo");
  }

  public TimeInfo getTimeInfo() {
    return getObject(TimeInfo.class, "timeInfo");
  }

  public String getUnits() {
    return getValue("units");
  }

  public int getTileX(final int zoomLevel, final double x) {
    TileInfo tileInfo = getTileInfo();
    double modelTileSize = tileInfo.getModelWidth(zoomLevel);
    double originX = tileInfo.getOriginX();
    double deltaX = x - originX;
    double ratio = deltaX / modelTileSize;
    int tileX = (int)Math.floor(ratio);
    if (tileX > 0 && ratio - tileX < 0.0001) {
      tileX--;
    }
    return tileX;
  }

  public int getTileY(final int zoomLevel, final double y) {
    TileInfo tileInfo = getTileInfo();
    double modelTileSize = tileInfo.getModelHeight(zoomLevel);

    double originY = tileInfo.getOriginY();
    double deltaY = originY - y;

    double ratio = deltaY / modelTileSize;
    int tileY = (int)Math.floor(ratio);
    return tileY;
  }

  public BoundingBox getBoundingBox(final int zoomLevel, final int tileX,
    final int tileY) {
    TileInfo tileInfo = getTileInfo();

    double originX = tileInfo.getOriginX();
    double tileWidth = tileInfo.getModelWidth(zoomLevel);
    double x1 = originX + tileWidth * tileX;
    double x2 = x1 + tileWidth;

    double originY = tileInfo.getOriginY();
    double tileHeight = tileInfo.getModelHeight(zoomLevel);
    double y1 = originY - tileHeight * tileY;
    double y2 = y1 - tileHeight;

    return new BoundingBox(tileInfo.getSpatialReference(), x1, y1, x2, y2);
  }

  public int getZoomLevel(final double metresPerPixel) {
    TileInfo tileInfo = getTileInfo();
    List<LevelOfDetail> levelOfDetails = tileInfo.getLevelOfDetails();
    Integer minLevel = levelOfDetails.get(0).getLevel();
    for (LevelOfDetail levelOfDetail : levelOfDetails) {
      final double zoomLevelMetresPerPixel = levelOfDetail.getResolution();
      if (metresPerPixel > zoomLevelMetresPerPixel) {
        Integer level = levelOfDetail.getLevel();
        return Math.max(level, minLevel);
      }
    }
    return levelOfDetails.get(levelOfDetails.size() - 1).getLevel();
  }

  public Image getTileImage(int zoomLevel, int tileX, int tileY) {
    final String url = getTileUrl(zoomLevel, tileX, tileY);
    try {
      final URLConnection connection = new URL(url).openConnection();
      final InputStream in = connection.getInputStream();
      return ImageIO.read(in);
    } catch (final IOException e) {
      throw new RuntimeException("Unable to download image from: " + url, e);
    }
  }

  public String getTileUrl(int zoomLevel, int tileX, int tileY) {
    return getServiceUrl() + getPath() + "/tile/" + zoomLevel + "/" + tileY
      + "/" + tileX;
  }
}
