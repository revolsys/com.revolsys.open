package com.revolsys.swing.map.layer.record;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import com.revolsys.util.Label;

public class CachedLayerRecordArrayList extends ArrayList<LayerRecord> {

  private static final AtomicInteger COUNT = new AtomicInteger();

  private final Label cacheId = new Label("list_" + COUNT.incrementAndGet());

  private final RecordStoreLayer layer;

  public CachedLayerRecordArrayList(final RecordStoreLayer layer) {
    super();
    this.layer = layer;
  }

  public CachedLayerRecordArrayList(final RecordStoreLayer layer,
    final Collection<? extends LayerRecord> collection) {
    super(collection);
    this.layer = layer;
  }

  @Override
  public void add(final int index, final LayerRecord element) {
    super.add(index, element);
    updateCache();
  }

  @Override
  public boolean add(final LayerRecord e) {
    final boolean result = super.add(e);
    updateCache();
    return result;
  }

  @Override
  public boolean addAll(final Collection<? extends LayerRecord> c) {
    final boolean result = super.addAll(c);
    updateCache();
    return result;
  }

  @Override
  public boolean addAll(final int index,
    final Collection<? extends LayerRecord> c) {
    final boolean addAll = super.addAll(index, c);
    updateCache();
    return addAll;
  }

  @Override
  public void clear() {
    super.clear();
    this.layer.clearCachedRecords(this.cacheId);
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    this.layer.clearCachedRecords(this.cacheId);
  }

  @Override
  public LayerRecord remove(final int index) {
    final LayerRecord result = super.remove(index);
    updateCache();
    return result;
  }

  @Override
  public boolean remove(final Object o) {
    final boolean result = super.remove(o);
    updateCache();
    return result;
  }

  @Override
  public boolean removeAll(final Collection<?> c) {
    final boolean result = super.removeAll(c);
    updateCache();
    return result;
  }

  @Override
  public LayerRecord set(final int index, final LayerRecord element) {
    final LayerRecord oldValue = super.set(index, element);
    updateCache();
    return oldValue;
  }

  private void updateCache() {
    // this.layer.setRecordsToCache(this.cacheId, this);
  }

}
