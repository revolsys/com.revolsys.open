package com.revolsys.gis.data.model.filter;

import java.util.Collection;
import java.util.HashSet;

import com.revolsys.filter.Filter;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.vividsolutions.jts.geom.Geometry;

public class DataObjectEqualsFilter implements Filter<DataObject> {
  private EqualsRegistry equalsRegistry = EqualsRegistry.INSTANCE;

  private final Collection<String> equalExclude = new HashSet<String>();

  private final DataObject searchObject;

  public DataObjectEqualsFilter(final DataObject searchObject) {
    this(null, searchObject, null);
  }

  public DataObjectEqualsFilter(final DataObject searchObject,
    final Collection<String> equalExclude) {
    this(null, searchObject, equalExclude);
  }

  public DataObjectEqualsFilter(final EqualsRegistry equalsRegistry,
    final DataObject searchObject) {
    this(null, searchObject, null);
  }

  public DataObjectEqualsFilter(final EqualsRegistry equalsRegistry,
    final DataObject searchObject, final Collection<String> equalExclude) {
    if (equalsRegistry != null) {
      this.equalsRegistry = equalsRegistry;
    }
    this.searchObject = searchObject;
    if (equalExclude != null) {
      this.equalExclude.addAll(equalExclude);
    }
  }

  public boolean accept(final DataObject object) {
    final Geometry serachGeometry = searchObject.getGeometryValue();
    final Geometry geometry = object.getGeometryValue();

    if (equalsRegistry.equals(serachGeometry, geometry, equalExclude)) {
      if (equalsRegistry.equals(searchObject, object, equalExclude)) {
        return true;
      }
    }
    return false;
  }

}
