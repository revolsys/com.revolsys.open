package com.revolsys.data.record.schema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.PlatformTransactionManager;

import com.revolsys.collection.ResultPager;
import com.revolsys.data.codes.CodeTable;
import com.revolsys.data.identifier.Identifier;
import com.revolsys.data.query.Q;
import com.revolsys.data.query.Query;
import com.revolsys.data.query.QueryValue;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.gis.io.Statistics;
import com.revolsys.gis.io.StatisticsMap;
import com.revolsys.io.PathName;
import com.revolsys.io.Reader;
import com.revolsys.io.Writer;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.impl.BoundingBoxDoubleGf;
import com.revolsys.transaction.Propagation;
import com.revolsys.transaction.Transaction;
import com.revolsys.util.Property;

public interface RecordStore extends RecordDefinitionFactory, AutoCloseable {
  void addCodeTable(CodeTable codeTable);

  void addCodeTables(Collection<CodeTable> codeTables);

  void addStatistic(String name, Record record);

  void addStatistic(String name, String typePath, int count);

  void appendQueryValue(Query query, StringBuilder sql, QueryValue queryValue);

  @Override
  void close();

  Record copy(Record record);

  Record create(PathName typePath);

  default Record create(final PathName typePath, final Map<String, ? extends Object> values) {
    final RecordDefinition recordDefinition = getRecordDefinition(typePath);
    if (recordDefinition == null) {
      throw new IllegalArgumentException("Cannot find table " + typePath + " for " + this);
    } else {
      final Record record = create(recordDefinition);
      if (record != null) {
        record.setValues(values);
        final String idFieldName = recordDefinition.getIdFieldName();
        if (Property.hasValue(idFieldName)) {
          if (values.get(idFieldName) == null) {
            final Object id = createPrimaryIdValue(typePath);
            record.setIdValue(id);
          }
        }
      }
      return record;
    }

  }

  Record create(RecordDefinition recordDefinition);

  @Deprecated
  default Record create(final String typePath) {
    return create(PathName.create(typePath));
  }

  @Deprecated
  default Record create(final String typePath, final Map<String, ? extends Object> values) {
    return create(PathName.create(typePath), values);
  }

  default <T> T createPrimaryIdValue(final PathName typePath) {
    return null;
  }

  @Deprecated
  default <T> T createPrimaryIdValue(final String typePath) {
    return createPrimaryIdValue(PathName.create(typePath));
  }

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

  RecordDefinition getRecordDefinition(PathName typePath);

  RecordDefinition getRecordDefinition(RecordDefinition recordDefinition);

  @Override
  RecordDefinition getRecordDefinition(String typePath);

  RecordFactory getRecordFactory();

  RecordStoreSchema getRootSchema();

  int getRowCount(Query query);

  RecordStoreSchema getSchema(PathName pathName);

  RecordStoreSchema getSchema(final String schemaName);

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

  default Record load(final PathName typePath, final Identifier id) {
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

  default Record load(final PathName typePath, final Object... id) {
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

  Record load(String typePath, Identifier id);

  Record load(String typePath, Object... id);

  Record lock(String typePath, Object id);

  ResultPager<Record> page(Query query);

  Reader<Record> query(List<?> queries);

  default Reader<Record> query(final PathName path) {
    if (path == null) {
      return query();
    } else {
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
  }

  Reader<Record> query(Query... queries);

  default Reader<Record> query(final String path) {
    final PathName pathName = PathName.create(path);
    return query(pathName);
  }

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
