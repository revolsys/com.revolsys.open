package com.revolsys.data.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.revolsys.data.equals.EqualsInstance;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordUtil;
import com.revolsys.filter.Filter;

/**
 * Filter Records by the value of the attributeName.
 *
 * @author Paul Austin
 */
public class AttributeValuesFilter implements Filter<Record> {
  private boolean allowNulls;

  /** The attributeName name, or path to match. */
  private String attributeName;

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
   * @param attributeName The attribute name.
   * @param values The list of values.
   */
  public AttributeValuesFilter(final String attributeName,
    final boolean allowNulls, final List<Object> values) {
    this.attributeName = attributeName;
    this.values = values;
    this.allowNulls = allowNulls;
  }

  /**
   * Construct a new AttributeValuesFilter.
   *
   * @param attributeName The attribute name.
   * @param values The array of values.
   */
  public AttributeValuesFilter(final String attributeName,
    final boolean allowNulls, final Object... values) {
    this(attributeName, allowNulls, Arrays.asList(values));
  }

  /**
   * Construct a new AttributeValuesFilter.
   *
   * @param attributeName The attribute name.
   * @param values The list of values.
   */
  public AttributeValuesFilter(final String attributeName,
    final List<Object> values) {
    this.attributeName = attributeName;
    this.values = values;
  }

  /**
   * Match the attributeName on the data object with the required value.
   *
   * @param object The object.
   * @return True if the object matched the filter, false otherwise.
   */
  @Override
  public boolean accept(final Record object) {
    final Object propertyValue = RecordUtil.getFieldByPath(object,
      this.attributeName);
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
   * Get the attributeName name, or path to match.
   *
   * @return The attributeName name, or path to match.
   */
  public String getFieldName() {
    return this.attributeName;
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
   * Set the attributeName name, or path to match.
   *
   * @param attributeName The attributeName name, or path to match.
   */
  public void setAttributeName(final String attributeName) {
    this.attributeName = attributeName;
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
    return this.attributeName + " in " + this.values;
  }

}
