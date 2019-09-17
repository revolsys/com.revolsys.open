package com.revolsys.swing.map.layer.record;

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
    if (layer.isLayerRecord(record)) {
      synchronized (layer.getSync()) {
        if (!(record.getState() == RecordState.DELETED && !layer.isDeleted(record))) {
          if (!containsRecord(record)) {
            final LayerRecord recordProxied = layer.getRecordProxied(record);
            return addRecordDo(recordProxied);
          }
        }
      }
    }
    return false;
  }

  public abstract boolean addRecordDo(LayerRecord record);

  public String getCacheId() {
    return this.cacheId;
  }

  @Override
  public boolean removeRecord(final LayerRecord record) {
    final L layer = this.layer;
    if (layer.isLayerRecord(record)) {
      synchronized (layer.getSync()) {
        final LayerRecord proxiedRecord = layer.getProxiedRecord(record);
        return removeRecordDo(proxiedRecord);
      }
    }
    return false;
  }

  public abstract boolean removeRecordDo(LayerRecord proxiedRecord);

  @Override
  public boolean replaceRecord(final LayerRecord record) {
    if (removeRecord(record)) {
      return addRecord(record);
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return this.cacheId;
  }
}
