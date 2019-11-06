package com.revolsys.swing.map.layer.record.table.model;

import java.awt.Color;
import java.util.List;

import javax.swing.Icon;

import org.jeometry.common.awt.WebColors;

import com.revolsys.record.RecordState;
import com.revolsys.swing.Icons;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.Property;

public class ModeChanged extends ModeAbstractCached {

  public ModeChanged(final RecordLayerTableModel model) {
    super(RecordLayerTableModel.MODE_RECORDS_CHANGED, model);
  }

  @Override
  public void activate() {
    final AbstractRecordLayer layer = getLayer();
    addListeners( //
      Property.addListenerNewValueSource(layer, AbstractRecordLayer.RECORDS_INSERTED,
        this::addCachedRecords), //
      newRecordsDeletedListener(layer), //
      Property.addListenerNewValueSource(layer, AbstractRecordLayer.RECORD_CACHE_MODIFIED,
        this::addCachedRecord) //
    );
    super.activate();
  }

  @Override
  public Color getBorderColor() {
    return WebColors.Fuchsia;
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
    if (Invoke.swingThread(this::recordsDeleted, records)) {
      for (final LayerRecord record : records) {
        if (RecordState.DELETED == record.getState()) {
          removeCachedRecord(record);
        } else {
          addCachedRecord(record);
        }
      }
    }
  }
}
