package com.revolsys.record.io;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.transaction.PlatformTransactionManager;

import com.revolsys.collection.ResultPager;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;
import com.revolsys.gis.io.Statistics;
import com.revolsys.gis.io.StatisticsMap;
import com.revolsys.io.PathName;
import com.revolsys.io.Writer;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.code.CodeTable;
import com.revolsys.record.property.RecordDefinitionProperty;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.AbstractRecordStore;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordStoreSchema;

public class DelegatingRecordStore extends AbstractRecordStore {
  private final AbstractRecordStore recordStore;

  public DelegatingRecordStore(final AbstractRecordStore recordStore) {
    this.recordStore = recordStore;
  }

  @Override
  public void addCodeTable(final CodeTable codeTable) {
    this.recordStore.addCodeTable(codeTable);
  }

  @Override
  public void addCodeTable(final String columnName, final CodeTable codeTable) {
    this.recordStore.addCodeTable(columnName, codeTable);
  }

  @Override
  public void addCodeTables(final Collection<CodeTable> codeTables) {
    this.recordStore.addCodeTables(codeTables);
  }

  @Override
  public void addStatistic(final String statisticName, final Record object) {
    this.recordStore.addStatistic(statisticName, object);
  }

  @Override
  public void addStatistic(final String statisticName, final String typePath, final int count) {
    this.recordStore.addStatistic(statisticName, typePath, count);
  }

  @Override
  public void clearProperties() {
    this.recordStore.clearProperties();
  }

  @Override
  @PreDestroy
  public void close() {
    this.recordStore.close();
  }

  @Override
  public Record newRecord(final PathName typePath) {
    return this.recordStore.newRecord(typePath);
  }

  @Override
  public Record newRecord(final RecordDefinition recordDefinition) {
    return this.recordStore.newRecord(recordDefinition);
  }

  @Override
  public <T> T createPrimaryIdValue(final PathName typePath) {
    return this.recordStore.createPrimaryIdValue(typePath);
  }

  @Override
  public Query createQuery(final String typePath, final String whereClause,
    final BoundingBoxDoubleGf boundingBox) {
    return this.recordStore.createQuery(typePath, whereClause, boundingBox);
  }

  @Override
  public RecordStoreQueryReader createReader() {
    return this.recordStore.createReader();
  }

  @Override
  public RecordWriter createWriter() {
    return this.recordStore.createWriter();
  }

  @Override
  public int delete(final Query query) {
    return this.recordStore.delete(query);
  }

  @Override
  public void delete(final Record object) {
    this.recordStore.delete(object);
  }

  @Override
  public void deleteAll(final Collection<Record> objects) {
    this.recordStore.deleteAll(objects);
  }

  @Override
  public boolean equals(final Object obj) {
    return this.recordStore.equals(obj);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends CodeTable> V getCodeTable(final PathName typePath) {
    return (V)this.recordStore.getCodeTable(typePath);
  }

  @Override
  public CodeTable getCodeTableByFieldName(final String columnName) {
    return this.recordStore.getCodeTableByFieldName(columnName);
  }

  @Override
  public Map<String, CodeTable> getCodeTableByFieldNameMap() {
    return this.recordStore.getCodeTableByFieldNameMap();
  }

  @Override
  public Map<String, List<String>> getCodeTableColumNames() {
    return this.recordStore.getCodeTableColumNames();
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.recordStore.getGeometryFactory();
  }

  @Override
  public String getLabel() {
    return this.recordStore.getLabel();
  }

  @Override
  public Map<String, Object> getProperties() {
    return this.recordStore.getProperties();
  }

  @Override
  public <C> C getProperty(final String name) {
    return this.recordStore.getProperty(name);
  }

  @Override
  public <C> C getProperty(final String name, final C defaultValue) {
    return this.recordStore.getProperty(name, defaultValue);
  }

  @Override
  public RecordDefinition getRecordDefinition(final RecordDefinition recordDefinition) {
    return this.recordStore.getRecordDefinition(recordDefinition);
  }

  @Override
  public RecordFactory getRecordFactory() {
    return this.recordStore.getRecordFactory();
  }

  public AbstractRecordStore getRecordStore() {
    return this.recordStore;
  }

  @Override
  public RecordStoreSchema getRootSchema() {
    return this.recordStore.getRootSchema();
  }

  @Override
  public int getRowCount(final Query query) {
    return this.recordStore.getRowCount(query);
  }

  @Override
  public StatisticsMap getStatistics() {
    return this.recordStore.getStatistics();
  }

  @Override
  public Statistics getStatistics(final String name) {
    return this.recordStore.getStatistics(name);
  }

  @Override
  public String getString(final Object name) {
    return this.recordStore.getString(name);
  }

  @Override
  public PlatformTransactionManager getTransactionManager() {
    return this.recordStore.getTransactionManager();
  }

  @Override
  public Writer<Record> getWriter() {
    return this.recordStore.getWriter();
  }

  @Override
  public int hashCode() {
    return this.recordStore.hashCode();
  }

  @Override
  @PostConstruct
  public void initialize() {
    this.recordStore.initialize();
  }

  @Override
  public void insert(final Record record) {
    this.recordStore.insert(record);
  }

  @Override
  public void insertAll(final Collection<Record> objects) {
    this.recordStore.insertAll(objects);
  }

  @Override
  public boolean isEditable(final String typePath) {
    return this.recordStore.isEditable(typePath);
  }

  @Override
  public Record load(final String typePath, final Object... id) {
    return this.recordStore.load(typePath, id);
  }

  @Override
  public Record lock(final String typePath, final Object id) {
    return this.recordStore.lock(typePath, id);
  }

  @Override
  public ResultPager<Record> page(final Query query) {
    return this.recordStore.page(query);
  }

  @Override
  public RecordReader query(final List<?> queries) {
    return this.recordStore.query(queries);
  }

  @Override
  public RecordReader query(final Query... queries) {
    return this.recordStore.query(queries);
  }

  @Override
  public RecordReader query(final String path) {
    return this.recordStore.query(path);
  }

  @Override
  public Record queryFirst(final Query query) {
    return this.recordStore.queryFirst(query);
  }

  @Override
  public void removeProperty(final String propertyName) {
    this.recordStore.removeProperty(propertyName);
  }

  @Override
  public void setCodeTableColumNames(final Map<String, List<String>> domainColumNames) {
    this.recordStore.setCodeTableColumNames(domainColumNames);
  }

  @Override
  public void setCommonRecordDefinitionProperties(
    final List<RecordDefinitionProperty> commonRecordDefinitionProperties) {
    this.recordStore.setCommonRecordDefinitionProperties(commonRecordDefinitionProperties);
  }

  @Override
  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.recordStore.setGeometryFactory(geometryFactory);
  }

  @Override
  public void setLabel(final String label) {
    this.recordStore.setLabel(label);
  }

  @Override
  public void setProperties(final Map<String, ? extends Object> properties) {
    this.recordStore.setProperties(properties);
  }

  @Override
  public void setProperty(final String name, final Object value) {
    this.recordStore.setProperty(name, value);
  }

  @Override
  public void setPropertySoft(final String name, final Object value) {
    this.recordStore.setPropertySoft(name, value);
  }

  @Override
  public void setPropertyWeak(final String name, final Object value) {
    this.recordStore.setPropertyWeak(name, value);
  }

  @Override
  public void setRecordFactory(final RecordFactory recordFactory) {
    this.recordStore.setRecordFactory(recordFactory);
  }

  @Override
  public void setTypeRecordDefinitionProperties(
    final Map<String, List<RecordDefinitionProperty>> typeRecordDefinitionProperties) {
    this.recordStore.setTypeRecordDefinitionProperties(typeRecordDefinitionProperties);
  }

  @Override
  public String toString() {
    return this.recordStore.toString();
  }

  @Override
  public void update(final Record object) {
    this.recordStore.update(object);
  }

  @Override
  public void updateAll(final Collection<Record> objects) {
    this.recordStore.updateAll(objects);
  }
}
