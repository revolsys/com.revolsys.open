package com.revolsys.gis.data.model.filter;

import com.revolsys.filter.Filter;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.jts.geom.Geometry;

public class GeometryValueFilter implements Filter<DataObject> {
  private final Geometry geometry;

  public GeometryValueFilter(final DataObject object) {
    this(object.getGeometryValue());
  }

  public GeometryValueFilter(final Geometry geometry) {
    this.geometry = geometry;
  }

  @Override
  public boolean accept(final DataObject object) {
    final Geometry value = object.getGeometryValue();
    if (value == geometry) {
      return true;
    } else if (value != null && geometry != null) {
      return value.equals(geometry);
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return " geometry == " + geometry;
  }

}
