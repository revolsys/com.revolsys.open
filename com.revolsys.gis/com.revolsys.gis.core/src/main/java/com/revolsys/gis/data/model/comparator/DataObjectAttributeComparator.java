package com.revolsys.gis.data.model.comparator;

import java.util.Comparator;

import com.revolsys.gis.data.model.DataObject;

public class DataObjectAttributeComparator implements Comparator<DataObject> {
  private String attributeName;

  public DataObjectAttributeComparator() {
  }

  public DataObjectAttributeComparator(
    final String attributeName) {
    this.attributeName = attributeName;
  }

  public int compare(
    final DataObject object1,
    final DataObject object2) {
    if (object1 == null) {
      if (object2 == null) {
        return 0;
      } else {
        return -1;
      }
    } else if (object2 == null) {
      return 1;
    }
    final Comparable<Object> value1 = object1.getValue(attributeName);
    final Comparable<Object> value2 = object2.getValue(attributeName);
    if (value1 == null) {
      if (value2 == null) {
        return 0;
      } else {
        return -1;
      }
    } else if (value2 == null) {
      return 1;
    } else {
      return value1.compareTo(value2);
    }
  }

  public String getAttributeName() {
    return attributeName;
  }

  public void setAttributeName(
    final String attributeName) {
    this.attributeName = attributeName;
  }
}
