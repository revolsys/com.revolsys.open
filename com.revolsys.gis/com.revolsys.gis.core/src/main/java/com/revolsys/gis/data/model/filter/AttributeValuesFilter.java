package com.revolsys.gis.data.model.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.revolsys.filter.Filter;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectUtil;

/**
 * Filter DataObjects by the value of the attributeName.
 * 
 * @author Paul Austin
 */
public class AttributeValuesFilter implements Filter<DataObject> {
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
   * @param values The array of values.
   */
  public AttributeValuesFilter(
    String attributeName,
    Object... values) {
    this(attributeName, Arrays.asList(values));
  }

  /**
   * Construct a new AttributeValuesFilter.
   * 
   * @param attributeName The attribute name.
   * @param values The list of values.
   */
  public AttributeValuesFilter(
    String attributeName,
    List<Object> values) {
    this.attributeName = attributeName;
    this.values = values;
  }

  /**
   * Match the attributeName on the data object with the required value.
   * 
   * @param object The object.
   * @return True if the object matched the filter, false otherwise.
   */
  public boolean accept(
    final DataObject object) {
    final Object propertyValue = DataObjectUtil.getAttributeByPath(object,
      attributeName);
    if (propertyValue == null) {
      return false;
    } else {
      return values.contains(propertyValue);
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

  /**
   * Set the attributeName name, or path to match.
   * 
   * @param attributeName The attributeName name, or path to match.
   */
  public void setAttributeName(
    final String attributeName) {
    this.attributeName = attributeName;
  }

  /**
   * @param values the values to set
   */
  public void setValues(
    final List<Object> values) {
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
