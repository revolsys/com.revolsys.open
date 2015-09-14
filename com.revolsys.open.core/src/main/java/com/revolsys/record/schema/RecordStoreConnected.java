package com.revolsys.record.schema;

public class RecordStoreConnected implements AutoCloseable {

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
