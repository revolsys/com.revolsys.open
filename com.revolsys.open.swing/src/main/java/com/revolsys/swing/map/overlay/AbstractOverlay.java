package com.revolsys.swing.map.overlay;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.undo.UndoableEdit;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.dataobject.renderer.GeometryStyleRenderer;
import com.revolsys.swing.map.layer.dataobject.style.GeometryStyle;
import com.revolsys.swing.undo.SetObjectProperty;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

@SuppressWarnings("serial")
public class AbstractOverlay extends JComponent implements
  PropertyChangeListener, MouseListener, MouseMotionListener,
  MouseWheelListener, KeyListener {
  private final LayerGroup project;

  private final MapPanel map;

  private final Viewport2D viewport;

  private Geometry xorGeometry;

  public static final GeometryStyle XOR_LINE_STYLE = GeometryStyle.line(
    new Color(0, 0, 255), 2);

  private final int hotspotPixels = 3;

  private GeometryFactory geometryFactory;

  protected AbstractOverlay(final MapPanel map) {
    this.map = map;
    this.viewport = map.getViewport();
    this.project = map.getProject();

    map.addMapOverlay(this);
  }

  protected void addUndo(final UndoableEdit edit) {
    map.addUndo(edit);
  }

  protected void clearMapCursor() {
    setMapCursor(Cursor.getDefaultCursor());
  }

  protected void clearUndoHistory() {
    getMap().getUndoManager().discardAllEdits();
  }

  protected void createPropertyUndo(final Object object,
    final String propertyName, final Object oldValue, final Object newValue) {
    final SetObjectProperty edit = new SetObjectProperty(object, propertyName,
      oldValue, newValue);
    addUndo(edit);
  }

  protected void drawXorGeometry(final Graphics2D graphics) {
    final Geometry geometry = xorGeometry;
    drawXorGeometry(graphics, geometry);
  }

  protected void drawXorGeometry(final Graphics2D graphics, Geometry geometry) {
    if (geometry != null) {
      geometry = getViewport().getGeometryFactory().copy(geometry);
      final Paint paint = graphics.getPaint();
      try {
        graphics.setXORMode(Color.WHITE);
        if (geometry instanceof Point) {
          final Point point = (Point)geometry;
          final Point2D screenPoint = viewport.toViewPoint(point);

          final double x = screenPoint.getX() - getHotspotPixels();
          final double y = screenPoint.getY() - getHotspotPixels();
          final int diameter = 2 * getHotspotPixels();
          final Shape shape = new Ellipse2D.Double(x, y, diameter, diameter);

          graphics.setPaint(new Color(0, 0, 255));
          graphics.fill(shape);
        } else {
          GeometryStyleRenderer.renderGeometry(viewport, graphics, geometry,
            XOR_LINE_STYLE);
        }
      } finally {
        graphics.setPaint(paint);
      }
    }
  }

  protected double getDistance(final MouseEvent event) {
    final int x = event.getX();
    final int y = event.getY();
    final GeometryFactory geometryFactory = getGeometryFactory();
    final Point p1 = geometryFactory.project(viewport.toModelPoint(x, y));
    final Point p2 = geometryFactory.project(viewport.toModelPoint(x
      + getHotspotPixels(), y + getHotspotPixels()));

    return p1.distance(p2);
  }

  protected GeometryFactory getGeometryFactory() {
    if (geometryFactory == null) {
      return project.getGeometryFactory();
    }
    return geometryFactory;
  }

  @Override
  public Graphics2D getGraphics() {
    return (Graphics2D)super.getGraphics();
  }

  public int getHotspotPixels() {
    return hotspotPixels;
  }

  public MapPanel getMap() {
    return map;
  }

  protected Point getPoint(final MouseEvent event) {
    final java.awt.Point eventPoint = event.getPoint();
    final GeometryFactory geometryFactory = getGeometryFactory();
    final Point point = viewport.toModelPointRounded(geometryFactory,
      eventPoint);
    return point;
  }

  public LayerGroup getProject() {
    return project;
  }

  public Viewport2D getViewport() {
    return viewport;
  }

  protected GeometryFactory getViewportGeometryFactory() {
    if (viewport == null) {
      return GeometryFactory.getFactory();
    } else {
      return viewport.getGeometryFactory();
    }
  }

  protected Point getViewportPoint(final MouseEvent event) {
    final java.awt.Point eventPoint = event.getPoint();
    return getViewportPoint(eventPoint);
  }

  protected Point getViewportPoint(final java.awt.Point eventPoint) {
    final Point point = viewport.toModelPoint(eventPoint);
    return point;
  }

  public Geometry getXorGeometry() {
    return xorGeometry;
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
  }

  @Override
  protected void paintComponent(final Graphics graphics) {
    paintComponent((Graphics2D)graphics);
  }

  protected void paintComponent(final Graphics2D graphics) {
    super.paintComponent(graphics);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
  }

  protected void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  protected void setMapCursor(final Cursor cursor) {
    if (map != null) {
      map.setCursor(cursor);
    }
  }

  public void setXorGeometry(final Geometry xorGeometry) {
    this.xorGeometry = xorGeometry;
  }

  protected void setXorGeometry(final Graphics2D graphics,
    final Geometry xorGeometry) {
    drawXorGeometry(graphics);
    this.xorGeometry = xorGeometry;
    drawXorGeometry(graphics);
  }
}
