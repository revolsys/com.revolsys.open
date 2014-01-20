package com.revolsys.swing.map.layer.dataobject.table.predicate;

import java.awt.Color;
import java.awt.Component;

import org.jdesktop.swingx.color.ColorUtil;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.swing.table.dataobject.model.DataObjectRowTableModel;
import com.revolsys.swing.table.dataobject.row.DataObjectRowTable;

public class ErrorPredicate implements HighlightPredicate {

  public static void add(final DataObjectRowTable table) {
    final DataObjectRowTableModel model = table.getTableModel();
    final Highlighter highlighter = getHighlighter(model);
    table.addHighlighter(highlighter);
  }

  public static Highlighter getHighlighter(final DataObjectRowTableModel model) {
    final ErrorPredicate predicate = new ErrorPredicate(model);
    return new ColorHighlighter(predicate, ColorUtil.setAlpha(Color.RED, 64),
      Color.RED, Color.RED, Color.YELLOW);
  }

  private final DataObjectRowTableModel model;

  public ErrorPredicate(final DataObjectRowTableModel model) {
    this.model = model;
  }

  @Override
  public boolean isHighlighted(final Component renderer,
    final ComponentAdapter adapter) {
    try {
      final int rowIndex = adapter.convertRowIndexToModel(adapter.row);
      final DataObject object = this.model.getRecord(rowIndex);
      if (object != null) {
        final int columnIndex = adapter.convertColumnIndexToModel(adapter.column);
        final String attributeName = model.getFieldName(columnIndex);
        if (!object.isValid(attributeName)) {
          return true;
        }
      }
    } catch (final IndexOutOfBoundsException e) {
    }
    return false;
  }
}
