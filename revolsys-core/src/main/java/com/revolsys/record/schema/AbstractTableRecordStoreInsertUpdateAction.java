package com.revolsys.record.schema;

import com.revolsys.record.Record;
import com.revolsys.record.query.InsertUpdateAction;

public class AbstractTableRecordStoreInsertUpdateAction implements InsertUpdateAction {
  private final AbstractTableRecordStore recordStore;

  public AbstractTableRecordStoreInsertUpdateAction(final AbstractTableRecordStore recordStore) {
    this.recordStore = recordStore;
  }

  @Override
  public Record newRecord() {
    return this.recordStore.newRecord();
  }

}
