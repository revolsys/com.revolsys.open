package com.revolsys.swing.table.highlighter;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.Border;

import org.jdesktop.swingx.decorator.AbstractHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;

public class OutsideBorderHighlighter extends AbstractHighlighter {

  private final Border bottomBorder;

  private final boolean compound;

  private final boolean inner;

  private final Border middleBorder;

  private final Border topBorder;

  public OutsideBorderHighlighter(final HighlightPredicate predicate, final Color color,
    final int thickness, final boolean compound, final boolean inner) {
    super(predicate);
    this.topBorder = BorderFactory.createMatteBorder(thickness, thickness, 0, thickness, color);
    this.middleBorder = BorderFactory.createMatteBorder(0, thickness, 0, thickness, color);
    this.bottomBorder = BorderFactory.createMatteBorder(0, thickness, thickness, thickness, color);
    this.compound = compound;
    this.inner = inner;
  }

  @Override
  protected boolean canHighlight(final Component component, final ComponentAdapter adapter) {
    return component instanceof JComponent;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Component doHighlight(final Component renderer, final ComponentAdapter adapter) {
    Border border;
    if (adapter.row == 0) {
      border = this.topBorder;
    } else if (adapter.row == adapter.getRowCount() - 1) {
      border = this.bottomBorder;
    } else {
      border = this.middleBorder;
    }
    final JComponent component = (JComponent)renderer;
    final Border componentBorder = component.getBorder();
    if (this.compound) {
      if (componentBorder != null) {
        if (this.inner) {
          border = BorderFactory.createCompoundBorder(componentBorder, border);
        }
        border = BorderFactory.createCompoundBorder(border, componentBorder);
      }
    }
    component.setBorder(border);
    return renderer;
  }

}
