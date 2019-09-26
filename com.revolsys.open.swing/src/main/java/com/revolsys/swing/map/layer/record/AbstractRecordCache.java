package com.revolsys.swing.map.layer.record;

import java.util.function.Consumer;

import com.revolsys.record.Record;
import com.revolsys.record.RecordState;

public abstract class AbstractRecordCache<L extends AbstractRecordLayer> implements RecordCache {

  protected L layer;

  protected String cacheId;

  public AbstractRecordCache(final L layer, final String cacheId) {
    this.layer = layer;
    this.cacheId = cacheId;
  }

  @Override
  public boolean addRecord(final LayerRecord record) {
    final L layer = this.layer;
    synchronized (layer.getSync()) {
      if (!(record.getState() == RecordState.DELETED && !layer.isDeleted(record))) {
        final LayerRecord recordProxied = layer.getProxiedRecord(record);
        if (recordProxied != null) {
          if (!containsRecordDo(recordProxied)) {
            return addRecordDo(recordProxied);
          }
        }
      }
    }
    return false;
  }

  public abstract boolean addRecordDo(LayerRecord record);

  @Override
  public final boolean containsRecord(final LayerRecord record) {
    final LayerRecord recordProxied = this.layer.getProxiedRecord(record);
    if (recordProxied != null) {
      return containsRecordDo(recordProxied);
    }
    return false;
  }

  public abstract boolean containsRecordDo(LayerRecord record);

  @Override
  public final <R extends Record> void forEachRecord(final Consumer<R> action) {
    final L layer = this.layer;
    synchronized (layer.getSync()) {
      forEachRecordDo(action);
    }
  }

  protected abstract <R extends Record> void forEachRecordDo(Consumer<R> action);

  @Override
  public String getCacheId() {
    return this.cacheId;
  }

  @Override
  public boolean removeContainsRecord(final LayerRecord record) {
    final L layer = this.layer;
    synchronized (layer.getSync()) {
      final LayerRecord recordProxied = this.layer.getProxiedRecord(record);
      if (recordProxied != null) {
        if (containsRecordDo(record)) {
          removeRecordDo(record);
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean removeRecord(final LayerRecord record) {
    final L layer = this.layer;
    synchronized (layer.getSync()) {
      final LayerRecord proxiedRecord = layer.getProxiedRecord(record);
      if (proxiedRecord != null) {
        return removeRecordDo(proxiedRecord);
      }
    }
    return true;
  }

  public abstract boolean removeRecordDo(LayerRecord proxiedRecord);

  @Override
  public boolean replaceRecord(final LayerRecord record) {
    final L layer = this.layer;
    synchronized (layer.getSync()) {
      final LayerRecord proxiedRecord = layer.getProxiedRecord(record);
      if (removeContainsRecord(proxiedRecord)) {
        return addRecordDo(proxiedRecord);
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return this.cacheId;
  }
}
