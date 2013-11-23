package com.revolsys.swing.component;

import java.awt.LayoutManager;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.ScrollableSizeHint;
import org.jdesktop.swingx.VerticalLayout;

public class BasePanel extends JXPanel {

  public BasePanel() {
    this(true);
  }

  public BasePanel(final boolean isDoubleBuffered) {
    this(new VerticalLayout(), isDoubleBuffered);
  }

  public BasePanel(final LayoutManager layout) {
    this(layout, true);
  }

  public BasePanel(final LayoutManager layout, final boolean isDoubleBuffered) {
    super(layout, isDoubleBuffered);
    setScrollableWidthHint(ScrollableSizeHint.FIT);
    setScrollableHeightHint(ScrollableSizeHint.VERTICAL_STRETCH);
    setOpaque(false);
  }

}
