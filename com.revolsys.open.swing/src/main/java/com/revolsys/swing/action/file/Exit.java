package com.revolsys.swing.action.file;

import java.awt.Window;
import java.awt.event.ActionEvent;

import com.revolsys.swing.action.I18nAction;

public class Exit extends I18nAction {
  private static final long serialVersionUID = 3170988831053372882L;

  @Override
  public void actionPerformed(final ActionEvent e) {
    final Window[] windows = Window.getOwnerlessWindows();
    for (final Window window : windows) {
      window.dispose();
    }
    System.exit(0);
  }
}
