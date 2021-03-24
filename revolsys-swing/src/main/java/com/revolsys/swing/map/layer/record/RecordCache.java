package com.revolsys.swing.map.layer.record;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import org.jeometry.common.data.identifier.Identifier;

import com.revolsys.record.Record;

public interface RecordCache {

  default boolean addRecord(final Record record) {
    throw new UnsupportedOperationException();
  }

  default void addRecords(final Iterable<? extends Record> records) {
    if (records != null) {
      for (final Record record : records) {
        addRecord(record);
      }
    }
  }

  default void clearRecords() {
  }

  default boolean containsRecord(final Record record) {
    throw new UnsupportedOperationException();
  }

  default <R extends Record> void forEachRecord(final Consumer<R> action) {
  }

  String getCacheId();

  Object getRecordCacheSync();

  default <R extends Record> List<R> getRecords() {
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

  default boolean removeContainsRecord(final Record record) {
    throw new UnsupportedOperationException();
  }

  boolean removeRecord(Record record);

  default void removeRecords(final Collection<? extends Record> records) {
    for (final Record record : records) {
      removeRecord(record);
    }
  }

  default boolean replaceRecord(final Record record) {
    return false;
  }

  void setRecords(final Iterable<? extends Record> records);

}
