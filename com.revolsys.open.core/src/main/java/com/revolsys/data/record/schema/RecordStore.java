package com.revolsys.data.record.schema;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.PlatformTransactionManager;

import com.revolsys.collection.ResultPager;
import com.revolsys.data.codes.CodeTable;
import com.revolsys.data.identifier.Identifier;
import com.revolsys.data.query.Query;
import com.revolsys.data.query.QueryValue;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.gis.io.Statistics;
import com.revolsys.gis.io.StatisticsMap;
import com.revolsys.io.Reader;
import com.revolsys.io.Writer;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.impl.BoundingBoxDoubleGf;
import com.revolsys.transaction.Propagation;
import com.revolsys.transaction.Transaction;

public interface RecordStore extends RecordDefinitionFactory, AutoCloseable {
  void addCodeTable(CodeTable codeTable);

  void addCodeTables(Collection<CodeTable> codeTables);

  void addStatistic(String name, Record record);

  void addStatistic(String name, String typePath, int count);

  void appendQueryValue(Query query, StringBuilder sql, QueryValue queryValue);

  @Override
  void close();

  Record copy(Record record);

  Record create(RecordDefinition recordDefinition);

  Record create(String typePath);

  Record create(String typePath, Map<String, ? extends Object> values);

  <T> T createPrimaryIdValue(String typePath);

  Query createQuery(final String typePath, String whereClause,
    final BoundingBoxDoubleGf boundingBox);

  Transaction createTransaction(Propagation propagation);

  Record createWithId(RecordDefinition recordDefinition);

  Writer<Record> createWriter();

  int delete(Query query);

  void delete(Record record);

  void deleteAll(Collection<Record> records);

  <V extends CodeTable> V getCodeTable(String typePath);

  CodeTable getCodeTableByFieldName(String fieldName);

  Map<String, CodeTable> getCodeTableByFieldNameMap();

  RecordStoreConnected getConnected();

  GeometryFactory getGeometryFactory();

  String getLabel();

  RecordDefinition getRecordDefinition(RecordDefinition recordDefinition);

  /**
   * Get the meta data for the specified type.
   *
   * @param typePath The type name.
   * @return The meta data.
   */
  @Override
  RecordDefinition getRecordDefinition(String typePath);

  RecordFactory getRecordFactory();

  RecordStoreSchema getRootSchema();

  int getRowCount(Query query);

  RecordStoreSchema getSchema(final String schemaName);

  /**
   * Get the list of name space names provided by the record store.
   *
   * @return The name space names.
   */
  List<RecordStoreSchema> getSchemas();

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

  List<RecordDefinition> getTypes(String namespace);

  String getUrl();

  String getUsername();

  Writer<Record> getWriter();

  Writer<Record> getWriter(boolean throwExceptions);

  boolean hasSchema(String name);

  void initialize();

  void insert(Record record);

  void insertAll(Collection<Record> records);

  boolean isEditable(String typePath);

  boolean isLoadFullSchema();

  Record load(String typePath, Identifier id);

  Record load(String typePath, Object... id);

  Record lock(String typePath, Object id);

  ResultPager<Record> page(Query query);

  Reader<Record> query(List<?> queries);

  Reader<Record> query(Query... queries);

  Reader<Record> query(String typePath);

  default Reader<Record> query(final String typePath, final BoundingBox boundingBox) {
    final RecordDefinition recordDefinition = getRecordDefinition(typePath);
    if (recordDefinition != null && recordDefinition.hasGeometryField()) {
      final Query query = Query.intersects(recordDefinition, boundingBox);
      return query(query);
    }
    return Reader.empty();
  }

  Record queryFirst(Query query);

  void setLabel(String label);

  void setLoadFullSchema(boolean loadFullSchema);

  void setLogCounts(boolean logCounts);

  void setRecordFactory(RecordFactory recordFactory);

  void update(Record record);

  void updateAll(Collection<Record> records);
}
