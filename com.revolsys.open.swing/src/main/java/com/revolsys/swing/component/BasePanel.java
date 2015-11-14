package com.revolsys.swing.component;

import java.awt.Component;
import java.awt.LayoutManager;

import javax.swing.BorderFactory;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.ScrollableSizeHint;
import org.jdesktop.swingx.VerticalLayout;

import com.revolsys.swing.SwingUtil;

public class BasePanel extends JXPanel {
  private static final long serialVersionUID = 1L;

  public static BasePanel newPanelTitled(final String title) {
    final BasePanel panel = new BasePanel();
    final javax.swing.border.TitledBorder border = BorderFactory.createTitledBorder(title);
    panel.setBorder(border);
    return panel;
  }

  public static BasePanel newPanelTitled(final String title, final Component... components) {
    final BasePanel panel = new BasePanel(components);
    final javax.swing.border.TitledBorder border = BorderFactory.createTitledBorder(title);
    panel.setBorder(border);
    return panel;
  }

  public static BasePanel newPanelTitled(final String title, final LayoutManager layoutManager,
    final Component... components) {
    final BasePanel panel = new BasePanel(layoutManager, components);
    final javax.swing.border.TitledBorder border = BorderFactory.createTitledBorder(title);
    panel.setBorder(border);
    return panel;
  }

  public BasePanel() {
    this(true);
  }

  public BasePanel(final boolean isDoubleBuffered) {
    this(new VerticalLayout(), isDoubleBuffered);
  }

  public BasePanel(final Component... components) {
    this();
    for (final Component component : components) {
      add(component);
    }
  }

  public BasePanel(final LayoutManager layout) {
    this(layout, true);
  }

  public BasePanel(final LayoutManager layout, final boolean isDoubleBuffered) {
    super(layout, isDoubleBuffered);
    setScrollableWidthHint(ScrollableSizeHint.FIT);
    setScrollableHeightHint(ScrollableSizeHint.PREFERRED_STRETCH);
    setOpaque(false);
  }

  public BasePanel(final LayoutManager layout, final Component... components) {
    this(layout);
    for (final Component component : components) {
      add(component);
    }
  }

  public BasePanel addComponents(final Component... components) {
    for (final Component component : components) {
      add(component);
    }
    return this;
  }

  public BasePanel addComponents(final LayoutManager layout, final Component... components) {
    setLayout(layout);
    return addComponents(components);
  }

  public void addWithLabel(final String label, final Component component) {
    if (component != null) {
      SwingUtil.addLabel(this, label);
      add(component);
    }
  }
}
