package com.revolsys.swing.map.layer.record.table.model;

import java.util.List;

import javax.swing.Icon;

import com.revolsys.record.RecordState;
import com.revolsys.swing.Icons;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.action.enablecheck.ObjectPropertyEnableCheck;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.Property;

public class ModeChanged extends ModeAbstractCached {
  private final EnableCheck enableCheck;

  public ModeChanged(final RecordLayerTableModel model) {
    super(RecordLayerTableModel.MODE_RECORDS_CHANGED, model);
    final AbstractRecordLayer layer = getLayer();
    this.enableCheck = new ObjectPropertyEnableCheck(layer, "hasChangedRecords");
  }

  @Override
  public void activate() {
    final AbstractRecordLayer layer = getLayer();
    addListeners( //
      Property.addListenerNewValueSource(layer, AbstractRecordLayer.RECORDS_INSERTED,
        this::addCachedRecords), //
      Property.addListenerNewValueSource(layer, AbstractRecordLayer.RECORDS_DELETED,
        this::recordsDeleted), //
      Property.addListenerNewValueSource(layer, AbstractRecordLayer.RECORD_CACHE_MODIFIED,
        this::addCachedRecord) //
    );
    super.activate();
  }

  @Override
  public EnableCheck getEnableCheck() {
    return this.enableCheck;
  }

  @Override
  public Icon getIcon() {
    return Icons.getIcon("change_table_filter");
  }

  @Override
  protected List<LayerRecord> getRecordsForCache() {
    final AbstractRecordLayer layer = getLayer();
    return layer.getRecordsChanged();
  }

  @Override
  public String getTitle() {
    return "Show Only Changed Records";
  }

  @Override
  protected void recordsDeleted(final List<LayerRecord> records) {
    Invoke.later(() -> {
      for (final LayerRecord record : records) {
        if (RecordState.DELETED == record.getState()) {
          removeCachedRecord(record);
        } else {
          addCachedRecord(record);
        }
      }
    });
  }
}
