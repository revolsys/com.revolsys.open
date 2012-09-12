package com.revolsys.gis.data.model.filter;

import com.revolsys.filter.Filter;
import com.revolsys.gis.data.model.DataObject;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPoint;

public class MultiPointDataObjectFilter implements Filter<DataObject> {
  public static final MultiPointDataObjectFilter FILTER = new MultiPointDataObjectFilter();

  private MultiPointDataObjectFilter() {
  }

  @Override
  public boolean accept(final DataObject object) {
    final Geometry geometry = object.getGeometryValue();
    return geometry instanceof MultiPoint;
  }

  @Override
  public String toString() {
    return "MultiPoint";
  }

}
