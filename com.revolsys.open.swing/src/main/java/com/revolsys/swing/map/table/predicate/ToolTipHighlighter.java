package com.revolsys.swing.map.table.predicate;

import java.awt.Component;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.AbstractHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.Highlighter;

public class ToolTipHighlighter extends AbstractHighlighter {

  public static void add(final JXTable table) {
    final Highlighter highlighter = new ToolTipHighlighter();
    table.addHighlighter(highlighter);
  }

  private String text = "";

  public ToolTipHighlighter() {
  }

  public ToolTipHighlighter(final String text) {
    this.text = text;
  }

  @Override
  protected Component doHighlight(final Component component,
    final ComponentAdapter adapter) {
    adapter.getComponent().setToolTipText(text);
    return component;
  }
}
