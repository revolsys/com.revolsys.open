package com.revolsys.swing.map.layer.record.table;

import javax.swing.RowSorter;
import javax.swing.table.TableModel;

import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.table.model.RecordLayerTableModel;
import com.revolsys.swing.table.record.editor.RecordTableCellEditor;
import com.revolsys.swing.table.record.row.RecordRowTable;

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
  protected RecordTableCellEditor createTableCellEditor() {
    return new RecordLayerTableCellEditor(this);
  }

  @SuppressWarnings("unchecked")
  public <V extends AbstractRecordLayer> V getLayer() {
    final RecordLayerTableModel model = (RecordLayerTableModel)getTableModel();
    return (V)model.getLayer();
  }
}
