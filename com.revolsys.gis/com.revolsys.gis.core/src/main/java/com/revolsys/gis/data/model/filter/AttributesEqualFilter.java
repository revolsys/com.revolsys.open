package com.revolsys.gis.data.model.filter;

import java.util.Arrays;
import java.util.Collection;

import com.revolsys.filter.Filter;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectUtil;
import com.revolsys.gis.model.data.equals.EqualsRegistry;

public class AttributesEqualFilter implements Filter<DataObject> {
  public static boolean accept(
    DataObject object1,
    DataObject object2,
    String... attributeNames) {
    return accept(object1, object2, Arrays.asList(attributeNames));
  }

  public static boolean accept(
    DataObject object1,
    DataObject object2,
    Collection<String> attributeNames) {
    for (String attributeName : attributeNames) {
      final Object value1 = DataObjectUtil.getAttributeByPath(object1,
        attributeName);
      final Object value2 = DataObjectUtil.getAttributeByPath(object2,
        attributeName);

      if (!EqualsRegistry.INSTANCE.equals(value1, value2)) {
        return false;
      }
    }
    return true;
  }

  private Collection<String> attributeNames;

  private DataObject object;

  public AttributesEqualFilter(
    DataObject object,
    Collection<String> attributeNames) {
    this.attributeNames = attributeNames;
    this.object = object;
  }

  public boolean accept(
    final DataObject object) {
    return accept(this.object, object, attributeNames);
  }

  @Override
  public String toString() {
    return "AttributeEquals" + attributeNames;
  }

}
