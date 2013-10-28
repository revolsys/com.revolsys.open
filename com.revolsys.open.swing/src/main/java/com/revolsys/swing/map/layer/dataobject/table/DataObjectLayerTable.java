package com.revolsys.swing.map.layer.dataobject.table;

import javax.swing.RowSorter;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import com.revolsys.swing.map.layer.dataobject.AbstractDataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.table.model.DataObjectLayerTableModel;
import com.revolsys.swing.table.dataobject.row.DataObjectRowTable;

public class DataObjectLayerTable extends DataObjectRowTable {

  private static final long serialVersionUID = 1L;

  public DataObjectLayerTable(final DataObjectLayerTableModel model) {
    super(model);
  }

  public DataObjectLayerTable(final DataObjectLayerTableModel model,
    final TableCellRenderer cellRenderer) {
    super(model, cellRenderer);
  }

  @Override
  protected RowSorter<? extends TableModel> createDefaultRowSorter() {
    final AbstractDataObjectLayer layer = getLayer();
    final DataObjectLayerTableModel model = (DataObjectLayerTableModel)getTableModel();
    return new DataObjectLayerTableRowSorter(layer, model);
  }

  @SuppressWarnings("unchecked")
  public <V extends AbstractDataObjectLayer> V getLayer() {
    final DataObjectLayerTableModel model = (DataObjectLayerTableModel)getTableModel();
    return (V)model.getLayer();
  }
}
