package com.revolsys.gis.data.model.filter;

import java.util.Arrays;
import java.util.Collection;

import com.revolsys.filter.Filter;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectUtil;
import com.revolsys.gis.model.data.equals.EqualsRegistry;

public class AttributesEqualFilter implements Filter<DataObject> {
  public static boolean accept(
    final DataObject object1,
    final DataObject object2,
    final boolean nullEqualsEmptyString,
    final Collection<String> attributeNames) {
    for (final String attributeName : attributeNames) {
      final Object value1 = DataObjectUtil.getAttributeByPath(object1,
        attributeName);
      final Object value2 = DataObjectUtil.getAttributeByPath(object2,
        attributeName);
      if (nullEqualsEmptyString) {
        if (value1 == null) {
          if (value2 != null && !"".equals(value2)) {
            return false;
          }
        } else if (value2 == null) {
          if (value1 != null && !"".equals(value1)) {
            return false;
          }
        } else if (!EqualsRegistry.INSTANCE.equals(value1, value2)) {
          return false;
        }
      } else {
        if (!EqualsRegistry.INSTANCE.equals(value1, value2)) {
          return false;
        }
      }
    }
    return true;
  }

  public static boolean accept(
    final DataObject object1,
    final DataObject object2,
    final boolean nullEqualsEmptyString,
    final String... attributeNames) {
    return accept(object1, object2, nullEqualsEmptyString,
      Arrays.asList(attributeNames));
  }

  public static boolean accept(
    final DataObject object1,
    final DataObject object2,
    final String... attributeNames) {
    return accept(object1, object2, false, Arrays.asList(attributeNames));
  }

  private final Collection<String> attributeNames;

  private final DataObject object;

  private boolean nullEqualsEmptyString;

  public AttributesEqualFilter(final DataObject object,
    final Collection<String> attributeNames) {
    this.attributeNames = attributeNames;
    this.object = object;
  }

  public AttributesEqualFilter(final DataObject object,
    final String... attributeNames) {
    this(object, Arrays.asList(attributeNames));
  }

  public boolean accept(final DataObject object) {
    return accept(this.object, object, nullEqualsEmptyString, attributeNames);
  }

  public boolean isNullEqualsEmptyString() {
    return nullEqualsEmptyString;
  }

  public void setNullEqualsEmptyString(final boolean nullEqualsEmptyString) {
    this.nullEqualsEmptyString = nullEqualsEmptyString;
  }

  @Override
  public String toString() {
    return "AttributeEquals" + attributeNames;
  }

}
