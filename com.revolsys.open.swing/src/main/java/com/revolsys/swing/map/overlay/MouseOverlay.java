package com.revolsys.swing.map.overlay;

import java.awt.Component;
import java.awt.Container;
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
  MouseMotionListener, MouseWheelListener {

  public MouseOverlay(JLayeredPane pane) {
    pane.add(this, new Integer(Integer.MAX_VALUE));
    addMouseListener(this);
    addMouseMotionListener(this);
    addMouseWheelListener(this);
  }

  private List<Component> getOverlays() {
    List<Component> overlays = new ArrayList<Component>();
    Container parent = getParent();
    if (parent instanceof JLayeredPane) {
      JLayeredPane layeredPane = (JLayeredPane)parent;
      for (Component component : layeredPane.getComponents()) {
        if (component.isEnabled() && !(component instanceof MouseOverlay)) {
          overlays.add(component);
        }
      }
    }
    return overlays;
  }

  @Override
  public void mouseWheelMoved(MouseWheelEvent e) {
    for (Component overlay : getOverlays()) {
      if (overlay instanceof MouseWheelListener) {
        MouseWheelListener listener = (MouseWheelListener)overlay;
        listener.mouseWheelMoved(e);
        if (e.isConsumed()) {
          return;
        }
      }
    }
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    for (Component overlay : getOverlays()) {
      if (overlay instanceof MouseMotionListener) {
        MouseMotionListener listener = (MouseMotionListener)overlay;
        listener.mouseDragged(e);
        if (e.isConsumed()) {
          return;
        }
      }
    }
  }

  @Override
  public void mouseMoved(MouseEvent e) {
    for (Component overlay : getOverlays()) {
      if (overlay instanceof MouseMotionListener) {
        MouseMotionListener listener = (MouseMotionListener)overlay;
        listener.mouseMoved(e);
        if (e.isConsumed()) {
          return;
        }
      }
    }
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    for (Component overlay : getOverlays()) {
      if (overlay instanceof MouseListener) {
        MouseListener listener = (MouseListener)overlay;
        listener.mouseClicked(e);
        if (e.isConsumed()) {
          return;
        }
      }
    }
  }

  @Override
  public void mousePressed(MouseEvent e) {
    for (Component overlay : getOverlays()) {
      if (overlay instanceof MouseListener) {
        MouseListener listener = (MouseListener)overlay;
        listener.mousePressed(e);
        if (e.isConsumed()) {
          return;
        }
      }
    }
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    for (Component overlay : getOverlays()) {
      if (overlay instanceof MouseListener) {
        MouseListener listener = (MouseListener)overlay;
        listener.mouseReleased(e);
        if (e.isConsumed()) {
          return;
        }
      }
    }
  }

  @Override
  public void mouseEntered(MouseEvent e) {
    for (Component overlay : getOverlays()) {
      if (overlay instanceof MouseListener) {
        MouseListener listener = (MouseListener)overlay;
        listener.mouseEntered(e);
        if (e.isConsumed()) {
          return;
        }
      }
    }
  }

  @Override
  public void mouseExited(MouseEvent e) {
    for (Component overlay : getOverlays()) {
      if (overlay instanceof MouseListener) {
        MouseListener listener = (MouseListener)overlay;
        listener.mouseExited(e);
        if (e.isConsumed()) {
          return;
        }
      }
    }
  }

}
