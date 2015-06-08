package com.revolsys.jdbc.data.model.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.revolsys.data.codes.CodeTable;
import com.revolsys.data.identifier.SingleIdentifier;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.filter.Filter;

/**
 * Filter Records by the value of the fieldName.
 *
 * @author Paul Austin
 */
public class RecordCodeTableValueFilter implements Filter<Record> {
  /** The fieldName name, or path to match. */
  private String fieldName;

  private String name;

  /** The value to match. */
  private final List<Object> values = new ArrayList<Object>();

  public RecordCodeTableValueFilter() {
  }

  public RecordCodeTableValueFilter(final String fieldName, final List<Object> values) {
    this.fieldName = fieldName;
    this.values.addAll(values);
  }

  public RecordCodeTableValueFilter(final String fieldName, final Object... values) {
    this(fieldName, Arrays.asList(values));
  }

  /**
   * Match the fieldName on the data object with the required value.
   *
   * @param object The object.
   * @return True if the object matched the filter, false otherwise.
   */
  @Override
  public boolean accept(final Record object) {
    final Object propertyValue = object.getValue(this.fieldName);
    if (this.values.contains(propertyValue)) {
      return true;
    } else {
      final RecordDefinition recordDefinition = object.getRecordDefinition();
      final CodeTable codeTable = recordDefinition.getCodeTableByFieldName(this.fieldName);
      if (codeTable != null) {
        final Object codeValue = codeTable.getValue(SingleIdentifier.create(propertyValue));
        if (this.values.contains(codeValue)) {
          this.values.add(propertyValue);
          return true;
        } else {
          return false;
        }
      } else {
        return false;
      }

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

  public void setFieldName(final String fieldName) {
    this.fieldName = fieldName;
  }

  /**
   * @param name the name to set
   */
  public void setName(final String name) {
    this.name = name;
  }

  public void setValue(final Object value) {
    setValues(Collections.singletonList(value));
  }

  /**
   * @param values the values to set
   */
  public void setValues(final List<Object> values) {
    this.values.clear();
    this.values.addAll(values);
  }

  /**
   * @return the name
   */
  @Override
  public String toString() {
    if (this.name == null) {
      return this.fieldName + " in " + this.values;
    } else {
      return this.name;
    }
  }

}
