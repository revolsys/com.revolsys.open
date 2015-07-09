package com.revolsys.swing.map.layer.record.table.model;

import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;

import com.revolsys.data.query.Query;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.types.DataType;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.swing.EventQueue;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.ListRecordLayer;
import com.revolsys.swing.map.layer.record.table.RecordLayerTable;

public class RecordListLayerTableModel extends RecordLayerTableModel
  implements PropertyChangeListener {
  private static final long serialVersionUID = 1L;

  public static RecordLayerTable createTable(final ListRecordLayer layer) {
    final RecordLayerTableModel model = new RecordListLayerTableModel(layer);
    final RecordLayerTable table = new RecordLayerTable(model);

    EventQueue.addPropertyChange(layer, "hasSelectedRecords", () -> selectionChanged(table, model));

    return table;
  }

  private List<LayerRecord> records = Collections.emptyList();

  public RecordListLayerTableModel(final ListRecordLayer layer) {
    super(layer);
    setEditable(false);
    setSortableModes(MODE_SELECTED, MODE_ALL);
  }

  @Override
  public int getRowCountInternal() {
    if (getFieldFilterMode().equals(MODE_ALL)) {
      final Query query = getFilterQuery();
      query.setOrderBy(getOrderBy());
      this.records = getLayer().query(query);
      return this.records.size();
    } else {
      return super.getRowCountInternal();
    }
  }

  @Override
  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    if (isEditable()) {
      final String columnName = getColumnName(columnIndex);
      final RecordDefinition recordDefinition = getRecordDefinition();
      final DataType dataType = recordDefinition.getFieldType(columnName);
      if (Geometry.class.isAssignableFrom(dataType.getJavaClass())) {
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  @Override
  protected LayerRecord loadLayerRecord(final int row) {
    if (row >= 0 && row < this.records.size()) {
      return this.records.get(row);
    } else {
      return null;
    }
  }

  @Override
  public void setValueAt(final Object value, final int rowIndex, final int columnIndex) {
    final Record record = getRecord(rowIndex);
    if (record != null) {
      final String name = getColumnName(columnIndex);
      final Object oldValue = record.getValueByPath(name);
      record.setValue(name, value);
      firePropertyChange(record, name, oldValue, value);
    }
  }
}
