package com.revolsys.gis.data.model.filter;

import java.util.Collection;

import com.revolsys.filter.Filter;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectUtil;
import com.revolsys.gis.model.data.equals.EqualsRegistry;

public class AttributesEqualFilter implements Filter<DataObject> {
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
    for (String attributeName : attributeNames) {
      final Object value1 = DataObjectUtil.getAttributeByPath(object,
        attributeName);
      final Object value2 = DataObjectUtil.getAttributeByPath(this.object,
        attributeName);

      if (!EqualsRegistry.INSTANCE.equals(value1, value2)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public String toString() {
    return "AttributeEquals" + attributeNames;
  }

}
