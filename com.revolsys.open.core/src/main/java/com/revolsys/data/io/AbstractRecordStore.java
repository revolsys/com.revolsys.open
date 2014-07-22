package com.revolsys.data.io;

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

import com.revolsys.collection.AbstractIterator;
import com.revolsys.collection.ListResultPager;
import com.revolsys.collection.ResultPager;
import com.revolsys.collection.ThreadSharedAttributes;
import com.revolsys.data.codes.CodeTable;
import com.revolsys.data.codes.CodeTableProperty;
import com.revolsys.data.identifier.Identifier;
import com.revolsys.data.query.Q;
import com.revolsys.data.query.Query;
import com.revolsys.data.query.QueryValue;
import com.revolsys.data.record.ArrayRecordFactory;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.property.RecordDefinitionProperty;
import com.revolsys.data.record.schema.Attribute;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.gis.io.Statistics;
import com.revolsys.gis.io.StatisticsMap;
import com.revolsys.io.AbstractObjectWithProperties;
import com.revolsys.io.PathUtil;
import com.revolsys.io.Reader;
import com.revolsys.io.Writer;
import com.revolsys.jdbc.io.RecordStoreIteratorFactory;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.impl.BoundingBoxDoubleGf;
import com.revolsys.transaction.Propagation;
import com.revolsys.transaction.Transaction;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.ExceptionUtil;
import com.revolsys.util.Property;

public abstract class AbstractRecordStore extends AbstractObjectWithProperties
implements RecordStore {

  private Map<String, Object> connectionProperties = new HashMap<String, Object>();

  private Map<String, List<String>> codeTableColumNames = new HashMap<String, List<String>>();

  private RecordFactory recordFactory;

  private final Map<String, CodeTable> columnToTableMap = new HashMap<String, CodeTable>();

  private String label;

  private Map<String, RecordStoreSchema> schemaMap = new TreeMap<String, RecordStoreSchema>();

  private List<RecordDefinitionProperty> commonMetaDataProperties = new ArrayList<RecordDefinitionProperty>();

  private final Map<String, Map<String, Object>> typeMetaDataProperties = new HashMap<String, Map<String, Object>>();

  private final StatisticsMap statistics = new StatisticsMap();

  private GeometryFactory geometryFactory;

  private RecordStoreIteratorFactory iteratorFactory = new RecordStoreIteratorFactory();

  private final Set<RecordStoreExtension> recordStoreExtensions = new LinkedHashSet<RecordStoreExtension>();

  public AbstractRecordStore() {
    this(new ArrayRecordFactory());
  }

  public AbstractRecordStore(final RecordFactory recordFactory) {
    this.recordFactory = recordFactory;
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
    final List<String> columnNames = this.codeTableColumNames.get(codeTableName);
    if (columnNames != null) {
      for (final String columnName : columnNames) {
        addCodeTable(columnName, codeTable);
      }
    }
  }

  public void addCodeTable(final String columnName, final CodeTable codeTable) {
    if (columnName != null && !columnName.equalsIgnoreCase("ID")) {
      this.columnToTableMap.put(columnName, codeTable);
      for (final RecordStoreSchema schema : getSchemas()) {
        if (schema.isInitialized()) {
          for (final RecordDefinition recordDefinition : schema.getTypes()) {
            final String idFieldName = recordDefinition.getIdAttributeName();
            for (final Attribute attribute : recordDefinition.getAttributes()) {
              final String fieldName = attribute.getName();
              if (fieldName.equals(columnName)
                && !fieldName.equals(idFieldName)) {
                attribute.setCodeTable(codeTable);
              }
            }
          }
        }
      }
    }
  }

  @Override
  public void addCodeTables(final Collection<CodeTable> codeTables) {
    for (final CodeTable codeTable : codeTables) {
      addCodeTable(codeTable);
    }
  }

  protected void addMetaData(final RecordDefinition recordDefinition) {
    final String typePath = recordDefinition.getPath();
    final String schemaName = PathUtil.getPath(typePath);
    final RecordStoreSchema schema = getSchema(schemaName);
    schema.addMetaData(recordDefinition);
    final String idFieldName = recordDefinition.getIdAttributeName();
    for (final Attribute attribute : recordDefinition.getAttributes()) {
      final String fieldName = attribute.getName();
      if (!fieldName.equals(idFieldName)) {
        final CodeTable codeTable = this.columnToTableMap.get(fieldName);
        if (codeTable != null) {
          attribute.setCodeTable(codeTable);
        }
      }
    }
  }

  protected void addMetaDataProperties(
    final RecordDefinitionImpl recordDefinition) {
    final String typePath = recordDefinition.getPath();
    for (final RecordDefinitionProperty property : this.commonMetaDataProperties) {
      final RecordDefinitionProperty clonedProperty = property.clone();
      clonedProperty.setRecordDefinition(recordDefinition);
    }
    final Map<String, Object> properties = this.typeMetaDataProperties.get(typePath);
    recordDefinition.setProperties(properties);
  }

  public void addRecordStoreExtension(final RecordStoreExtension extension) {
    if (extension != null) {
      try {
        final Map<String, Object> connectionProperties = getConnectionProperties();
        extension.initialize(this, connectionProperties);
        this.recordStoreExtensions.add(extension);
      } catch (final Throwable e) {
        ExceptionUtil.log(extension.getClass(), "Unable to initialize", e);
      }
    }
  }

  protected void addSchema(final RecordStoreSchema schema) {
    this.schemaMap.put(schema.getPath(), schema);
  }

  @Override
  public void addStatistic(final String statisticName, final Record object) {
    if (this.statistics != null) {
      this.statistics.add(statisticName, object);
    }
  }

  @Override
  public void addStatistic(final String statisticName, final String typePath,
    final int count) {
    if (this.statistics != null) {
      this.statistics.add(statisticName, typePath, count);
    }
  }

  @Override
  public void appendQueryValue(final Query query, final StringBuffer sql,
    final QueryValue queryValue) {
    queryValue.appendDefaultSql(query, this, sql);
  }

  @Override
  @PreDestroy
  public void close() {
    try {
      super.close();
      if (this.statistics != null) {
        this.statistics.disconnect();
      }
      if (this.schemaMap != null) {
        for (final RecordStoreSchema schema : this.schemaMap.values()) {
          schema.close();
        }
        this.schemaMap.clear();
      }
    } finally {
      this.codeTableColumNames.clear();
      this.columnToTableMap.clear();
      this.commonMetaDataProperties.clear();
      this.connectionProperties.clear();
      this.recordFactory = null;
      this.recordStoreExtensions.clear();
      this.iteratorFactory = null;
      this.label = "deleted";
      this.schemaMap.clear();
      this.statistics.clear();
      this.typeMetaDataProperties.clear();
    }
  }

  @Override
  public Record copy(final Record record) {
    final RecordDefinition recordDefinition = getRecordDefinition(record.getRecordDefinition());
    final RecordFactory recordFactory = this.recordFactory;
    if (recordDefinition == null || recordFactory == null) {
      return null;
    } else {
      final Record copy = recordFactory.createRecord(recordDefinition);
      copy.setValues(record);
      copy.setIdValue(null);
      return copy;
    }
  }

  @Override
  public Record create(final RecordDefinition objectMetaData) {
    final RecordDefinition recordDefinition = getRecordDefinition(objectMetaData);
    final RecordFactory recordFactory = this.recordFactory;
    if (recordDefinition == null || recordFactory == null) {
      return null;
    } else {
      final Record object = recordFactory.createRecord(recordDefinition);
      return object;
    }
  }

  @Override
  public Record create(final String typePath) {
    final RecordDefinition recordDefinition = getRecordDefinition(typePath);
    if (recordDefinition == null) {
      return null;
    } else {
      return create(recordDefinition);
    }
  }

  @Override
  public Record create(final String typePath,
    final Map<String, ? extends Object> values) {
    final RecordDefinition recordDefinition = getRecordDefinition(typePath);
    if (recordDefinition == null) {
      throw new IllegalArgumentException("Cannot find table " + typePath
        + " for " + this);
    } else {
      final Record record = create(recordDefinition);
      if (record != null) {
        record.setValues(values);
        final String idAttributeName = recordDefinition.getIdAttributeName();
        if (Property.hasValue(idAttributeName)) {
          if (values.get(idAttributeName) == null) {
            final Object id = createPrimaryIdValue(typePath);
            record.setIdValue(id);
          }
        }
      }
      return record;
    }

  }

  public AbstractIterator<Record> createIterator(final Query query,
    Map<String, Object> properties) {
    if (properties == null) {
      properties = Collections.emptyMap();
    }
    if (query == null) {
      return null;
    } else {
      final RecordDefinition recordDefinition = query.getRecordDefinition();
      if (recordDefinition != null) {
        final RecordStoreIteratorFactory recordDefinitionIteratorFactory = recordDefinition.getProperty("recordStoreIteratorFactory");
        if (recordDefinitionIteratorFactory != null) {
          final AbstractIterator<Record> iterator = recordDefinitionIteratorFactory.createIterator(
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
    final BoundingBoxDoubleGf boundingBox) {
    throw new UnsupportedOperationException();
  }

  public RecordStoreQueryReader createReader() {
    final RecordStoreQueryReader reader = new RecordStoreQueryReader(this);
    return reader;
  }

  @Override
  public Transaction createTransaction(final Propagation propagation) {
    final PlatformTransactionManager transactionManager = getTransactionManager();
    return new Transaction(transactionManager, propagation);
  }

  @Override
  public Record createWithId(final RecordDefinition recordDefinition) {
    final Record record = create(recordDefinition);
    if (record != null) {
      final String idAttributeName = recordDefinition.getIdAttributeName();
      if (Property.hasValue(idAttributeName)) {
        final String typePath = recordDefinition.getPath();
        final Object id = createPrimaryIdValue(typePath);
        record.setIdValue(id);
      }
    }
    return record;
  }

  @Override
  public int delete(final Query query) {
    int i = 0;
    final Reader<Record> reader = query(query);
    try {
      for (final Record object : reader) {
        delete(object);
        i++;
      }
    } finally {
      reader.close();
    }
    return i;
  }

  @Override
  public void delete(final Record object) {
    throw new UnsupportedOperationException("Delete not supported");
  }

  @Override
  public void deleteAll(final Collection<Record> objects) {
    for (final Record object : objects) {
      delete(object);
    }
  }

  protected RecordDefinition findMetaData(final String typePath) {
    final String schemaName = PathUtil.getPath(typePath);
    final RecordStoreSchema schema = getSchema(schemaName);
    if (schema == null) {
      return null;
    } else {
      return schema.findMetaData(typePath);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends CodeTable> V getCodeTable(final String typePath) {
    final RecordDefinition recordDefinition = getRecordDefinition(typePath);
    if (recordDefinition == null) {
      return null;
    } else {
      final CodeTableProperty codeTable = CodeTableProperty.getProperty(recordDefinition);
      return (V)codeTable;
    }
  }

  @Override
  public CodeTable getCodeTableByColumn(final String columnName) {
    final CodeTable codeTable = this.columnToTableMap.get(columnName);
    return codeTable;

  }

  @Override
  public Map<String, CodeTable> getCodeTableByColumnMap() {
    return new HashMap<String, CodeTable>(this.columnToTableMap);
  }

  public Map<String, List<String>> getCodeTableColumNames() {
    return this.codeTableColumNames;
  }

  protected Map<String, Object> getConnectionProperties() {
    return this.connectionProperties;
  }

  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  public RecordStoreIteratorFactory getIteratorFactory() {
    return this.iteratorFactory;
  }

  @Override
  public String getLabel() {
    return this.label;
  }

  @Override
  public RecordDefinition getRecordDefinition(
    final RecordDefinition objectMetaData) {
    final String typePath = objectMetaData.getPath();
    final RecordDefinition recordDefinition = getRecordDefinition(typePath);
    return recordDefinition;
  }

  @Override
  public RecordDefinition getRecordDefinition(final String typePath) {
    final String schemaName = PathUtil.getPath(typePath);
    final RecordStoreSchema schema = getSchema(schemaName);
    if (schema == null) {
      return null;
    } else {
      return schema.getRecordDefinition(typePath);
    }
  }

  @Override
  public RecordFactory getRecordFactory() {
    return this.recordFactory;
  }

  public Collection<RecordStoreExtension> getRecordStoreExtensions() {
    return this.recordStoreExtensions;
  }

  @Override
  public RecordStoreSchema getSchema(String schemaName) {
    if (schemaName == null || this.schemaMap == null) {
      return null;
    } else {
      synchronized (this.schemaMap) {
        if (this.schemaMap.isEmpty()) {
          loadSchemas(this.schemaMap);
        }
        if (!schemaName.startsWith("/")) {
          schemaName = "/" + schemaName;
        }
        return this.schemaMap.get(schemaName.toUpperCase());
      }
    }
  }

  public Map<String, RecordStoreSchema> getSchemaMap() {
    return this.schemaMap;
  }

  @Override
  public List<RecordStoreSchema> getSchemas() {
    synchronized (this.schemaMap) {
      if (this.schemaMap.isEmpty()) {
        loadSchemas(this.schemaMap);
      }
      return new ArrayList<RecordStoreSchema>(this.schemaMap.values());
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
    return this.statistics;
  }

  @Override
  public Statistics getStatistics(final String name) {
    return this.statistics.getStatistics(name);
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
    final RecordStoreSchema schema = getSchema(schemaName);
    if (schema == null) {
      return Collections.emptyList();
    } else {
      return schema.getTypeNames();
    }
  }

  @Override
  public List<RecordDefinition> getTypes(final String namespace) {
    final List<RecordDefinition> types = new ArrayList<RecordDefinition>();
    for (final String typePath : getTypeNames(namespace)) {
      types.add(getRecordDefinition(typePath));
    }
    return types;
  }

  @Override
  public String getUrl() {
    return (String)this.connectionProperties.get("url");
  }

  @Override
  public String getUsername() {
    return (String)this.connectionProperties.get("username");
  }

  @Override
  public Writer<Record> getWriter() {
    return createWriter();
  }

  @Override
  public Writer<Record> getWriter(final boolean throwExceptions) {
    return getWriter();
  }

  @Override
  public boolean hasSchema(final String schemaName) {
    return getSchema(schemaName) != null;
  }

  @Override
  @PostConstruct
  public void initialize() {
    this.statistics.connect();
  }

  @Override
  public void insert(final Record record) {
    throw new UnsupportedOperationException("Insert not supported");
  }

  @Override
  public void insertAll(final Collection<Record> objects) {
    for (final Record object : objects) {
      insert(object);
    }
  }

  @Override
  public boolean isEditable(final String typePath) {
    return false;
  }

  @Override
  public Record load(final String typePath, final Identifier id) {
    final RecordDefinition recordDefinition = getRecordDefinition(typePath);
    if (recordDefinition == null) {
      return null;
    } else {
      final List<Object> values = id.getValues();
      final List<String> idAttributeNames = recordDefinition.getIdAttributeNames();
      if (idAttributeNames.isEmpty()) {
        throw new IllegalArgumentException(typePath
          + " does not have a primary key");
      } else if (values.size() != idAttributeNames.size()) {
        throw new IllegalArgumentException(id + " not a valid id for "
          + typePath + " requires " + idAttributeNames);
      } else {
        final Query query = new Query(recordDefinition);
        for (int i = 0; i < idAttributeNames.size(); i++) {
          final String name = idAttributeNames.get(i);
          final Object value = values.get(i);
          final Attribute attribute = recordDefinition.getAttribute(name);
          query.and(Q.equal(attribute, value));
        }
        return queryFirst(query);
      }
    }
  }

  @Override
  public Record load(final String typePath, final Object... id) {
    final RecordDefinition recordDefinition = getRecordDefinition(typePath);
    if (recordDefinition == null) {
      return null;
    } else {
      final List<String> idAttributeNames = recordDefinition.getIdAttributeNames();
      if (idAttributeNames.isEmpty()) {
        throw new IllegalArgumentException(typePath
          + " does not have a primary key");
      } else if (id.length != idAttributeNames.size()) {
        throw new IllegalArgumentException(Arrays.toString(id)
          + " not a valid id for " + typePath + " requires " + idAttributeNames);
      } else {
        final Query query = new Query(recordDefinition);
        for (int i = 0; i < idAttributeNames.size(); i++) {
          final String name = idAttributeNames.get(i);
          final Object value = id[i];
          final Attribute attribute = recordDefinition.getAttribute(name);
          query.and(Q.equal(attribute, value));
        }
        return queryFirst(query);
      }
    }
  }

  protected abstract void loadSchemaRecordDefinitions(RecordStoreSchema schema,
    Map<String, RecordDefinition> recordDefinitionMap);

  protected abstract void loadSchemas(Map<String, RecordStoreSchema> schemaMap);

  @Override
  public Record lock(final String typePath, final Object id) {
    final RecordDefinition recordDefinition = getRecordDefinition(typePath);
    if (recordDefinition == null) {
      return null;
    } else {
      final String idAttributeName = recordDefinition.getIdAttributeName();
      if (idAttributeName == null) {
        throw new IllegalArgumentException(typePath
          + " does not have a primary key");
      } else {
        final Query query = Query.equal(recordDefinition, idAttributeName, id);
        query.setLockResults(true);
        return queryFirst(query);
      }
    }
  }

  @Override
  public ResultPager<Record> page(final Query query) {
    final Reader<Record> results = query(query);
    final List<Record> list = results.read();
    return new ListResultPager<Record>(list);
  }

  @Override
  public Reader<Record> query(final List<?> queries) {
    final List<Query> queryObjects = new ArrayList<Query>();
    for (final Object object : queries) {
      if (object instanceof Query) {
        final Query query = (Query)object;
        queryObjects.add(query);
        // System.out.println(query.getTypeName() + " "
        // + query.getWhereCondition());
      } else {
        final Query query = new Query(object.toString());
        queryObjects.add(query);
      }
    }
    final RecordStoreQueryReader reader = createReader();
    reader.setQueries(queryObjects);

    return reader;
  }

  @Override
  public Reader<Record> query(final Query... queries) {
    return query(Arrays.asList(queries));
  }

  @Override
  public Reader<Record> query(final String path) {
    final RecordStoreSchema schema = getSchema(path);
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
  public Record queryFirst(final Query query) {
    final Reader<Record> reader = query(query);
    try {
      final Iterator<Record> iterator = reader.iterator();
      if (iterator.hasNext()) {
        final Record object = iterator.next();
        return object;
      } else {
        return null;
      }
    } finally {
      reader.close();
    }
  }

  protected void refreshMetaData(final String schemaName) {
    final RecordStoreSchema schema = getSchema(schemaName);
    if (schema != null) {
      schema.refreshMetaData();
    }
  }

  protected void refreshSchema() {
    this.schemaMap.clear();
  }

  public void setCodeTableColumNames(
    final Map<String, List<String>> domainColumNames) {
    this.codeTableColumNames = domainColumNames;
  }

  public void setCommonMetaDataProperties(
    final List<RecordDefinitionProperty> commonMetaDataProperties) {
    this.commonMetaDataProperties = commonMetaDataProperties;
  }

  protected void setConnectionProperties(
    final Map<String, ? extends Object> connectionProperties) {
    this.connectionProperties = CollectionUtil.createHashMap(connectionProperties);
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  public void setIteratorFactory(
    final RecordStoreIteratorFactory iteratorFactory) {
    this.iteratorFactory = iteratorFactory;
  }

  @Override
  public void setLabel(final String label) {
    this.label = label;
    this.statistics.setPrefix(label);
  }

  @Override
  public void setLogCounts(final boolean logCounts) {
    this.statistics.setLogCounts(logCounts);
  }

  @Override
  public void setRecordFactory(final RecordFactory recordFactory) {
    this.recordFactory = recordFactory;
  }

  public void setSchemaMap(final Map<String, RecordStoreSchema> schemaMap) {
    this.schemaMap = new RecordStoreSchemaMapProxy(this, schemaMap);
  }

  protected void setSharedAttribute(final String name, final Object value) {
    final Map<String, Object> sharedAttributes = getSharedAttributes();
    sharedAttributes.put(name, value);
  }

  public void setTypeMetaDataProperties(
    final Map<String, List<RecordDefinitionProperty>> typeMetaProperties) {
    for (final Entry<String, List<RecordDefinitionProperty>> typeProperties : typeMetaProperties.entrySet()) {
      final String typePath = typeProperties.getKey();
      Map<String, Object> currentProperties = this.typeMetaDataProperties.get(typePath);
      if (currentProperties == null) {
        currentProperties = new LinkedHashMap<String, Object>();
        this.typeMetaDataProperties.put(typePath, currentProperties);
      }
      final List<RecordDefinitionProperty> properties = typeProperties.getValue();
      for (final RecordDefinitionProperty property : properties) {
        final String name = property.getPropertyName();
        currentProperties.put(name, property);
      }
    }
  }

  @Override
  public String toString() {
    if (Property.hasValue(this.label)) {
      return this.label;
    } else {
      return super.toString();
    }
  }

  @Override
  public void update(final Record object) {
    throw new UnsupportedOperationException("Update not supported");
  }

  @Override
  public void updateAll(final Collection<Record> objects) {
    for (final Record object : objects) {
      update(object);
    }
  }
}
