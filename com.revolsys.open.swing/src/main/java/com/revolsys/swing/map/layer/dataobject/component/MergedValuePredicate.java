package com.revolsys.swing.map.layer.dataobject.component;

import java.awt.Component;

import javax.swing.BorderFactory;

import org.jdesktop.swingx.decorator.BorderHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;

import com.revolsys.awt.WebColors;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.model.data.equals.EqualsRegistry;

public class MergedValuePredicate implements HighlightPredicate {

  public static void add(final MergedRecordsTable table) {
    final MergedRecordsTableModel model = (MergedRecordsTableModel)table.getModel();
    final Highlighter highlighter = getHighlighter(model);
    table.addHighlighter(highlighter);
  }

  public static Highlighter getHighlighter(final MergedRecordsTableModel model) {
    final MergedValuePredicate predicate = new MergedValuePredicate(model);
    return new BorderHighlighter(predicate, BorderFactory.createLineBorder(
      WebColors.Red, 2));
  }

  private final MergedRecordsTableModel model;

  public MergedValuePredicate(final MergedRecordsTableModel model) {
    this.model = model;
  }

  @Override
  public boolean isHighlighted(final Component renderer,
    final ComponentAdapter adapter) {
    try {
      final int rowIndex = adapter.convertRowIndexToView(adapter.row);
      final int columnIndex = adapter.convertColumnIndexToView(adapter.column);
      final DataObject object = model.getObject(rowIndex);
      final DataObject mergedObject = model.getMergedObject();

      if (object == mergedObject) {
        return false;
      } else {
        final String attributeName = this.model.getAttributeName(columnIndex);
        final Object value = object.getValue(attributeName);
        final Object mergedValue = mergedObject.getValue(attributeName);
        return !EqualsRegistry.equal(value, mergedValue);
      }
    } catch (final IndexOutOfBoundsException e) {
      return false;
    }
  }
}
