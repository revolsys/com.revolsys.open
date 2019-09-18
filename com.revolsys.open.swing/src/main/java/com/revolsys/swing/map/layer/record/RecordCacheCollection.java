package com.revolsys.swing.map.layer.record;

import java.util.Collection;
import java.util.function.Consumer;

import com.revolsys.util.ExitLoopException;

public class RecordCacheCollection extends AbstractRecordCache<AbstractRecordLayer> {

  private final Collection<LayerRecord> records;

  public RecordCacheCollection(final String cacheId, final AbstractRecordLayer layer) {
    super(layer, cacheId);
    this.records = layer.newRecordCacheCollection();
  }

  @Override
  public boolean addRecordDo(final LayerRecord record) {
    return this.records.add(record);
  }

  @Override
  public void clearRecords() {
    this.records.clear();
  }

  @Override
  public boolean containsRecordDo(final LayerRecord record) {
    return record.contains(this.records);
  }

  @Override
  public <R extends LayerRecord> void forEachRecord(final Consumer<R> action) {
    try {
      final AbstractRecordLayer layer = this.layer;
      for (final LayerRecord record : this.records) {
        final R proxyRecord = layer.newProxyLayerRecord(record);
        action.accept(proxyRecord);
      }
    } catch (final ExitLoopException e) {
    }
  }

  @Override
  public int getSize() {
    return this.records.size();
  }

  @Override
  public boolean removeRecordDo(final LayerRecord record) {
    return record.removeFrom(this.records) != -1;
  }

  @Override
  public String toString() {
    return getSize() + "\t" + super.toString();
  }
}
