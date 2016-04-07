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

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.Viewport2D;

public class MouseOverlay extends JComponent implements MouseListener, MouseMotionListener,
  MouseWheelListener, KeyListener, FocusListener {

  private static final long serialVersionUID = 1L;

  private static int x;

  private static int y;

  public static int getEventX() {
    return x;
  }

  public static int getEventY() {
    return y;
  }

  public static boolean isMouseInMap() {
    return x != -1;
  }

  private final Viewport2D viewport;

  private final MapPanel mapPanel;

  public MouseOverlay(final MapPanel mapPanel, final JLayeredPane layeredPane) {
    this.mapPanel = mapPanel;
    this.viewport = mapPanel.getViewport();
    setFocusable(true);
    layeredPane.add(this, new Integer(Integer.MAX_VALUE));
    addMouseListener(this);
    addMouseMotionListener(this);
    addMouseWheelListener(this);
    addKeyListener(this);
    addFocusListener(this);
  }

  @Override
  public final void focusGained(final FocusEvent e) {
  }

  @Override
  public void focusLost(final FocusEvent e) {
    if (!e.isTemporary()) {
      final Component component = e.getComponent();
      if (component != this) {
        final Component oppositeComponent = e.getOppositeComponent();
        if (oppositeComponent != SwingUtilities.getWindowAncestor(this)) {
          for (final Component overlay : getOverlays()) {
            if (overlay instanceof FocusListener) {
              final FocusListener listener = (FocusListener)overlay;
              listener.focusLost(e);
            }
          }
        }
      }
    }
  }

  public Point getEventPoint() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final Point point = this.viewport.toModelPointRounded(geometryFactory, x, y);
    return point;
  }

  public java.awt.Point getEventPosition() {
    return new java.awt.Point(x, y);
  }

  private GeometryFactory getGeometryFactory() {
    return this.viewport.getGeometryFactory();
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
    updateEventPoint(e);
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
    updateEventPoint(e);
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
    updateEventPoint(e);
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
  public void mouseExited(final MouseEvent event) {
    x = -1;
    y = -1;
    this.mapPanel.mouseExitedCloseSelected(event);
    for (final Component overlay : getOverlays()) {
      if (overlay instanceof MouseListener) {
        final MouseListener listener = (MouseListener)overlay;
        listener.mouseExited(event);
        if (event.isConsumed()) {
          return;
        }
      }
    }
  }

  @Override
  public void mouseMoved(final MouseEvent event) {
    updateEventPoint(event);
    try {
      requestFocusInWindow();
      this.mapPanel.mouseMovedCloseSelected(event);
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
    updateEventPoint(e);
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
    updateEventPoint(e);
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
    updateEventPoint(e);
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

  private void updateEventPoint(final MouseEvent e) {
    x = e.getX();
    y = e.getY();
  }

}
