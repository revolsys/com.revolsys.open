package com.revolsys.swing.map.table;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

import org.jdesktop.swingx.decorator.BorderHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.swing.WebColors;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.table.dataobject.row.DataObjectRowTable;

public class ModifiedPredicate implements HighlightPredicate {

  private static final Border BORDER = BorderFactory.createLineBorder(
    WebColors.Green, 2);

  public static void add(final DataObjectRowTable table) {
    final DataObjectLayerTableModel model = (DataObjectLayerTableModel)table.getModel();
    final Highlighter highlighter = getHighlighter(model);
    table.addHighlighter(highlighter);
  }

  public static Highlighter getHighlighter(final DataObjectLayerTableModel model) {
    final ModifiedPredicate predicate = new ModifiedPredicate(model);
    return new BorderHighlighter(predicate, BORDER);
  }

  private final DataObjectLayerTableModel model;

  public ModifiedPredicate(final DataObjectLayerTableModel model) {
    this.model = model;
  }

  @Override
  public boolean isHighlighted(final Component renderer,
    final ComponentAdapter adapter) {
    final DataObject object = model.getObject(adapter.row);
    final DataObjectLayer layer = model.getLayer();
    return layer.isModified(object);
  }
}
