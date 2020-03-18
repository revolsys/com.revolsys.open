package com.revolsys.swing.map.layer.record.table;

import java.util.Comparator;

import org.jeometry.common.logging.Logs;

import com.revolsys.record.code.CodeTable;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.table.model.RecordLayerTableModel;
import com.revolsys.swing.table.BaseRowSorter;

public class RecordLayerTableRowSorter extends BaseRowSorter {
  private final AbstractRecordLayer layer;

  public RecordLayerTableRowSorter(final AbstractRecordLayer layer,
    final RecordLayerTableModel model) {
    super(model);
    this.layer = layer;
  }

  @Override
  public Comparator<?> getComparator(final int columnIndex) {
    final RecordLayerTableModel model = (RecordLayerTableModel)getModel();
    final String fieldName = model.getColumnFieldName(columnIndex);
    final RecordDefinition recordDefinition = this.layer.getRecordDefinition();
    final CodeTable codeTable = recordDefinition.getCodeTableByFieldName(fieldName);
    if (codeTable == null) {
      final Comparator<?> comparator = super.getComparator(columnIndex);
      if (comparator == null) {
        return this.layer.getComparator(fieldName);
      } else {
        return comparator;
      }
    } else {
      return codeTable;
    }
  }

  @Override
  public boolean isSortable(final int columnIndex) {
    final RecordLayerTableModel model = (RecordLayerTableModel)getModel();
    return model.isColumnSortable(columnIndex);
  }

  @Override
  public void toggleSortOrder(final int column) {
    try {
      super.toggleSortOrder(column);
    } catch (final NullPointerException e) {
      Logs.debug(this, "Unable to toggle sort order", e);
    }
  }
}
