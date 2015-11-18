package com.revolsys.swing.map.layer.record.table.predicate;

import java.awt.Color;
import java.awt.Component;

import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;

import com.revolsys.awt.WebColors;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.table.model.RecordLayerTableModel;
import com.revolsys.swing.table.highlighter.ColorHighlighter;
import com.revolsys.swing.table.record.RecordRowTable;

public class ModifiedPredicate implements HighlightPredicate {
  public static void add(final RecordRowTable table) {
    final RecordLayerTableModel model = (RecordLayerTableModel)table.getModel();
    final ModifiedPredicate predicate = new ModifiedPredicate(model);

    table.addHighlighter(
      new ColorHighlighter(new AndHighlightPredicate(predicate, HighlightPredicate.EVEN),
        WebColors.setAlpha(WebColors.LimeGreen, 127), WebColors.Black,
        WebColors.setAlpha(WebColors.DarkGreen, 191), Color.WHITE));

    table.addHighlighter(
      new ColorHighlighter(new AndHighlightPredicate(predicate, HighlightPredicate.ODD),
        WebColors.LimeGreen, WebColors.Black, WebColors.DarkGreen, Color.WHITE));
  }

  private final RecordLayerTableModel model;

  public ModifiedPredicate(final RecordLayerTableModel model) {
    this.model = model;
  }

  @Override
  public boolean isHighlighted(final Component renderer, final ComponentAdapter adapter) {
    try {
      final int rowIndex = adapter.convertRowIndexToModel(adapter.row);
      final LayerRecord record = this.model.getRecord(rowIndex);
      final AbstractRecordLayer layer = this.model.getLayer();
      return layer.isModified(record);
    } catch (final Throwable e) {
      return false;
    }
  }
}
