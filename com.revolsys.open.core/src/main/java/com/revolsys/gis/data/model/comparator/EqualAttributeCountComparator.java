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
  private final DataObject object;

  private final boolean invert;

  private final List<String> attributeNames;

  public EqualAttributeCountComparator(final DataObject object) {
    this(object, false);
  }

  public EqualAttributeCountComparator(final DataObject object,
    final boolean invert) {
    this.object = object;
    final DataObjectMetaData metaData = object.getMetaData();
    attributeNames = metaData.getAttributeNames();
    this.invert = invert;
  }

  @Override
  public int compare(final DataObject object1, final DataObject object2) {
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

      for (final String attributeName : attributeNames) {
        final Object value = object.getValue(attributeName);

        final Object value1 = object1.getValue(attributeName);
        if (EqualsRegistry.INSTANCE.equals(value, value1)) {
          count1++;
        }

        final Object value2 = object1.getValue(attributeName);
        if (EqualsRegistry.INSTANCE.equals(value, value2)) {
          count2++;
        }
      }
      compare = CompareUtil.compare(count1, count2);
    }
    return compare;
  }
}
