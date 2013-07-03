package com.revolsys.swing.map.layer.dataobject.component;

import java.awt.Component;

import org.jdesktop.swingx.color.ColorUtil;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;

import com.revolsys.awt.WebColors;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;
import com.revolsys.swing.table.dataobject.row.DataObjectRowTable;
import com.revolsys.swing.table.dataobject.row.DataObjectRowTableModel;

public class MergedObjectPredicate implements HighlightPredicate {

  public static void add(final DataObjectRowTable table,
    final DataObject mergedObject) {
    final DataObjectRowTableModel model = (DataObjectRowTableModel)table.getModel();
    final Highlighter highlighter = getHighlighter(model, mergedObject);
    table.addHighlighter(highlighter);
  }

  public static Highlighter getHighlighter(final DataObjectRowTableModel model,
    final DataObject mergedObject) {
    final MergedObjectPredicate predicate = new MergedObjectPredicate(model,
      mergedObject);
    return new ColorHighlighter(predicate, ColorUtil.setAlpha(WebColors.Green,
      64), WebColors.Black, WebColors.Green, WebColors.White);
  }

  private final DataObjectRowTableModel model;

  private final DataObject mergedObject;

  public MergedObjectPredicate(final DataObjectRowTableModel model,
    final DataObject mergedObject) {
    this.model = model;
    this.mergedObject = mergedObject;
  }

  @Override
  public boolean isHighlighted(final Component renderer,
    final ComponentAdapter adapter) {
    try {
      final int rowIndex = adapter.convertRowIndexToModel(adapter.row);
      final LayerDataObject object = model.getObject(rowIndex);
      if (object == mergedObject) {
        return true;
      } else {
        return false;
      }
    } catch (final IndexOutOfBoundsException e) {
      return false;
    }
  }
}
