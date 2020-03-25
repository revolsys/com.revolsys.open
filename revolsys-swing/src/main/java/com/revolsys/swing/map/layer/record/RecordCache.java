package com.revolsys.swing.map.layer.record;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import org.jeometry.common.data.identifier.Identifier;

import com.revolsys.record.Record;

public interface RecordCache {

  default boolean addRecord(final LayerRecord record) {
    throw new UnsupportedOperationException();
  }

  default void addRecords(final Iterable<? extends LayerRecord> records) {
    if (records != null) {
      for (final LayerRecord record : records) {
        addRecord(record);
      }
    }
  }

  default void clearRecords() {
  }

  default boolean containsRecord(final LayerRecord record) {
    throw new UnsupportedOperationException();
  }

  default <R extends Record> void forEachRecord(final Consumer<R> action) {
  }

  String getCacheId();

  Object getRecordCacheSync();

  default <R extends LayerRecord> List<R> getRecords() {
    final List<R> records = new ArrayList<>();
    final Consumer<R> action = records::add;
    forEachRecord(action);
    return records;
  }

  default int getSize() {
    return 0;
  }

  default boolean hasRecords() {
    return getSize() > 0;
  }

  default boolean isCached(final Identifier identifier) {
    throw new UnsupportedOperationException("isCached");
  }

  default boolean removeContainsRecord(final LayerRecord record) {
    throw new UnsupportedOperationException();
  }

  boolean removeRecord(LayerRecord record);

  default void removeRecords(final Collection<? extends LayerRecord> records) {
    for (final LayerRecord record : records) {
      removeRecord(record);
    }
  }

  default boolean replaceRecord(final LayerRecord record) {
    return false;
  }

  void setRecords(final Iterable<? extends LayerRecord> records);

}
