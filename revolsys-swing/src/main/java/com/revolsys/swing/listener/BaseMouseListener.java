package com.revolsys.swing.listener;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.function.Consumer;

public interface BaseMouseListener extends MouseListener {
  static MouseListener clicked(final Consumer<MouseEvent> consumer) {
    return new BaseMouseListener() {
      @Override
      public void mouseClicked(final MouseEvent e) {
        consumer.accept(e);
      }
    };
  }

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
