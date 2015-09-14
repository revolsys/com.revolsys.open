package com.revolsys.record.io;

import com.revolsys.record.schema.RecordStore;

public interface RecordStoreProxy {
  <V extends RecordStore> V getRecordStore();
}
