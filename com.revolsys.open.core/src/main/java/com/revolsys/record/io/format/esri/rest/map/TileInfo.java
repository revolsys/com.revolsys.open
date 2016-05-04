package com.revolsys.record.io.format.esri.rest.map;

import java.util.List;
import java.util.Map;

import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.map.Maps;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDouble;
import com.revolsys.record.io.format.esri.rest.ArcGisResponse;
import com.revolsys.record.io.format.esri.rest.CatalogElement;

public class TileInfo extends ArcGisResponse implements CatalogElement {
  private double originX = Double.NaN;

  private double originY = Double.NaN;

  private ArcGisRestMapServer mapServer;

  public TileInfo() {
  }

  public Integer getCompressionQuality() {
    return getIntValue("compressionQuality");
  }

  public Integer getDpi() {
    return getIntValue("dpi");
  }

  public String getFormat() {
    return getValue("format");
  }

  public Integer getHeight() {
    return getIntValue("rows");
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
    return getList(LevelOfDetail.class, "lods");
  }

  public ArcGisRestMapServer getMapServer() {
    return this.mapServer;
  }

  public double getModelHeight(final int zoomLevel) {
    return getModelValue(zoomLevel, getHeight());
  }

  public double getModelValue(final int zoomLevel, final int pixels) {
    final LevelOfDetail levelOfDetail = getLevelOfDetail(zoomLevel);
    final double modelValue = pixels * levelOfDetail.getResolution();
    return modelValue;
  }

  public double getModelWidth(final int zoomLevel) {
    return getModelValue(zoomLevel, getWidth());
  }

  @Override
  public String getName() {
    return "Tile Cache";
  }

  public Point getOrigin() {
    final Map<String, Object> origin = getValue("origin");
    if (origin == null) {
      return null;
    } else {
      final Double x = Maps.getDoubleValue(origin, "x");
      final Double y = Maps.getDoubleValue(origin, "y");
      return new PointDouble(x, y);
    }
  }

  public Point getOriginPoint() {
    final GeometryFactory spatialReference = getSpatialReference();
    final Point origin = getOrigin();
    return spatialReference.point(origin);
  }

  public double getOriginX() {
    return this.originX;
  }

  public double getOriginY() {
    return this.originY;
  }

  @Override
  public ArcGisRestMapServer getParent() {
    return this.mapServer;
  }

  public double getPixelSize() {
    final int dpi = getDpi();
    final double pixelSize = 0.0254 / dpi;
    return pixelSize;
  }

  public Integer getWidth() {
    return getIntValue("cols");
  }

  public void setMapServer(final ArcGisRestMapServer mapServer) {
    this.mapServer = mapServer;
  }

  @Override
  protected void setValues(final MapEx values) {
    super.setValues(values);
    final Map<String, Object> origin = getValue("origin");
    if (origin == null) {
      this.originX = Double.NaN;
      this.originY = Double.NaN;
    } else {
      this.originX = Maps.getDoubleValue(origin, "x");
      this.originY = Maps.getDoubleValue(origin, "y");
    }
  }
}
