package com.revolsys.swing.listener;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

public interface MouseListeners extends MouseListener {

  default void addMouseListener(final MouseListener listener) {
    if (listener != null) {
      final List<MouseListener> mouseListeners = getMouseListeners();
      if (!mouseListeners.contains(listener)) {
        mouseListeners.add(listener);
      }
    }
  }

  default void clearMouseListeners() {
    final List<MouseListener> mouseListeners = getMouseListeners();
    mouseListeners.clear();
  }

  List<MouseListener> getMouseListeners();

  @Override
  default void mouseClicked(final MouseEvent e) {
    for (final MouseListener listener : getMouseListeners()) {
      listener.mouseClicked(e);
    }
  }

  @Override
  default void mouseEntered(final MouseEvent e) {
    for (final MouseListener listener : getMouseListeners()) {
      listener.mouseEntered(e);
    }
  }

  @Override
  default void mouseExited(final MouseEvent e) {
    for (final MouseListener listener : getMouseListeners()) {
      listener.mouseExited(e);
    }
  }

  @Override
  default void mousePressed(final MouseEvent e) {
    for (final MouseListener listener : getMouseListeners()) {
      listener.mousePressed(e);
    }
  }

  @Override
  default void mouseReleased(final MouseEvent e) {
    for (final MouseListener listener : getMouseListeners()) {
      listener.mouseReleased(e);
    }
  }

  default void removeMouseListener(final MouseListener listener) {
    if (listener != null) {
      final List<MouseListener> mouseListeners = getMouseListeners();
      mouseListeners.add(listener);
    }
  }
}
