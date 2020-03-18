package com.revolsys.swing.map.layer.record.table.predicate;

import java.awt.Color;
import java.awt.Component;

import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jeometry.common.awt.WebColors;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.record.Record;
import com.revolsys.swing.table.record.RecordRowTable;
import com.revolsys.swing.table.record.model.RecordRowTableModel;
import com.revolsys.util.Debug;

public class ErrorPredicate implements HighlightPredicate {

  public static void add(final RecordRowTable table) {
    final RecordRowTableModel model = table.getTableModel();
    // final Highlighter highlighter = getHighlighter(model);
    // table.addHighlighter(highlighter);
    table.addColorHighlighter((rowIndex, columnIndex) -> {
      try {
        final Record record = model.getRecord(rowIndex);
        if (record != null) {
          final Class<?> columnClass = model.getColumnClass(columnIndex);
          if (Geometry.class.isAssignableFrom(columnClass)) {
            return false;
          } else {
            final String fieldName = model.getColumnFieldName(columnIndex);
            if (!record.isValid(fieldName)) {
              return true;
            }
          }
        }
        return false;
      } catch (final Exception e) {
        return true;
      }
    }, WebColors.Pink, WebColors.Red);
  }

  public static Highlighter getHighlighter(final RecordRowTableModel model) {
    final ErrorPredicate predicate = new ErrorPredicate(model);

    return new ColorHighlighter(predicate, WebColors.newAlpha(Color.RED, 64), Color.RED, Color.RED,
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
        final Class<?> columnClass = this.model.getColumnClass(columnIndex);
        if (Geometry.class.isAssignableFrom(columnClass)) {
          return false;
        } else {
          final String fieldName = this.model.getColumnFieldName(columnIndex);
          if (!record.isValid(fieldName)) {
            return true;
          }
        }
      }
    } catch (final Exception e) {
      Debug.noOp();
    }
    return false;
  }
}
