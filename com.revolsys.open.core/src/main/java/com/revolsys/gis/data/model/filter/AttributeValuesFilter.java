package com.revolsys.gis.data.model.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.revolsys.filter.Filter;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectUtil;
import com.revolsys.gis.model.data.equals.EqualsRegistry;

/**
 * Filter DataObjects by the value of the attributeName.
 * 
 * @author Paul Austin
 */
public class AttributeValuesFilter implements Filter<DataObject> {
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
   * Construct a new AttributeValuesFilter.
   * 
   * @param attributeName The attribute name.
   * @param values The array of values.
   */
  public AttributeValuesFilter(final String attributeName,
    final Object... values) {
    this(attributeName, Arrays.asList(values));
  }

  /**
   * Match the attributeName on the data object with the required value.
   * 
   * @param object The object.
   * @return True if the object matched the filter, false otherwise.
   */
  public boolean accept(final DataObject object) {
    final Object propertyValue = DataObjectUtil.getAttributeByPath(object,
      attributeName);
    if (propertyValue == null) {
      if (allowNulls) {
        return true;
      } else {
        return false;
      }
    } else {
      for (final Object value : values) {
        if (EqualsRegistry.INSTANCE.equals(value, propertyValue)) {
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
  public String getAttributeName() {
    return attributeName;
  }

  /**
   * @return the values
   */
  public List<Object> getValues() {
    return values;
  }

  public boolean isAllowNulls() {
    return allowNulls;
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
    return attributeName + " in " + values;
  }

}
