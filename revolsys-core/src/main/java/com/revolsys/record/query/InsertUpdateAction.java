package com.revolsys.record.query;

import com.revolsys.record.Record;

public interface InsertUpdateAction {

  default void insertOrupdateRecord(final Record record) {
  }

  default Record insertRecord() {
    final Record record = newRecord();
    insertOrupdateRecord(record);
    return record;
  }

  Record newRecord();

  default void updateRecord(final Record record) {
    insertOrupdateRecord(record);
  }

}
