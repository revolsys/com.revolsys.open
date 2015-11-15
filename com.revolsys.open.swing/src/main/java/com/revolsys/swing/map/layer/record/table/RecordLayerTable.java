package com.revolsys.swing.map.layer.record.table;

import java.util.EventObject;

import javax.swing.RowSorter;
import javax.swing.table.TableModel;

import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.LayerRecordMenu;
import com.revolsys.swing.map.layer.record.table.model.RecordLayerTableModel;
import com.revolsys.swing.table.record.RecordRowTable;
import com.revolsys.swing.table.record.editor.RecordTableCellEditor;

public class RecordLayerTable extends RecordRowTable {
  private static final long serialVersionUID = 1L;

  public RecordLayerTable(final RecordLayerTableModel model) {
    super(model);
    getTableHeader().setReorderingAllowed(false);
  }

  @Override
  protected RowSorter<? extends TableModel> createDefaultRowSorter() {
    final AbstractRecordLayer layer = getLayer();
    final RecordLayerTableModel model = (RecordLayerTableModel)getTableModel();
    return new RecordLayerTableRowSorter(layer, model);
  }

  @Override
  public boolean editCellAt(final int row, final int column, final EventObject e) {
    final LayerRecord record = getRecord(row);
    LayerRecordMenu.setEventRecord(record);
    return super.editCellAt(row, column, e);
  }

  @SuppressWarnings("unchecked")
  public <V extends AbstractRecordLayer> V getLayer() {
    final RecordLayerTableModel model = (RecordLayerTableModel)getTableModel();
    return (V)model.getLayer();
  }

  public LayerRecord getRecord(final int row) {
    final RecordLayerTableModel model = getTableModel();
    if (model == null) {
      return null;
    } else {
      return model.getRecord(row);
    }
  }

  @Override
  protected RecordTableCellEditor newTableCellEditor() {
    return new RecordLayerTableCellEditor(this);
  }
}
