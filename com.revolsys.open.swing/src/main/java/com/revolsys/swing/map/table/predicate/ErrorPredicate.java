package com.revolsys.swing.map.table.predicate;

import java.awt.Color;
import java.awt.Component;

import org.jdesktop.swingx.color.ColorUtil;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;
import com.revolsys.swing.map.table.DataObjectLayerTableModel;
import com.revolsys.swing.table.dataobject.row.DataObjectRowTable;

public class ErrorPredicate implements HighlightPredicate {

  public static void add(final DataObjectRowTable table) {
    final DataObjectLayerTableModel model = (DataObjectLayerTableModel)table.getModel();
    final Highlighter highlighter = getHighlighter(model);
    table.addHighlighter(highlighter);
  }

  public static Highlighter getHighlighter(final DataObjectLayerTableModel model) {
    final ErrorPredicate predicate = new ErrorPredicate(model);
    return new ColorHighlighter(predicate, ColorUtil.setAlpha(Color.RED, 64),
      Color.RED, Color.RED, Color.YELLOW);
  }

  private final DataObjectLayerTableModel model;

  public ErrorPredicate(final DataObjectLayerTableModel model) {
    this.model = model;
  }

  @Override
  public boolean isHighlighted(final Component renderer,
    final ComponentAdapter adapter) {
    final int rowIndex = adapter.convertRowIndexToModel(adapter.row);
    final DataObject object = model.getObject(rowIndex);
    if (object instanceof LayerDataObject) {
      final LayerDataObject layerDataObject = (LayerDataObject)object;
      final int columnIndex = adapter.convertRowIndexToModel(adapter.column);
      if (!layerDataObject.isValid(columnIndex)) {
        return true;
      }
    }
    return false;
  }
}
