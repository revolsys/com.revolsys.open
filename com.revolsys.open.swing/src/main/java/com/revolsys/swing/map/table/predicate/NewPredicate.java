package com.revolsys.swing.map.table.predicate;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

import org.jdesktop.swingx.decorator.BorderHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;

import com.revolsys.awt.WebColors;
import com.revolsys.gis.data.model.DataObjectState;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;
import com.revolsys.swing.map.table.DataObjectLayerTableModel;
import com.revolsys.swing.table.dataobject.row.DataObjectRowTable;

public class NewPredicate implements HighlightPredicate {

  private static final Border BORDER = BorderFactory.createLineBorder(
    WebColors.Blue, 2);

  public static void add(final DataObjectRowTable table) {
    final DataObjectLayerTableModel model = (DataObjectLayerTableModel)table.getModel();
    final Highlighter highlighter = getHighlighter(model);
    table.addHighlighter(highlighter);
  }

  public static Highlighter getHighlighter(final DataObjectLayerTableModel model) {
    final NewPredicate predicate = new NewPredicate(model);
    return new BorderHighlighter(predicate, BORDER);
  }

  private final DataObjectLayerTableModel model;

  public NewPredicate(final DataObjectLayerTableModel model) {
    this.model = model;
  }

  @Override
  public boolean isHighlighted(final Component renderer,
    final ComponentAdapter adapter) {
    try {
      final int rowIndex = adapter.convertRowIndexToModel(adapter.row);
      final LayerDataObject object = model.getObject(rowIndex);
      if (object != null) {
        DataObjectState state = object.getState();
        return state.equals(DataObjectState.New);
      }
    } catch (final Throwable e) {
    }
    return false;
  }
}
