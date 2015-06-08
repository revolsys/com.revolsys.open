package com.revolsys.swing.list.renderer;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JList;

public class IconListCellRenderer extends DefaultListCellRenderer {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private Map<Object, Icon> icons = new HashMap<>();

  public IconListCellRenderer(final Map<Object, Icon> icons) {
    super();
    this.icons = icons;
  }

  @Override
  public Component getListCellRendererComponent(final JList<? extends Object> list,
    final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
    final Component component = super.getListCellRendererComponent(list, value, index, isSelected,
      cellHasFocus);
    final Icon icon = this.icons.get(value);
    setIcon(icon);
    return component;
  }

}
