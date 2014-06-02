package com.revolsys.gis.data.io;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.PlatformTransactionManager;

import com.revolsys.collection.ResultPager;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataFactory;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.gis.data.query.Query;
import com.revolsys.gis.data.query.QueryValue;
import com.revolsys.gis.io.Statistics;
import com.revolsys.gis.io.StatisticsMap;
import com.revolsys.io.Reader;
import com.revolsys.io.Writer;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.transaction.Propagation;
import com.revolsys.transaction.Transaction;

public interface DataObjectStore extends DataObjectMetaDataFactory,
  AutoCloseable {
  void addCodeTable(CodeTable codeTable);

  void addCodeTables(Collection<CodeTable> codeTables);

  void addStatistic(String name, DataObject object);

  void addStatistic(String name, String typePath, int count);

  void appendQueryValue(Query query, StringBuffer sql, QueryValue queryValue);

  @Override
  void close();

  DataObject copy(DataObject record);

  DataObject create(DataObjectMetaData metaData);

  DataObject create(String typePath);

  DataObject create(String typePath, Map<String, ? extends Object> values);

  <T> T createPrimaryIdValue(String typePath);

  Query createQuery(final String typePath, String whereClause,
    final Envelope boundingBox);

  Transaction createTransaction(Propagation propagation);

  DataObject createWithId(DataObjectMetaData objectMetaData);

  Writer<DataObject> createWriter();

  void delete(DataObject object);

  int delete(Query query);

  void deleteAll(Collection<DataObject> objects);

  <V extends CodeTable> V getCodeTable(String typePath);

  CodeTable getCodeTableByColumn(String columnName);

  Map<String, CodeTable> getCodeTableByColumnMap();

  DataObjectFactory getDataObjectFactory();

  String getLabel();

  DataObjectMetaData getMetaData(DataObjectMetaData metaData);

  /**
   * Get the meta data for the specified type.
   * 
   * @param typePath The type name.
   * @return The meta data.
   */
  @Override
  DataObjectMetaData getMetaData(String typePath);

  int getRowCount(Query query);

  DataObjectStoreSchema getSchema(final String schemaName);

  /**
   * Get the list of name space names provided by the data store.
   * 
   * @return The name space names.
   */
  List<DataObjectStoreSchema> getSchemas();

  StatisticsMap getStatistics();

  Statistics getStatistics(String string);

  PlatformTransactionManager getTransactionManager();

  /**
   * Get the list of type names (including name space) in the name space.
   * 
   * @param namespace The name space.
   * @return The type names.
   */
  List<String> getTypeNames(String namespace);

  List<DataObjectMetaData> getTypes(String namespace);

  String getUrl();

  String getUsername();

  Writer<DataObject> getWriter();

  Writer<DataObject> getWriter(boolean throwExceptions);

  boolean hasSchema(String name);

  void initialize();

  void insert(DataObject object);

  void insertAll(Collection<DataObject> objects);

  boolean isEditable(String typePath);

  DataObject load(String typePath, Object... id);

  DataObject lock(String typePath, Object id);

  ResultPager<DataObject> page(Query query);

  Reader<DataObject> query(List<?> queries);

  Reader<DataObject> query(Query... queries);

  Reader<DataObject> query(String typePath);

  DataObject queryFirst(Query query);

  void setDataObjectFactory(DataObjectFactory dataObjectFactory);

  void setLabel(String label);

  void setLogCounts(boolean logCounts);

  void update(DataObject object);

  void updateAll(Collection<DataObject> objects);
}
