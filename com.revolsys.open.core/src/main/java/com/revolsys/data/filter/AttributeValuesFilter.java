package com.revolsys.data.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.revolsys.data.equals.EqualsInstance;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.Records;
import com.revolsys.filter.Filter;

/**
 * Filter Records by the value of the fieldName.
 *
 * @author Paul Austin
 */
public class AttributeValuesFilter implements Filter<Record> {
  private boolean allowNulls;

  /** The fieldName name, or path to match. */
  private String fieldName;

  /** The value to match. */
  private List<Object> values = new ArrayList<Object>();

  /**
   * Construct a new AttributeValuesFilter.
   */
  public AttributeValuesFilter() {
  }

  /**
   * Construct a new AttributeValuesFilter.
   *
   * @param fieldName The attribute name.
   * @param values The list of values.
   */
  public AttributeValuesFilter(final String fieldName, final boolean allowNulls,
    final List<Object> values) {
    this.fieldName = fieldName;
    this.values = values;
    this.allowNulls = allowNulls;
  }

  /**
   * Construct a new AttributeValuesFilter.
   *
   * @param fieldName The attribute name.
   * @param values The array of values.
   */
  public AttributeValuesFilter(final String fieldName, final boolean allowNulls,
    final Object... values) {
    this(fieldName, allowNulls, Arrays.asList(values));
  }

  /**
   * Construct a new AttributeValuesFilter.
   *
   * @param fieldName The attribute name.
   * @param values The list of values.
   */
  public AttributeValuesFilter(final String fieldName, final List<Object> values) {
    this.fieldName = fieldName;
    this.values = values;
  }

  /**
   * Match the fieldName on the data object with the required value.
   *
   * @param object The object.
   * @return True if the object matched the filter, false otherwise.
   */
  @Override
  public boolean accept(final Record object) {
    final Object propertyValue = Records.getFieldByPath(object, this.fieldName);
    if (propertyValue == null) {
      if (this.allowNulls) {
        return true;
      } else {
        return false;
      }
    } else {
      for (final Object value : this.values) {
        if (EqualsInstance.INSTANCE.equals(value, propertyValue)) {
          return true;
        }
      }
      return false;
    }
  }

  /**
   * Get the fieldName name, or path to match.
   *
   * @return The fieldName name, or path to match.
   */
  public String getFieldName() {
    return this.fieldName;
  }

  /**
   * @return the values
   */
  public List<Object> getValues() {
    return this.values;
  }

  public boolean isAllowNulls() {
    return this.allowNulls;
  }

  public void setAllowNulls(final boolean allowNulls) {
    this.allowNulls = allowNulls;
  }

  /**
   * Set the fieldName name, or path to match.
   *
   * @param fieldName The fieldName name, or path to match.
   */
  public void setFieldName(final String fieldName) {
    this.fieldName = fieldName;
  }

  /**
   * @param values the values to set
   */
  public void setValues(final List<Object> values) {
    this.values = values;
  }

  /**
   * @return the name
   */
  @Override
  public String toString() {
    return this.fieldName + " in " + this.values;
  }

}
