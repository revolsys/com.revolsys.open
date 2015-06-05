package com.revolsys.data.record.io;

import com.revolsys.data.record.schema.RecordStore;

public interface RecordStoreProxy {
  <V extends RecordStore> V getRecordStore();
}
