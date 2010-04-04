package com.revolsys.jump.ui.model;

import java.util.Comparator;

import com.vividsolutions.jump.feature.Feature;

public class FeatureIdComparitor implements Comparator<Feature> {
  private boolean sortAscending = true;

  public FeatureIdComparitor(final boolean sortAscending) {
    this.sortAscending = sortAscending;
  }

  public int compare(final Feature feature1, final Feature feature2) {
    Integer id1 = feature1.getID();
    Integer id2 = feature2.getID();
    int compare = id1.compareTo(id2);
    if (!sortAscending) {
      return -compare;
    } else {
      return compare;
    }
  }

  /**
   * @return the sortAscending
   */
  protected boolean isSortAscending() {
    return sortAscending;
  }
}
