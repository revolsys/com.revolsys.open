package com.revolsys.gis.parallel;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.filter.AndFilter;
import com.revolsys.filter.Factory;
import com.revolsys.filter.Filter;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.filter.AttributesEqualFilter;
import com.revolsys.gis.data.model.filter.AttributesEqualOrNullFilter;

public class CompareFilterFactory implements
  Factory<Filter<DataObject>, DataObject> {
  private List<String> equalAttributeNames = new ArrayList<String>();

  private List<String> equalOrNullAttributeNames = new ArrayList<String>();

  public Filter<DataObject> create(final DataObject object) {
    final AndFilter<DataObject> filters = new AndFilter<DataObject>();
    if (!equalAttributeNames.isEmpty()) {
      final Filter<DataObject> valuesFilter = new AttributesEqualFilter(object,
        equalAttributeNames);
      filters.addFilter(valuesFilter);
    }
    if (!equalOrNullAttributeNames.isEmpty()) {
      final Filter<DataObject> valuesFilter = new AttributesEqualOrNullFilter(
        object, equalOrNullAttributeNames);
      filters.addFilter(valuesFilter);
    }

    return filters;
  }

  public List<String> getEqualAttributeNames() {
    return equalAttributeNames;
  }

  public List<String> getEqualOrNullAttributeNames() {
    return equalOrNullAttributeNames;
  }

  public void setEqualAttributeNames(final List<String> equalAttributeNames) {
    this.equalAttributeNames = equalAttributeNames;
  }

  public void setEqualOrNullAttributeNames(
    final List<String> equalOrNullAttributeNames) {
    this.equalOrNullAttributeNames = equalOrNullAttributeNames;
  }

}
