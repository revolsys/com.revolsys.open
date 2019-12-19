package com.revolsys.swing.map.layer.record.table.model;

import java.awt.Color;
import java.util.List;

import javax.swing.Icon;

import org.jeometry.common.awt.WebColors;

import com.revolsys.record.query.Query;
import com.revolsys.swing.Icons;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.util.Property;

public class ModeAllList extends ModeAbstractCached {
  public ModeAllList(final ListRecordLayerTableModel model) {
    super(RecordLayerTableModel.MODE_RECORDS_ALL, model);
  }

  @Override
  public void activate() {
    final AbstractRecordLayer layer = getLayer();
    addListeners( //
      Property.addListenerNewValueSource(layer, AbstractRecordLayer.RECORDS_INSERTED,
        this::addCachedRecords), //
      newRecordsDeletedListener(layer) //
    );

    super.activate();
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
  protected List<LayerRecord> getRecordsForCache() {
    final AbstractRecordLayer layer = getLayer();
    return layer.getRecords();
  }

  @Override
  public String getTitle() {
    return "Show All Records";
  }

  @Override
  public boolean isFilterByBoundingBoxSupported() {
    return true;
  }

  @Override
  protected void queryChanged(final Query query) {
    refresh();
  }
}
