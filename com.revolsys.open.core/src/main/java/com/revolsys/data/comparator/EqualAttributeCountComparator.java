package com.revolsys.data.comparator;

import java.util.Comparator;
import java.util.List;

import com.revolsys.data.equals.EqualsInstance;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.util.CompareUtil;

/**
 * The EqualAttributeCountComparator compares the number of attributes of the
 * two objects which are equal to a test object. Unless invert is true a smaller
 * number of equal attributes will appear before a large amount.
 * 
 * @author Paul Austin
 */
public class EqualAttributeCountComparator implements Comparator<Record> {
  private final Record object;

  private final boolean invert;

  private final List<String> attributeNames;

  public EqualAttributeCountComparator(final Record object) {
    this(object, false);
  }

  public EqualAttributeCountComparator(final Record object,
    final boolean invert) {
    this.object = object;
    final RecordDefinition recordDefinition = object.getRecordDefinition();
    attributeNames = recordDefinition.getAttributeNames();
    this.invert = invert;
  }

  @Override
  public int compare(final Record object1, final Record object2) {
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
        if (EqualsInstance.INSTANCE.equals(value, value1)) {
          count1++;
        }

        final Object value2 = object1.getValue(attributeName);
        if (EqualsInstance.INSTANCE.equals(value, value2)) {
          count2++;
        }
      }
      compare = CompareUtil.compare(count1, count2);
    }
    return compare;
  }
}
