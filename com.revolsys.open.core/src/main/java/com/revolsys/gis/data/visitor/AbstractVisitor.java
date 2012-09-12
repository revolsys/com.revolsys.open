package com.revolsys.gis.data.visitor;

import java.util.Comparator;

import com.revolsys.collection.Visitor;
import com.revolsys.comparator.ComparatorProxy;
import com.revolsys.filter.AndFilter;
import com.revolsys.filter.Filter;
import com.revolsys.filter.FilterProxy;

public abstract class AbstractVisitor<T> implements Visitor<T>, FilterProxy<T>,
  ComparatorProxy<T> {
  private Filter<T> filter;

  private Comparator<T> comparator;

  @Override
  public Comparator<T> getComparator() {
    return comparator;
  }

  @Override
  public Filter<T> getFilter() {
    return filter;
  }

  public void setComparator(final Comparator<T> comparator) {
    this.comparator = comparator;
  }

  public void setFilter(final Filter<T> filter) {
    this.filter = filter;
  }

  public void setFilters(final Filter<T>... filters) {
    this.filter = new AndFilter<T>(filters);
  }
}
