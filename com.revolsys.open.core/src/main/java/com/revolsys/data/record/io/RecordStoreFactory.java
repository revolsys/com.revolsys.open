package com.revolsys.data.record.io;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import com.revolsys.data.record.Available;
import com.revolsys.data.record.schema.RecordStore;
import com.revolsys.io.Paths;

public interface RecordStoreFactory extends Available {
  default boolean canOpen(final Path path) {
    final String fileNameExtension = Paths.getFileNameExtension(path);
    return getRecordStoreFileExtensions().contains(fileNameExtension);
  }

  RecordStore createRecordStore(Map<String, ? extends Object> connectionProperties);

  String getName();

  List<String> getRecordStoreFileExtensions();

  Class<? extends RecordStore> getRecordStoreInterfaceClass(
    Map<String, ? extends Object> connectionProperties);

  List<String> getUrlPatterns();
}
