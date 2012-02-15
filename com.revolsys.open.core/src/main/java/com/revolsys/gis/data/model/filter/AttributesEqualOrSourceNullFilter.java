package com.revolsys.gis.data.model.filter;

import java.util.Arrays;
import java.util.Collection;

import com.revolsys.filter.Filter;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectUtil;
import com.revolsys.gis.model.data.equals.EqualsRegistry;

public class AttributesEqualOrSourceNullFilter implements Filter<DataObject> {
  public static boolean accept(
    final DataObject object1,
    final DataObject object2,
    final Collection<String> attributeNames) {
    for (final String attributeName : attributeNames) {
      final Object value1 = DataObjectUtil.getAttributeByPath(object1,
        attributeName);
      final Object value2 = DataObjectUtil.getAttributeByPath(object2,
        attributeName);

      if (value1 != null && !EqualsRegistry.INSTANCE.equals(value1, value2)) {
        return false;
      }
    }
    return true;
  }

  public static boolean accept(
    final DataObject object1,
    final DataObject object2,
    final String... attributeNames) {
    return accept(object1, object2, Arrays.asList(attributeNames));
  }

  private final Collection<String> attributeNames;

  private final DataObject object;

  public AttributesEqualOrSourceNullFilter(final DataObject object,
    final Collection<String> attributeNames) {
    this.attributeNames = attributeNames;
    this.object = object;
  }

  public AttributesEqualOrSourceNullFilter(final DataObject object,
    final String... attributeNames) {
    this(object, Arrays.asList(attributeNames));
  }

  public boolean accept(final DataObject object) {
    return accept(this.object, object, attributeNames);
  }

  @Override
  public String toString() {
    return "AttributeEquals" + attributeNames;
  }

}
