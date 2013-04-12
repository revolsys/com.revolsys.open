package com.revolsys.gis.graph.comparator;

import java.util.Collection;
import java.util.Comparator;

import com.revolsys.comparator.CollectionComparator;
import com.revolsys.gis.graph.Edge;

public class EdgeAttributeValueComparator<T> implements Comparator<Edge<T>> {
  private String[] attributeNames;

  public EdgeAttributeValueComparator() {
  }

  public EdgeAttributeValueComparator(final String... attributeNames) {
    this.attributeNames = attributeNames;
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  @Override
  public int compare(final Edge<T> edge1, final Edge<T> edge2) {
    if (edge1 == edge2) {
      return 0;
    } else if (edge1.isRemoved()) {
      return 1;
    } else if (edge2.isRemoved()) {
      return -11;
    } else {
      for (final String attributeName : attributeNames) {
        final Object object1 = edge1.getAttribute(attributeName);

        final Object object2 = edge2.getAttribute(attributeName);
        if (object1 == null) {
          if (object2 != null) {
            return 1;
          }
        } else if (object2 == null) {
          return -1;
        } else {
          int compare = -1;
          if (object1 instanceof Comparable) {
            final Comparable<Object> comparable1 = (Comparable<Object>)object1;
            compare = comparable1.compareTo(object2);
          } else if (object1 instanceof Collection) {
            final Collection collection1 = (Collection)object1;
            compare = new CollectionComparator().compare(collection1,
              (Collection)object2);

          }
          if (compare != 0) {
            return compare;

          }
        }
      }
      final Integer id1 = edge1.getId();
      final Integer id2 = edge2.getId();
      return id1.compareTo(id2);
    }
  }

}
