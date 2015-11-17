package com.revolsys.swing.listener;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public interface BaseMouseListener extends MouseListener {
  @Override
  default void mouseClicked(final MouseEvent e) {
  }

  @Override
  default void mouseEntered(final MouseEvent e) {
  }

  @Override
  default void mouseExited(final MouseEvent e) {
  }

  @Override
  default void mousePressed(final MouseEvent e) {
  }

  @Override
  default void mouseReleased(final MouseEvent e) {
  }
}
