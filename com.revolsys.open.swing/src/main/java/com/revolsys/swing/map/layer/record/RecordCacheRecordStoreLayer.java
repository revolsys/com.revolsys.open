package com.revolsys.swing.map.layer.record;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.jeometry.common.data.identifier.Identifier;

public class RecordCacheRecordStoreLayer extends AbstractRecordCache<RecordStoreLayer> {

  private final Set<Identifier> identifiers = new LinkedHashSet<>();

  protected final RecordCacheCollection parentCache;

  public RecordCacheRecordStoreLayer(final String cacheId, final RecordStoreLayer layer,
    final RecordCacheCollection parentCache) {
    super(layer, cacheId);
    this.parentCache = parentCache;
  }

  public void addIdentifiersToSet(final Set<Identifier> identifiers) {
    identifiers.addAll(this.identifiers);
  }

  @Override
  public boolean addRecordDo(final LayerRecord record) {
    final Identifier identifier = record.getIdentifier();
    if (identifier == null) {
      return this.parentCache.addRecordDo(record);
    } else {
      this.layer.getCachedRecord(identifier, record, false);
      return this.identifiers.add(identifier);
    }
  }

  @Override
  public void clearRecords() {
    this.identifiers.clear();
    this.parentCache.clearRecords();
  }

  @Override
  public boolean containsRecord(final LayerRecord record) {
    final Identifier identifier = record.getIdentifier();
    if (identifier != null) {
      if (this.identifiers.contains(identifier)) {
        return true;
      }
    }
    return this.parentCache.containsRecord(record);
  }

  @Override
  public <R extends LayerRecord> void forEachRecord(final Consumer<R> action) {
    this.parentCache.forEachRecord(action);
    for (final Identifier identifier : this.identifiers) {
      @SuppressWarnings("unchecked")
      final R record = (R)this.layer.getRecordById(identifier);
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
    boolean removed = false;
    final Identifier identifier = record.getIdentifier();
    if (identifier != null) {
      removed |= this.identifiers.remove(identifier);
    }
    removed |= this.parentCache.removeRecordDo(record);
    return removed;
  }

  @Override
  public String toString() {
    return this.identifiers.size() + "\t" + this.parentCache.getSize();
  }
}
