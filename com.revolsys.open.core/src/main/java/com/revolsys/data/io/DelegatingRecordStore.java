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
  private final AbstractRecordStore recordStore;

  public DelegatingRecordStore(final AbstractRecordStore recordStore) {
    this.recordStore = recordStore;
  }

  @Override
  public void addCodeTable(final CodeTable codeTable) {
    recordStore.addCodeTable(codeTable);
  }

  @Override
  public void addCodeTable(final String columnName, final CodeTable codeTable) {
    recordStore.addCodeTable(columnName, codeTable);
  }

  @Override
  public void addCodeTables(final Collection<CodeTable> codeTables) {
    recordStore.addCodeTables(codeTables);
  }

  @Override
  public void addStatistic(final String statisticName, final Record object) {
    recordStore.addStatistic(statisticName, object);
  }

  @Override
  public void addStatistic(final String statisticName, final String typePath,
    final int count) {
    recordStore.addStatistic(statisticName, typePath, count);
  }

  @Override
  public void clearProperties() {
    recordStore.clearProperties();
  }

  @Override
  @PreDestroy
  public void close() {
    recordStore.close();
  }

  @Override
  public Record create(final RecordDefinition objectMetaData) {
    return recordStore.create(objectMetaData);
  }

  @Override
  public Record create(final String typePath) {
    return recordStore.create(typePath);
  }

  @Override
  public <T> T createPrimaryIdValue(final String typePath) {
    return recordStore.createPrimaryIdValue(typePath);
  }

  @Override
  public Query createQuery(final String typePath, final String whereClause,
    final BoundingBoxDoubleGf boundingBox) {
    return recordStore.createQuery(typePath, whereClause, boundingBox);
  }

  @Override
  public RecordStoreQueryReader createReader() {
    return recordStore.createReader();
  }

  @Override
  public Writer<Record> createWriter() {
    return recordStore.createWriter();
  }

  @Override
  public void delete(final Record object) {
    recordStore.delete(object);
  }

  @Override
  public int delete(final Query query) {
    return recordStore.delete(query);
  }

  @Override
  public void deleteAll(final Collection<Record> objects) {
    recordStore.deleteAll(objects);
  }

  @Override
  public boolean equals(final Object obj) {
    return recordStore.equals(obj);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends CodeTable> V getCodeTable(final String typePath) {
    return (V)recordStore.getCodeTable(typePath);
  }

  @Override
  public CodeTable getCodeTableByColumn(final String columnName) {
    return recordStore.getCodeTableByColumn(columnName);
  }

  @Override
  public Map<String, CodeTable> getCodeTableByColumnMap() {
    return recordStore.getCodeTableByColumnMap();
  }

  @Override
  public Map<String, List<String>> getCodeTableColumNames() {
    return recordStore.getCodeTableColumNames();
  }

  @Override
  public RecordFactory getRecordFactory() {
    return recordStore.getRecordFactory();
  }

  public AbstractRecordStore getDataStore() {
    return recordStore;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return recordStore.getGeometryFactory();
  }

  @Override
  public String getLabel() {
    return recordStore.getLabel();
  }

  @Override
  public RecordDefinition getRecordDefinition(final RecordDefinition objectMetaData) {
    return recordStore.getRecordDefinition(objectMetaData);
  }

  @Override
  public RecordDefinition getRecordDefinition(final String typePath) {
    return recordStore.getRecordDefinition(typePath);
  }

  @Override
  public Map<String, Object> getProperties() {
    return recordStore.getProperties();
  }

  @Override
  public <C> C getProperty(final String name) {
    return recordStore.getProperty(name);
  }

  @Override
  public <C> C getProperty(final String name, final C defaultValue) {
    return recordStore.getProperty(name, defaultValue);
  }

  @Override
  public int getRowCount(final Query query) {
    return recordStore.getRowCount(query);
  }

  @Override
  public RecordStoreSchema getSchema(final String schemaName) {
    return recordStore.getSchema(schemaName);
  }

  @Override
  public Map<String, RecordStoreSchema> getSchemaMap() {
    return recordStore.getSchemaMap();
  }

  @Override
  public List<RecordStoreSchema> getSchemas() {
    return recordStore.getSchemas();
  }

  @Override
  public StatisticsMap getStatistics() {
    return recordStore.getStatistics();
  }

  @Override
  public Statistics getStatistics(final String name) {
    return recordStore.getStatistics(name);
  }

  @Override
  public String getString(final Object name) {
    return recordStore.getString(name);
  }

  @Override
  public PlatformTransactionManager getTransactionManager() {
    return recordStore.getTransactionManager();
  }

  @Override
  public List<String> getTypeNames(final String schemaName) {
    return recordStore.getTypeNames(schemaName);
  }

  @Override
  public List<RecordDefinition> getTypes(final String namespace) {
    return recordStore.getTypes(namespace);
  }

  @Override
  public Writer<Record> getWriter() {
    return recordStore.getWriter();
  }

  @Override
  public int hashCode() {
    return recordStore.hashCode();
  }

  @Override
  @PostConstruct
  public void initialize() {
    recordStore.initialize();
  }

  @Override
  public void insert(final Record record) {
    recordStore.insert(record);
  }

  @Override
  public void insertAll(final Collection<Record> objects) {
    recordStore.insertAll(objects);
  }

  @Override
  public boolean isEditable(final String typePath) {
    return recordStore.isEditable(typePath);
  }

  @Override
  public Record load(final String typePath, final Object... id) {
    return recordStore.load(typePath, id);
  }

  @Override
  protected void loadSchemaRecordDefinitions(
    final RecordStoreSchema schema,
    final Map<String, RecordDefinition> recordDefinitionMap) {
  }

  @Override
  protected void loadSchemas(final Map<String, RecordStoreSchema> schemaMap) {
  }

  @Override
  public Record lock(final String typePath, final Object id) {
    return recordStore.lock(typePath, id);
  }

  @Override
  public ResultPager<Record> page(final Query query) {
    return recordStore.page(query);
  }

  @Override
  public Reader<Record> query(final List<?> queries) {
    return recordStore.query(queries);
  }

  @Override
  public Reader<Record> query(final Query... queries) {
    return recordStore.query(queries);
  }

  @Override
  public Reader<Record> query(final String path) {
    return recordStore.query(path);
  }

  @Override
  public Record queryFirst(final Query query) {
    return recordStore.queryFirst(query);
  }

  @Override
  public void removeProperty(final String propertyName) {
    recordStore.removeProperty(propertyName);
  }

  @Override
  public void setCodeTableColumNames(
    final Map<String, List<String>> domainColumNames) {
    recordStore.setCodeTableColumNames(domainColumNames);
  }

  @Override
  public void setCommonMetaDataProperties(
    final List<RecordDefinitionProperty> commonMetaDataProperties) {
    recordStore.setCommonMetaDataProperties(commonMetaDataProperties);
  }

  @Override
  public void setRecordFactory(final RecordFactory recordFactory) {
    recordStore.setRecordFactory(recordFactory);
  }

  @Override
  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    recordStore.setGeometryFactory(geometryFactory);
  }

  @Override
  public void setLabel(final String label) {
    recordStore.setLabel(label);
  }

  @Override
  public void setProperties(final Map<String, ? extends Object> properties) {
    recordStore.setProperties(properties);
  }

  @Override
  public void setProperty(final String name, final Object value) {
    recordStore.setProperty(name, value);
  }

  @Override
  public void setPropertySoft(final String name, final Object value) {
    recordStore.setPropertySoft(name, value);
  }

  @Override
  public void setPropertyWeak(final String name, final Object value) {
    recordStore.setPropertyWeak(name, value);
  }

  @Override
  public void setSchemaMap(final Map<String, RecordStoreSchema> schemaMap) {
    recordStore.setSchemaMap(schemaMap);
  }

  @Override
  public void setTypeMetaDataProperties(
    final Map<String, List<RecordDefinitionProperty>> typeMetaProperties) {
    recordStore.setTypeMetaDataProperties(typeMetaProperties);
  }

  @Override
  public String toString() {
    return recordStore.toString();
  }

  @Override
  public void update(final Record object) {
    recordStore.update(object);
  }

  @Override
  public void updateAll(final Collection<Record> objects) {
    recordStore.updateAll(objects);
  }
}
