package com.revolsys.record.filter;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Predicate;

import com.revolsys.datatype.DataType;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.record.Record;

public class RecordEqualsFilter implements Predicate<Record> {
  private final Collection<String> equalExclude = new HashSet<String>();

  private final Record searchRecord;

  public RecordEqualsFilter(final Record searchRecord) {
    this(searchRecord, null);
  }

  public RecordEqualsFilter(final Record searchRecord, final Collection<String> equalExclude) {
    this.searchRecord = searchRecord;
    if (equalExclude != null) {
      this.equalExclude.addAll(equalExclude);
    }
  }

  @Override
  public boolean test(final Record object) {
    final Geometry serachGeometry = this.searchRecord.getGeometry();
    final Geometry geometry = object.getGeometry();

    if (DataType.equal(serachGeometry, geometry, this.equalExclude)) {
      if (DataType.equal(this.searchRecord, object, this.equalExclude)) {
        return true;
      }
    }
    return false;
  }

}
