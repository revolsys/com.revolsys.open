package com.revolsys.record.schema;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import org.jeometry.common.io.PathName;
import org.jeometry.common.io.PathNameProxy;

import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.query.Query;
import com.revolsys.record.query.QueryValue;
import com.revolsys.transaction.Propagation;
import com.revolsys.transaction.Transaction;

public interface TableRecordStoreConnection {

  void addDefaultSortOrder(PathName tablePath, Query query);

  Map<QueryValue, Boolean> getDefaultSortOrder(PathName tablePath);

  Record getRecord(Query query);

  <C extends AbstractTableRecordStore> C getRecordController(PathName pathName);

  <C extends AbstractTableRecordStore> C getRecordController(PathNameProxy pathNameProxy);

  long getRecordCount(Query query);

  RecordDefinition getRecordDefinition(PathName tablePath);

  RecordReader getRecordReader(Query query);

  List<Record> getRecords(Query query);

  Record insertRecord(Record record);

  Query newQuery(PathName tablePath);

  Record newRecord(PathName tablePath, JsonObject json);

  Transaction newTransaction();

  Transaction newTransaction(Propagation propagation);

  Record updateRecord(PathName tablePath, UUID id, Consumer<Record> updateAction);

  Record updateRecord(PathName tablePath, UUID id, JsonObject values);

  Record updateRecord(Record record, Consumer<Record> updateAction);

}
