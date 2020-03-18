package com.revolsys.swing;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.Border;

public interface Borders {
  static void titled(final JComponent component, final String title) {
    final Border border = BorderFactory.createTitledBorder(title);
    component.setBorder(border);
  }
}
