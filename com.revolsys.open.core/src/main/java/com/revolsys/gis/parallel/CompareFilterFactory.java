package com.revolsys.gis.parallel;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.data.filter.AttributesEqualFilter;
import com.revolsys.data.filter.AttributesEqualOrNullFilter;
import com.revolsys.data.record.Record;
import com.revolsys.filter.AndFilter;
import com.revolsys.filter.Factory;
import com.revolsys.filter.Filter;

public class CompareFilterFactory implements Factory<Filter<Record>, Record> {
  private List<String> equalFieldNames = new ArrayList<String>();

  private List<String> equalOrNullFieldNames = new ArrayList<String>();

  @Override
  public Filter<Record> create(final Record object) {
    final AndFilter<Record> filters = new AndFilter<Record>();
    if (!this.equalFieldNames.isEmpty()) {
      final Filter<Record> valuesFilter = new AttributesEqualFilter(object,
        this.equalFieldNames);
      filters.addFilter(valuesFilter);
    }
    if (!this.equalOrNullFieldNames.isEmpty()) {
      final Filter<Record> valuesFilter = new AttributesEqualOrNullFilter(
        object, this.equalOrNullFieldNames);
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
