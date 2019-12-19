package com.revolsys.swing.map.layer.record.table.model;

import java.awt.Color;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.Icon;
import javax.swing.SwingWorker;

import org.jeometry.common.awt.WebColors;
import org.jeometry.common.logging.Logs;

import com.revolsys.collection.map.LruMap;
import com.revolsys.predicate.Predicates;
import com.revolsys.record.Record;
import com.revolsys.record.query.Condition;
import com.revolsys.record.query.Query;
import com.revolsys.swing.Icons;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.Property;

public class ModeAllPaged extends ModeAbstractCached {
  private int persistedRecordCount;

  private SwingWorker<?, ?> recordCountWorker;

  private final int pageSize = 100;

  private final Map<Integer, List<LayerRecord>> pageCache = new LruMap<>(5);

  private final Set<Integer> loadingPageNumbers = new LinkedHashSet<>();

  public ModeAllPaged(final RecordLayerTableModel model) {
    super(RecordLayerTableModel.MODE_RECORDS_ALL, model);
  }

  @Override
  public void activate() {
    final AbstractRecordLayer layer = getLayer();
    addListeners( //
      Property.addListenerNewValueSource(layer, AbstractRecordLayer.RECORDS_INSERTED,
        this::addCachedRecords), //
      newRecordsDeletedListener(layer)//
    );
    super.activate();
  }

  private void clear() {
    synchronized (this) {
      if (this.recordCountWorker != null) {
        this.recordCountWorker.cancel(true);
        this.recordCountWorker = null;
      }
      synchronized (this.querySync) {
        this.loadingPageNumbers.clear();
        this.pageCache.clear();
        this.persistedRecordCount = -1;
      }
    }
  }

  @Override
  public void deactivate() {
    clear();
    super.deactivate();
  }

  @Override
  public void forEachRecord(final Query query, final Consumer<? super LayerRecord> action) {
    final AbstractRecordLayer layer = getLayer();
    layer.forEachRecord(query, action);
  }

  @Override
  public Color getBorderColor() {
    return WebColors.Blue;
  }

  @Override
  public Icon getIcon() {
    return Icons.getIcon("table_filter");
  }

  @Override
  public int getRecordCount() {
    synchronized (this) {
      if (this.persistedRecordCount < 0) {
        if (this.recordCountWorker == null) {
          final long refreshIndex = getRefreshIndexNext();
          final AbstractRecordLayer layer = getLayer();
          this.recordCountWorker = Invoke.background("Query row count " + layer.getName(),
            this::getRecordCountPersisted, (rowCount) -> {
              if (canRefreshFinish(refreshIndex)) {
                this.persistedRecordCount = rowCount;
                this.recordCountWorker = null;
                fireTableDataChanged();
              }
            });
        }
        return 0;
      } else {
        int count = super.getRecordCount();
        count += this.persistedRecordCount;
        return count;
      }
    }
  }

  protected int getRecordCountPersisted() {
    final Query query = getQuery();
    try {
      final AbstractRecordLayer layer = getLayer();
      if (query == null) {
        return layer.getRecordCountPersisted();
      } else {
        return layer.getRecordCountPersisted(query);
      }
    } catch (final Throwable e) {
      Logs.debug(this, "Error running query:" + query, e);
      return 0;
    }
  }

  @Override
  protected LayerRecord getRecordDo(final int row) {
    final int recordCountChanges = super.getRecordCount();
    if (row >= 0) {
      if (row < recordCountChanges) {
        return super.getRecordDo(row);
      } else {
        final int persistedRow = row - recordCountChanges;
        final int recordCount = getRecordCount();
        if (row < recordCount) {
          final int pageNumber = persistedRow / this.pageSize;
          final int recordNumber = persistedRow % this.pageSize;
          final LayerRecord record = getRecordPagePersisted(pageNumber, recordNumber);
          return record;
        }
      }
    }
    return null;
  }

  protected LayerRecord getRecordPagePersisted(final int pageNumber, final int recordIndex) {
    synchronized (this) {
      final List<LayerRecord> page;
      synchronized (this.querySync) {
        page = this.pageCache.get(pageNumber);
      }
      if (page == null) {
        Query query;
        synchronized (this.querySync) {
          if (this.loadingPageNumbers.contains(pageNumber)) {
            return null;
          } else {
            this.loadingPageNumbers.add(pageNumber);
            query = getQuery();
          }
        }
        if (query != null) {
          final long refreshIndex = getRefreshIndex();
          Invoke.background("loadPage" + getTypeName(), 2, "Loading records " + getTypeName(),
            () -> loadPage(query, pageNumber), //
            (records) -> {
              setRecords(query, refreshIndex, pageNumber, records);
            });
        }
      } else {
        if (recordIndex < page.size()) {
          final LayerRecord record = page.get(recordIndex);
          return record;
        }
      }
    }
    return null;
  }

  @Override
  protected List<LayerRecord> getRecordsForCache() {
    final AbstractRecordLayer layer = getLayer();
    final List<LayerRecord> records = layer.getRecordsNew();
    final Condition filter = getFilter();
    if (!filter.isEmpty()) {
      Predicates.retain(records, filter);
      for (final LayerRecord modifiedRecord : layer.getRecordsModified()) {
        if (layer.filterTestModified(filter, modifiedRecord)) {
          records.add(modifiedRecord);
        }
      }
    }
    final RecordLayerTableModel model = getTableModel();
    final Comparator<Record> comparator = model.getOrderByComparatorIdentifier();
    if (comparator != null) {
      records.sort(comparator);
    }
    return records;
  }

  protected List<LayerRecord> getRecordsLayer(final Query query) {
    final AbstractRecordLayer layer = getLayer();
    return layer.getRecordsPersisted(query);
  }

  @Override
  public String getTitle() {
    return "Show All Records";
  }

  private String getTypeName() {
    return getTableModel().getTypeName();
  }

  @Override
  public boolean isFilterByBoundingBoxSupported() {
    return true;
  }

  /**
   * Has the record been changed such that
   * @param record
   * @return
   */
  protected boolean isRecordPageQueryChanged(final LayerRecord record) {
    final AbstractRecordLayer layer = getLayer();
    if (layer.isModified(record)) {
      final Condition filter = getFilter();
      final RecordLayerTableModel model = getTableModel();
      final Comparator<Record> comparator = model.getOrderByComparatorIdentifier();
      if (comparator != null) {
        final Record orginialRecord = record.getOriginalRecord();
        final int compare = comparator.compare(record, orginialRecord);
        if (compare != 0) {
          return true;
        }
      }
      if (!filter.isEmpty()) {
        if (filter.test(record)) {
          final Record orginialRecord = record.getOriginalRecord();
          if (!filter.test(orginialRecord)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  @Override
  public boolean isSortable() {
    return false;
  }

  private List<LayerRecord> loadPage(final Query query, final int pageNumber) {
    try {
      final Query pageQuery = query//
        .clone()//
        .setOffset(this.pageSize * pageNumber)//
        .setLimit(this.pageSize) //
      ;
      return getRecordsLayer(pageQuery);
    } finally {
      synchronized (this.querySync) {
        if (query == getQuery()) {
          this.loadingPageNumbers.remove(pageNumber);
        }
      }
    }
  }

  @Override
  protected void queryChanged(final Query query) {
    refresh();
  }

  @Override
  protected void recordUpdated(final LayerRecord record) {
    Invoke.later(() -> {
      final Condition filter = getFilter();
      final AbstractRecordLayer layer = getLayer();
      if (layer.isNew(record)) {
        if (filter.test(record)) {
          addCachedRecord(record);
        } else {
          removeCachedRecord(record);
        }
      } else if (!filter.isEmpty()) {
        if (layer.isModified(record)) {
          if (layer.filterTestModified(filter, record)) {
            addCachedRecord(record);
          } else {
            removeCachedRecord(record);
          }

        }
      }
    });
  }

  @Override
  public void refresh(final long refreshIndex) {
    clear();
    super.refresh(refreshIndex);
  }

  protected void setRecords(final Query query, final long refreshIndex, final int pageNumber,
    final List<LayerRecord> records) {
    boolean updated = false;
    synchronized (this.querySync) {
      if (query == getQuery() && canRefreshFinish(refreshIndex)) {
        this.pageCache.put(pageNumber, records);
        updated = true;
      }
    }
    if (updated) {
      final RecordLayerTableModel model = getTableModel();
      model.fireTableRowsUpdated(pageNumber * this.pageSize,
        Math.min(getRecordCount(), (pageNumber + 1) * this.pageSize - 1));

    }
  }
}
