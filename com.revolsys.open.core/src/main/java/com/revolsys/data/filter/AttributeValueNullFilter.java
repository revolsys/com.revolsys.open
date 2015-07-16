package com.revolsys.data.filter;

import java.util.function.Predicate;

import com.revolsys.data.record.Record;

/**
 * Filter Records by the the attribute having a null value.
 *
 * @author Paul Austin
 */
public class AttributeValueNullFilter implements Predicate<Record> {

  /** The property name, or path to match. */
  private String fieldName;

  public AttributeValueNullFilter() {
  }

  public AttributeValueNullFilter(final String fieldName) {
    this.fieldName = fieldName;
  }

  public String getFieldName() {
    return this.fieldName;
  }

  public void setFieldName(final String fieldName) {
    this.fieldName = fieldName;
  }

  /**
   * Match the property on the data object with the required value.
   *
   * @param object The object.
   * @return True if the object matched the filter, false otherwise.
   */
  @Override
  public boolean test(final Record object) {
    final Object propertyValue = object.getValue(this.fieldName);
    return propertyValue == null;
  }

  /**
   * @return the name
   */
  @Override
  public String toString() {
    return this.fieldName + " == null ";
  }
}
