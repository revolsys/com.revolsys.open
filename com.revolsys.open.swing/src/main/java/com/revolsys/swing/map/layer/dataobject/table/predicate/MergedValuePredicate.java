package com.revolsys.swing.map.layer.dataobject.table.predicate;

import java.awt.Component;

import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;

import com.revolsys.awt.WebColors;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.swing.map.layer.dataobject.table.model.MergedRecordsTableModel;
import com.revolsys.swing.table.dataobject.row.DataObjectRowTable;
import com.vividsolutions.jts.geom.Geometry;

public class MergedValuePredicate implements HighlightPredicate {

  public static void add(final DataObjectRowTable table) {
    final MergedRecordsTableModel model = table.getTableModel();
    final MergedValuePredicate predicate = new MergedValuePredicate(model);
    table.addHighlighter(new ColorHighlighter(predicate, WebColors.Salmon,
      WebColors.Black, WebColors.Red, WebColors.Yellow));
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
        final String attributeName = this.model.getFieldName(columnIndex);
        final Object value = object.getValue(attributeName);
        final Object mergedValue = mergedObject.getValue(attributeName);
        if (value instanceof Geometry) {
          return false;
        } else if (mergedValue instanceof Geometry) {
          return false;
        } else {
          return !EqualsRegistry.equal(value, mergedValue);
        }
      }
    } catch (final IndexOutOfBoundsException e) {
      return false;
    }
  }
}
