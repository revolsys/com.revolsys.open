package com.revolsys.swing.listener;

import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

public class MouseListenersBase implements MouseListeners {
  private final List<MouseListener> mouseListeners = new ArrayList<>();

  @Override
  public List<MouseListener> getMouseListeners() {
    return this.mouseListeners;
  }
}
