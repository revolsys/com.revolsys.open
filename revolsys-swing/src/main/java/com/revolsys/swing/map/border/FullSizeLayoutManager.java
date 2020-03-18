package com.revolsys.swing.map.border;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

public class FullSizeLayoutManager implements LayoutManager {
  @Override
  public void addLayoutComponent(final String name, final Component comp) {
  }

  @Override
  public void layoutContainer(final Container parent) {
    final Insets insets = parent.getInsets();
    final int x = insets.top;
    final int y = insets.left;
    final int width = parent.getWidth() - insets.right - x;
    final int height = parent.getHeight() - insets.bottom - y;
    for (final Component component : parent.getComponents()) {
      component.setBounds(x, y, width, height);
    }
  }

  @Override
  public Dimension minimumLayoutSize(final Container parent) {
    int maxWidth = 0;
    int maxHeight = 0;
    for (final Component component : parent.getComponents()) {
      final Dimension minimum = component.getMinimumSize();
      final int width = minimum.width;
      if (width > maxWidth) {
        maxWidth = width;
      }
      final int height = minimum.height;
      if (height > maxHeight) {
        maxHeight = height;
      }
    }

    return new Dimension(maxWidth, maxHeight);
  }

  @Override
  public Dimension preferredLayoutSize(final Container parent) {
    int maxWidth = 0;
    int maxHeight = 0;
    for (final Component component : parent.getComponents()) {
      final Dimension minimum = component.getPreferredSize();
      final int width = minimum.width;
      if (width > maxWidth) {
        maxWidth = width;
      }
      final int height = minimum.height;
      if (height > maxHeight) {
        maxHeight = height;
      }
    }

    return new Dimension(maxWidth, maxHeight);
  }

  @Override
  public void removeLayoutComponent(final Component comp) {
  }
}
