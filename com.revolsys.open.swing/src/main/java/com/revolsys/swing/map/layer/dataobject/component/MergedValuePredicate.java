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
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;
import com.revolsys.swing.table.dataobject.row.DataObjectRowTable;
import com.revolsys.swing.table.dataobject.row.DataObjectRowTableModel;

public class MergedValuePredicate implements HighlightPredicate {

  public static void add(final DataObjectRowTable table,
    final DataObject mergedObject) {
    final DataObjectRowTableModel model = (DataObjectRowTableModel)table.getModel();
    final Highlighter highlighter = getHighlighter(model, mergedObject);
    table.addHighlighter(highlighter);
  }

  public static Highlighter getHighlighter(final DataObjectRowTableModel model,
    final DataObject mergedObject) {
    final MergedValuePredicate predicate = new MergedValuePredicate(model,
      mergedObject);
    return new BorderHighlighter(predicate, BorderFactory.createLineBorder(
      WebColors.Red, 2));
  }

  private final DataObjectRowTableModel model;

  private final DataObject mergedObject;

  public MergedValuePredicate(final DataObjectRowTableModel model,
    final DataObject mergedObject) {
    this.model = model;
    this.mergedObject = mergedObject;
  }

  @Override
  public boolean isHighlighted(final Component renderer,
    final ComponentAdapter adapter) {
    try {
      final int rowIndex = adapter.convertRowIndexToView(adapter.row);
      final int columnIndex = adapter.convertColumnIndexToView(adapter.column);
      final LayerDataObject object = model.getObject(rowIndex);
      if (object == mergedObject || mergedObject == null) {
        return false;
      } else {
        final String attributeName = model.getAttributeName(columnIndex);
        final Object value = object.getValue(attributeName);
        final Object mergedValue = mergedObject.getValue(attributeName);
        return !EqualsRegistry.equal(value, mergedValue);
      }
    } catch (final IndexOutOfBoundsException e) {
      return false;
    }
  }
}
