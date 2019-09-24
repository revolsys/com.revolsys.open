package com.revolsys.swing.map.layer.record.table.model;

import java.awt.Color;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ListSelectionModel;

import org.jeometry.common.awt.WebColors;

import com.revolsys.swing.Icons;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.util.Property;

public class ModeSelected extends ModeAbstractCached {

  public ModeSelected(final RecordLayerTableModel model) {
    super(RecordLayerTableModel.MODE_RECORDS_SELECTED, model);
  }

  @Override
  public void activate() {
    final AbstractRecordLayer layer = getLayer();
    addListeners( //
      Property.addListenerRunnable(layer, AbstractRecordLayer.RECORDS_DELETED, this::refresh), //
      Property.addListenerRunnable(layer, AbstractRecordLayer.RECORDS_SELECTED, this::refresh) //
    );
    super.activate();
  }

  @Override
  protected boolean canAddCachedRecord(final LayerRecord record) {
    final AbstractRecordLayer layer = getLayer();
    if (layer.isSelected(record)) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void deactivate() {
    super.deactivate();
    final AbstractRecordLayer layer = getLayer();
    layer.clearHighlightedRecords();
  }

  @Override
  public Color getBorderColor() {
    return WebColors.LimeGreen;
  }

  @Override
  public Icon getIcon() {
    return Icons.getIcon("filter_selected");
  }

  @Override
  protected List<LayerRecord> getRecordsForCache() {
    final AbstractRecordLayer layer = getLayer();
    final List<LayerRecord> selectedRecords = layer.getSelectedRecords();
    return selectedRecords;
  }

  @Override
  public String getTitle() {
    return "Show Only Selected Records";
  }

  @Override
  protected ListSelectionModel newSelectionModel(final RecordLayerTableModel tableModel) {
    return new RecordLayerHighlightedListSelectionModel(tableModel);
  }

  @Override
  protected void recordUpdated(final LayerRecord record) {
    // final AbstractRecordLayer layer = getLayer();
    // if (layer.isSelected(record)) {
    // final int index = indexOf(record);
    // if (index != -1) {
    // fireRecordUpdated(index);
    // }
    // }
  }
}
