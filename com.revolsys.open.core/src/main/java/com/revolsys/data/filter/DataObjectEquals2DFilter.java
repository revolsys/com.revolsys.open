package com.revolsys.data.filter;

import java.util.Collection;
import java.util.HashSet;

import com.revolsys.data.equals.EqualsInstance;
import com.revolsys.data.equals.Geometry2DEquals;
import com.revolsys.data.record.Record;
import com.revolsys.filter.Filter;
import com.revolsys.jts.geom.Geometry;

/**
 * The exact match item visitor finds the first match in the archive features
 * for the update feature, excluding the attributes {@value #equalExclude}.
 * 
 * @author Paul Austin
 */
public class DataObjectEquals2DFilter implements Filter<Record> {
  private final Collection<String> equalExclude = new HashSet<String>();

  /** The update feature to find a match for. */
  private final Record searchObject;

  public DataObjectEquals2DFilter(final Record searchObject) {
    this(searchObject, null);
  }

  public DataObjectEquals2DFilter(final Record searchObject,
    final Collection<String> equalExclude) {
    this.searchObject = searchObject;
    if (equalExclude != null) {
      this.equalExclude.addAll(equalExclude);
    }
  }

  @Override
  public boolean accept(final Record object) {
    final Geometry serachGeometry = searchObject.getGeometryValue();
    final Geometry geometry = object.getGeometryValue();

    if (Geometry2DEquals.INSTANCE.equals(serachGeometry, geometry, equalExclude)) {
      if (EqualsInstance.INSTANCE.equals(searchObject, object, equalExclude)) {
        return true;
      }
    }
    return false;
  }

}
