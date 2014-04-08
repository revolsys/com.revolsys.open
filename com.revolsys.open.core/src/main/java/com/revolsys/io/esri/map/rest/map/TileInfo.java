package com.revolsys.io.esri.map.rest.map;

import java.util.List;
import java.util.Map;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.io.esri.map.rest.AbstractMapWrapper;
import com.revolsys.util.CollectionUtil;
import com.revolsys.jts.geom.Point;

public class TileInfo extends AbstractMapWrapper {
  private double originX = Double.NaN;

  private double originY = Double.NaN;

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

  public Point getOriginPoint() {
    final GeometryFactory spatialReference = getSpatialReference();
    final Coordinates origin = getOrigin();
    return spatialReference.createPoint(origin);
  }

  public double getOriginX() {
    return originX;
  }

  public double getOriginY() {
    return originY;
  }

  public double getPixelSize() {
    final int dpi = getDpi();
    final double pixelSize = 0.0254 / dpi;
    return pixelSize;
  }

  public Integer getWidth() {
    return getIntValue("cols");
  }

  @Override
  protected void setValues(final Map<String, Object> values) {
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
}
