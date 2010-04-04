package com.revolsys.gis.jdbc.data.model.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.revolsys.filter.Filter;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.gis.jdbc.io.JdbcDataObjectStore;

/**
 * Filter DataObjects by the value of the attributeName.
 * 
 * @author Paul Austin
 */
public class DataObjectCodeTableValueFilter implements Filter<DataObject> {
  /** The attributeName name, or path to match. */
  private String attributeName;

  private String name;

  /** The value to match. */
  private List<Object> values = new ArrayList<Object>();

  public DataObjectCodeTableValueFilter() {
  }

  public DataObjectCodeTableValueFilter(
    final String attributeName,
    final List<Object> values) {
    this.attributeName = attributeName;
    this.values.addAll(values);
  }

  public DataObjectCodeTableValueFilter(
    final String attributeName,
    final Object... values) {
    this(attributeName, Arrays.asList(values));
  }

  /**
   * Match the attributeName on the data object with the required value.
   * 
   * @param object The object.
   * @return True if the object matched the filter, false otherwise.
   */
  public boolean accept(
    final DataObject object) {
    final Object propertyValue = object.getValue(attributeName);
    if (values.contains(propertyValue)) {
      return true;
    } else {
      final DataObjectMetaData metaData = object.getMetaData();
      final JdbcDataObjectStore dataObjectStore = (JdbcDataObjectStore)metaData.getDataObjectStore();
      final CodeTable codeTable = dataObjectStore.getCodeTableByColumn(attributeName);
      if (codeTable != null) {
        final Object codeValue = codeTable.getValue((Number)propertyValue);
        if (values.contains(codeValue)) {
          values.add(propertyValue);
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

  public void setAttributeName(
    final String attributeName) {
    this.attributeName = attributeName;
  }

  /**
   * @param name the name to set
   */
  public void setName(
    final String name) {
    this.name = name;
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
    if (name == null) {
      return attributeName + " in " + values;
    } else {
      return name;
    }
  }

}
