package com.revolsys.data.filter;

import com.revolsys.data.record.Record;
import com.revolsys.filter.Filter;

/**
 * Filter Records by the the attribute having a null value.
 *
 * @author Paul Austin
 */
public class AttributeValueNullFilter implements Filter<Record> {

  /** The property name, or path to match. */
  private String fieldName;

  public AttributeValueNullFilter() {
  }

  public AttributeValueNullFilter(final String fieldName) {
    this.fieldName = fieldName;
  }

  /**
   * Match the property on the data object with the required value.
   *
   * @param object The object.
   * @return True if the object matched the filter, false otherwise.
   */
  @Override
  public boolean accept(final Record object) {
    final Object propertyValue = object.getValue(this.fieldName);
    return propertyValue == null;
  }

  public String getFieldName() {
    return this.fieldName;
  }

  public void setFieldName(final String fieldName) {
    this.fieldName = fieldName;
  }

  /**
   * @return the name
   */
  @Override
  public String toString() {
    return this.fieldName + " == null ";
  }
}
