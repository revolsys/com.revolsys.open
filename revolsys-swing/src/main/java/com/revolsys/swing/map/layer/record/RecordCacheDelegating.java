package com.revolsys.swing.map.layer.record;

import java.util.function.Consumer;

import org.jeometry.common.data.identifier.Identifier;

import com.revolsys.record.Record;

public class RecordCacheDelegating implements RecordCache {

  protected final RecordCache cache;

  public RecordCacheDelegating(final RecordCache cache) {
    this.cache = cache;
  }

  @Override
  public boolean addRecord(final LayerRecord record) {
    return this.cache.addRecord(record);
  }

  @Override
  public void clearRecords() {
    this.cache.clearRecords();
  }

  @Override
  public boolean containsRecord(final LayerRecord record) {
    return this.cache.containsRecord(record);
  }

  @Override
  public <R extends Record> void forEachRecord(final Consumer<R> action) {
    this.cache.forEachRecord(action);
  }

  public RecordCache getCache() {
    return this.cache;
  }

  @Override
  public String getCacheId() {
    return this.cache.getCacheId();
  }

  @Override
  public Object getRecordCacheSync() {
    return this.cache.getRecordCacheSync();
  }

  @Override
  public int getSize() {
    return this.cache.getSize();
  }

  @Override
  public boolean isCached(final Identifier identifier) {
    return this.cache.isCached(identifier);
  }

  @Override
  public boolean removeContainsRecord(final LayerRecord record) {
    return this.cache.removeContainsRecord(record);
  }

  @Override
  public boolean removeRecord(final LayerRecord record) {
    return this.cache.removeRecord(record);
  }

  @Override
  public boolean replaceRecord(final LayerRecord record) {
    return this.cache.replaceRecord(record);
  }

  @Override
  public void setRecords(final Iterable<? extends LayerRecord> records) {
    this.cache.setRecords(records);
  }

  @Override
  public String toString() {
    return this.cache.toString();
  }
}
