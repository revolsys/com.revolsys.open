package com.revolsys.data.record.schema;

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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.transaction.PlatformTransactionManager;

import com.revolsys.collection.ListResultPager;
import com.revolsys.collection.ResultPager;
import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.collection.map.Maps;
import com.revolsys.collection.map.ThreadSharedAttributes;
import com.revolsys.data.codes.CodeTable;
import com.revolsys.data.codes.CodeTableProperty;
import com.revolsys.data.identifier.Identifier;
import com.revolsys.data.query.Q;
import com.revolsys.data.query.Query;
import com.revolsys.data.query.QueryValue;
import com.revolsys.data.record.ArrayRecordFactory;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.io.RecordStoreExtension;
import com.revolsys.data.record.io.RecordStoreQueryReader;
import com.revolsys.data.record.property.RecordDefinitionProperty;
import com.revolsys.gis.io.Statistics;
import com.revolsys.gis.io.StatisticsMap;
import com.revolsys.io.Path;
import com.revolsys.io.PathName;
import com.revolsys.io.Reader;
import com.revolsys.io.Writer;
import com.revolsys.jdbc.io.RecordStoreIteratorFactory;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.impl.BoundingBoxDoubleGf;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.transaction.Propagation;
import com.revolsys.transaction.Transaction;
import com.revolsys.util.ExceptionUtil;
import com.revolsys.util.Property;

public abstract class AbstractRecordStore extends BaseObjectWithProperties implements RecordStore {

  private Map<String, Object> connectionProperties = new HashMap<String, Object>();

  private Map<String, List<String>> codeTableColumNames = new HashMap<String, List<String>>();

  private RecordFactory recordFactory;

  private final Map<String, CodeTable> columnToTableMap = new HashMap<String, CodeTable>();

  private String label;

  private final RecordStoreSchema rootSchema = new RecordStoreSchema(this);

  private List<RecordDefinitionProperty> commonRecordDefinitionProperties = new ArrayList<RecordDefinitionProperty>();

  private final Map<String, Map<String, Object>> typeRecordDefinitionProperties = new HashMap<String, Map<String, Object>>();

  private final StatisticsMap statistics = new StatisticsMap();

  private GeometryFactory geometryFactory;

  private RecordStoreIteratorFactory iteratorFactory = new RecordStoreIteratorFactory();

  private final Set<RecordStoreExtension> recordStoreExtensions = new LinkedHashSet<RecordStoreExtension>();

  private boolean loadFullSchema = true;

  public AbstractRecordStore() {
    this(new ArrayRecordFactory());
  }

  public AbstractRecordStore(final RecordFactory recordFactory) {
    this.recordFactory = recordFactory;
  }

  @Override
  public void addCodeTable(final CodeTable codeTable) {
    final String idColumn = codeTable.getIdFieldName();
    addCodeTable(idColumn, codeTable);
    final List<String> fieldAliases = codeTable.getFieldAliases();
    for (final String alias : fieldAliases) {
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
      final RecordStoreSchema rootSchema = getRootSchema();
      addCodeTableColumns(rootSchema, codeTable, columnName);
    }
  }

  protected void addCodeTableColumns(final RecordStoreSchema schema, final CodeTable codeTable,
    final String columnName) {
    if (schema.isInitialized()) {
      for (final RecordStoreSchema childSchema : schema.getSchemas()) {
        addCodeTableColumns(childSchema, codeTable, columnName);
      }
      for (final RecordDefinition recordDefinition : schema.getRecordDefinitions()) {
        final String idFieldName = recordDefinition.getIdFieldName();
        for (final FieldDefinition field : recordDefinition.getFields()) {
          final String fieldName = field.getName();
          if (fieldName.equals(columnName) && !fieldName.equals(idFieldName)) {
            field.setCodeTable(codeTable);
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

  protected void addRecordDefinition(final RecordDefinition recordDefinition) {
    final String idFieldName = recordDefinition.getIdFieldName();
    for (final FieldDefinition field : recordDefinition.getFields()) {
      final String fieldName = field.getName();
      if (!fieldName.equals(idFieldName)) {
        final CodeTable codeTable = this.columnToTableMap.get(fieldName);
        if (codeTable != null) {
          field.setCodeTable(codeTable);
        }
      }
    }
  }

  protected void addRecordDefinitionProperties(final RecordDefinitionImpl recordDefinition) {
    final String typePath = recordDefinition.getPath();
    for (final RecordDefinitionProperty property : this.commonRecordDefinitionProperties) {
      final RecordDefinitionProperty clonedProperty = property.clone();
      clonedProperty.setRecordDefinition(recordDefinition);
    }
    final Map<String, Object> properties = this.typeRecordDefinitionProperties.get(typePath);
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

  @Override
  public void addStatistic(final String statisticName, final Record object) {
    if (this.statistics != null) {
      this.statistics.add(statisticName, object);
    }
  }

  @Override
  public void addStatistic(final String statisticName, final String typePath, final int count) {
    if (this.statistics != null) {
      this.statistics.add(statisticName, typePath, count);
    }
  }

  @Override
  public void appendQueryValue(final Query query, final StringBuilder sql,
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
      getRootSchema().close();
    } finally {
      this.codeTableColumNames.clear();
      this.columnToTableMap.clear();
      this.commonRecordDefinitionProperties.clear();
      this.connectionProperties.clear();
      this.recordFactory = null;
      this.recordStoreExtensions.clear();
      this.iteratorFactory = null;
      this.label = "deleted";
      this.statistics.clear();
      this.typeRecordDefinitionProperties.clear();
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
  public Record create(final PathName typePath) {
    final RecordDefinition recordDefinition = getRecordDefinition(typePath);
    if (recordDefinition == null) {
      return null;
    } else {
      return create(recordDefinition);
    }
  }

  @Override
  public Record create(final RecordDefinition objectRecordDefinition) {
    final RecordDefinition recordDefinition = getRecordDefinition(objectRecordDefinition);
    final RecordFactory recordFactory = this.recordFactory;
    if (recordDefinition == null || recordFactory == null) {
      return null;
    } else {
      final Record object = recordFactory.createRecord(recordDefinition);
      return object;
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
        final RecordStoreIteratorFactory recordDefinitionIteratorFactory = recordDefinition
          .getProperty("recordStoreIteratorFactory");
        if (recordDefinitionIteratorFactory != null) {
          final AbstractIterator<Record> iterator = recordDefinitionIteratorFactory
            .createIterator(this, query, properties);
          if (iterator != null) {
            return iterator;
          }
        }
      }
      return this.iteratorFactory.createIterator(this, query, properties);
    }
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
      final String idFieldName = recordDefinition.getIdFieldName();
      if (Property.hasValue(idFieldName)) {
        final PathName typePath = recordDefinition.getPathName();
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

  protected RecordDefinition findRecordDefinition(final String typePath) {
    final String schemaName = Path.getPath(typePath);
    final RecordStoreSchema schema = getSchema(schemaName);
    if (schema == null) {
      return null;
    } else {
      return schema.findRecordDefinition(typePath);
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
  public CodeTable getCodeTableByFieldName(final String columnName) {
    final CodeTable codeTable = this.columnToTableMap.get(columnName);

    return codeTable;

  }

  @Override
  public Map<String, CodeTable> getCodeTableByFieldNameMap() {
    return new HashMap<String, CodeTable>(this.columnToTableMap);
  }

  public Map<String, List<String>> getCodeTableColumNames() {
    return this.codeTableColumNames;
  }

  @Override
  public RecordStoreConnected getConnected() {
    return new RecordStoreConnected(this);
  }

  protected Map<String, Object> getConnectionProperties() {
    return this.connectionProperties;
  }

  @Override
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
  public RecordDefinition getRecordDefinition(final PathName typePath) {
    if (typePath == null) {
      return null;
    } else {

      final PathName schemaPath = typePath.getParent();
      final RecordStoreSchema schema = getSchema(schemaPath);
      if (schema == null) {
        return null;
      } else {
        return schema.getRecordDefinition(typePath);
      }
    }
  }

  @Override
  public RecordDefinition getRecordDefinition(final RecordDefinition objectRecordDefinition) {
    final String typePath = objectRecordDefinition.getPath();
    final RecordDefinition recordDefinition = getRecordDefinition(typePath);
    return recordDefinition;
  }

  @Override
  public RecordDefinition getRecordDefinition(final String typePath) {
    final String schemaPath = Path.getPath(typePath);
    final RecordStoreSchema schema = getSchema(schemaPath);
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
  public RecordStoreSchema getRootSchema() {
    return this.rootSchema;
  }

  @Override
  public RecordStoreSchema getSchema(final PathName pathName) {
    final RecordStoreSchema rootSchema = getRootSchema();
    return rootSchema.getSchema(pathName);
  }

  @Override
  public RecordStoreSchema getSchema(final String path) {
    final RecordStoreSchema rootSchema = getRootSchema();
    return rootSchema.getSchema(path);
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
  public boolean isLoadFullSchema() {
    return this.loadFullSchema;
  }

  @Override
  public final Record load(final String typePath, final Identifier id) {
    final RecordDefinition recordDefinition = getRecordDefinition(typePath);
    if (recordDefinition == null || id == null) {
      return null;
    } else {
      final List<Object> values = id.getValues();
      final List<String> idFieldNames = recordDefinition.getIdFieldNames();
      if (idFieldNames.isEmpty()) {
        throw new IllegalArgumentException(typePath + " does not have a primary key");
      } else if (values.size() != idFieldNames.size()) {
        throw new IllegalArgumentException(
          id + " not a valid id for " + typePath + " requires " + idFieldNames);
      } else {
        final Query query = new Query(recordDefinition);
        for (int i = 0; i < idFieldNames.size(); i++) {
          final String name = idFieldNames.get(i);
          final Object value = values.get(i);
          final FieldDefinition field = recordDefinition.getField(name);
          query.and(Q.equal(field, value));
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
      final List<String> idFieldNames = recordDefinition.getIdFieldNames();
      if (idFieldNames.isEmpty()) {
        throw new IllegalArgumentException(typePath + " does not have a primary key");
      } else if (id.length != idFieldNames.size()) {
        throw new IllegalArgumentException(
          Arrays.toString(id) + " not a valid id for " + typePath + " requires " + idFieldNames);
      } else {
        final Query query = new Query(recordDefinition);
        for (int i = 0; i < idFieldNames.size(); i++) {
          final String name = idFieldNames.get(i);
          final Object value = id[i];
          final FieldDefinition field = recordDefinition.getField(name);
          query.and(Q.equal(field, value));
        }
        return queryFirst(query);
      }
    }
  }

  @Override
  public Record lock(final String typePath, final Object id) {
    final RecordDefinition recordDefinition = getRecordDefinition(typePath);
    if (recordDefinition == null) {
      return null;
    } else {
      final String idFieldName = recordDefinition.getIdFieldName();
      if (idFieldName == null) {
        throw new IllegalArgumentException(typePath + " does not have a primary key");
      } else {
        final Query query = Query.equal(recordDefinition, idFieldName, id);
        query.setLockResults(true);
        return queryFirst(query);
      }
    }
  }

  protected void obtainConnected() {
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
    for (final Object queryObject : queries) {
      if (queryObject instanceof Query) {
        final Query query = (Query)queryObject;
        queryObjects.add(query);
      } else {
        final Query query = new Query(queryObject.toString());
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

  protected RecordDefinition refreshRecordDefinition(final RecordStoreSchema schema,
    final PathName typePath) {
    return null;
  }

  protected void refreshSchema() {
    getRootSchema().refresh();
  }

  protected void refreshSchema(final String schemaName) {
    final RecordStoreSchema schema = getSchema(schemaName);
    if (schema != null) {
      schema.refresh();
    }
  }

  protected Map<String, ? extends RecordStoreSchemaElement> refreshSchemaElements(
    final RecordStoreSchema schema) {
    return Collections.emptyMap();
  }

  protected void releaseConnected() {
  }

  public void setCodeTableColumNames(final Map<String, List<String>> domainColumNames) {
    this.codeTableColumNames = domainColumNames;
  }

  public void setCommonRecordDefinitionProperties(
    final List<RecordDefinitionProperty> commonRecordDefinitionProperties) {
    this.commonRecordDefinitionProperties = commonRecordDefinitionProperties;
  }

  protected void setConnectionProperties(final Map<String, ? extends Object> connectionProperties) {
    this.connectionProperties = Maps.createHashMap(connectionProperties);
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  public void setIteratorFactory(final RecordStoreIteratorFactory iteratorFactory) {
    this.iteratorFactory = iteratorFactory;
  }

  @Override
  public void setLabel(final String label) {
    this.label = label;
    this.statistics.setPrefix(label);
  }

  @Override
  public void setLoadFullSchema(final boolean loadFullSchema) {
    this.loadFullSchema = loadFullSchema;
  }

  @Override
  public void setLogCounts(final boolean logCounts) {
    this.statistics.setLogCounts(logCounts);
  }

  @Override
  public void setRecordFactory(final RecordFactory recordFactory) {
    this.recordFactory = recordFactory;
  }

  protected void setSharedAttribute(final String name, final Object value) {
    final Map<String, Object> sharedAttributes = getSharedAttributes();
    sharedAttributes.put(name, value);
  }

  public void setTypeRecordDefinitionProperties(
    final Map<String, List<RecordDefinitionProperty>> typeRecordDefinitionProperties) {
    for (final Entry<String, List<RecordDefinitionProperty>> typeProperties : typeRecordDefinitionProperties
      .entrySet()) {
      final String typePath = typeProperties.getKey();
      Map<String, Object> currentProperties = this.typeRecordDefinitionProperties.get(typePath);
      if (currentProperties == null) {
        currentProperties = new LinkedHashMap<String, Object>();
        this.typeRecordDefinitionProperties.put(typePath, currentProperties);
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
