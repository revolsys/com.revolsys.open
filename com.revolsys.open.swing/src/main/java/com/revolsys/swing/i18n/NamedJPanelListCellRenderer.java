package com.revolsys.swing.i18n;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.SwingConstants;

public class NamedJPanelListCellRenderer extends DefaultListCellRenderer {

  /**
   *
   */
  private static final long serialVersionUID = -5909086452296276015L;

  @Override
  public Component getListCellRendererComponent(final JList list, final Object value,
    final int index, final boolean isSelected, final boolean cellHasFocus) {
    setVerticalAlignment(SwingConstants.BOTTOM);
    setVerticalTextPosition(SwingConstants.BOTTOM);
    setHorizontalAlignment(SwingConstants.CENTER);
    setHorizontalTextPosition(SwingConstants.CENTER);
    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    if (value instanceof NamedJPanel) {
      final NamedJPanel panel = (NamedJPanel)value;
      setText(panel.getName());
      setIcon(panel.getIcon());
    }

    return this;
  }

}
