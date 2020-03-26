package com.revolsys.swing.map.layer.record;

import java.util.Collection;
import java.util.function.Consumer;

import org.jeometry.common.data.identifier.Identifier;

import com.revolsys.record.Record;
import com.revolsys.util.ExitLoopException;

public class RecordCacheCollection extends AbstractRecordCache<AbstractRecordLayer> {

  private final Collection<LayerRecord> records;

  public RecordCacheCollection(final String cacheId, final AbstractRecordLayer layer) {
    super(layer, cacheId);
    this.records = layer.newRecordCacheCollection();
  }

  @Override
  protected boolean addRecordDo(final LayerRecord record) {
    return this.records.add(record);
  }

  @Override
  public void clearRecordsDo() {
    this.records.clear();
  }

  @Override
  public boolean containsRecordDo(final LayerRecord record) {
    if (this.records.isEmpty()) {
      return false;
    } else {
      return record.contains(this.records);
    }
  }

  @Override
  public <R extends Record> void forEachRecordDo(final Consumer<R> action) {
    try {
      for (final LayerRecord record : this.records) {
        @SuppressWarnings("unchecked")
        final R proxyRecord = (R)record.getRecordProxy();
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
  public boolean isCached(final Identifier identifier) {
    return false;
  }

  @Override
  public boolean removeRecordDo(final LayerRecord record) {
    record.removeFrom(this.records);
    return true;
  }

  @Override
  public String toString() {
    return super.toString() + "\t" + getSize() + "\t";
  }
}
