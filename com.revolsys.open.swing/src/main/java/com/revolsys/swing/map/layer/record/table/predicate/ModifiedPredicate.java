package com.revolsys.swing.map.layer.record.table.predicate;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

import org.jdesktop.swingx.decorator.BorderHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;

import com.revolsys.awt.WebColors;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.table.model.RecordLayerTableModel;
import com.revolsys.swing.table.record.row.RecordRowTable;

public class ModifiedPredicate implements HighlightPredicate {

  private static final Border BORDER = BorderFactory.createLineBorder(WebColors.Green, 2);

  public static void add(final RecordRowTable table) {
    final RecordLayerTableModel model = (RecordLayerTableModel)table.getModel();
    final Highlighter highlighter = getHighlighter(model);
    table.addHighlighter(highlighter);
  }

  public static Highlighter getHighlighter(final RecordLayerTableModel model) {
    final ModifiedPredicate predicate = new ModifiedPredicate(model);
    return new BorderHighlighter(predicate, BORDER);
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
    } catch (final IndexOutOfBoundsException e) {
      return false;
    }

  }
}
