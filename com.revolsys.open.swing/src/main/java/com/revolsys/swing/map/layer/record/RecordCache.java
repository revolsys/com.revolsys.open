package com.revolsys.swing.map.layer.record;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public interface RecordCache {

  boolean addRecord(LayerRecord record);

  default void addRecords(final Iterable<? extends LayerRecord> records) {
    for (final LayerRecord record : records) {
      addRecord(record);
    }
  }

  void clearRecords();

  boolean containsRecord(LayerRecord record);

  <R extends LayerRecord> void forEachRecord(Consumer<R> action);

  String getCacheId();

  default <R extends LayerRecord> List<R> getRecords() {
    final List<R> records = new ArrayList<>();
    final Consumer<R> action = records::add;
    forEachRecord(action);
    return records;
  }

  int getSize();

  default boolean hasRecords() {
    return getSize() > 0;
  }

  boolean removeRecord(LayerRecord record);

  boolean replaceRecord(LayerRecord record);

}
