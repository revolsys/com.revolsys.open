package com.revolsys.gis.data.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.annotation.PreDestroy;
import javax.xml.namespace.QName;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.collection.ThreadSharedAttributes;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.data.model.ArrayDataObjectFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.DataObjectMetaDataProperty;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.gis.data.model.codes.CodeTableProperty;
import com.revolsys.gis.io.Statistics;
import com.revolsys.io.AbstractObjectWithProperties;
import com.revolsys.io.Reader;

public abstract class AbstractDataObjectStore extends
  AbstractObjectWithProperties implements DataObjectStore {

  private Map<String, List<String>> codeTableColumNames = new HashMap<String, List<String>>();

  private DataObjectFactory dataObjectFactory;

  private final Map<String, CodeTable> columnToTableMap = new HashMap<String, CodeTable>();

  private String label;

  private Map<String, DataObjectStoreSchema> schemaMap = new TreeMap<String, DataObjectStoreSchema>();

  private List<DataObjectMetaDataProperty> commonMetaDataProperties = new ArrayList<DataObjectMetaDataProperty>();

  private final Map<QName, Map<String, Object>> typeMetaDataProperties = new HashMap<QName, Map<String, Object>>();

  private Statistics queryStatistics;

  public AbstractDataObjectStore() {
    this(new ArrayDataObjectFactory());
  }

  public AbstractDataObjectStore(final DataObjectFactory dataObjectFactory) {
    this.dataObjectFactory = dataObjectFactory;
  }

  public void addCodeTable(final CodeTable codeTable) {
    final String idColumn = codeTable.getIdAttributeName();
    addCodeTable(idColumn, codeTable);
    final List<String> attributeAliases = codeTable.getAttributeAliases();
    for (final String alias : attributeAliases) {
      addCodeTable(alias, codeTable);
    }
    final String codeTableName = codeTable.getName();
    final List<String> columnNames = codeTableColumNames.get(codeTableName);
    if (columnNames != null) {
      for (final String columnName : columnNames) {
        addCodeTable(columnName, codeTable);
      }
    }
  }

  public void addCodeTable(final String columnName, final CodeTable codeTable) {
    if (columnName != null && !columnName.equalsIgnoreCase("ID")) {
      this.columnToTableMap.put(columnName, codeTable);
    }
  }

  protected void addMetaData(final DataObjectMetaData metaData) {
    final QName typeName = metaData.getName();
    final String schemaName = typeName.getNamespaceURI();
    final DataObjectStoreSchema schema = getSchema(schemaName);
    schema.addMetaData(metaData);
  }

  protected void addMetaDataProperties(final DataObjectMetaDataImpl metaData) {
    final QName typeName = metaData.getName();
    for (final DataObjectMetaDataProperty property : commonMetaDataProperties) {
      final DataObjectMetaDataProperty clonedProperty = property.clone();
      clonedProperty.setMetaData(metaData);
    }
    final Map<String, Object> properties = typeMetaDataProperties.get(typeName);
    metaData.setProperties(properties);
  }

  protected void addSchema(final DataObjectStoreSchema schema) {
    schemaMap.put(schema.getName(), schema);
  }

  @PreDestroy
  public void close() {
    if (queryStatistics != null) {
      queryStatistics.disconnect();
    }
  }

  public DataObject create(final DataObjectMetaData objectMetaData) {
    final DataObjectMetaData metaData = getMetaData(objectMetaData);
    if (metaData == null) {
      return null;
    } else {
      return dataObjectFactory.createDataObject(metaData);
    }
  }

  public DataObject create(final QName typeName) {
    final DataObjectMetaData metaData = getMetaData(typeName);
    if (metaData == null) {
      return null;
    } else {
      return dataObjectFactory.createDataObject(metaData);
    }
  }

  protected AbstractIterator<DataObject> createIterator(final Query query,
    final Map<String, Object> properties) {
    throw new UnsupportedOperationException();
  }

  public Object createPrimaryIdValue(final QName typeName) {
    return null;
  }

  public Query createQuery(final QName typeName, final String whereClause,
    final BoundingBox boundingBox) {
    throw new UnsupportedOperationException();
  }

  public DataObjectStoreQueryReader createReader() {
    final DataObjectStoreQueryReader reader = new DataObjectStoreQueryReader(
      this);
    return reader;
  }

  public DataObjectReader createReader(final QName typeName,
    final String query, final List<Object> parameters) {
    throw new UnsupportedOperationException();
  }

  public void delete(final DataObject object) {
    throw new UnsupportedOperationException("Delete not supported");
  }

  public void deleteAll(final Collection<DataObject> objects) {
    for (final DataObject object : objects) {
      delete(object);
    }
  }

  protected DataObjectMetaData findMetaData(final QName typeName) {
    final String schemaName = typeName.getNamespaceURI();
    final DataObjectStoreSchema schema = getSchema(schemaName);
    if (schema == null) {
      return null;
    } else {
      return schema.findMetaData(typeName);
    }
  }

  public CodeTable getCodeTable(final QName typeName) {
    final DataObjectMetaData metaData = getMetaData(typeName);
    if (metaData == null) {
      return null;
    } else {
      final CodeTableProperty codeTable = CodeTableProperty.getProperty(metaData);
      return codeTable;
    }
  }

  public CodeTable getCodeTableByColumn(final String columnName) {
    final CodeTable codeTable = columnToTableMap.get(columnName);
    return codeTable;

  }

  public Map<String, List<String>> getCodeTableColumNames() {
    return codeTableColumNames;
  }

  public DataObjectFactory getDataObjectFactory() {
    return this.dataObjectFactory;
  }

  public String getLabel() {
    return label;
  }

  public DataObjectMetaData getMetaData(final DataObjectMetaData objectMetaData) {
    final QName typeName = objectMetaData.getName();
    final DataObjectMetaData metaData = getMetaData(typeName);
    return metaData;
  }

  public DataObjectMetaData getMetaData(final QName typeName) {
    final String schemaName = typeName.getNamespaceURI();
    final DataObjectStoreSchema schema = getSchema(schemaName);
    if (schema == null) {
      return null;
    } else {
      return schema.getMetaData(typeName);
    }
  }

  public QName getQName(final Object name) {
    if (name instanceof QName) {
      return (QName)name;
    } else {
      return QName.valueOf(name.toString());
    }
  }

  public Statistics getQueryStatistics() {
    if (queryStatistics == null) {
      if (label == null) {
        queryStatistics = new Statistics("Query");
      } else {
        queryStatistics = new Statistics(label + " Query");
      }
      queryStatistics.connect();
    }
    return queryStatistics;
  }

  public DataObjectStoreSchema getSchema(final String schemaName) {
    synchronized (schemaMap) {
      if (schemaMap.isEmpty()) {
        loadSchemas(schemaMap);
      }
      return schemaMap.get(schemaName);
    }
  }

  public Map<String, DataObjectStoreSchema> getSchemaMap() {
    return schemaMap;
  }

  public List<DataObjectStoreSchema> getSchemas() {
    synchronized (schemaMap) {
      if (schemaMap.isEmpty()) {
        loadSchemas(schemaMap);
      }
      return new ArrayList<DataObjectStoreSchema>(schemaMap.values());
    }
  }

  @SuppressWarnings("unchecked")
  protected <T> T getSharedAttribute(final String name) {
    final Map<String, Object> sharedAttributes = getSharedAttributes();
    final T value = (T)sharedAttributes.get(name);
    return value;
  }

  protected synchronized Map<String, Object> getSharedAttributes() {
    Map<String, Object> sharedAttributes = ThreadSharedAttributes.getAttribute(this);
    if (sharedAttributes == null) {
      sharedAttributes = new HashMap<String, Object>();
      ThreadSharedAttributes.setAttribute(this, sharedAttributes);
    }
    return sharedAttributes;
  }

  public List<QName> getTypeNames(final String schemaName) {
    final DataObjectStoreSchema schema = getSchema(schemaName);
    return schema.getTypeNames();
  }

  public List<DataObjectMetaData> getTypes(final String namespace) {
    final List<DataObjectMetaData> types = new ArrayList<DataObjectMetaData>();
    for (final QName typeName : getTypeNames(namespace)) {
      types.add(getMetaData(typeName));
    }
    return types;
  }

  public void insert(final DataObject dataObject) {
    throw new UnsupportedOperationException("Insert not supported");
  }

  public void insertAll(final Collection<DataObject> objects) {
    for (final DataObject object : objects) {
      insert(object);
    }
  }

  public boolean isEditable(final QName typeName) {
    return false;
  }

  public DataObject load(final QName typeName, final Object id) {
    final DataObjectMetaData metaData = getMetaData(typeName);
    if (metaData == null) {
      return null;
    } else {
      final String idAttributeName = metaData.getIdAttributeName();
      if (idAttributeName == null) {
        throw new IllegalArgumentException(typeName
          + " does not have a primary key");
      } else {
        final StringBuffer where = new StringBuffer();
        where.append(idAttributeName);
        where.append(" = ?");

        final Query query = new Query(typeName);
        query.setWhereClause(where.toString());
        query.addParameter(id);
        return queryFirst(query);
      }
    }
  }

  protected abstract void loadSchemaDataObjectMetaData(
    DataObjectStoreSchema schema, Map<QName, DataObjectMetaData> metaDataMap);

  protected abstract void loadSchemas(
    Map<String, DataObjectStoreSchema> schemaMap);

  public Reader<DataObject> query(final List<Query> queries) {
    final DataObjectStoreQueryReader reader = createReader();
    reader.setQueries(queries);
    return reader;
  }

  public Reader<DataObject> query(final QName typeName) {
    final Query query = new Query(typeName);
    return query(query);
  }

  public Reader<DataObject> query(final Query... queries) {
    return query(Arrays.asList(queries));
  }

  public DataObject queryFirst(final Query query) {
    final Reader<DataObject> reader = query(query);
    final Iterator<DataObject> iterator = reader.iterator();
    try {
      if (iterator.hasNext()) {
        final DataObject object = iterator.next();
        return object;
      } else {
        return null;
      }
    } finally {
      reader.close();
    }
  }

  protected void refreshMetaData(final String schemaName) {
    final DataObjectStoreSchema schema = getSchema(schemaName);
    if (schema != null) {
      schema.refreshMetaData();
    }
  }

  public void setCodeTableColumNames(
    final Map<String, List<String>> domainColumNames) {
    this.codeTableColumNames = domainColumNames;
  }

  public void setCommonMetaDataProperties(
    final List<DataObjectMetaDataProperty> commonMetaDataProperties) {
    this.commonMetaDataProperties = commonMetaDataProperties;
  }

  public void setDataObjectFactory(final DataObjectFactory dataObjectFactory) {
    this.dataObjectFactory = dataObjectFactory;
  }

  public void setLabel(final String label) {
    this.label = label;
  }

  public void setSchemaMap(final Map<String, DataObjectStoreSchema> schemaMap) {
    this.schemaMap = new DataObjectStoreSchemaMapProxy(this, schemaMap);
  }

  protected void setSharedAttribute(final String name, final Object value) {
    final Map<String, Object> sharedAttributes = getSharedAttributes();
    sharedAttributes.put(name, value);
  }

  public void setTypeMetaDataProperties(
    final Map<Object, List<DataObjectMetaDataProperty>> typeMetaProperties) {
    for (final Entry<Object, List<DataObjectMetaDataProperty>> typeProperties : typeMetaProperties.entrySet()) {
      final QName typeName = getQName(typeProperties.getKey());
      Map<String, Object> currentProperties = this.typeMetaDataProperties.get(typeName);
      if (currentProperties == null) {
        currentProperties = new HashMap<String, Object>();
        this.typeMetaDataProperties.put(typeName, currentProperties);
      }
      final List<DataObjectMetaDataProperty> properties = typeProperties.getValue();
      for (final DataObjectMetaDataProperty property : properties) {
        final String name = property.getPropertyName();
        currentProperties.put(name, property);
      }
    }
  }

  @Override
  public String toString() {
    return label;
  }

  public void update(final DataObject object) {
    throw new UnsupportedOperationException("Update not supported");
  }

  public void updateAll(final Collection<DataObject> objects) {
    for (final DataObject object : objects) {
      update(object);
    }
  }
}
