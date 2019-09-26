package com.revolsys.swing.map.layer.record;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.jeometry.common.data.identifier.Identifier;

import com.revolsys.record.Record;

public class RecordCacheRecordStoreLayer extends AbstractRecordCache<RecordStoreLayer> {

  final Set<Identifier> identifiers = new HashSet<>();

  protected final RecordCacheCollection parentCache;

  public RecordCacheRecordStoreLayer(final String cacheId, final RecordStoreLayer layer,
    final RecordCacheCollection parentCache) {
    super(layer, cacheId);
    this.parentCache = parentCache;
  }

  @Override
  public boolean addRecordDo(final LayerRecord record) {
    final Identifier identifier = record.getIdentifier();
    if (identifier == null) {
      return this.parentCache.addRecordDo(record);
    } else {
      this.layer.getCachedRecord(identifier, record, false);
      synchronized (this.identifiers) {
        return this.identifiers.add(identifier);
      }
    }
  }

  @Override
  public void clearRecords() {
    synchronized (this.identifiers) {
      this.identifiers.clear();
    }
    this.parentCache.clearRecords();
  }

  @Override
  public boolean containsRecordDo(final LayerRecord record) {
    final Identifier identifier = record.getIdentifier();
    if (identifier != null) {
      synchronized (this.identifiers) {
        if (this.identifiers.contains(identifier)) {
          return true;
        }
      }
    }
    return this.parentCache.containsRecordDo(record);
  }

  @Override
  public <R extends Record> void forEachRecordDo(final Consumer<R> action) {
    this.parentCache.forEachRecord(action);
    final RecordStoreLayer layer = this.layer;
    for (final Identifier identifier : this.identifiers) {
      @SuppressWarnings("unchecked")
      final R record = (R)layer.getRecordById(identifier);
      if (record != null) {
        action.accept(record);
      }
    }
  }

  @Override
  public int getSize() {
    return this.parentCache.getSize() + this.identifiers.size();
  }

  @Override
  public boolean removeRecordDo(final LayerRecord record) {
    final Identifier identifier = record.getIdentifier();
    if (identifier != null) {
      synchronized (this.identifiers) {
        this.identifiers.remove(identifier);
      }
    }
    this.parentCache.removeRecordDo(record);
    return true;

  }

  @Override
  public String toString() {
    return this.parentCache.toString() + "\t" + this.identifiers.size();
  }
}
