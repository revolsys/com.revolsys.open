package com.revolsys.swing.listener;

import java.awt.event.ActionListener;

public interface ActionListenable {
  void addActionListener(final ActionListener listener);

  void removeActionListener(final ActionListener listener);
}
