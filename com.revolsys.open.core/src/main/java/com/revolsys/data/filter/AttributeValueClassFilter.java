package com.revolsys.data.filter;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordUtil;
import com.revolsys.filter.Filter;

/**
 * Filter Records by the type (Java class) of the attributeName value.
 *
 * @author Paul Austin
 */
public class AttributeValueClassFilter implements Filter<Record> {
  /** The attributeName name, or path to match. */
  private String attributeName;

  /** The type to match. */
  private Class<?> type = Object.class;

  /**
   * Match the attributeName on the data object with the required value.
   *
   * @param object The object.
   * @return True if the object matched the filter, false otherwise.
   */
  @Override
  public boolean accept(final Record object) {
    final Object propertyValue = RecordUtil.getAttributeByPath(object,
      this.attributeName);
    return this.type.isInstance(propertyValue);
  }

  /**
   * Get the attributeName name, or path to match.
   *
   * @return The attributeName name, or path to match.
   */
  public String getAttributeName() {
    return this.attributeName;
  }

  /**
   * Get the type to match.
   *
   * @return The type to match.
   */
  public String getType() {
    return this.type.getName();
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
   * Set the type to match.
   *
   * @param type The type to match.
   */
  public void setType(final String type) {
    try {
      this.type = Class.forName(type);
    } catch (final ClassNotFoundException e) {
      throw new IllegalArgumentException(e.getMessage());
    }
  }

  /**
   * @return the name
   */
  @Override
  public String toString() {
    return this.attributeName + " type " + this.type;
  }
}
