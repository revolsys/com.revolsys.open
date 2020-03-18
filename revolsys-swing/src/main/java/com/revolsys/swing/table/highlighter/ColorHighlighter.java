package com.revolsys.swing.table.highlighter;

import java.awt.Color;
import java.awt.Component;

import org.jdesktop.swingx.decorator.AbstractHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;

public class ColorHighlighter extends AbstractHighlighter {
  private final Color background;

  private final Color foreground;

  private final Color selectedBackground;

  private final Color selectedForeground;

  public ColorHighlighter() {
    this(null);
  }

  public ColorHighlighter(final Color cellBackground, final Color cellForeground) {
    this(null, cellBackground, cellForeground);
  }

  public ColorHighlighter(final Color cellBackground, final Color cellForeground,
    final Color selectedBackground, final Color selectedForeground) {
    this(null, cellBackground, cellForeground, selectedBackground, selectedForeground);
  }

  public ColorHighlighter(final HighlightPredicate predicate) {
    this(predicate, null, null);
  }

  public ColorHighlighter(final HighlightPredicate predicate, final Color cellBackground,
    final Color cellForeground) {
    this(predicate, cellBackground, cellForeground, null, null);
  }

  public ColorHighlighter(final HighlightPredicate predicate, final Color cellBackground,
    final Color cellForeground, final Color selectedBackground, final Color selectedForeground) {
    super(predicate);
    this.background = cellBackground;
    this.foreground = cellForeground;
    this.selectedBackground = selectedBackground;
    this.selectedForeground = selectedForeground;
  }

  @Override
  protected Component doHighlight(final Component renderer, final ComponentAdapter adapter) {
    final boolean selected = adapter.isSelected();
    if (selected) {
      renderer.setBackground(this.selectedBackground);
      renderer.setForeground(this.selectedForeground);
    } else {
      renderer.setBackground(this.background);
      renderer.setForeground(this.foreground);
    }
    return renderer;
  }

}
