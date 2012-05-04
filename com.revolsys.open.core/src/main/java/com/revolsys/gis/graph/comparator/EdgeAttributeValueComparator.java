package com.revolsys.gis.graph.comparator;

import java.util.Comparator;

import com.revolsys.gis.graph.Edge;

public class EdgeAttributeValueComparator<T> implements Comparator<Edge<T>> {
  private String attributeName;

  public EdgeAttributeValueComparator() {
  }

  public EdgeAttributeValueComparator(final String attributeName) {
    this.attributeName = attributeName;
  }

  public int compare(final Edge<T> edge1, final Edge<T> edge2) {
    if (edge1 == edge2) {
      return 0;
    } else if (edge1.isRemoved()) {
      return 1;
    } else if (edge2.isRemoved()) {
      return -11;
    } else {
      final Comparable<Object> object1 = edge1.getAttribute(attributeName);

      final Object object2 = edge2.getAttribute(attributeName);
      if (object1 == null) {
        if (object2 == null) {
          return 0;
        } else {
          return 1;
        }
      } else if (object2 == null) {
        return -1;
      } else {
        int compare = object1.compareTo(object2);
        if (compare == 0) {
          final Integer id1 = edge1.getId();
          final Integer id2 = edge2.getId();
          compare = id1.compareTo(id2);
        }
        return compare;
      }
    }
  }

  public String getAttributeName() {
    return attributeName;
  }

  public void setAttributeName(final String propertyName) {
    this.attributeName = propertyName;
  }

}
