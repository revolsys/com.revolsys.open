package com.revolsys.swing;

import java.awt.BorderLayout;
import java.awt.LayoutManager;

import javax.swing.JPanel;
import javax.swing.SpringLayout;

import org.jdesktop.swingx.VerticalLayout;

public interface Panels {
  static JPanel titledTransparent(final LayoutManager layout, final String title) {
    final JPanel panel = transparent(layout);
    Borders.titled(panel, title);
    return panel;
  }

  static JPanel titledTransparent(final String title) {
    final JPanel panel = new JPanel();
    panel.setOpaque(false);
    Borders.titled(panel, title);
    return panel;
  }

  static JPanel titledTransparentBorderLayout(final String title) {
    final LayoutManager layout = new BorderLayout();
    return titledTransparent(layout, title);
  }

  static JPanel titledTransparentVerticalLayout(final String title) {
    final LayoutManager layout = new VerticalLayout();
    return titledTransparent(layout, title);
  }

  static JPanel titledTransparentVerticalLayout(final String title, final int gap) {
    final LayoutManager layout = new VerticalLayout(gap);
    return titledTransparent(layout, title);
  }

  static JPanel transparent(final LayoutManager layout) {
    final JPanel panel = new JPanel(layout);
    panel.setOpaque(false);
    return panel;
  }

  static JPanel transparentSpringLayout() {
    final LayoutManager layout = new SpringLayout();
    return transparent(layout);
  }
}
