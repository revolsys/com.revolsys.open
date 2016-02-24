package com.revolsys.swing.map.layer.record.table.predicate;

import java.awt.Component;

import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;

import com.revolsys.awt.WebColors;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.record.Record;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.swing.map.layer.record.table.model.MergedRecordsTableModel;
import com.revolsys.swing.table.record.RecordRowTable;

public class MergedValuePredicate implements HighlightPredicate {
  public static void add(final RecordRowTable table) {
    final MergedRecordsTableModel model = table.getTableModel();
    final MergedValuePredicate predicate = new MergedValuePredicate(model);

    table.addHighlighter(new ColorHighlighter(predicate, WebColors.Moccasin, WebColors.DarkOrange,
      WebColors.DarkOrange, WebColors.Moccasin));
  }

  private final MergedRecordsTableModel model;

  public MergedValuePredicate(final MergedRecordsTableModel model) {
    this.model = model;
  }

  @Override
  public boolean isHighlighted(final Component renderer, final ComponentAdapter adapter) {
    try {
      final int rowIndex = adapter.convertRowIndexToView(adapter.row);
      final int columnIndex = adapter.convertColumnIndexToView(adapter.column);
      final Record record = this.model.getRecord(rowIndex);
      final Record mergedRecord = this.model.getMergedRecord();

      if (record == mergedRecord) {
        return false;
      } else {
        final String fieldName = this.model.getFieldName(columnIndex);
        final FieldDefinition field = this.model.getColumnFieldDefinition(columnIndex);
        if (field != null) {
          final Object value = record.getValue(fieldName);
          final Object mergedValue = mergedRecord.getValue(fieldName);
          if (value instanceof Geometry) {
            return false;
          } else if (mergedValue instanceof Geometry) {
            return false;
          } else {
            return !field.equals(value, mergedValue);
          }
        }
      }
    } catch (final IndexOutOfBoundsException e) {
    }
    return false;
  }
}
