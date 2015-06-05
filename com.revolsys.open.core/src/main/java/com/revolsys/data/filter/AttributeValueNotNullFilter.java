package com.revolsys.data.filter;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.Records;
import com.revolsys.filter.Filter;

/**
 * Filter Records by the the attribute not having a null value.
 *
 * @author Paul Austin
 */
public class AttributeValueNotNullFilter implements Filter<Record> {

  /** The property name, or path to match. */
  private String fieldName;

  public AttributeValueNotNullFilter() {
  }

  public AttributeValueNotNullFilter(final String fieldName) {
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
    final Object propertyValue = Records.getFieldByPath(object,
      this.fieldName);
    return propertyValue != null;
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
    return this.fieldName + " != null ";
  }
}
