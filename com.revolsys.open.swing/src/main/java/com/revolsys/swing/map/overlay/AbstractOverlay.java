package com.revolsys.swing.map.overlay;

import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;

import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.Project;

@SuppressWarnings("serial")
public class AbstractOverlay extends JComponent implements
  PropertyChangeListener, MouseListener, MouseMotionListener,
  MouseWheelListener, KeyListener {
  private final Project project;

  private final MapPanel map;

  private final Viewport2D viewport;

  protected AbstractOverlay(final MapPanel map) {
    this.map = map;
    this.viewport = map.getViewport();
    this.project = map.getProject();

    map.addMapOverlay(this);
  }

  protected void clearMapCursor() {
    setMapCursor(Cursor.getDefaultCursor());
  }

  public MapPanel getMap() {
    return map;
  }

  public Project getProject() {
    return project;
  }

  public Viewport2D getViewport() {
    return viewport;
  }

  @Override
  public void keyPressed(final KeyEvent e) {
  }

  @Override
  public void keyReleased(final KeyEvent e) {
  }

  @Override
  public void keyTyped(final KeyEvent e) {
  }

  @Override
  public void mouseClicked(final MouseEvent event) {
  }

  @Override
  public void mouseDragged(final MouseEvent event) {
  }

  @Override
  public void mouseEntered(final MouseEvent e) {
  }

  @Override
  public void mouseExited(final MouseEvent e) {
  }

  @Override
  public void mouseMoved(final MouseEvent e) {
  }

  @Override
  public void mousePressed(final MouseEvent event) {
  }

  @Override
  public void mouseReleased(final MouseEvent event) {
  }

  @Override
  public void mouseWheelMoved(final MouseWheelEvent e) {
    // TODO Auto-generated method stub

  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
  }

  protected void setMapCursor(final Cursor cursor) {
    if (map != null) {
      map.setCursor(cursor);
    }
  }
}
