package com.revolsys.swing.map.layer.record.table.model;

import java.util.Collection;
import java.util.Map;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.revolsys.record.Record;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.table.predicate.MergedNullValuePredicate;
import com.revolsys.swing.map.layer.record.table.predicate.MergedRecordPredicate;
import com.revolsys.swing.map.layer.record.table.predicate.MergedValuePredicate;
import com.revolsys.swing.table.SortableTableModel;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.swing.table.record.RecordRowTable;
import com.revolsys.swing.table.record.model.RecordListTableModel;

public class MergedRecordsTableModel extends RecordListTableModel
  implements SortableTableModel, ListSelectionListener {
  private static final long serialVersionUID = 1L;

  public static TablePanel createPanel(final AbstractRecordLayer layer, final Record mergedObject,
    final Collection<LayerRecord> objects) {
    final MergedRecordsTableModel model = new MergedRecordsTableModel(layer, mergedObject, objects);
    final RecordRowTable table = new RecordRowTable(model);
    table.setVisibleRowCount(objects.size() + 2);
    MergedValuePredicate.add(table);
    MergedRecordPredicate.add(table);
    MergedNullValuePredicate.add(table);
    table.setSortable(false);
    table.getSelectionModel().addListSelectionListener(model);
    return new TablePanel(table);
  }

  private final AbstractRecordLayer layer;

  private final Record mergedRecord;

  public MergedRecordsTableModel(final AbstractRecordLayer layer) {
    this(layer, null, null);
  }

  public MergedRecordsTableModel(final AbstractRecordLayer layer, final Record mergedRecord,
    final Collection<LayerRecord> records) {
    super(layer.getRecordDefinition(), records, layer.getFieldNames());
    this.layer = layer;
    setFieldsOffset(1);
    this.mergedRecord = mergedRecord;
    setEditable(true);
    setReadOnlyFieldNames(layer.getUserReadOnlyFieldNames());
  }

  @Override
  public String getColumnName(final int columnIndex) {
    if (columnIndex == 0) {
      return "#";
    } else {
      return super.getColumnName(columnIndex);
    }
  }

  public Record getMergedRecord() {
    return this.mergedRecord;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Record> V getRecord(final int index) {
    if (index == super.getRowCount()) {
      return (V)this.mergedRecord;
    } else {
      return (V)super.getRecord(index);
    }
  }

  @Override
  public int getRowCount() {
    return super.getRowCount() + 1;
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    if (columnIndex == 0) {
      if (rowIndex == getRowCount() - 1) {
        return "Merge";
      } else {
        return rowIndex + 1;
      }
    } else {
      return super.getValueAt(rowIndex, columnIndex);
    }
  }

  @Override
  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    if (columnIndex == 0) {
      return false;
    } else if (rowIndex == getRowCount() - 1) {
      return super.isCellEditable(rowIndex, columnIndex);
    } else {
      return false;
    }
  }

  @Override
  public void setValueAt(final Object value, final int rowIndex, final int columnIndex) {
    final Map<String, Object> record = getRecord(rowIndex);
    if (record != null) {
      final String name = getFieldName(columnIndex);
      record.put(name, value);
    }
  }

  @Override
  public void valueChanged(final ListSelectionEvent event) {
    final RecordRowTable table = getTable();
    final ListSelectionModel selectionModel = table.getSelectionModel();
    final int rowCount = super.getRowCount();
    final boolean mergedSelected = selectionModel.isSelectedIndex(rowCount);
    for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
      final Record record = getRecord(rowIndex);
      if (record != null) {
        if (mergedSelected || selectionModel.isSelectedIndex(rowIndex)) {
          this.layer.addHighlightedRecords((LayerRecord)record);
        } else {
          this.layer.unHighlightRecords((LayerRecord)record);
        }
      }
    }
    this.layer.zoomToHighlighted();
  }

}
