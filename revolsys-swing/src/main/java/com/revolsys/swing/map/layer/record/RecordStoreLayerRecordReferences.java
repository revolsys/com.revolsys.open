package com.revolsys.swing.map.layer.record;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jeometry.common.data.identifier.Identifier;

import com.revolsys.collection.map.IntegerCountMap;

public class RecordStoreLayerRecordReferences {
  private Map<Identifier, RecordStoreLayerRecord> recordsByIdentifier = new HashMap<>();

  private IntegerCountMap<Identifier> recordCountsByIdentifier = new IntegerCountMap<>();

  public synchronized void addRecord(final Identifier identifier,
    final RecordStoreLayerRecord record) {
    if (identifier != null) {
      this.recordsByIdentifier.put(identifier, record);
    }
  }

  public void addRecord(final RecordStoreLayerRecord record) {
    final Identifier identifier = record.getIdentifier();
    addRecord(identifier, record);
  }

  public synchronized void clear() {
    this.recordCountsByIdentifier.clearCounts();
    this.recordsByIdentifier.clear();
  }

  public synchronized void decrementReferenceCount(final Identifier identifier) {
    if (!this.recordCountsByIdentifier.decrementCount(identifier)) {
      removeIdentifier(identifier);
    }
  }

  public synchronized List<Identifier> getIdentifiers() {
    if (this.recordsByIdentifier.isEmpty()) {
      return Collections.emptyList();
    } else {
      return new ArrayList<>(this.recordsByIdentifier.keySet());
    }
  }

  public synchronized RecordStoreLayerRecord getRecord(final Identifier identifier) {
    return this.recordsByIdentifier.get(identifier);
  }

  public synchronized void incrementReferenceCount(final Identifier identifier) {
    this.recordCountsByIdentifier.incrementCount(identifier);
  }

  public synchronized void rebuildReferenceCounts(
    final List<RecordCacheRecordStoreLayer> recordStoreLayerCaches) {
    final Map<Identifier, RecordStoreLayerRecord> recordsByIdentifier = new HashMap<>();
    final IntegerCountMap<Identifier> recordCountsByIdentifier = new IntegerCountMap<>();
    final Map<Identifier, RecordStoreLayerRecord> oldRecordsByIdentifier = this.recordsByIdentifier;
    for (final RecordCacheRecordStoreLayer recordCache : recordStoreLayerCaches) {
      for (final Identifier identifier : recordCache.getIdentifiers()) {
        final RecordStoreLayerRecord record = oldRecordsByIdentifier.get(identifier);
        recordsByIdentifier.put(identifier, record);
        recordCountsByIdentifier.incrementCount(identifier);
      }
    }
    this.recordsByIdentifier = recordsByIdentifier;
    this.recordCountsByIdentifier = recordCountsByIdentifier;
  }

  public synchronized boolean removeIdentifier(final Identifier identifier) {
    if (identifier == null) {
      return false;
    } else {
      return this.recordsByIdentifier.remove(identifier) != null;
    }
  }
}
