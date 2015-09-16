package com.revolsys.record.schema;

import java.io.Closeable;

public class RecordStoreConnected implements Closeable {

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
