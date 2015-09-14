package com.revolsys.record.filter;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Predicate;

import com.revolsys.equals.EqualsInstance;
import com.revolsys.equals.Geometry2DEquals;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.record.Record;

/**
 * The exact match item visitor finds the first match in the archive features
 * for the update feature, excluding the attributes {@value #equalExclude}.
 *
 * @author Paul Austin
 */
public class RecordEquals2DFilter implements Predicate<Record> {
  private final Collection<String> equalExclude = new HashSet<String>();

  /** The update feature to find a match for. */
  private final Record searchObject;

  public RecordEquals2DFilter(final Record searchObject) {
    this(searchObject, null);
  }

  public RecordEquals2DFilter(final Record searchObject, final Collection<String> equalExclude) {
    this.searchObject = searchObject;
    if (equalExclude != null) {
      this.equalExclude.addAll(equalExclude);
    }
  }

  @Override
  public boolean test(final Record object) {
    final Geometry serachGeometry = this.searchObject.getGeometry();
    final Geometry geometry = object.getGeometry();

    if (Geometry2DEquals.INSTANCE.equals(serachGeometry, geometry, this.equalExclude)) {
      if (EqualsInstance.INSTANCE.equals(this.searchObject, object, this.equalExclude)) {
        return true;
      }
    }
    return false;
  }

}
