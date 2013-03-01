package com.revolsys.io.esri.map.rest.map;

import java.util.List;
import java.util.Map;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.io.esri.map.rest.AbstractMapWrapper;
import com.revolsys.util.CollectionUtil;
import com.vividsolutions.jts.geom.Point;

public class TileInfo extends AbstractMapWrapper {
  public TileInfo() {
  }

  public Integer getWidth() {
    return getIntValue("cols");
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

  public List<LevelOfDetail> getLevelOfDetails() {
    return getList(LevelOfDetail.class, "lods");
  }

  public double getModelValue(int zoomLevel, int pixels) {
    double pixelSize = getPixelSize();
    LevelOfDetail levelOfDetail = getLevelOfDetail(zoomLevel);
    Double scale = levelOfDetail.getScale();
    double modelValue = pixels * pixelSize * scale;
    return modelValue;
  }

  public double getModelWidth(int zoomLevel) {
    return getModelValue(zoomLevel, getWidth());
  }

  public double getModelHeight(int zoomLevel) {
    return getModelValue(zoomLevel, getHeight());
  }

  public double getPixelSize() {
    int dpi = getDpi();
    double pixelSize = 0.0254 / dpi;
    return pixelSize;
  }

  public Coordinates getOrigin() {
    final Map<String, Object> origin = getValue("origin");
    if (origin == null) {
      return null;
    } else {
      final Double x = CollectionUtil.getDoubleValue(origin, "x");
      final Double y = CollectionUtil.getDoubleValue(origin, "y");
      return new DoubleCoordinates(x, y);
    }
  }

  public LevelOfDetail getLevelOfDetail(final int zoomLevel) {
    List<LevelOfDetail> levelOfDetails = getLevelOfDetails();
    for (LevelOfDetail levelOfDetail : levelOfDetails) {
      Integer level = levelOfDetail.getLevel();
      if (level == zoomLevel) {
        return levelOfDetail;
      }
    }
    return null;
  }

  @Override
  protected void setValues(Map<String, Object> values) {
    super.setValues(values);
    final Map<String, Object> origin = getValue("origin");
    if (origin == null) {
      originX = Double.NaN;
      originY = Double.NaN;
    } else {
      originX = CollectionUtil.getDoubleValue(origin, "x");
      originY = CollectionUtil.getDoubleValue(origin, "y");
    }
  }

  private double originX = Double.NaN;

  private double originY = Double.NaN;

  public double getOriginX() {
    return originX;
  }

  public double getOriginY() {
    return originY;
  }

  public Point getOriginPoint() {
    final GeometryFactory spatialReference = getSpatialReference();
    final Coordinates origin = getOrigin();
    return spatialReference.createPoint(origin);
  }

  public Integer getHeight() {
    return getIntValue("rows");
  }

}
