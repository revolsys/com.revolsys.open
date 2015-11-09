package com.revolsys.record.io;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.revolsys.io.IoFactory;
import com.revolsys.io.Paths;
import com.revolsys.record.Available;
import com.revolsys.record.schema.RecordStore;

public interface RecordStoreFactory extends Available, IoFactory {
  default boolean canOpen(final Path path) {
    final String fileNameExtension = Paths.getFileNameExtension(path);
    return getRecordStoreFileExtensions().contains(fileNameExtension);
  }

  default boolean canOpenUrl(final String url) {
    for (final Pattern pattern : getUrlPatterns()) {
      if (pattern.matcher(url).matches()) {
        return true;
      }
    }
    return false;
  }

  @Override
  String getName();

  default List<String> getRecordStoreFileExtensions() {
    return Collections.emptyList();
  }

  Class<? extends RecordStore> getRecordStoreInterfaceClass(
    Map<String, ? extends Object> connectionProperties);

  List<Pattern> getUrlPatterns();

  RecordStore newRecordStore(Map<String, ? extends Object> connectionProperties);
}
