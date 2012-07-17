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

  public Integer getRows() {
    return getIntValue("rows");
  }

  public Integer getCols() {
    return getIntValue("cols");
  }

  public Integer getDpi() {
    return getIntValue("dpi");
  }

  public String getFormat() {
    return getValue("format");
  }

  public Integer getCompressionQuality() {
    return getIntValue("compressionQuality");
  }

  public Coordinates getOrigin() {
    Map<String, Object> origin = getValue("origin");
    if (origin == null) {
      return null;
    } else {
      Double x = CollectionUtil.getDoubleValue(origin, "x");
      Double y = CollectionUtil.getDoubleValue(origin, "y");
      return new DoubleCoordinates(x, y);
    }
  }

  public Point getOriginPoint() {
    GeometryFactory spatialReference = getSpatialReference();
    Coordinates origin = getOrigin();
    return spatialReference.createPoint(origin);
  }

  public List<LevelOfDetail> getLods() {
    return getList(LevelOfDetail.class, "lods");
  }

}
