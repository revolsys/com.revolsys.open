package com.revolsys.swing.map.layer.record.table.predicate;

import java.awt.Component;

import javax.swing.BorderFactory;

import org.jdesktop.swingx.decorator.BorderHighlighter;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;

import com.revolsys.awt.WebColors;
import com.revolsys.record.Record;
import com.revolsys.swing.map.layer.record.table.model.MergedRecordsTableModel;
import com.revolsys.swing.table.record.row.RecordRowTable;

public class MergedRecordPredicate implements HighlightPredicate {

  public static void add(final RecordRowTable table) {
    final MergedRecordsTableModel model = table.getTableModel();
    final MergedRecordPredicate predicate = new MergedRecordPredicate(model);
    final Highlighter colors = new ColorHighlighter(predicate,
      WebColors.setAlpha(WebColors.Green, 64), WebColors.Black, WebColors.Green, WebColors.White);
    table.addHighlighter(colors);
    table.addHighlighter(
      new BorderHighlighter(predicate, BorderFactory.createLineBorder(WebColors.Green)));
  }

  private final MergedRecordsTableModel model;

  public MergedRecordPredicate(final MergedRecordsTableModel model) {
    this.model = model;
  }

  @Override
  public boolean isHighlighted(final Component renderer, final ComponentAdapter adapter) {
    try {
      final int rowIndex = adapter.convertRowIndexToModel(adapter.row);
      final Record object = this.model.getRecord(rowIndex);
      if (object == this.model.getMergedRecord()) {
        return true;
      } else {
        return false;
      }
    } catch (final IndexOutOfBoundsException e) {
      return false;
    }
  }
}
