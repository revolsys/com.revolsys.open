package com.revolsys.gis.converter;

import org.springframework.core.convert.converter.Converter;

import com.revolsys.filter.Filter;
import com.revolsys.gis.data.model.DataObject;

public class FilterDataObjectConverter {
  private Converter<DataObject, DataObject> converter;

  private Filter<DataObject> filter;

  public FilterDataObjectConverter() {
  }

  public FilterDataObjectConverter(final Filter<DataObject> filter,
    final Converter<DataObject, DataObject> converter) {
    this.filter = filter;
    this.converter = converter;
  }

  public Converter<DataObject, DataObject> getConverter() {
    return converter;
  }

  public Filter<DataObject> getFilter() {
    return filter;
  }

  @Override
  public String toString() {
    return "filter=" + filter + "\nconverter=" + converter;
  }
}
