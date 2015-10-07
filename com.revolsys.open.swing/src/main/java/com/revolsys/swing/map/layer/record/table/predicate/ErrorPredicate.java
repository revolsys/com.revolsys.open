package com.revolsys.swing.map.layer.record.table.predicate;

import java.awt.Color;
import java.awt.Component;

import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;

import com.revolsys.awt.WebColors;
import com.revolsys.record.Record;
import com.revolsys.swing.table.record.RecordRowTable;
import com.revolsys.swing.table.record.model.RecordRowTableModel;

public class ErrorPredicate implements HighlightPredicate {

  public static void add(final RecordRowTable table) {
    final RecordRowTableModel model = table.getTableModel();
    final Highlighter highlighter = getHighlighter(model);
    table.addHighlighter(highlighter);
  }

  public static Highlighter getHighlighter(final RecordRowTableModel model) {
    final ErrorPredicate predicate = new ErrorPredicate(model);
    return new ColorHighlighter(predicate, WebColors.setAlpha(Color.RED, 64), Color.RED, Color.RED,
      Color.YELLOW);
  }

  private final RecordRowTableModel model;

  public ErrorPredicate(final RecordRowTableModel model) {
    this.model = model;
  }

  @Override
  public boolean isHighlighted(final Component renderer, final ComponentAdapter adapter) {
    try {
      final int rowIndex = adapter.convertRowIndexToModel(adapter.row);
      final Record record = this.model.getRecord(rowIndex);
      if (record != null) {
        final int columnIndex = adapter.convertColumnIndexToModel(adapter.column);
        final String fieldName = this.model.getFieldName(columnIndex);
        if (!record.isValid(fieldName)) {
          return true;
        }
      }
    } catch (final IndexOutOfBoundsException e) {
    }
    return false;
  }
}
