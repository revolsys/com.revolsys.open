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

  public Integer getCols() {
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

  public List<LevelOfDetail> getLods() {
    return getList(LevelOfDetail.class, "lods");
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

  public Integer getRows() {
    return getIntValue("rows");
  }

}
