package com.revolsys.gis.data.model.filter;

import java.util.Collection;
import java.util.HashSet;

import com.revolsys.filter.Filter;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.gis.model.data.equals.Geometry2DEquals;
import com.vividsolutions.jts.geom.Geometry;

/**
 * The exact match item visitor finds the first match in the archive features
 * for the update feature, excluding the attributes {@value #equalExclude}.
 * 
 * @author Paul Austin
 */
public class DataObjectEquals2DFilter implements Filter<DataObject> {
  private final Collection<String> equalExclude = new HashSet<String>();

  /** The update feature to find a match for. */
  private final DataObject searchObject;

  public DataObjectEquals2DFilter(final DataObject searchObject) {
    this(searchObject, null);
  }

  public DataObjectEquals2DFilter(final DataObject searchObject,
    final Collection<String> equalExclude) {
    this.searchObject = searchObject;
    if (equalExclude != null) {
      this.equalExclude.addAll(equalExclude);
    }
  }

  public boolean accept(final DataObject object) {
    final Geometry serachGeometry = searchObject.getGeometryValue();
    final Geometry geometry = object.getGeometryValue();

    if (Geometry2DEquals.INSTANCE.equals(serachGeometry, geometry, equalExclude)) {
      if (EqualsRegistry.INSTANCE.equals(searchObject, object, equalExclude)) {
        return true;
      }
    }
    return false;
  }

}
