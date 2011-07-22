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

  public Filter<T> getFilter() {
    return filter;
  }

  public void setFilter(Filter<T> filter) {
    this.filter = filter;
  }

  private Comparator<T> comparator;

  public Comparator<T> getComparator() {
    return comparator;
  }

  public void setComparator(Comparator<T> comparator) {
    this.comparator = comparator;
  }

  public void setFilters(Filter<T>... filters) {
    this.filter = new AndFilter<T>(filters);
  }
}
