package com.revolsys.data.io;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.transaction.PlatformTransactionManager;

import com.revolsys.collection.ResultPager;
import com.revolsys.data.codes.CodeTable;
import com.revolsys.data.query.Query;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.property.RecordDefinitionProperty;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.gis.io.Statistics;
import com.revolsys.gis.io.StatisticsMap;
import com.revolsys.io.Reader;
import com.revolsys.io.Writer;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.impl.BoundingBoxDoubleGf;

public class DelegatingRecordStore extends AbstractRecordStore {
  private final AbstractRecordStore dataStore;

  public DelegatingRecordStore(final AbstractRecordStore dataStore) {
    this.dataStore = dataStore;
  }

  @Override
  public void addCodeTable(final CodeTable codeTable) {
    dataStore.addCodeTable(codeTable);
  }

  @Override
  public void addCodeTable(final String columnName, final CodeTable codeTable) {
    dataStore.addCodeTable(columnName, codeTable);
  }

  @Override
  public void addCodeTables(final Collection<CodeTable> codeTables) {
    dataStore.addCodeTables(codeTables);
  }

  @Override
  public void addStatistic(final String statisticName, final Record object) {
    dataStore.addStatistic(statisticName, object);
  }

  @Override
  public void addStatistic(final String statisticName, final String typePath,
    final int count) {
    dataStore.addStatistic(statisticName, typePath, count);
  }

  @Override
  public void clearProperties() {
    dataStore.clearProperties();
  }

  @Override
  @PreDestroy
  public void close() {
    dataStore.close();
  }

  @Override
  public Record create(final RecordDefinition objectMetaData) {
    return dataStore.create(objectMetaData);
  }

  @Override
  public Record create(final String typePath) {
    return dataStore.create(typePath);
  }

  @Override
  public <T> T createPrimaryIdValue(final String typePath) {
    return dataStore.createPrimaryIdValue(typePath);
  }

  @Override
  public Query createQuery(final String typePath, final String whereClause,
    final BoundingBoxDoubleGf boundingBox) {
    return dataStore.createQuery(typePath, whereClause, boundingBox);
  }

  @Override
  public RecordStoreQueryReader createReader() {
    return dataStore.createReader();
  }

  @Override
  public Writer<Record> createWriter() {
    return dataStore.createWriter();
  }

  @Override
  public void delete(final Record object) {
    dataStore.delete(object);
  }

  @Override
  public int delete(final Query query) {
    return dataStore.delete(query);
  }

  @Override
  public void deleteAll(final Collection<Record> objects) {
    dataStore.deleteAll(objects);
  }

  @Override
  public boolean equals(final Object obj) {
    return dataStore.equals(obj);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends CodeTable> V getCodeTable(final String typePath) {
    return (V)dataStore.getCodeTable(typePath);
  }

  @Override
  public CodeTable getCodeTableByColumn(final String columnName) {
    return dataStore.getCodeTableByColumn(columnName);
  }

  @Override
  public Map<String, CodeTable> getCodeTableByColumnMap() {
    return dataStore.getCodeTableByColumnMap();
  }

  @Override
  public Map<String, List<String>> getCodeTableColumNames() {
    return dataStore.getCodeTableColumNames();
  }

  @Override
  public RecordFactory getRecordFactory() {
    return dataStore.getRecordFactory();
  }

  public AbstractRecordStore getDataStore() {
    return dataStore;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return dataStore.getGeometryFactory();
  }

  @Override
  public String getLabel() {
    return dataStore.getLabel();
  }

  @Override
  public RecordDefinition getMetaData(final RecordDefinition objectMetaData) {
    return dataStore.getMetaData(objectMetaData);
  }

  @Override
  public RecordDefinition getRecordDefinition(final String typePath) {
    return dataStore.getRecordDefinition(typePath);
  }

  @Override
  public Map<String, Object> getProperties() {
    return dataStore.getProperties();
  }

  @Override
  public <C> C getProperty(final String name) {
    return dataStore.getProperty(name);
  }

  @Override
  public <C> C getProperty(final String name, final C defaultValue) {
    return dataStore.getProperty(name, defaultValue);
  }

  @Override
  public int getRowCount(final Query query) {
    return dataStore.getRowCount(query);
  }

  @Override
  public RecordStoreSchema getSchema(final String schemaName) {
    return dataStore.getSchema(schemaName);
  }

  @Override
  public Map<String, RecordStoreSchema> getSchemaMap() {
    return dataStore.getSchemaMap();
  }

  @Override
  public List<RecordStoreSchema> getSchemas() {
    return dataStore.getSchemas();
  }

  @Override
  public StatisticsMap getStatistics() {
    return dataStore.getStatistics();
  }

  @Override
  public Statistics getStatistics(final String name) {
    return dataStore.getStatistics(name);
  }

  @Override
  public String getString(final Object name) {
    return dataStore.getString(name);
  }

  @Override
  public PlatformTransactionManager getTransactionManager() {
    return dataStore.getTransactionManager();
  }

  @Override
  public List<String> getTypeNames(final String schemaName) {
    return dataStore.getTypeNames(schemaName);
  }

  @Override
  public List<RecordDefinition> getTypes(final String namespace) {
    return dataStore.getTypes(namespace);
  }

  @Override
  public Writer<Record> getWriter() {
    return dataStore.getWriter();
  }

  @Override
  public int hashCode() {
    return dataStore.hashCode();
  }

  @Override
  @PostConstruct
  public void initialize() {
    dataStore.initialize();
  }

  @Override
  public void insert(final Record dataObject) {
    dataStore.insert(dataObject);
  }

  @Override
  public void insertAll(final Collection<Record> objects) {
    dataStore.insertAll(objects);
  }

  @Override
  public boolean isEditable(final String typePath) {
    return dataStore.isEditable(typePath);
  }

  @Override
  public Record load(final String typePath, final Object... id) {
    return dataStore.load(typePath, id);
  }

  @Override
  protected void loadSchemaRecordDefinitions(
    final RecordStoreSchema schema,
    final Map<String, RecordDefinition> metaDataMap) {
  }

  @Override
  protected void loadSchemas(final Map<String, RecordStoreSchema> schemaMap) {
  }

  @Override
  public Record lock(final String typePath, final Object id) {
    return dataStore.lock(typePath, id);
  }

  @Override
  public ResultPager<Record> page(final Query query) {
    return dataStore.page(query);
  }

  @Override
  public Reader<Record> query(final List<?> queries) {
    return dataStore.query(queries);
  }

  @Override
  public Reader<Record> query(final Query... queries) {
    return dataStore.query(queries);
  }

  @Override
  public Reader<Record> query(final String path) {
    return dataStore.query(path);
  }

  @Override
  public Record queryFirst(final Query query) {
    return dataStore.queryFirst(query);
  }

  @Override
  public void removeProperty(final String propertyName) {
    dataStore.removeProperty(propertyName);
  }

  @Override
  public void setCodeTableColumNames(
    final Map<String, List<String>> domainColumNames) {
    dataStore.setCodeTableColumNames(domainColumNames);
  }

  @Override
  public void setCommonMetaDataProperties(
    final List<RecordDefinitionProperty> commonMetaDataProperties) {
    dataStore.setCommonMetaDataProperties(commonMetaDataProperties);
  }

  @Override
  public void setRecordFactory(final RecordFactory dataObjectFactory) {
    dataStore.setRecordFactory(dataObjectFactory);
  }

  @Override
  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    dataStore.setGeometryFactory(geometryFactory);
  }

  @Override
  public void setLabel(final String label) {
    dataStore.setLabel(label);
  }

  @Override
  public void setProperties(final Map<String, ? extends Object> properties) {
    dataStore.setProperties(properties);
  }

  @Override
  public void setProperty(final String name, final Object value) {
    dataStore.setProperty(name, value);
  }

  @Override
  public void setPropertySoft(final String name, final Object value) {
    dataStore.setPropertySoft(name, value);
  }

  @Override
  public void setPropertyWeak(final String name, final Object value) {
    dataStore.setPropertyWeak(name, value);
  }

  @Override
  public void setSchemaMap(final Map<String, RecordStoreSchema> schemaMap) {
    dataStore.setSchemaMap(schemaMap);
  }

  @Override
  public void setTypeMetaDataProperties(
    final Map<String, List<RecordDefinitionProperty>> typeMetaProperties) {
    dataStore.setTypeMetaDataProperties(typeMetaProperties);
  }

  @Override
  public String toString() {
    return dataStore.toString();
  }

  @Override
  public void update(final Record object) {
    dataStore.update(object);
  }

  @Override
  public void updateAll(final Collection<Record> objects) {
    dataStore.updateAll(objects);
  }
}
