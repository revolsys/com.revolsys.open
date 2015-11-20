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

public class NewPredicate implements HighlightPredicate {
  public static void add(final RecordRowTable table) {
    final RecordLayerTableModel model = (RecordLayerTableModel)table.getModel();
    final NewPredicate predicate = new NewPredicate(model);

    table.addHighlighter(
      new ColorHighlighter(new AndHighlightPredicate(predicate, HighlightPredicate.EVEN),
        WebColors.setAlpha(WebColors.LightSkyBlue, 127), WebColors.Black,
        WebColors.setAlpha(WebColors.RoyalBlue, 191), Color.WHITE));

    table.addHighlighter(
      new ColorHighlighter(new AndHighlightPredicate(predicate, HighlightPredicate.ODD),
        WebColors.LightSkyBlue, WebColors.Black, WebColors.RoyalBlue, Color.WHITE));
  }

  private final RecordLayerTableModel model;

  public NewPredicate(final RecordLayerTableModel model) {
    this.model = model;
  }

  @Override
  public boolean isHighlighted(final Component renderer, final ComponentAdapter adapter) {
    try {
      final int rowIndex = adapter.convertRowIndexToModel(adapter.row);
      final LayerRecord record = this.model.getRecord(rowIndex);
      final AbstractRecordLayer layer = this.model.getLayer();
      return layer.isNew(record);
    } catch (final Throwable e) {
      return false;
    }
  }
}
