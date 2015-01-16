package com.revolsys.gis.converter;

import org.springframework.core.convert.converter.Converter;

import com.revolsys.data.record.Record;
import com.revolsys.filter.Filter;

public class FilterRecordConverter {
  private Converter<Record, Record> converter;

  private Filter<Record> filter;

  public FilterRecordConverter() {
  }

  public FilterRecordConverter(final Filter<Record> filter,
    final Converter<Record, Record> converter) {
    this.filter = filter;
    this.converter = converter;
  }

  public Converter<Record, Record> getConverter() {
    return this.converter;
  }

  public Filter<Record> getFilter() {
    return this.filter;
  }

  @Override
  public String toString() {
    return "filter=" + this.filter + "\nconverter=" + this.converter;
  }
}
