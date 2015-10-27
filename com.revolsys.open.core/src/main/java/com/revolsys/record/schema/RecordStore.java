package com.revolsys.record.schema;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.PlatformTransactionManager;

import com.revolsys.collection.ListResultPager;
import com.revolsys.collection.ResultPager;
import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactoryProxy;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;
import com.revolsys.gis.io.Statistics;
import com.revolsys.gis.io.StatisticsMap;
import com.revolsys.identifier.Identifier;
import com.revolsys.io.PathName;
import com.revolsys.io.Writer;
import com.revolsys.jdbc.io.RecordStoreIteratorFactory;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.code.CodeTable;
import com.revolsys.record.code.CodeTableProperty;
import com.revolsys.record.io.ListRecordReader;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.RecordStoreQueryReader;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.query.Q;
import com.revolsys.record.query.Query;
import com.revolsys.record.query.QueryValue;
import com.revolsys.transaction.Transactionable;
import com.revolsys.util.Property;

public interface RecordStore
  extends GeometryFactoryProxy, RecordDefinitionFactory, Transactionable, Closeable {
  void addCodeTable(CodeTable codeTable);

  default void addCodeTables(final Collection<CodeTable> codeTables) {
    for (final CodeTable codeTable : codeTables) {
      addCodeTable(codeTable);
    }
  }

  default void addStatistic(final String statisticName, final Record object) {
    if (getStatistics() != null) {
      getStatistics().add(statisticName, object);
    }
  }

  default void addStatistic(final String statisticName, final String typePath, final int count) {
    if (getStatistics() != null) {
      getStatistics().add(statisticName, typePath, count);
    }
  }

  default void appendQueryValue(final Query query, final StringBuilder sql,
    final QueryValue queryValue) {
    queryValue.appendDefaultSql(query, this, sql);
  }

  @Override
  void close();

  default Record copy(final Record record) {
    final RecordDefinition recordDefinition = getRecordDefinition(record.getRecordDefinition());
    final RecordFactory recordFactory = getRecordFactory();
    if (recordDefinition == null || recordFactory == null) {
      return null;
    } else {
      final Record copy = recordFactory.newRecord(recordDefinition);
      copy.setValuesClone(record);
      copy.setIdentifier(null);
      return copy;
    }
  }

  default int delete(final PathName typePath, final Identifier identifier) {
    final RecordDefinition recordDefinition = getRecordDefinition(typePath);
    if (recordDefinition != null) {
      final String idFieldName = recordDefinition.getIdFieldName();
      if (idFieldName != null) {
        final Query query = Query.equal(recordDefinition, idFieldName, identifier);
        return delete(query);
      }
    }
    return 0;
  }

  default int delete(final Query query) {
    int i = 0;
    try (
      final RecordReader reader = query(query)) {
      for (final Record record : reader) {
        delete(record);
        i++;
      }
    }
    return i;
  }

  default void delete(final Record record) {
    throw new UnsupportedOperationException("Delete not supported");
  }

  default void deleteAll(final Collection<Record> records) {
    for (final Record record : records) {
      delete(record);
    }
  }

  default RecordDefinition findRecordDefinition(final PathName typePath) {
    final PathName schemaName = typePath.getParent();
    final RecordStoreSchema schema = getSchema(schemaName);
    if (schema == null) {
      return null;
    } else {
      return schema.findRecordDefinition(typePath);
    }
  }

  @SuppressWarnings("unchecked")
  default <V extends CodeTable> V getCodeTable(final PathName typePath) {
    final RecordDefinition recordDefinition = getRecordDefinition(typePath);
    if (recordDefinition == null) {
      return null;
    } else {
      final CodeTableProperty codeTable = CodeTableProperty.getProperty(recordDefinition);
      return (V)codeTable;
    }
  }

  default <V extends CodeTable> V getCodeTable(final String typePath) {
    return getCodeTable(PathName.newPathName(typePath));
  }

  CodeTable getCodeTableByFieldName(String fieldName);

  Map<String, CodeTable> getCodeTableByFieldNameMap();

  RecordStoreConnected getConnected();

  RecordStoreIteratorFactory getIteratorFactory();

  String getLabel();

  default RecordDefinition getRecordDefinition(final PathName path) {
    if (path == null) {
      return null;
    } else {
      final PathName schemaPath = path.getParent();
      final RecordStoreSchema schema = getSchema(schemaPath);
      if (schema == null) {
        return null;
      } else {
        return schema.getRecordDefinition(path);
      }
    }
  }

  default RecordDefinition getRecordDefinition(final RecordDefinition objectRecordDefinition) {
    final String typePath = objectRecordDefinition.getPath();
    final RecordDefinition recordDefinition = getRecordDefinition(typePath);
    return recordDefinition;
  }

  @Override
  default RecordDefinition getRecordDefinition(final String path) {
    return getRecordDefinition(PathName.newPathName(path));
  }

  default List<RecordDefinition> getRecordDefinitions(final PathName path) {
    final RecordStoreSchema schema = getSchema(path);
    if (schema == null) {
      return Collections.emptyList();
    } else {
      return schema.getRecordDefinitions();
    }
  }

  RecordFactory getRecordFactory();

  RecordStoreSchema getRootSchema();

  int getRowCount(Query query);

  default RecordStoreSchema getSchema(final PathName pathName) {
    final RecordStoreSchema rootSchema = getRootSchema();
    return rootSchema.getSchema(pathName);
  }

  default RecordStoreSchema getSchema(final String path) {
    return getSchema(PathName.newPathName(path));
  }

  StatisticsMap getStatistics();

  default Statistics getStatistics(final String name) {
    final StatisticsMap statistics = getStatistics();
    return statistics.getStatistics(name);
  }

  @Override
  default PlatformTransactionManager getTransactionManager() {
    return null;
  }

  String getUrl();

  String getUsername();

  default Writer<Record> getWriter() {
    return newWriter();
  }

  default Writer<Record> getWriter(final boolean throwExceptions) {
    return getWriter();
  }

  default boolean hasSchema(final PathName schemaName) {
    return getSchema(schemaName) != null;
  }

  void initialize();

  default void insert(final Record record) {
    throw new UnsupportedOperationException("Insert not supported");
  }

  default void insertAll(final Collection<Record> records) {
    for (final Record record : records) {
      insert(record);
    }
  }

  default boolean isClosed() {
    return false;
  }

  default boolean isEditable(final String typePath) {
    return false;
  }

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

  default Record load(final String typePath, final Identifier id) {
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

  default Record load(final String typePath, final Object... id) {
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

  default Record lock(final String typePath, final Object id) {
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

  default AbstractIterator<Record> newIterator(final Query query, Map<String, Object> properties) {
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
            .newIterator(this, query, properties);
          if (iterator != null) {
            return iterator;
          }
        }
      }
      final RecordStoreIteratorFactory iteratorFactory = getIteratorFactory();
      return iteratorFactory.newIterator(this, query, properties);
    }
  }

  default Identifier newPrimaryIdentifier(final PathName typePath) {
    final Object identifier = newPrimaryIdValue(typePath);
    return Identifier.create(identifier);
  }

  default <T> T newPrimaryIdValue(final PathName typePath) {
    return null;
  }

  default Query newQuery(final String typePath, final String whereClause,
    final BoundingBoxDoubleGf boundingBox) {
    throw new UnsupportedOperationException();
  }

  default RecordStoreQueryReader newReader() {
    final RecordStoreQueryReader reader = new RecordStoreQueryReader(this);
    return reader;
  }

  default Record newRecord(final PathName typePath) {
    final RecordDefinition recordDefinition = getRecordDefinition(typePath);
    if (recordDefinition == null) {
      return null;
    } else {
      return newRecord(recordDefinition);
    }
  }

  default Record newRecord(final PathName typePath, final Map<String, ? extends Object> values) {
    final RecordDefinition recordDefinition = getRecordDefinition(typePath);
    if (recordDefinition == null) {
      throw new IllegalArgumentException("Cannot find table " + typePath + " for " + this);
    } else {
      final Record record = newRecord(recordDefinition);
      if (record != null) {
        record.setValues(values);
        final String idFieldName = recordDefinition.getIdFieldName();
        if (Property.hasValue(idFieldName)) {
          if (values.get(idFieldName) == null) {
            final Identifier id = newPrimaryIdentifier(typePath);
            record.setIdentifier(id);
          }
        }
      }
      return record;
    }
  }

  default Record newRecord(final RecordDefinition objectRecordDefinition) {
    final RecordDefinition recordDefinition = getRecordDefinition(objectRecordDefinition);
    final RecordFactory recordFactory = getRecordFactory();
    if (recordDefinition == null || recordFactory == null) {
      return null;
    } else {
      final Record record = recordFactory.newRecord(recordDefinition);
      return record;
    }
  }

  default Record newRecord(RecordDefinition recordDefinition,
    final Map<String, ? extends Object> values) {
    final PathName typePath = recordDefinition.getPathName();
    recordDefinition = getRecordDefinition(recordDefinition);
    if (recordDefinition == null) {
      throw new IllegalArgumentException("Cannot find table " + typePath + " for " + this);
    } else {
      final Record record = newRecord(recordDefinition);
      if (record != null) {
        record.setValues(values);
        final String idFieldName = recordDefinition.getIdFieldName();
        if (Property.hasValue(idFieldName)) {
          if (values.get(idFieldName) == null) {
            final Identifier id = newPrimaryIdentifier(typePath);
            record.setIdentifier(id);
          }
        }
      }
      return record;
    }
  }

  default Record newRecordWithIdentifier(final RecordDefinition recordDefinition) {
    final Record record = newRecord(recordDefinition);
    if (record != null) {
      final String idFieldName = recordDefinition.getIdFieldName();
      if (Property.hasValue(idFieldName)) {
        final PathName typePath = recordDefinition.getPathName();
        final Identifier id = newPrimaryIdentifier(typePath);
        record.setIdentifier(id);
      }
    }
    return record;
  }

  RecordWriter newWriter();

  default RecordWriter newWriter(final RecordDefinition recordDefinition) {
    return newWriter();
  }

  default ResultPager<Record> page(final Query query) {
    final RecordReader results = query(query);
    final List<Record> list = results.read();
    return new ListResultPager<Record>(list);
  }

  default RecordReader query(final Iterable<?> queries) {
    final RecordStoreQueryReader reader = newReader();
    for (final Object queryObject : queries) {
      if (queryObject instanceof Query) {
        final Query query = (Query)queryObject;
        reader.addQuery(query);
      } else {
        final Query query = new Query(queryObject.toString());
        reader.addQuery(query);
      }
    }
    return reader;
  }

  default RecordReader query(final List<?> queries) {
    final RecordStoreQueryReader reader = newReader();
    for (final Object queryObject : queries) {
      if (queryObject instanceof Query) {
        final Query query = (Query)queryObject;
        reader.addQuery(query);
      } else {
        final Query query = new Query(queryObject.toString());
        reader.addQuery(query);
      }
    }
    return reader;
  }

  default RecordReader query(final PathName path) {
    final RecordStoreSchemaElement element = getRootSchema().getElement(path);
    if (element instanceof RecordDefinition) {
      final RecordDefinition recordDefinition = (RecordDefinition)element;
      final Query query = new Query(recordDefinition);
      return query(query);
    } else if (element instanceof RecordStoreSchema) {
      final RecordStoreSchema schema = (RecordStoreSchema)element;
      final List<Query> queries = new ArrayList<>();
      for (final RecordDefinition recordDefinition : schema.getRecordDefinitions()) {
        final Query query = new Query(recordDefinition);
        queries.add(query);
      }
      return query(queries);
    } else {
      return new ListRecordReader(null, Collections.emptyList());
    }
  }

  default RecordReader query(final Query query) {
    final RecordStoreQueryReader reader = newReader();
    reader.addQuery(query);
    return reader;
  }

  default RecordReader query(final Query... queries) {
    return query(Arrays.asList(queries));
  }

  default RecordReader query(final String path) {
    final PathName pathName = PathName.newPathName(path);
    return query(pathName);
  }

  default RecordReader query(final String typePath, final BoundingBox boundingBox) {
    final RecordDefinition recordDefinition = getRecordDefinition(typePath);
    if (recordDefinition != null && recordDefinition.hasGeometryField()) {
      final Query query = Query.intersects(recordDefinition, boundingBox);
      return query(query);
    }
    return new ListRecordReader(recordDefinition);
  }

  default Record queryFirst(final Query query) {
    try (
      final RecordReader reader = query(query)) {
      final Iterator<Record> iterator = reader.iterator();
      if (iterator.hasNext()) {
        final Record record = iterator.next();
        return record;
      } else {
        return null;
      }
    }
  }

  void setLabel(String label);

  void setLoadFullSchema(boolean loadFullSchema);

  void setRecordFactory(RecordFactory recordFactory);

  default void setStatistics(final String name, final Statistics statistics) {
    final StatisticsMap statisticsMap = getStatistics();
    statisticsMap.setStatistics(name, statistics);
  }

  default void update(final Record object) {
    throw new UnsupportedOperationException("Update not supported");
  }

  default void updateAll(final Collection<Record> records) {
    for (final Record record : records) {
      update(record);
    }
  }
}
