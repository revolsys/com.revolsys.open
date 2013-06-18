package com.revolsys.gis.data.io;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.transaction.PlatformTransactionManager;

import com.revolsys.collection.ResultPager;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataProperty;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.gis.data.query.Query;
import com.revolsys.gis.io.Statistics;
import com.revolsys.gis.io.StatisticsMap;
import com.revolsys.io.Reader;
import com.revolsys.io.Writer;
import com.vividsolutions.jts.geom.Geometry;

public class DelegatingDataObjectStore extends AbstractDataObjectStore {
  private final AbstractDataObjectStore dataStore;

  public DelegatingDataObjectStore(final AbstractDataObjectStore dataStore) {
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
  public void addStatistic(final String statisticName, final DataObject object) {
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
  public DataObject create(final DataObjectMetaData objectMetaData) {
    return dataStore.create(objectMetaData);
  }

  @Override
  public DataObject create(final String typePath) {
    return dataStore.create(typePath);
  }

  @Override
  public <T> T createPrimaryIdValue(final String typePath) {
    return dataStore.createPrimaryIdValue(typePath);
  }

  @Override
  public Query createQuery(final String typePath, final String whereClause,
    final BoundingBox boundingBox) {
    return dataStore.createQuery(typePath, whereClause, boundingBox);
  }

  @Override
  public DataObjectStoreQueryReader createReader() {
    return dataStore.createReader();
  }

  @Override
  public Writer<DataObject> createWriter() {
    return dataStore.createWriter();
  }

  @Override
  public void delete(final DataObject object) {
    dataStore.delete(object);
  }

  @Override
  public int delete(final Query query) {
    return dataStore.delete(query);
  }

  @Override
  public void deleteAll(final Collection<DataObject> objects) {
    dataStore.deleteAll(objects);
  }

  @Override
  public boolean equals(final Object obj) {
    return dataStore.equals(obj);
  }

  @Override
  public CodeTable getCodeTable(final String typePath) {
    return dataStore.getCodeTable(typePath);
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
  public DataObjectFactory getDataObjectFactory() {
    return dataStore.getDataObjectFactory();
  }

  public AbstractDataObjectStore getDataStore() {
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
  public DataObjectMetaData getMetaData(final DataObjectMetaData objectMetaData) {
    return dataStore.getMetaData(objectMetaData);
  }

  @Override
  public DataObjectMetaData getMetaData(final String typePath) {
    return dataStore.getMetaData(typePath);
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
  public DataObjectStoreSchema getSchema(final String schemaName) {
    return dataStore.getSchema(schemaName);
  }

  @Override
  public Map<String, DataObjectStoreSchema> getSchemaMap() {
    return dataStore.getSchemaMap();
  }

  @Override
  public List<DataObjectStoreSchema> getSchemas() {
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
  public List<DataObjectMetaData> getTypes(final String namespace) {
    return dataStore.getTypes(namespace);
  }

  @Override
  public Writer<DataObject> getWriter() {
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
  public void insert(final DataObject dataObject) {
    dataStore.insert(dataObject);
  }

  @Override
  public void insertAll(final Collection<DataObject> objects) {
    dataStore.insertAll(objects);
  }

  @Override
  public boolean isEditable(final String typePath) {
    return dataStore.isEditable(typePath);
  }

  @Override
  public DataObject load(final String typePath, final Object id) {
    return dataStore.load(typePath, id);
  }

  @Override
  protected void loadSchemaDataObjectMetaData(
    final DataObjectStoreSchema schema,
    final Map<String, DataObjectMetaData> metaDataMap) {
  }

  @Override
  protected void loadSchemas(final Map<String, DataObjectStoreSchema> schemaMap) {
  }

  @Override
  public DataObject lock(final String typePath, final Object id) {
    return dataStore.lock(typePath, id);
  }

  @Override
  public ResultPager<DataObject> page(final Query query) {
    return dataStore.page(query);
  }

  @Override
  public Reader<DataObject> query(final List<Query> queries) {
    return dataStore.query(queries);
  }

  @Override
  public Reader<DataObject> query(final Query... queries) {
    return dataStore.query(queries);
  }

  @Override
  public Reader<DataObject> query(final String path) {
    return dataStore.query(path);
  }

  @Override
  public Reader<DataObject> query(DataObjectFactory dataObjectFactory,
    final String typePath, final BoundingBox boundingBox) {
    return dataStore.query(null, typePath, boundingBox);
  }

  @Override
  public Reader<DataObject> query(DataObjectFactory dataObjectFactory, final String typePath, final Geometry geometry) {
    return dataStore.query(null, typePath, geometry);
  }

  @Override
  public DataObject queryFirst(final Query query) {
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
    final List<DataObjectMetaDataProperty> commonMetaDataProperties) {
    dataStore.setCommonMetaDataProperties(commonMetaDataProperties);
  }

  @Override
  public void setDataObjectFactory(final DataObjectFactory dataObjectFactory) {
    dataStore.setDataObjectFactory(dataObjectFactory);
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
  public void setSchemaMap(final Map<String, DataObjectStoreSchema> schemaMap) {
    dataStore.setSchemaMap(schemaMap);
  }

  @Override
  public void setTypeMetaDataProperties(
    final Map<String, List<DataObjectMetaDataProperty>> typeMetaProperties) {
    dataStore.setTypeMetaDataProperties(typeMetaProperties);
  }

  @Override
  public String toString() {
    return dataStore.toString();
  }

  @Override
  public void update(final DataObject object) {
    dataStore.update(object);
  }

  @Override
  public void updateAll(final Collection<DataObject> objects) {
    dataStore.updateAll(objects);
  }
}
