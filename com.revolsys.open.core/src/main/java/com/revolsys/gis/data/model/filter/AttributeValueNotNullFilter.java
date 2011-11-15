package com.revolsys.gis.data.model.filter;

import com.revolsys.filter.Filter;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectUtil;

/**
 * Filter DataObjects by the the attribute not having a null value.
 * 
 * @author Paul Austin
 */
public class AttributeValueNotNullFilter implements Filter<DataObject> {

  /** The property name, or path to match. */
  private String attributeName;

  public AttributeValueNotNullFilter() {
  }

  public AttributeValueNotNullFilter(final String attributeName) {
    this.attributeName = attributeName;
  }

  /**
   * Match the property on the data object with the required value.
   * 
   * @param object The object.
   * @return True if the object matched the filter, false otherwise.
   */
  public boolean accept(final DataObject object) {
    final Object propertyValue = DataObjectUtil.getAttributeByPath(object,
      attributeName);
    return propertyValue != null;
  }

  public String getAttributeName() {
    return attributeName;
  }

  public void setAttributeName(final String attributeName) {
    this.attributeName = attributeName;
  }

  /**
   * @return the name
   */
  @Override
  public String toString() {
    return attributeName + " != null ";
  }
}
