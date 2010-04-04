package com.revolsys.gis.data.model.filter;

import java.util.Collection;
import java.util.HashSet;

import com.revolsys.filter.Filter;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.vividsolutions.jts.geom.Geometry;

public class DataObjectEqualsFilter implements Filter<DataObject> {
  private final Collection<String> equalExclude = new HashSet<String>();

  private final DataObject searchObject;

  public DataObjectEqualsFilter(
    final DataObject searchObject) {
    this(searchObject, null);
  }

  public DataObjectEqualsFilter(
    final DataObject searchObject,
    final Collection<String> equalExclude) {
    this.searchObject = searchObject;
    final DataObjectMetaData metaData = searchObject.getMetaData();
    if (equalExclude != null) {
      this.equalExclude.addAll(equalExclude);
    }
  }

  public boolean accept(
    final DataObject object) {
    final Geometry serachGeometry = searchObject.getGeometryValue();
    final Geometry geometry = object.getGeometryValue();

    if (EqualsRegistry.INSTANCE.equals(serachGeometry, geometry, equalExclude)) {
      if (EqualsRegistry.INSTANCE.equals(searchObject, object, equalExclude)) {
        return true;
      }
    }
    return false;
  }

}
