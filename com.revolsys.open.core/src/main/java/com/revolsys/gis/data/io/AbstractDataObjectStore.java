package com.revolsys.gis.data.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StringUtils;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.collection.ListResultPager;
import com.revolsys.collection.ResultPager;
import com.revolsys.collection.ThreadSharedAttributes;
import com.revolsys.filter.Filter;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.ArrayDataObjectFactory;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.DataObjectMetaDataProperty;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.gis.data.model.codes.CodeTableProperty;
import com.revolsys.gis.data.model.filter.DataObjectGeometryIntersectsFilter;
import com.revolsys.gis.data.query.Q;
import com.revolsys.gis.data.query.Query;
import com.revolsys.gis.io.Statistics;
import com.revolsys.gis.io.StatisticsMap;
import com.revolsys.io.AbstractObjectWithProperties;
import com.revolsys.io.FilterReader;
import com.revolsys.io.PathUtil;
import com.revolsys.io.Reader;
import com.revolsys.io.Writer;
import com.revolsys.jdbc.io.DataStoreIteratorFactory;
import com.revolsys.transaction.Propagation;
import com.revolsys.transaction.Transaction;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.ExceptionUtil;
import com.vividsolutions.jts.geom.Geometry;

public abstract class AbstractDataObjectStore extends
  AbstractObjectWithProperties implements DataObjectStore {

  public static DataObjectStore close(
    final Collection<DataObjectStore> dataStores) {
    final List<RuntimeException> exceptions = new ArrayList<RuntimeException>();
    for (final DataObjectStore dataStore : dataStores) {
      if (dataStore != null) {
        try {
          dataStore.close();
        } catch (final RuntimeException e) {
          exceptions.add(e);
        }
      }
    }
    if (!exceptions.isEmpty()) {
      throw exceptions.get(0);
    }
    return null;
  }

  public static DataObjectStore close(final DataObjectStore... dataStores) {
    return close(Arrays.asList(dataStores));
  }

  private Map<String, Object> connectionProperties = new HashMap<String, Object>();

  private Map<String, List<String>> codeTableColumNames = new HashMap<String, List<String>>();

  private DataObjectFactory dataObjectFactory;

  private final Map<String, CodeTable> columnToTableMap = new HashMap<String, CodeTable>();

  private String label;

  private Map<String, DataObjectStoreSchema> schemaMap = new TreeMap<String, DataObjectStoreSchema>();

  private List<DataObjectMetaDataProperty> commonMetaDataProperties = new ArrayList<DataObjectMetaDataProperty>();

  private final Map<String, Map<String, Object>> typeMetaDataProperties = new HashMap<String, Map<String, Object>>();

  private final StatisticsMap statistics = new StatisticsMap();

  private GeometryFactory geometryFactory;

  private DataStoreIteratorFactory iteratorFactory = new DataStoreIteratorFactory();

  private final Set<DataObjectStoreExtension> dataStoreExtensions = new LinkedHashSet<DataObjectStoreExtension>();

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

  public void addDataStoreExtension(final DataObjectStoreExtension extension) {
    if (extension != null) {
      try {
        final Map<String, Object> connectionProperties = getConnectionProperties();
        extension.initialize(this, connectionProperties);
        dataStoreExtensions.add(extension);
      } catch (final Throwable e) {
        ExceptionUtil.log(extension.getClass(), "Unable to initialize", e);
      }
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
    if (statistics != null) {
      statistics.add(statisticName, object);
    }
  }

  @Override
  public void addStatistic(final String statisticName, final String typePath,
    final int count) {
    if (statistics != null) {
      statistics.add(statisticName, typePath, count);
    }
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
      codeTableColumNames.clear();
      columnToTableMap.clear();
      commonMetaDataProperties.clear();
      connectionProperties.clear();
      dataObjectFactory = null;
      dataStoreExtensions.clear();
      iteratorFactory = null;
      label = "deleted";
      schemaMap.clear();
      statistics.clear();
      typeMetaDataProperties.clear();
    }
  }

  @Override
  public DataObject copy(final DataObject record) {
    final DataObjectMetaData metaData = getMetaData(record.getMetaData());
    final DataObjectFactory dataObjectFactory = this.dataObjectFactory;
    if (metaData == null || dataObjectFactory == null) {
      return null;
    } else {
      final DataObject copy = dataObjectFactory.createDataObject(metaData);
      copy.setValues(record);
      copy.setIdValue(null);
      return copy;
    }
  }

  @Override
  public DataObject create(final DataObjectMetaData objectMetaData) {
    final DataObjectMetaData metaData = getMetaData(objectMetaData);
    final DataObjectFactory dataObjectFactory = this.dataObjectFactory;
    if (metaData == null || dataObjectFactory == null) {
      return null;
    } else {
      final DataObject object = dataObjectFactory.createDataObject(metaData);
      return object;
    }
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

  public AbstractIterator<DataObject> createIterator(final Query query,
    Map<String, Object> properties) {
    if (properties == null) {
      properties = Collections.emptyMap();
    }
    if (query == null) {
      return null;
    } else {
      final DataObjectMetaData metaData = query.getMetaData();
      if (metaData != null) {
        final DataStoreIteratorFactory metaDataIteratorFactory = metaData.getProperty("dataStoreIteratorFactory");
        if (metaDataIteratorFactory != null) {
          final AbstractIterator<DataObject> iterator = metaDataIteratorFactory.createIterator(
            this, query, properties);
          if (iterator != null) {
            return iterator;
          }
        }
      }
      return this.iteratorFactory.createIterator(this, query, properties);
    }
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
  public Transaction createTransaction(final Propagation propagation) {
    final PlatformTransactionManager transactionManager = getTransactionManager();
    return new Transaction(transactionManager, propagation);
  }

  @Override
  public DataObject createWithId(final DataObjectMetaData metaData) {
    final DataObject object = create(metaData);
    if (object != null) {
      final String idAttributeName = metaData.getIdAttributeName();
      if (StringUtils.hasText(idAttributeName)) {
        final String typePath = metaData.getPath();
        final Object id = createPrimaryIdValue(typePath);
        object.setIdValue(id);
      }
    }
    return object;
  }

  @Override
  public void delete(final DataObject object) {
    throw new UnsupportedOperationException("Delete not supported");
  }

  @Override
  public int delete(final Query query) {
    int i = 0;
    final Reader<DataObject> reader = query(query);
    try {
      for (final DataObject object : reader) {
        delete(object);
        i++;
      }
    } finally {
      reader.close();
    }
    return i;
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

  protected Map<String, Object> getConnectionProperties() {
    return connectionProperties;
  }

  @Override
  public DataObjectFactory getDataObjectFactory() {
    return this.dataObjectFactory;
  }

  public Collection<DataObjectStoreExtension> getDataStoreExtensions() {
    return dataStoreExtensions;
  }

  public GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }

  public DataStoreIteratorFactory getIteratorFactory() {
    return iteratorFactory;
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
    if (schemaName == null || schemaMap == null) {
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

  protected Map<String, Object> getSharedAttributes() {
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

  public Statistics getStatistics(final String name) {
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
  public PlatformTransactionManager getTransactionManager() {
    return null;
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
  public String getUrl() {
    return (String)connectionProperties.get("url");
  }

  @Override
  public String getUsername() {
    return (String)connectionProperties.get("username");
  }

  @Override
  public Writer<DataObject> getWriter() {
    return createWriter();
  }

  @Override
  public Writer<DataObject> getWriter(final boolean throwExceptions) {
    return getWriter();
  }

  @Override
  public boolean hasSchema(final String schemaName) {
    return getSchema(schemaName) != null;
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
  public DataObject load(final String typePath, final Object... id) {
    final DataObjectMetaData metaData = getMetaData(typePath);
    if (metaData == null) {
      return null;
    } else {
      final List<String> idAttributeNames = metaData.getIdAttributeNames();
      if (idAttributeNames.isEmpty()) {
        throw new IllegalArgumentException(typePath
          + " does not have a primary key");
      } else if (id.length != idAttributeNames.size()) {
        throw new IllegalArgumentException(Arrays.toString(id)
          + " not a valid id for " + typePath + " requires " + idAttributeNames);
      } else {
        final Query query = new Query(metaData);
        for (int i = 0; i < idAttributeNames.size(); i++) {
          final String name = idAttributeNames.get(i);
          final Object value = id[i];
          final Attribute attribute = metaData.getAttribute(name);
          query.and(Q.equal(attribute, value));
        }
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
        final Query query = Query.equal(metaData, idAttributeName, id);
        query.setLockResults(true);
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
  public Reader<DataObject> query(final DataObjectFactory dataObjectFactory,
    final String typePath, final Geometry geometry) {
    final BoundingBox boundingBox = BoundingBox.getBoundingBox(geometry);
    final Query query = new Query(typePath);
    query.setBoundingBox(boundingBox);
    query.setProperty("dataObjectFactory", dataObjectFactory);
    final Reader<DataObject> reader = query(query);
    final Filter<DataObject> filter = new DataObjectGeometryIntersectsFilter(
      geometry);
    return new FilterReader<DataObject>(filter, reader);
  }

  @Override
  public Reader<DataObject> query(final DataObjectFactory dataObjectFactory,
    final String typePath, final Geometry geometry, final double distance) {
    final Geometry searchGeometry;
    if (geometry == null || geometry.isEmpty() || distance <= 0) {
      searchGeometry = geometry;
    } else {
      final Geometry bufferedGeometry = geometry.buffer(distance);
      if (bufferedGeometry.isEmpty()) {
        searchGeometry = geometry;
      } else {
        searchGeometry = bufferedGeometry;
      }
    }
    return query(dataObjectFactory, typePath, searchGeometry);
  }

  @Override
  public Reader<DataObject> query(final List<?> queries) {
    final List<Query> queryObjects = new ArrayList<Query>();
    for (final Object object : queries) {
      if (object instanceof Query) {
        final Query query = (Query)object;
        queryObjects.add(query);
      } else {
        final Query query = new Query(object.toString());
        queryObjects.add(query);
      }
    }
    final DataObjectStoreQueryReader reader = createReader();
    reader.setQueries(queryObjects);
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
  public Reader<DataObject> query(final String typePath, final Geometry geometry) {
    final DataObjectFactory dataObjectFactory = getDataObjectFactory();
    return query(dataObjectFactory, typePath, geometry);
  }

  @Override
  public Reader<DataObject> query(final String typePath,
    final Geometry geometry, final double distance) {
    final DataObjectFactory dataObjectFactory = getDataObjectFactory();
    return query(dataObjectFactory, typePath, geometry, distance);
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

  protected void setConnectionProperties(
    final Map<String, ? extends Object> connectionProperties) {
    this.connectionProperties = CollectionUtil.createHashMap(connectionProperties);
  }

  @Override
  public void setDataObjectFactory(final DataObjectFactory dataObjectFactory) {
    this.dataObjectFactory = dataObjectFactory;
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  public void setIteratorFactory(final DataStoreIteratorFactory iteratorFactory) {
    this.iteratorFactory = iteratorFactory;
  }

  @Override
  public void setLabel(final String label) {
    this.label = label;
    statistics.setPrefix(label);
  }

  @Override
  public void setLogCounts(final boolean logCounts) {
    statistics.setLogCounts(logCounts);
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
        currentProperties = new LinkedHashMap<String, Object>();
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
