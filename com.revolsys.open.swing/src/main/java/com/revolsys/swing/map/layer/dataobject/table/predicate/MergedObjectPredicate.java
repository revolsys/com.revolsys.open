package com.revolsys.swing.map.layer.dataobject.table.predicate;

import java.awt.Component;

import javax.swing.BorderFactory;

import org.jdesktop.swingx.color.ColorUtil;
import org.jdesktop.swingx.decorator.BorderHighlighter;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;

import com.revolsys.awt.WebColors;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.swing.map.layer.dataobject.table.model.MergedRecordsTableModel;
import com.revolsys.swing.table.dataobject.row.DataObjectRowTable;

public class MergedObjectPredicate implements HighlightPredicate {

  public static void add(final DataObjectRowTable table) {
    final MergedRecordsTableModel model = table.getTableModel();
    final MergedObjectPredicate predicate = new MergedObjectPredicate(model);
    final Highlighter colors = new ColorHighlighter(predicate,
      ColorUtil.setAlpha(WebColors.Green, 64), WebColors.Black,
      WebColors.Green, WebColors.White);
    table.addHighlighter(colors);
    table.addHighlighter(new BorderHighlighter(predicate,
      BorderFactory.createLineBorder(WebColors.Green)));
  }

  private final MergedRecordsTableModel model;

  public MergedObjectPredicate(final MergedRecordsTableModel model) {
    this.model = model;
  }

  @Override
  public boolean isHighlighted(final Component renderer,
    final ComponentAdapter adapter) {
    try {
      final int rowIndex = adapter.convertRowIndexToModel(adapter.row);
      final DataObject object = this.model.getRecord(rowIndex);
      if (object == model.getMergedObject()) {
        return true;
      } else {
        return false;
      }
    } catch (final IndexOutOfBoundsException e) {
      return false;
    }
  }
}
