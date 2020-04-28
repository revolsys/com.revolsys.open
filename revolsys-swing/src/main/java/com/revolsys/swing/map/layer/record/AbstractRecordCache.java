package com.revolsys.swing.map.layer.record;

import java.util.function.Consumer;

import com.revolsys.record.Record;
import com.revolsys.record.RecordState;

public abstract class AbstractRecordCache<L extends AbstractRecordLayer> implements RecordCache {

  private static class RecordCacheSync {
  }

  private final RecordCacheSync recordCacheSync = new RecordCacheSync();

  protected final L layer;

  protected String cacheId;

  public AbstractRecordCache(final L layer, final String cacheId) {
    this.layer = layer;
    this.cacheId = cacheId;
  }

  @Override
  public boolean addRecord(final LayerRecord record) {
    if (record != null) {
      synchronized (getRecordCacheSync()) {
        final L layer = this.layer;
        if (!(record.getState() == RecordState.DELETED && !layer.isDeleted(record))) {
          final LayerRecord recordProxied = layer.getProxiedRecord(record);
          if (recordProxied != null) {
            if (!containsRecordDo(recordProxied)) {
              return addRecordDo(recordProxied);
            }
          }
        }
      }
    }
    return false;
  }

  protected boolean addRecordDo(final LayerRecord record) {
    throw new UnsupportedOperationException("addRecordDo");
  }

  @Override
  public final void clearRecords() {
    synchronized (getRecordCacheSync()) {
      clearRecordsDo();
    }
    clearRecordsAfter();
  }

  public void clearRecordsAfter() {
  }

  protected void clearRecordsDo() {
    throw new UnsupportedOperationException("clearRecordsDo");
  }

  @Override
  public final boolean containsRecord(final LayerRecord record) {
    if (record != null) {
      synchronized (getRecordCacheSync()) {
        final L layer = this.layer;
        final LayerRecord recordProxied = layer.getProxiedRecord(record);
        if (recordProxied != null) {
          return containsRecordDo(recordProxied);
        }
      }
    }
    return false;
  }

  public boolean containsRecordDo(final LayerRecord record) {
    return false;
  }

  @Override
  public final <R extends Record> void forEachRecord(final Consumer<R> action) {
    synchronized (getRecordCacheSync()) {
      forEachRecordDo(action);
    }
  }

  protected <R extends Record> void forEachRecordDo(final Consumer<R> action) {
  }

  @Override
  public String getCacheId() {
    return this.cacheId;
  }

  @Override
  public Object getRecordCacheSync() {
    return this.recordCacheSync;
  }

  @Override
  public boolean removeContainsRecord(final LayerRecord record) {
    if (record != null) {
      synchronized (getRecordCacheSync()) {
        final LayerRecord recordProxied = this.layer.getProxiedRecord(record);
        if (recordProxied != null) {
          if (containsRecordDo(recordProxied)) {
            removeRecordDo(recordProxied);
            return true;
          }
        }
      }
    }
    return false;
  }

  @Override
  public boolean removeRecord(final LayerRecord record) {
    if (record != null) {
      synchronized (getRecordCacheSync()) {
        final LayerRecord proxiedRecord = this.layer.getProxiedRecord(record);
        if (proxiedRecord != null) {
          return removeRecordDo(proxiedRecord);
        }
      }
    }
    return true;
  }

  public boolean removeRecordDo(final LayerRecord proxiedRecord) {
    throw new UnsupportedOperationException("removeRecordDo");
  }

  @Override
  public boolean replaceRecord(final LayerRecord record) {
    if (record != null) {
      synchronized (getRecordCacheSync()) {
        final LayerRecord proxiedRecord = this.layer.getProxiedRecord(record);
        if (removeContainsRecord(proxiedRecord)) {
          return addRecordDo(proxiedRecord);
        }
      }
    }
    return false;
  }

  @Override
  public void setRecords(final Iterable<? extends LayerRecord> records) {
    synchronized (getRecordCacheSync()) {
      clearRecordsDo();
      addRecords(records);
    }
  }

  @Override
  public String toString() {
    return this.cacheId;
  }
}
