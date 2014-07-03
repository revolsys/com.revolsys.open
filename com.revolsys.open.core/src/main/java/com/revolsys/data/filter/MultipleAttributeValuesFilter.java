package com.revolsys.data.filter;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import com.revolsys.data.equals.EqualsInstance;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordUtil;
import com.revolsys.filter.Filter;

/**
 * Filter DataObjects by the value of the property.
 * 
 * @author Paul Austin
 */
public class MultipleAttributeValuesFilter implements Filter<Record> {
  /** The values to match. */
  private Map<String, ? extends Object> values = Collections.emptyMap();

  public MultipleAttributeValuesFilter(
    final Map<String, ? extends Object> values) {
    this.values = values;
  }

  /**
   * Match the property on the data object with the required value.
   * 
   * @param object The object.
   * @return True if the object matched the filter, false otherwise.
   */
  @Override
  public boolean accept(final Record object) {
    for (final Entry<String, ? extends Object> entry : values.entrySet()) {
      final String attributeName = entry.getKey();
      final Object value = entry.getValue();
      final Object objectValue = RecordUtil.getAttributeByPath(object,
        attributeName);
      if (objectValue == null) {
        if (value != null) {
          if (!EqualsInstance.INSTANCE.equals(value, objectValue)) {
            return false;
          }
        }
      } else {
        if (!EqualsInstance.INSTANCE.equals(objectValue, value)) {
          return false;
        }
      }
    }
    return true;
  }

  public Map<String, ? extends Object> getValues() {
    return values;
  }

  public void setValues(final Map<String, ? extends Object> values) {
    this.values = values;
  }

  /**
   * @return the name
   */
  @Override
  public String toString() {
    return values.toString();
  }

}
