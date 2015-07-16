package com.revolsys.gis.parallel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import com.revolsys.data.filter.AttributesEqualFilter;
import com.revolsys.data.filter.AttributesEqualOrNullFilter;
import com.revolsys.data.record.Record;
import com.revolsys.predicate.AndPredicate;

public class CompareFilterFactory implements Function<Record, Predicate<Record>> {
  private List<String> equalFieldNames = new ArrayList<String>();

  private List<String> equalOrNullFieldNames = new ArrayList<String>();

  @Override
  public Predicate<Record> apply(final Record object) {
    final AndPredicate<Record> filters = new AndPredicate<Record>();
    if (!this.equalFieldNames.isEmpty()) {
      final Predicate<Record> valuesFilter = new AttributesEqualFilter(object,
        this.equalFieldNames);
      filters.addFilter(valuesFilter);
    }
    if (!this.equalOrNullFieldNames.isEmpty()) {
      final Predicate<Record> valuesFilter = new AttributesEqualOrNullFilter(object,
        this.equalOrNullFieldNames);
      filters.addFilter(valuesFilter);
    }

    return filters;
  }

  public List<String> getEqualFieldNames() {
    return this.equalFieldNames;
  }

  public List<String> getEqualOrNullFieldNames() {
    return this.equalOrNullFieldNames;
  }

  public void setEqualFieldNames(final List<String> equalFieldNames) {
    this.equalFieldNames = equalFieldNames;
  }

  public void setEqualOrNullFieldNames(final List<String> equalOrNullFieldNames) {
    this.equalOrNullFieldNames = equalOrNullFieldNames;
  }

}
