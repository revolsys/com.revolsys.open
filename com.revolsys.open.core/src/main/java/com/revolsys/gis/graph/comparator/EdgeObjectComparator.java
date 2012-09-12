package com.revolsys.gis.graph.comparator;

import java.util.Comparator;

import com.revolsys.gis.graph.Edge;

public class EdgeObjectComparator<T> implements Comparator<Edge<T>> {
  private Comparator<T> comparator;

  public EdgeObjectComparator() {
  }

  public EdgeObjectComparator(final Comparator<T> comparator) {
    this.comparator = comparator;
  }

  @Override
  public int compare(final Edge<T> edge1, final Edge<T> edge2) {
    if (edge1 == edge2) {
      return 0;
    } else {
      final T object1 = edge1.getObject();
      final T object2 = edge2.getObject();
      return comparator.compare(object1, object2);
    }
  }

  public Comparator<T> getComparator() {
    return comparator;
  }

  public void setComparator(final Comparator<T> comparator) {
    this.comparator = comparator;
  }

}
