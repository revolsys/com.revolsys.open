package com.revolsys.comparator;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.revolsys.util.Property;

public class ObjectPropertyComparator<V> implements Comparator<V> {
  private List<String> attributeNames;

  private boolean invert;

  private boolean nullFirst;

  public ObjectPropertyComparator() {
  }

  public ObjectPropertyComparator(final boolean sortAsceding,
    final String... attributeNames) {
    this(Arrays.asList(attributeNames));
    this.invert = !sortAsceding;
  }

  public ObjectPropertyComparator(final List<String> attributeNames) {
    this.attributeNames = attributeNames;
  }

  public ObjectPropertyComparator(final String... attributeNames) {
    this(Arrays.asList(attributeNames));
  }

  @Override
  public int compare(final Object object1, final Object object2) {
    for (final String attributeName : attributeNames) {
      final int compare = compare(object1, object2, attributeName);
      if (compare != 0) {
        return compare;
      }
    }

    return 0;
  }

  public int compare(final Object object1, final Object object2,
    final String attributeName) {
    final Comparable<Object> value1 = Property.get(object1, attributeName);
    final Comparable<Object> value2 = Property.get(object2, attributeName);
    if (value1 == null) {
      if (value2 == null) {
        return 0;
      } else {
        if (nullFirst) {
          return -1;
        } else {
          return 1;
        }
      }
    } else if (value2 == null) {
      if (nullFirst) {
        return 1;
      } else {
        return -1;
      }
    } else {
      final int compare = value1.compareTo(value2);
      if (invert) {
        return -compare;
      } else {
        return compare;
      }
    }
  }

  public List<String> getFieldNames() {
    return attributeNames;
  }

  public boolean isInvert() {
    return invert;
  }

  public boolean isNullFirst() {
    return nullFirst;
  }

  public void setAttributeNames(final List<String> attributeNames) {
    this.attributeNames = attributeNames;
  }

  public void setInvert(final boolean invert) {
    this.invert = invert;
  }

  public void setNullFirst(final boolean nullFirst) {
    this.nullFirst = nullFirst;
  }

}
