package com.revolsys.data.io;

import java.util.List;
import java.util.Map;

import com.revolsys.data.record.schema.RecordStore;

public interface RecordStoreFactory {
  RecordStore createRecordStore(Map<String, ? extends Object> connectionProperties);

  String getName();

  List<String> getRecordStoreFileExtensions();

  Class<? extends RecordStore> getRecordStoreInterfaceClass(
    Map<String, ? extends Object> connectionProperties);

  List<String> getUrlPatterns();

  boolean isAvailable();
}
