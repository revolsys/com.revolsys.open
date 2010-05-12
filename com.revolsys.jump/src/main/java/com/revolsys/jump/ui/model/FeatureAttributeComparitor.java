package com.revolsys.jump.ui.model;

import com.vividsolutions.jump.feature.Feature;

public class FeatureAttributeComparitor extends FeatureIdComparitor {
  private int attributeIndex;

  public FeatureAttributeComparitor(final boolean sortAscending,
    final int attributeIndex) {
    super(sortAscending);
    this.attributeIndex = attributeIndex;
  }

  @SuppressWarnings("unchecked")
  public int compare(final Feature feature1, final Feature feature2) {
    int compare = 0;
    Object value1 = feature1.getAttribute(attributeIndex);
    Object value2 = feature2.getAttribute(attributeIndex);
    if (value1 == null) {
      if (value2 == null) {
        compare = 0;
      } else {
        compare = -1;
      }
    } else if (value2 == null) {
      compare = 1;
    } else {
      if (value1 instanceof Number && value2 instanceof Number) {
        Number comp1 = (Number)value1;
        Number comp2 = (Number)value2;
        compare = Double.compare(comp1.doubleValue(), comp2.doubleValue());
      } else if (value1 instanceof Comparable && value2 instanceof Comparable) {
        Comparable comp1 = (Comparable)value1;
        Comparable comp2 = (Comparable)value2;
        compare = comp1.compareTo(comp2);
      } else {
        String string1 = value1.toString();
        String string2 = value2.toString();
        compare = string1.compareTo(string2);
      }
    }

    if (compare == 0) {
      return super.compare(feature1, feature2);
    }
    if (!isSortAscending()) {
      return -compare;
    } else {
      return compare;
    }
  }
}
