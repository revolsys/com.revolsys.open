package com.revolsys.swing.map.border;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

public class FullSizeLayoutManager implements LayoutManager {
  @Override
  public void removeLayoutComponent(Component comp) {
  }

  @Override
  public Dimension preferredLayoutSize(Container parent) {
    int maxWidth = 0;
    int maxHeight = 0;
    for (Component component : parent.getComponents()) {
      Dimension minimum = component.getPreferredSize();
      int width = minimum.width;
      if (width > maxWidth) {
        maxWidth = width;
      }
      int height = minimum.height;
      if (height > maxHeight) {
        maxHeight = height;
      }
    }

    return new Dimension(maxWidth, maxHeight);
  }

  @Override
  public Dimension minimumLayoutSize(Container parent) {
    int maxWidth = 0;
    int maxHeight = 0;
    for (Component component : parent.getComponents()) {
      Dimension minimum = component.getMinimumSize();
      int width = minimum.width;
      if (width > maxWidth) {
        maxWidth = width;
      }
      int height = minimum.height;
      if (height > maxHeight) {
        maxHeight = height;
      }
    }

    return new Dimension(maxWidth, maxHeight);
  }

  @Override
  public void layoutContainer(Container parent) {
    Insets insets = parent.getInsets();
    int x = insets.top;
    int y = insets.left;
    int width = parent.getWidth() - insets.right - x;
    int height = parent.getHeight() - insets.bottom - y;
    for (Component component : parent.getComponents()) {
      component.setBounds(x, y, width, height);
    }
  }

  @Override
  public void addLayoutComponent(String name, Component comp) {
  }
}
