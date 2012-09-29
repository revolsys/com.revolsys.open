package com.revolsys.gis.data.model.codes;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.util.StringUtils;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataProperty;
import com.revolsys.gis.data.query.Query;
import com.revolsys.io.PathUtil;
import com.revolsys.io.Reader;

public class CodeTableProperty extends AbstractCodeTable implements
  DataObjectMetaDataProperty {

  public static final String PROPERTY_NAME = CodeTableProperty.class.getName();

  public static final CodeTableProperty getProperty(
    final DataObjectMetaData metaData) {
    final CodeTableProperty property = metaData.getProperty(PROPERTY_NAME);
    return property;
  }

  private String creationTimestampAttributeName;

  private String modificationTimestampAttributeName;

  private List<String> attributeAliases = new ArrayList<String>();

  private DataObjectStore dataStore;

  private boolean loadAll = true;

  private DataObjectMetaData metaData;

  private List<String> valueAttributeNames = new ArrayList<String>(
    Arrays.asList("VALUE"));

  private List<String> orderBy = new ArrayList<String>();

  private String typePath;

  public CodeTableProperty() {
  }

  public void addAttributeAlias(final String columnName) {
    attributeAliases.add(columnName);
  }

  public void addValue(final DataObject code) {
    final Object id = code.getValue(getIdAttributeName());
    final List<Object> values = new ArrayList<Object>();
    for (final String attributeName : this.valueAttributeNames) {
      final Object value = code.getValue(attributeName);
      values.add(value);
    }
    addValue(id, values);
  }

  protected void addValues(final Reader<DataObject> allCodes) {
    for (final DataObject code : allCodes) {
      addValue(code);
    }
  }

  @Override
  public CodeTableProperty cloneCoordinates() {
    final CodeTableProperty clone = (CodeTableProperty)super.clone();
    clone.attributeAliases = new ArrayList<String>(attributeAliases);
    clone.valueAttributeNames = new ArrayList<String>(valueAttributeNames);
    return clone;
  }

  protected synchronized Object createId(final List<Object> values) {
    // TODO prevent duplicates from other threads/processes
    final DataObject code = dataStore.create(typePath);
    final DataObjectMetaData metaData = code.getMetaData();
    Object id = dataStore.createPrimaryIdValue(typePath);
    if (id == null) {
      final Attribute idAttribute = metaData.getIdAttribute();
      if (idAttribute != null) {
        if (Number.class.isAssignableFrom(idAttribute.getType().getJavaClass())) {
          id = getNextId();
        } else {
          id = UUID.randomUUID().toString();
        }
      }
    }
    code.setIdValue(id);
    for (int i = 0; i < valueAttributeNames.size(); i++) {
      final String name = valueAttributeNames.get(i);
      final Object value = values.get(i);
      code.setValue(name, value);
    }

    final Timestamp now = new Timestamp(System.currentTimeMillis());
    if (creationTimestampAttributeName != null) {
      code.setValue(creationTimestampAttributeName, now);
    }
    if (modificationTimestampAttributeName != null) {
      code.setValue(modificationTimestampAttributeName, now);
    }

    dataStore.insert(code);
    id = code.getIdValue();
    return id;
  }

  @Override
  public List<String> getAttributeAliases() {
    return attributeAliases;
  }

  @Override
  public Map<Object, List<Object>> getCodes() {
    final Map<Object, List<Object>> codes = super.getCodes();
    if (codes.isEmpty() && isLoadAll()) {
      loadAll();
      return super.getCodes();
    } else {
      return codes;
    }
  }

  public String getCreationTimestampAttributeName() {
    return creationTimestampAttributeName;
  }

  public DataObjectStore getDataStore() {
    return dataStore;
  }

  private String idAttributeName;

  public void setIdAttributeName(String idAttributeName) {
    this.idAttributeName = idAttributeName;
  }

  @Override
  public String getIdAttributeName() {
    if (StringUtils.hasText(idAttributeName)) {
      return idAttributeName;
    } else if (metaData == null) {
      return "";
    } else {
      final String idAttributeName = metaData.getIdAttributeName();
      if (StringUtils.hasText(idAttributeName)) {
        return idAttributeName;
      } else {
        return metaData.getAttributeName(0);
      }
    }
  }

  @Override
  public Map<String, ? extends Object> getMap(final Object id) {
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

  @Override
  public DataObjectMetaData getMetaData() {
    return metaData;
  }

  public String getModificationTimestampAttributeName() {
    return modificationTimestampAttributeName;
  }

  @Override
  public String getPropertyName() {
    return PROPERTY_NAME;
  }

  public String getTypeName() {
    return typePath;
  }

  @Override
  public List<String> getValueAttributeNames() {
    return valueAttributeNames;
  }

  public boolean isLoadAll() {
    return loadAll;
  }

  protected synchronized void loadAll() {
    final Query query = new Query(typePath);
    for (final String order : orderBy) {
      query.addOrderBy(order, true);
    }
    final Reader<DataObject> allCodes = dataStore.query(query);
    addValues(allCodes);
  }

  @Override
  protected synchronized Object loadId(final List<Object> values,
    final boolean createId) {
    Object id = null;
    if (createId && loadAll) {
      loadAll();
      id = getId(values, false);
    } else {
      final StringBuffer where = new StringBuffer();
      final List<Object> queryValues = new ArrayList<Object>();
      if (!values.isEmpty()) {
        int i = 0;
        for (final String attributeName : valueAttributeNames) {
          if (i > 0) {
            where.append(" AND ");
          }
          where.append(attributeName);
          final Object value = values.get(i);
          if (value == null) {
            where.append(" IS NULL");
          } else {
            queryValues.add(value);
            where.append(" = ?");
          }
          i++;
        }
      }
      final Query query = new Query(typePath);
      query.setWhereClause(where.toString());
      query.setParameters(queryValues);
      final Reader<DataObject> reader = dataStore.query(query);
      try {
        addValues(reader);
        id = getIdByValue(values);
      } finally {
        reader.close();
      }
    }
    if (createId && id == null) {
      return createId(values);
    } else {
      return id;
    }
  }

  @Override
  protected List<Object> loadValues(final Object id) {
    List<Object> values = null;
    if (loadAll) {
      loadAll();
      values = getValueById(id);
    } else {
      final DataObject code = dataStore.load(typePath, id);
      if (code != null) {
        addValue(code);
        values = getValueById(id);
      }
    }
    return values;
  }

  public void setAttributeAliases(final List<String> columnAliases) {
    this.attributeAliases = columnAliases;
  }

  public void setCreationTimestampAttributeName(
    final String creationTimestampAttributeName) {
    this.creationTimestampAttributeName = creationTimestampAttributeName;
  }

  public void setLoadAll(final boolean loadAll) {
    this.loadAll = loadAll;
  }

  @Override
  public void setMetaData(final DataObjectMetaData metaData) {
    if (this.metaData != metaData) {
      if (this.metaData != null) {
        this.metaData.setProperty(getPropertyName(), null);
      }
      this.metaData = metaData;
      if (metaData == null) {
        this.dataStore = null;
        this.typePath = null;
      } else {
        this.typePath = metaData.getPath();
        setName(PathUtil.getName(typePath));
        this.dataStore = this.metaData.getDataObjectStore();
        metaData.setProperty(getPropertyName(), this);
        dataStore.addCodeTable(this);
      }
    }
  }

  public void setModificationTimestampAttributeName(
    final String modificationTimestampAttributeName) {
    this.modificationTimestampAttributeName = modificationTimestampAttributeName;
  }

  public void setOrderBy(final List<String> orderBy) {
    this.orderBy = orderBy;
  }

  public void setValueAttributeName(final String valueColumns) {
    setValueAttributeNames(valueColumns);
  }

  public void setValueAttributeNames(final List<String> valueColumns) {
    this.valueAttributeNames.clear();
    for (final String column : valueColumns) {
      this.valueAttributeNames.add(column);
    }
  }

  public void setValueAttributeNames(final String... valueColumns) {
    this.valueAttributeNames.clear();
    for (final String column : valueColumns) {
      this.valueAttributeNames.add(column);
    }
  }

  @Override
  public String toString() {
    return typePath + " " + getIdAttributeName() + " " + valueAttributeNames;

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
