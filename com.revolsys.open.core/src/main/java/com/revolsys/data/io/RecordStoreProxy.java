package com.revolsys.data.io;

public interface RecordStoreProxy {
  <V extends RecordStore> V getRecordStore();
}
