package com.revolsys.filter;

public interface FilterProxy<T> {
  Filter<T> getFilter();
}
