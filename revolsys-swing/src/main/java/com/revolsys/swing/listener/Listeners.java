package com.revolsys.swing.listener;

import java.awt.Component;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;

public class Listeners {
  public static void addKey(final Object object, final KeyListener listener) {
    if (object instanceof Component) {
      final Component component = (Component)object;
      component.addKeyListener(listener);
    }
  }

  public static void addMouse(final Object object, final MouseListener listener) {
    if (object instanceof Component) {
      final Component component = (Component)object;
      component.addMouseListener(listener);
    }
  }

  public static void addMouseMotion(final Object object, final MouseMotionListener listener) {
    if (object instanceof Component) {
      final Component component = (Component)object;
      component.addMouseMotionListener(listener);
    }
  }

  public static void addMouseWheel(final Object object, final MouseWheelListener listener) {
    if (object instanceof Component) {
      final Component component = (Component)object;
      component.addMouseWheelListener(listener);
    }
  }

  public static void mouseEvent(final MouseListener listener, final MouseEvent e) {
    if (listener != null) {
      final int id = e.getID();
      switch (id) {
        case MouseEvent.MOUSE_PRESSED:
          listener.mousePressed(e);
        break;
        case MouseEvent.MOUSE_RELEASED:
          listener.mouseReleased(e);
        break;
        case MouseEvent.MOUSE_CLICKED:
          listener.mouseClicked(e);
        break;
        case MouseEvent.MOUSE_EXITED:
          listener.mouseExited(e);
        break;
        case MouseEvent.MOUSE_ENTERED:
          listener.mouseEntered(e);
        break;
      }
    }
  }

  public static void remove(final Object object, final KeyListener listener) {
    if (object instanceof Component) {
      final Component component = (Component)object;
      component.removeKeyListener(listener);
    }
  }

  public static void removeMouse(final Object object, final MouseListener listener) {
    if (object instanceof Component) {
      final Component component = (Component)object;
      component.removeMouseListener(listener);
    }
  }

  public static void removeMouseMotion(final Object object, final MouseMotionListener listener) {
    if (object instanceof Component) {
      final Component component = (Component)object;
      component.removeMouseMotionListener(listener);
    }
  }

  public static void removeMouseWheel(final Object object, final MouseWheelListener listener) {
    if (object instanceof Component) {
      final Component component = (Component)object;
      component.removeMouseWheelListener(listener);
    }
  }

}
