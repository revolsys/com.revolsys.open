package com.revolsys.swing.table.highlighter;

import java.awt.Color;
import java.awt.Component;

import org.jdesktop.swingx.decorator.AbstractHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jeometry.common.awt.WebColors;

public class OddEvenColorHighlighter extends AbstractHighlighter {
  private final Color backgroundSelected;

  private final Color background;

  private final Color backgroundSelectedOdd;

  private final Color backgroundOdd;

  private Color foreground = WebColors.Black;

  private Color foregroundSelected = WebColors.White;

  public OddEvenColorHighlighter(final HighlightPredicate predicate, final Color background,
    final Color backgroundSelected) {
    super(predicate);
    this.background = background;
    this.backgroundOdd = new Color(Math.max((int)(background.getRed() * 0.9), 0),
      Math.max((int)(background.getGreen() * 0.9), 0),
      Math.max((int)(background.getBlue() * 0.9), 0));
    this.backgroundSelected = backgroundSelected;
    this.backgroundSelectedOdd = new Color(Math.max((int)(backgroundSelected.getRed() * 0.9), 0),
      Math.max((int)(backgroundSelected.getGreen() * 0.9), 0),
      Math.max((int)(backgroundSelected.getBlue() * 0.9), 0));
  }

  @Override
  protected Component doHighlight(final Component renderer, final ComponentAdapter adapter) {
    final boolean selected = adapter.isSelected();
    final boolean even = adapter.row % 2 == 0;
    if (selected) {
      if (even) {
        renderer.setBackground(this.backgroundSelected);
      } else {
        renderer.setBackground(this.backgroundSelectedOdd);
      }
      renderer.setForeground(this.foregroundSelected);
    } else {
      if (even) {
        renderer.setBackground(this.background);
      } else {
        renderer.setBackground(this.backgroundOdd);
      }
      renderer.setForeground(this.foreground);
    }
    return renderer;
  }

  public OddEvenColorHighlighter setForeground(final Color foreground) {
    this.foreground = foreground;
    return this;
  }

  public OddEvenColorHighlighter setForegroundSelected(final Color foregroundSelected) {
    this.foregroundSelected = foregroundSelected;
    return this;
  }

}
