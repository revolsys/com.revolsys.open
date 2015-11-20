package com.revolsys.swing.listener;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public interface BaseMouseMotionListener extends MouseMotionListener {
  @Override
  default void mouseDragged(final MouseEvent e) {
  }

  @Override
  default void mouseMoved(final MouseEvent e) {
  }
}
