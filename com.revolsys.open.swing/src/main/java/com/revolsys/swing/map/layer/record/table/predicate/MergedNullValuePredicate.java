package com.revolsys.swing.map.layer.record.table.predicate;

import java.awt.Component;

import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;

import com.revolsys.awt.WebColors;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.swing.map.layer.record.table.model.MergedRecordsTableModel;
import com.revolsys.swing.table.dataobject.row.DataObjectRowTable;

public class MergedNullValuePredicate implements HighlightPredicate {

  public static void add(final DataObjectRowTable table) {
    final MergedRecordsTableModel model = table.getTableModel();
    final MergedNullValuePredicate predicate = new MergedNullValuePredicate(
      model);
    table.addHighlighter(new ColorHighlighter(predicate, WebColors.Yellow,
      WebColors.Black, WebColors.Orange, WebColors.Black));
  }

  private final MergedRecordsTableModel model;

  public MergedNullValuePredicate(final MergedRecordsTableModel model) {
    this.model = model;
  }

  @Override
  public boolean isHighlighted(final Component renderer,
    final ComponentAdapter adapter) {
    try {
      final int rowIndex = adapter.convertRowIndexToView(adapter.row);
      final int columnIndex = adapter.convertColumnIndexToView(adapter.column);
      final DataObject object = model.getRecord(rowIndex);
      final DataObject mergedObject = model.getMergedObject();

      if (object != mergedObject) {
        final String attributeName = this.model.getFieldName(columnIndex);
        final Object value = object.getValue(attributeName);
        final Object mergedValue = mergedObject.getValue(attributeName);
        if (value == null && mergedValue != null) {
          return true;
        }
      }
    } catch (final IndexOutOfBoundsException e) {
    }
    return false;
  }
}
