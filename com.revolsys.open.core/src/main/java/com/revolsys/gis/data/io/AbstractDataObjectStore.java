package com.revolsys.gis.data.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.util.StringUtils;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.collection.ListResultPager;
import com.revolsys.collection.ResultPager;
import com.revolsys.collection.ThreadSharedAttributes;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.ArrayDataObjectFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.DataObjectMetaDataProperty;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.gis.data.model.codes.CodeTableProperty;
import com.revolsys.gis.data.query.Query;
import com.revolsys.gis.io.Statistics;
import com.revolsys.gis.io.StatisticsMap;
import com.revolsys.io.AbstractObjectWithProperties;
import com.revolsys.io.PathUtil;
import com.revolsys.io.Reader;
import com.revolsys.io.Writer;

public abstract class AbstractDataObjectStore extends
  AbstractObjectWithProperties implements DataObjectStore {

  private Map<String, List<String>> codeTableColumNames = new HashMap<String, List<String>>();

  private DataObjectFactory dataObjectFactory;

  private Map<String, CodeTable> columnToTableMap = new HashMap<String, CodeTable>();

  private String label;

  private Map<String, DataObjectStoreSchema> schemaMap = new TreeMap<String, DataObjectStoreSchema>();

  private List<DataObjectMetaDataProperty> commonMetaDataProperties = new ArrayList<DataObjectMetaDataProperty>();

  private Map<String, Map<String, Object>> typeMetaDataProperties = new HashMap<String, Map<String, Object>>();

  private StatisticsMap statistics = new StatisticsMap();

  private GeometryFactory geometryFactory;

  public AbstractDataObjectStore() {
    this(new ArrayDataObjectFactory());
  }

  public AbstractDataObjectStore(final DataObjectFactory dataObjectFactory) {
    this.dataObjectFactory = dataObjectFactory;
  }

  @Override
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

  @Override
  public void addCodeTables(final Collection<CodeTable> codeTables) {
    for (final CodeTable codeTable : codeTables) {
      addCodeTable(codeTable);
    }
  }

  protected void addMetaData(final DataObjectMetaData metaData) {
    final String typePath = metaData.getPath();
    final String schemaName = PathUtil.getPath(typePath);
    final DataObjectStoreSchema schema = getSchema(schemaName);
    schema.addMetaData(metaData);
  }

  protected void addMetaDataProperties(final DataObjectMetaDataImpl metaData) {
    final String typePath = metaData.getPath();
    for (final DataObjectMetaDataProperty property : commonMetaDataProperties) {
      final DataObjectMetaDataProperty clonedProperty = property.clone();
      clonedProperty.setMetaData(metaData);
    }
    final Map<String, Object> properties = typeMetaDataProperties.get(typePath);
    metaData.setProperties(properties);
  }

  protected void addSchema(final DataObjectStoreSchema schema) {
    schemaMap.put(schema.getPath(), schema);
  }

  @Override
  public void addStatistic(final String statisticName, final DataObject object) {
    statistics.add(statisticName, object);
  }

  @Override
  public void addStatistic(final String statisticName, final String typePath,
    final int count) {
    statistics.add(statisticName, typePath, count);
  }

  @Override
  @PreDestroy
  public void close() {
    try {
      super.close();
      if (statistics != null) {
        statistics.disconnect();
      }
      if (schemaMap != null) {
        for (final DataObjectStoreSchema schema : schemaMap.values()) {
          schema.close();
        }
        schemaMap.clear();
      }
    } finally {
      codeTableColumNames = null;
      columnToTableMap = null;
      commonMetaDataProperties = null;
      dataObjectFactory = null;
      geometryFactory = null;
      label = null;

      schemaMap = null;
      statistics = null;
      typeMetaDataProperties = null;
    }
  }

  @Override
  public DataObject create(final DataObjectMetaData objectMetaData) {
    final DataObjectMetaData metaData = getMetaData(objectMetaData);
    if (metaData == null) {
      return null;
    } else {
      DataObject object = dataObjectFactory.createDataObject(metaData);
      return object;
    }
  }

  @Override
  public DataObject createWithId(final DataObjectMetaData metaData) {
    DataObject object = create(metaData);
    if (object != null) {
      String idAttributeName = metaData.getIdAttributeName();
      if (StringUtils.hasText(idAttributeName)) {
        String typePath = metaData.getPath();
        Object id = createPrimaryIdValue(typePath);
        object.setIdValue(id);
      }
    }
    return object;
  }

  @Override
  public DataObject create(final String typePath) {
    final DataObjectMetaData metaData = getMetaData(typePath);
    if (metaData == null) {
      return null;
    } else {
      return create(metaData);
    }
  }

  public Query createBoundingBoxQuery(final Query query,
    final BoundingBox boundingBox) {
    throw new UnsupportedOperationException();
  }

  public AbstractIterator<DataObject> createIterator(final Query query,
    final Map<String, Object> properties) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> T createPrimaryIdValue(final String typePath) {
    return null;
  }

  @Override
  public Query createQuery(final String typePath, final String whereClause,
    final BoundingBox boundingBox) {
    throw new UnsupportedOperationException();
  }

  public DataObjectStoreQueryReader createReader() {
    final DataObjectStoreQueryReader reader = new DataObjectStoreQueryReader(
      this);
    return reader;
  }

  @Override
  public DataObjectReader createReader(final String typePath,
    final String query, final List<Object> parameters) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void delete(final DataObject object) {
    throw new UnsupportedOperationException("Delete not supported");
  }

  @Override
  public int delete(final Query query) {
    int i = 0;
    Reader<DataObject> reader = query(query);
    try {
      for (DataObject object : reader) {
        delete(object);
      }
    } finally {
      reader.close();
    }
    return i++;
  }

  @Override
  public void deleteAll(final Collection<DataObject> objects) {
    for (final DataObject object : objects) {
      delete(object);
    }
  }

  protected DataObjectMetaData findMetaData(final String typePath) {
    final String schemaName = PathUtil.getPath(typePath);
    final DataObjectStoreSchema schema = getSchema(schemaName);
    if (schema == null) {
      return null;
    } else {
      return schema.findMetaData(typePath);
    }
  }

  @Override
  public CodeTable getCodeTable(final String typePath) {
    final DataObjectMetaData metaData = getMetaData(typePath);
    if (metaData == null) {
      return null;
    } else {
      final CodeTableProperty codeTable = CodeTableProperty.getProperty(metaData);
      return codeTable;
    }
  }

  @Override
  public CodeTable getCodeTableByColumn(final String columnName) {
    final CodeTable codeTable = columnToTableMap.get(columnName);
    return codeTable;

  }

  @Override
  public Map<String, CodeTable> getCodeTableByColumnMap() {
    return new HashMap<String, CodeTable>(columnToTableMap);
  }

  public Map<String, List<String>> getCodeTableColumNames() {
    return codeTableColumNames;
  }

  @Override
  public DataObjectFactory getDataObjectFactory() {
    return this.dataObjectFactory;
  }

  public GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }

  @Override
  public String getLabel() {
    return label;
  }

  @Override
  public DataObjectMetaData getMetaData(final DataObjectMetaData objectMetaData) {
    final String typePath = objectMetaData.getPath();
    final DataObjectMetaData metaData = getMetaData(typePath);
    return metaData;
  }

  @Override
  public DataObjectMetaData getMetaData(final String typePath) {
    final String schemaName = PathUtil.getPath(typePath);
    final DataObjectStoreSchema schema = getSchema(schemaName);
    if (schema == null) {
      return null;
    } else {
      return schema.getMetaData(typePath);
    }
  }

  @Override
  public DataObjectStoreSchema getSchema(String schemaName) {
    if (schemaName == null) {
      return null;
    } else {
      synchronized (schemaMap) {
        if (schemaMap.isEmpty()) {
          loadSchemas(schemaMap);
        }
        if (!schemaName.startsWith("/")) {
          schemaName = "/" + schemaName;
        }
        return schemaMap.get(schemaName.toUpperCase());
      }
    }
  }

  public Map<String, DataObjectStoreSchema> getSchemaMap() {
    return schemaMap;
  }

  @Override
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

  @Override
  public StatisticsMap getStatistics() {
    return statistics;
  }

  public Statistics getStatistics(String name) {
    return statistics.getStatistics(name);
  }

  public String getString(final Object name) {
    if (name instanceof String) {
      return (String)name;
    } else {
      return String.valueOf(name.toString());
    }
  }

  @Override
  public List<String> getTypeNames(final String schemaName) {
    final DataObjectStoreSchema schema = getSchema(schemaName);
    if (schema == null) {
      return Collections.emptyList();
    } else {
      return schema.getTypeNames();
    }
  }

  @Override
  public List<DataObjectMetaData> getTypes(final String namespace) {
    final List<DataObjectMetaData> types = new ArrayList<DataObjectMetaData>();
    for (final String typePath : getTypeNames(namespace)) {
      types.add(getMetaData(typePath));
    }
    return types;
  }

  @Override
  public Writer<DataObject> getWriter() {
    return createWriter();
  }

  @Override
  @PostConstruct
  public void initialize() {
    statistics.connect();
  }

  @Override
  public void insert(final DataObject dataObject) {
    throw new UnsupportedOperationException("Insert not supported");
  }

  @Override
  public void insertAll(final Collection<DataObject> objects) {
    for (final DataObject object : objects) {
      insert(object);
    }
  }

  @Override
  public boolean isEditable(final String typePath) {
    return false;
  }

  @Override
  public DataObject load(final String typePath, final Object id) {
    final DataObjectMetaData metaData = getMetaData(typePath);
    if (metaData == null) {
      return null;
    } else {
      final String idAttributeName = metaData.getIdAttributeName();
      if (idAttributeName == null) {
        throw new IllegalArgumentException(typePath
          + " does not have a primary key");
      } else {
        final StringBuffer where = new StringBuffer();
        where.append(idAttributeName);
        where.append(" = ?");

        final Query query = new Query(typePath);
        query.setWhereClause(where.toString());
        query.addParameter(id);
        return queryFirst(query);
      }
    }
  }

  protected abstract void loadSchemaDataObjectMetaData(
    DataObjectStoreSchema schema, Map<String, DataObjectMetaData> metaDataMap);

  protected abstract void loadSchemas(
    Map<String, DataObjectStoreSchema> schemaMap);

  @Override
  public DataObject lock(final String typePath, final Object id) {
    final DataObjectMetaData metaData = getMetaData(typePath);
    if (metaData == null) {
      return null;
    } else {
      final String idAttributeName = metaData.getIdAttributeName();
      if (idAttributeName == null) {
        throw new IllegalArgumentException(typePath
          + " does not have a primary key");
      } else {
        final StringBuffer where = new StringBuffer();
        where.append(idAttributeName);
        where.append(" = ?");

        final Query query = new Query(typePath);
        query.setLockResults(true);
        query.setWhereClause(where.toString());
        query.addParameter(id);
        return queryFirst(query);
      }
    }
  }

  @Override
  public ResultPager<DataObject> page(final Query query) {
    final Reader<DataObject> results = query(query);
    final List<DataObject> list = results.read();
    return new ListResultPager<DataObject>(list);
  }

  @Override
  public Reader<DataObject> query(final List<Query> queries) {
    final DataObjectStoreQueryReader reader = createReader();
    reader.setQueries(queries);
    return reader;
  }

  @Override
  public Reader<DataObject> query(final Query... queries) {
    return query(Arrays.asList(queries));
  }

  @Override
  public Reader<DataObject> query(final String path) {
    final DataObjectStoreSchema schema = getSchema(path);
    if (schema == null) {
      final Query query = new Query(path);
      return query(query);
    } else {
      final List<Query> queries = new ArrayList<Query>();
      for (final String typeName : schema.getTypeNames()) {
        queries.add(new Query(typeName));
      }
      return query(queries);
    }
  }

  @Override
  public DataObject queryFirst(final Query query) {
    final Reader<DataObject> reader = query(query);
    try {
      final Iterator<DataObject> iterator = reader.iterator();
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

  protected void refreshSchema() {
    schemaMap.clear();
  }

  public void setCodeTableColumNames(
    final Map<String, List<String>> domainColumNames) {
    this.codeTableColumNames = domainColumNames;
  }

  public void setCommonMetaDataProperties(
    final List<DataObjectMetaDataProperty> commonMetaDataProperties) {
    this.commonMetaDataProperties = commonMetaDataProperties;
  }

  @Override
  public void setDataObjectFactory(final DataObjectFactory dataObjectFactory) {
    this.dataObjectFactory = dataObjectFactory;
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  @Override
  public void setLabel(final String label) {
    this.label = label;
    statistics.setPrefix(label);
  }

  public void setSchemaMap(final Map<String, DataObjectStoreSchema> schemaMap) {
    this.schemaMap = new DataObjectStoreSchemaMapProxy(this, schemaMap);
  }

  protected void setSharedAttribute(final String name, final Object value) {
    final Map<String, Object> sharedAttributes = getSharedAttributes();
    sharedAttributes.put(name, value);
  }

  public void setTypeMetaDataProperties(
    final Map<String, List<DataObjectMetaDataProperty>> typeMetaProperties) {
    for (final Entry<String, List<DataObjectMetaDataProperty>> typeProperties : typeMetaProperties.entrySet()) {
      final String typePath = typeProperties.getKey();
      Map<String, Object> currentProperties = this.typeMetaDataProperties.get(typePath);
      if (currentProperties == null) {
        currentProperties = new HashMap<String, Object>();
        this.typeMetaDataProperties.put(typePath, currentProperties);
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
    if (StringUtils.hasText(label)) {
      return label;
    } else {
      return super.toString();
    }
  }

  @Override
  public void update(final DataObject object) {
    throw new UnsupportedOperationException("Update not supported");
  }

  @Override
  public void updateAll(final Collection<DataObject> objects) {
    for (final DataObject object : objects) {
      update(object);
    }
  }
}
