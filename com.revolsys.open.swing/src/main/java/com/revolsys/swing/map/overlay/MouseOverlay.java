package com.revolsys.swing.map.overlay;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
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
import javax.swing.SwingUtilities;

import org.slf4j.LoggerFactory;

import com.revolsys.swing.SwingUtil;

public class MouseOverlay extends JComponent implements MouseListener,
MouseMotionListener, MouseWheelListener, KeyListener, FocusListener {
  private static final long serialVersionUID = 1L;

  public MouseOverlay(final JLayeredPane pane) {
    setFocusable(true);
    pane.add(this, new Integer(Integer.MAX_VALUE));
    addMouseListener(this);
    addMouseMotionListener(this);
    addMouseWheelListener(this);
    addKeyListener(this);
    addFocusListener(this);
  }

  @Override
  public void focusGained(final FocusEvent e) {
    if (e.getComponent() == this
        && e.getOppositeComponent() == SwingUtilities.getWindowAncestor(this)) {
    } else {
      for (final Component overlay : getOverlays()) {
        if (overlay instanceof FocusListener) {
          final FocusListener listener = (FocusListener)overlay;
          listener.focusGained(e);
        }
      }
    }
  }

  @Override
  public void focusLost(final FocusEvent e) {
    if (e.getComponent() == this
        && e.getOppositeComponent() == SwingUtilities.getWindowAncestor(this)) {
    } else {
      for (final Component overlay : getOverlays()) {
        if (overlay instanceof FocusListener) {
          final FocusListener listener = (FocusListener)overlay;
          listener.focusLost(e);
        }
      }
    }
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
    requestFocusInWindow();
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
    requestFocusInWindow();
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
    requestFocusInWindow();
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
  public void mouseMoved(final MouseEvent event) {
    try {
      requestFocusInWindow();
      for (final Component overlay : getOverlays()) {
        if (overlay instanceof MouseMotionListener) {
          final MouseMotionListener listener = (MouseMotionListener)overlay;
          listener.mouseMoved(event);
          if (event.isConsumed()) {
            return;
          }
        }
      }
    } catch (final RuntimeException e) {
      LoggerFactory.getLogger(getClass()).error("Mouse move error", e);
    }
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    final Window window = SwingUtil.getWindowAncestor(this);
    window.setAlwaysOnTop(true);
    window.toFront();
    window.setFocusableWindowState(true);
    window.requestFocus();
    window.setAlwaysOnTop(false);

    requestFocusInWindow();
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
    requestFocusInWindow();
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
