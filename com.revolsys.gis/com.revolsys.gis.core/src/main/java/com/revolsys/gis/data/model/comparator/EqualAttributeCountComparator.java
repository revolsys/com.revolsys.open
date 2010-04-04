package com.revolsys.gis.data.model.comparator;

import java.util.Comparator;
import java.util.List;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.util.CompareUtil;

/**
 * The EqualAttributeCountComparator compares the number of attributes of the
 * two objects which are equal to a test object. Unless invert is true a smaller
 * number of equal attributes will appear before a large amount.
 * 
 * @author Paul Austin
 */
public class EqualAttributeCountComparator implements Comparator<DataObject> {
  private DataObject object;

  private boolean invert;

  private List<String> attributeNames;

  public EqualAttributeCountComparator(
    DataObject object) {
    this(object, false);
  }

  public EqualAttributeCountComparator(
    DataObject object,
    boolean invert) {
    this.object = object;
    final DataObjectMetaData metaData = object.getMetaData();
    attributeNames = metaData.getAttributeNames();
    this.invert = invert;
  }

  public int compare(
    final DataObject object1,
    final DataObject object2) {
    final int compare;
    if (object1 == null) {
      if (object2 == null) {
        compare = 0;
      } else {
        compare = -1;
      }
    } else if (object2 == null) {
      compare = 1;
    } else {
      int count1 = 0;
      int count2 = 0;

      for (String attributeName : attributeNames) {
        Object value = object.getValue(attributeName);

        Object value1 = object1.getValue(attributeName);
        if (EqualsRegistry.INSTANCE.equals(value, value1)) {
          count1++;
        }

        Object value2 = object1.getValue(attributeName);
        if (EqualsRegistry.INSTANCE.equals(value, value2)) {
          count2++;
        }
      }
      compare = CompareUtil.compare(count1, count2);
    }
    return compare;
  }
}
