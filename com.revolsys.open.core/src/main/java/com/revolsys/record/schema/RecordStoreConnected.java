package com.revolsys.record.schema;

import com.revolsys.io.BaseCloseable;

public class RecordStoreConnected implements BaseCloseable {

  private AbstractRecordStore recordStore;

  public RecordStoreConnected(final AbstractRecordStore recordStore) {
    this.recordStore = recordStore;
    if (recordStore != null) {
      recordStore.obtainConnected();
    }
  }

  @Override
  public void close() {
    if (this.recordStore != null) {
      this.recordStore.releaseConnected();
      this.recordStore = null;
    }
  }

  @Override
  protected void finalize() throws Throwable {
    close();
  }
}
