package com.revolsys.record.io.format.esri.rest.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.record.io.format.esri.rest.AbstractMapWrapper;
import com.revolsys.record.io.format.esri.rest.CatalogElement;

public class TileInfo extends AbstractMapWrapper implements CatalogElement {
  private double originX = Double.NaN;

  private double originY = Double.NaN;

  private ArcGisRestMapService mapServer;

  private GeometryFactory geometryFactory;

  private int compressionQuality;

  private int dpi;

  private String format;

  private int rows;

  private int cols;

  private List<LevelOfDetail> levelOfDetails = new ArrayList<>();

  public TileInfo() {
  }

  public int getCols() {
    return this.cols;
  }

  public int getCompressionQuality() {
    return this.compressionQuality;
  }

  public int getDpi() {
    return this.dpi;
  }

  public String getFormat() {
    return this.format;
  }

  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  @Override
  public String getIconName() {
    return "map";
  }

  public LevelOfDetail getLevelOfDetail(final int zoomLevel) {
    final List<LevelOfDetail> levelOfDetails = getLevelOfDetails();
    for (final LevelOfDetail levelOfDetail : levelOfDetails) {
      final Integer level = levelOfDetail.getLevel();
      if (level == zoomLevel) {
        return levelOfDetail;
      }
    }
    return null;
  }

  public List<LevelOfDetail> getLevelOfDetails() {
    return this.levelOfDetails;
  }

  public ArcGisRestMapService getMapServer() {
    return this.mapServer;
  }

  public double getModelHeight(final int zoomLevel) {
    return getModelValue(zoomLevel, getRows());
  }

  public double getModelValue(final int zoomLevel, final int pixels) {
    final LevelOfDetail levelOfDetail = getLevelOfDetail(zoomLevel);
    final double modelValue = pixels * levelOfDetail.getResolution();
    return modelValue;
  }

  public double getModelWidth(final int zoomLevel) {
    return getModelValue(zoomLevel, getCols());
  }

  @Override
  public String getName() {
    return "Tile Cache";
  }

  public Point getOriginPoint() {
    if (Double.isNaN(this.originX)) {
      return null;
    } else {
      final GeometryFactory spatialReference = getGeometryFactory();
      return spatialReference.point(this.originX, this.originY);
    }
  }

  public double getOriginX() {
    return this.originX;
  }

  public double getOriginY() {
    return this.originY;
  }

  @Override
  public ArcGisRestMapService getParent() {
    return this.mapServer;
  }

  public double getPixelSize() {
    final int dpi = getDpi();
    final double pixelSize = 0.0254 / dpi;
    return pixelSize;
  }

  public int getRows() {
    return this.rows;
  }

  @Override
  public String getServiceUrl() {
    return this.mapServer.getServiceUrl() + "/tile";
  }

  public void setCols(final int cols) {
    this.cols = cols;
  }

  public void setCompressionQuality(final int compressionQuality) {
    this.compressionQuality = compressionQuality;
  }

  public void setDpi(final int dpi) {
    this.dpi = dpi;
  }

  public void setFormat(final String format) {
    this.format = format;
  }

  public void setMapServer(final ArcGisRestMapService mapServer) {
    this.mapServer = mapServer;
  }

  @Override
  public void setProperties(final Map<String, ? extends Object> values) {
    super.setProperties(values);
    final MapEx origin = (MapEx)values.get("origin");
    if (origin == null) {
      this.originX = Double.NaN;
      this.originY = Double.NaN;
    } else {
      this.originX = origin.getDouble("x");
      this.originY = origin.getDouble("y");
    }
    this.geometryFactory = newGeometryFactory((MapEx)values, "spatialReference");
    this.levelOfDetails = newList(LevelOfDetail.class, (MapEx)values, "lods");
  }

  public void setRows(final int rows) {
    this.rows = rows;
  }
}
