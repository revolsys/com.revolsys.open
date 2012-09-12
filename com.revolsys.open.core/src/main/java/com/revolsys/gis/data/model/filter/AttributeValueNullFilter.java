package com.revolsys.gis.data.model.filter;

import com.revolsys.filter.Filter;
import com.revolsys.gis.data.model.DataObject;

/**
 * Filter DataObjects by the the attribute having a null value.
 * 
 * @author Paul Austin
 */
public class AttributeValueNullFilter implements Filter<DataObject> {

  /** The property name, or path to match. */
  private String attributeName;

  public AttributeValueNullFilter() {
  }

  public AttributeValueNullFilter(final String attributeName) {
    this.attributeName = attributeName;
  }

  /**
   * Match the property on the data object with the required value.
   * 
   * @param object The object.
   * @return True if the object matched the filter, false otherwise.
   */
  @Override
  public boolean accept(final DataObject object) {
    final Object propertyValue = object.getValue(attributeName);
    return propertyValue == null;
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
    return attributeName + " == null ";
  }
}
