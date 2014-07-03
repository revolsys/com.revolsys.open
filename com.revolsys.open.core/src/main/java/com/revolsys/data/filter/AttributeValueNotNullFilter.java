package com.revolsys.data.filter;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordUtil;
import com.revolsys.filter.Filter;

/**
 * Filter DataObjects by the the attribute not having a null value.
 * 
 * @author Paul Austin
 */
public class AttributeValueNotNullFilter implements Filter<Record> {

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
  @Override
  public boolean accept(final Record object) {
    final Object propertyValue = RecordUtil.getAttributeByPath(object,
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
