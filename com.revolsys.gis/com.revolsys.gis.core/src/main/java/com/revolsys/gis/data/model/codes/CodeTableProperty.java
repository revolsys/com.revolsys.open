package com.revolsys.gis.data.model.codes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataProperty;
import com.revolsys.io.Reader;

public class CodeTableProperty extends AbstractCodeTable implements
  DataObjectMetaDataProperty {

  public static final String PROPERTY_NAME = CodeTableProperty.class.getName();

  public static final CodeTableProperty getProperty(
    final DataObjectMetaData metaData) {
    final CodeTableProperty property = metaData.getProperty(PROPERTY_NAME);
    return property;
  }

  private List<String> attributeAliases = new ArrayList<String>();

  private DataObjectStore dataStore;

  private boolean loadAll = true;

  private DataObjectMetaData metaData;

  private List<String> valueAttributeNames = new ArrayList<String>(
    Arrays.asList("VALUE"));

  private QName typeName;

  public CodeTableProperty() {
  }

  protected void addValues(final Reader<DataObject> allCodes) {
    for (final DataObject code : allCodes) {
      final Number id = code.getValue(getIdAttributeName());
      final List<Object> values = new ArrayList<Object>();
      for (final String attributeName : this.valueAttributeNames) {
        final Object value = code.getValue(attributeName);
        values.add(value);
      }
      addValue(id, values);
    }
  }

  @Override
  public CodeTableProperty clone() {
    final CodeTableProperty clone = (CodeTableProperty)super.clone();
    clone.attributeAliases = new ArrayList<String>(attributeAliases);
    clone.valueAttributeNames = new ArrayList<String>(valueAttributeNames);
    return clone;
  }

  protected synchronized Number createId(final List<Object> values) {
    // TODO prevent duplicates from other threads/processes
    final DataObject code = dataStore.create(typeName);
    final Number id;
    if (loadAll) {
      id = getNextId();
    } else {
      id = dataStore.createPrimaryId(typeName);
    }
    code.setIdValue(id);
    for (int i = 0; i < valueAttributeNames.size(); i++) {
      final String name = valueAttributeNames.get(i);
      final Object value = values.get(i);
      code.setValue(name, value);
    }
    dataStore.insert(code);
    return id;
  }

  @Override
  public List<String> getAttributeAliases() {
    return attributeAliases;
  }

  public DataObjectStore getDataStore() {
    return dataStore;
  }

  public String getIdAttributeName() {
    return metaData.getIdAttributeName();
  }

  @Override
  public Map<String, ? extends Object> getMap(final Number id) {
    final List<Object> values = getValues(id);
    if (values == null) {
      return Collections.emptyMap();
    } else {
      final Map<String, Object> map = new HashMap<String, Object>();
      for (int i = 0; i < values.size(); i++) {
        final String name = valueAttributeNames.get(i);
        final Object value = values.get(i);
        map.put(name, value);
      }
      return map;
    }
  }

  public DataObjectMetaData getMetaData() {
    return metaData;
  }

  public String getPropertyName() {
    return PROPERTY_NAME;
  }

  @Override
  public List<String> getValueAttributeNames() {
    return valueAttributeNames;
  }

  public boolean isLoadAll() {
    return loadAll;
  }

  protected void loadAll() {
    final Reader<DataObject> allCodes = dataStore.query(typeName);
    addValues(allCodes);
  }

  @Override
  protected synchronized Number loadId(final List<Object> values,
    final boolean createId) {
    Number id = null;
    if (createId && loadAll) {
      loadAll();
      id = getIdByValue(values);
    } else {
      final StringBuffer where = new StringBuffer();
      if (!values.isEmpty()) {
        for (final String attributeName : valueAttributeNames) {
          if (where.length() > 0) {
            where.append(" AND ");
          }
          where.append(attributeName + " = ?");
        }
      }
      final Reader<DataObject> reader = dataStore.query(typeName,
        where.toString(), values.toArray());
      addValues(reader);
      id = getIdByValue(values);
    }
    if (createId && id == null) {
      return createId(values);
    } else {
      return id;
    }
  }

  @Override
  protected List<Object> loadValues(final Number id) {
    List<Object> values = null;
    if (loadAll) {
      loadAll();
      values = getValueById(id);
    } else {
      final Reader<DataObject> reader = dataStore.query(typeName,
        getIdAttributeName() + " = ?", id);
      addValues(reader);
      values = getValueById(id);
    }
    return values;
  }

  public void setAttributeAliases(final List<String> columnAliases) {
    this.attributeAliases = columnAliases;
  }

  public void setLoadAll(final boolean loadAll) {
    this.loadAll = loadAll;
  }

  public void setMetaData(final DataObjectMetaData metaData) {
    if (this.metaData != metaData) {
      if (this.metaData != null) {
        this.metaData.setProperty(getPropertyName(), null);
      }
      this.metaData = metaData;
      if (metaData == null) {
        this.dataStore = null;
        this.typeName = null;
      } else {
        this.typeName = metaData.getName();
        this.dataStore = this.metaData.getDataObjectStore();
        metaData.setProperty(getPropertyName(), this);
        dataStore.addCodeTable(this);
      }
    }
  }

  public void setValueAttributeNames(final List<String> valueColumns) {
    this.valueAttributeNames.clear();
    for (final String column : valueColumns) {
      this.valueAttributeNames.add(column);
    }
  }

  public void setValueAttributeNames(final String valueColumn) {
    this.valueAttributeNames.clear();
    this.valueAttributeNames.add(valueColumn);
  }

  public void setValueAttributeNames(final String... valueColumns) {
    this.valueAttributeNames.clear();
    for (final String column : valueColumns) {
      this.valueAttributeNames.add(column);
    }
  }

  @Override
  public String toString() {
    return typeName + " " + getIdAttributeName() + " " + valueAttributeNames;

  }

  public String toString(final List<String> values) {
    final StringBuffer string = new StringBuffer(values.get(0));
    for (int i = 1; i < values.size(); i++) {
      final String value = values.get(i);
      string.append(",");
      string.append(value);
    }
    return string.toString();
  }
}
