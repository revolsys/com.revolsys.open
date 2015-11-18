package com.revolsys.swing.map.layer.record.table.predicate;

import java.awt.Component;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.Map;

import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.FontHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;

import com.revolsys.awt.WebColors;
import com.revolsys.swing.map.layer.record.table.RecordLayerTable;
import com.revolsys.swing.map.layer.record.table.model.RecordLayerTableModel;

public class DeletedPredicate implements HighlightPredicate {
  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  public static void add(final RecordLayerTable table) {
    final RecordLayerTableModel model = table.getModel();
    final DeletedPredicate predicate = new DeletedPredicate(model);

    table.addHighlighter(
      new ColorHighlighter(new AndHighlightPredicate(predicate, HighlightPredicate.EVEN),
        WebColors.setAlpha(WebColors.Pink, 127), WebColors.FireBrick, WebColors.LightCoral,
        WebColors.FireBrick));

    table.addHighlighter(
      new ColorHighlighter(new AndHighlightPredicate(predicate, HighlightPredicate.ODD),
        WebColors.Pink, WebColors.FireBrick, WebColors.Crimson, WebColors.White));

    final Font tableFont = table.getFont();
    final Map<TextAttribute, Object> fontAttributes = (Map)tableFont.getAttributes();
    fontAttributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
    final Font font = new Font(fontAttributes);
    final FontHighlighter fontHighlighter = new FontHighlighter(predicate, font);
    table.addHighlighter(fontHighlighter);
  }

  private final RecordLayerTableModel model;

  private DeletedPredicate(final RecordLayerTableModel model) {
    this.model = model;
  }

  @Override
  public boolean isHighlighted(final Component renderer, final ComponentAdapter adapter) {
    try {
      final int rowIndex = adapter.convertRowIndexToModel(adapter.row);
      return this.model.isDeleted(rowIndex);
    } catch (final Throwable e) {
    }
    return false;
  }
}
