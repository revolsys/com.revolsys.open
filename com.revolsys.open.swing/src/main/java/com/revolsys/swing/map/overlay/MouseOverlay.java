package com.revolsys.swing.map.overlay;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;

@SuppressWarnings("serial")
public class MouseOverlay extends JComponent implements MouseListener,
  MouseMotionListener, MouseWheelListener, KeyListener {

  public MouseOverlay(final JLayeredPane pane) {
    setFocusable(true);
    pane.add(this, new Integer(Integer.MAX_VALUE));
    addMouseListener(this);
    addMouseMotionListener(this);
    addMouseWheelListener(this);
    addKeyListener(this);
  }

  private List<Component> getOverlays() {
    final List<Component> overlays = new ArrayList<Component>();
    final Container parent = getParent();
    if (parent instanceof JLayeredPane) {
      final JLayeredPane layeredPane = (JLayeredPane)parent;
      for (final Component component : layeredPane.getComponents()) {
        if (component.isEnabled() && !(component instanceof MouseOverlay)) {
          overlays.add(component);
        }
      }
    }
    return overlays;
  }

  @Override
  public void keyPressed(final KeyEvent e) {
    for (final Component overlay : getOverlays()) {
      if (overlay instanceof KeyListener) {
        final KeyListener listener = (KeyListener)overlay;
        listener.keyPressed(e);
        if (e.isConsumed()) {
          return;
        }
      }
    }
  }

  @Override
  public void keyReleased(final KeyEvent e) {
    for (final Component overlay : getOverlays()) {
      if (overlay instanceof KeyListener) {
        final KeyListener listener = (KeyListener)overlay;
        listener.keyReleased(e);
        if (e.isConsumed()) {
          return;
        }
      }
    }
  }

  @Override
  public void keyTyped(final KeyEvent e) {
    for (final Component overlay : getOverlays()) {
      if (overlay instanceof KeyListener) {
        final KeyListener listener = (KeyListener)overlay;
        listener.keyTyped(e);
        if (e.isConsumed()) {
          return;
        }
      }
    }
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
    requestFocusInWindow(true);
    for (final Component overlay : getOverlays()) {
      if (overlay instanceof MouseListener) {
        final MouseListener listener = (MouseListener)overlay;
        listener.mouseClicked(e);
        if (e.isConsumed()) {
          return;
        }
      }
    }
  }

  @Override
  public void mouseDragged(final MouseEvent e) {
    requestFocusInWindow(true);
    for (final Component overlay : getOverlays()) {
      if (overlay instanceof MouseMotionListener) {
        final MouseMotionListener listener = (MouseMotionListener)overlay;
        listener.mouseDragged(e);
        if (e.isConsumed()) {
          return;
        }
      }
    }
  }

  @Override
  public void mouseEntered(final MouseEvent e) {
    for (final Component overlay : getOverlays()) {
      if (overlay instanceof MouseListener) {
        final MouseListener listener = (MouseListener)overlay;
        listener.mouseEntered(e);
        if (e.isConsumed()) {
          return;
        }
      }
    }
  }

  @Override
  public void mouseExited(final MouseEvent e) {
    for (final Component overlay : getOverlays()) {
      if (overlay instanceof MouseListener) {
        final MouseListener listener = (MouseListener)overlay;
        listener.mouseExited(e);
        if (e.isConsumed()) {
          return;
        }
      }
    }
  }

  @Override
  public void mouseMoved(final MouseEvent e) {
    for (final Component overlay : getOverlays()) {
      if (overlay instanceof MouseMotionListener) {
        final MouseMotionListener listener = (MouseMotionListener)overlay;
        listener.mouseMoved(e);
        if (e.isConsumed()) {
          return;
        }
      }
    }
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    requestFocusInWindow(true);
    for (final Component overlay : getOverlays()) {
      if (overlay instanceof MouseListener) {
        final MouseListener listener = (MouseListener)overlay;
        listener.mousePressed(e);
        if (e.isConsumed()) {
          return;
        }
      }
    }
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    for (final Component overlay : getOverlays()) {
      if (overlay instanceof MouseListener) {
        final MouseListener listener = (MouseListener)overlay;
        listener.mouseReleased(e);
        if (e.isConsumed()) {
          return;
        }
      }
    }
  }

  @Override
  public void mouseWheelMoved(final MouseWheelEvent e) {
    for (final Component overlay : getOverlays()) {
      if (overlay instanceof MouseWheelListener) {
        final MouseWheelListener listener = (MouseWheelListener)overlay;
        listener.mouseWheelMoved(e);
        if (e.isConsumed()) {
          return;
        }
      }
    }
  }

}
