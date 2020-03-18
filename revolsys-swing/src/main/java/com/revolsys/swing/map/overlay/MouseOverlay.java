package com.revolsys.swing.map.overlay;

import java.awt.Component;
import java.awt.Container;
import java.awt.KeyboardFocusManager;
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
import java.util.function.Consumer;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;

import org.jeometry.common.logging.Logs;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.Viewport2D;

public class MouseOverlay extends JComponent
  implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener, FocusListener {

  private static final long serialVersionUID = 1L;

  private static int x;

  private static int y;

  private static Point point = GeometryFactory.DEFAULT_3D.point();

  public static Point getEventPoint() {
    return point;
  }

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
    layeredPane.add(this, Integer.valueOf(Integer.MAX_VALUE));
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
          forEachOverlay(overlay -> {
            if (overlay instanceof FocusListener) {
              final FocusListener listener = (FocusListener)overlay;
              listener.focusLost(e);
            }
          });
        }
      }
    }
  }

  private void forEachOverlay(final Consumer<Component> action) {
    final Container parent = getParent();
    if (parent instanceof JLayeredPane) {
      final JLayeredPane layeredPane = (JLayeredPane)parent;
      final int componentCount = layeredPane.getComponentCount();
      for (int i = 0; i < componentCount; i++) {
        final Component component = layeredPane.getComponent(i);
        if (component.isEnabled() && !(component instanceof MouseOverlay)) {
          if (this.mapPanel.isMenuVisible()) {
            return;
          } else {
            action.accept(component);
          }
        }
      }
    }
  }

  public Point getEventPointRounded() {
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

  @Override
  public void keyPressed(final KeyEvent e) {
    if (!this.mapPanel.isMenuVisible()) {
      forEachOverlay(overlay -> {
        if (!e.isConsumed() && overlay instanceof KeyListener) {
          final KeyListener listener = (KeyListener)overlay;
          listener.keyPressed(e);
        }
      });
    }
  }

  @Override
  public void keyReleased(final KeyEvent e) {
    if (!this.mapPanel.isMenuVisible()) {
      forEachOverlay(overlay -> {
        if (!e.isConsumed() && overlay instanceof KeyListener) {
          final KeyListener listener = (KeyListener)overlay;
          listener.keyReleased(e);
        }
      });
    }
  }

  @Override
  public void keyTyped(final KeyEvent e) {
    if (!this.mapPanel.isMenuVisible()) {
      forEachOverlay(overlay -> {
        if (!e.isConsumed() && overlay instanceof KeyListener) {
          final KeyListener listener = (KeyListener)overlay;
          listener.keyTyped(e);
        }
      });
    }
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
    if (!this.mapPanel.isMenuVisible()) {
      updateEventPoint(e);
    }
    requestFocusInWindow();
    forEachOverlay(overlay -> {
      if (!e.isConsumed() && overlay instanceof MouseListener) {
        final MouseListener listener = (MouseListener)overlay;
        listener.mouseClicked(e);
      }
    });
  }

  @Override
  public void mouseDragged(final MouseEvent e) {
    if (!this.mapPanel.isMenuVisible()) {
      updateEventPoint(e);
      requestFocusInWindow();
      forEachOverlay(overlay -> {
        if (!e.isConsumed() && overlay instanceof MouseMotionListener) {
          final MouseMotionListener listener = (MouseMotionListener)overlay;
          listener.mouseDragged(e);
        }
      });
    }
  }

  @Override
  public void mouseEntered(final MouseEvent e) {
    updateEventPoint(e);
    requestFocusIfNotWindow();
    forEachOverlay(overlay -> {
      if (!e.isConsumed() && overlay instanceof MouseListener) {
        final MouseListener listener = (MouseListener)overlay;
        listener.mouseEntered(e);
      }
    });
  }

  @Override
  public void mouseExited(final MouseEvent event) {
    MouseOverlay.x = -1;
    MouseOverlay.y = -1;
    MouseOverlay.point = GeometryFactory.DEFAULT_3D.point();
    this.mapPanel.mouseExitedCloseSelected(event);
    forEachOverlay(overlay -> {
      if (overlay instanceof MouseListener) {
        final MouseListener listener = (MouseListener)overlay;
        listener.mouseExited(event);
      }
    });
  }

  @Override
  public void mouseMoved(final MouseEvent event) {
    if (!this.mapPanel.isMenuVisible()) {
      try {
        requestFocusIfNotWindow();
        updateEventPoint(event);
        this.mapPanel.mouseMovedCloseSelected(event);
        forEachOverlay(overlay -> {
          if (!event.isConsumed() && overlay instanceof MouseMotionListener) {
            final MouseMotionListener listener = (MouseMotionListener)overlay;
            listener.mouseMoved(event);
          }
        });
      } catch (final RuntimeException e) {
        Logs.error(this, "Mouse move error", e);
      }
    }
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    if (!this.mapPanel.isMenuVisible()) {
      requestFocusInWindow();

      updateEventPoint(e);
      final Window window = SwingUtil.getWindowAncestor(this);
      SwingUtil.toFront(window);
      window.setFocusableWindowState(true);
      window.requestFocus();

      forEachOverlay(overlay -> {
        if (!e.isConsumed() && overlay instanceof MouseListener) {
          final MouseListener listener = (MouseListener)overlay;
          listener.mousePressed(e);
        }
      });
    }
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    if (!this.mapPanel.isMenuVisible()) {
      requestFocusInWindow();
      updateEventPoint(e);
      forEachOverlay(overlay -> {
        if (!e.isConsumed() && overlay instanceof MouseListener) {
          final MouseListener listener = (MouseListener)overlay;
          listener.mouseReleased(e);
        }
      });
    }
  }

  @Override
  public void mouseWheelMoved(final MouseWheelEvent e) {
    if (!this.mapPanel.isMenuVisible()) {
      updateEventPoint(e);
      forEachOverlay(overlay -> {
        if (!e.isConsumed() && overlay instanceof MouseWheelListener) {
          final MouseWheelListener listener = (MouseWheelListener)overlay;
          listener.mouseWheelMoved(e);
        }
      });
    }
  }

  public boolean requestFocusIfNotWindow() {
    final Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager()
      .getFocusOwner();
    if (SwingUtil.getWindowAncestor(focusOwner) != SwingUtil.getWindowAncestor(this)) {
      return super.requestFocusInWindow();
    }
    return true;
  }

  private void updateEventPoint(final MouseEvent e) {
    MouseOverlay.x = e.getX();
    MouseOverlay.y = e.getY();
    MouseOverlay.point = this.viewport.toModelPoint(x, y);
  }
}
