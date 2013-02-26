package com.revolsys.swing.map.overlay;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.Viewport2D;
import com.vividsolutions.jts.geom.Point;

@SuppressWarnings("serial")
public class ZoomOverlay extends JComponent implements MouseListener,
  MouseMotionListener, MouseWheelListener {

  private final MapPanel map;

  private static final Color TRANS_BG = new Color(0, 0, 0, 30);

  private java.awt.Point zoomBoxFirstPoint;

  private java.awt.Point panFirstPoint;

  private Rectangle2D zoomBox;

  private Cursor cursor;

  private boolean panning;

  private final Viewport2D viewport;

  private BufferedImage panImage;

  public ZoomOverlay(final MapPanel map) {
    this.map = map;
    this.viewport = map.getViewport();
    map.addMapOverlay(this);
    addMouseListener(this);
    addMouseMotionListener(this);
    addMouseWheelListener(this);
  }

  @Override
  public void mouseClicked(final MouseEvent event) {
    if (event.getClickCount() == 2) {
      final int x = event.getX();
      final int y = event.getY();
      int numSteps = 0;
      if (SwingUtilities.isLeftMouseButton(event)) {
        numSteps = -1;
      } else if (SwingUtilities.isRightMouseButton(event)) {
        numSteps = 1;
      }
      if (numSteps != 0) {
        final Point mapPoint = viewport.toModelPoint(x, y);
        map.zoom(mapPoint, numSteps);
        event.consume();
      }
    }
  }

  @Override
  public void mouseDragged(final MouseEvent event) {
    if (panning) {
      panDrag(event);
    } else if (zoomBoxFirstPoint != null) {
      zoomBoxDrag(event);
    }
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
    final boolean shiftDown = event.isShiftDown();
    if (shiftDown) {
      zoomBoxStart(event);
    } else {
      panStart(event);
    }
  }

  @Override
  public void mouseReleased(final MouseEvent event) {
    if (panning) {
      panFinish(event);
    } else if (zoomBox != null) {
      zoomBoxFinish(event);
    }
  }

  @Override
  public void mouseWheelMoved(final MouseWheelEvent event) {
    int numSteps = event.getWheelRotation();
    final int scrollType = event.getScrollType();
    if (scrollType == MouseWheelEvent.WHEEL_BLOCK_SCROLL) {
      numSteps *= 2;
    }
    final int x = event.getX();
    final int y = event.getY();
    final Point mapPoint = viewport.toModelPoint(x, y);
    map.zoom(mapPoint, numSteps);
    event.consume();
  }

  @Override
  public void paintComponent(final Graphics graphics) {
    if (zoomBox != null) {
      final Graphics2D g = (Graphics2D)graphics;
      g.setColor(Color.DARK_GRAY);
      g.setStroke(new BasicStroke(2, BasicStroke.CAP_SQUARE,
        BasicStroke.JOIN_MITER, 2, new float[] {
          6, 6
        }, 0f));
      g.draw(zoomBox);
      g.setPaint(TRANS_BG);
      g.fill(zoomBox);
    }
  }

  public void panDrag(final MouseEvent event) {
    java.awt.Point p = event.getPoint();
    final int dx = (int)(p.getX() - panFirstPoint.getX());
    final int dy = (int)(p.getY() - panFirstPoint.getY());

    Container parent = getParent();
    Graphics2D graphics = (Graphics2D)getGraphics();
    int width = viewport.getViewWidthPixels();
    int height = viewport.getViewHeightPixels();
    graphics.setColor(Color.WHITE);
    graphics.fillRect(0, 0, width, height);
    graphics.drawImage(panImage, dx, dy, parent);
    event.consume();
  }

  public void panFinish(final MouseEvent event) {
    java.awt.Point point = event.getPoint();
    final Point fromPoint = viewport.toModelPoint(panFirstPoint);
    final Point toPoint = viewport.toModelPoint(point);

    final double deltaX = fromPoint.getX() - toPoint.getX();
    final double deltaY = fromPoint.getY() - toPoint.getY();
    
    final BoundingBox boundingBox = viewport.getBoundingBox().clone();
    boundingBox.move(deltaX, deltaY);
    viewport.setBoundingBox(boundingBox);

    panFirstPoint = null;
    panning = false;
    restoreCursor();
    panImage = null;
    getParent().repaint();
    event.consume();
  }

  public void panStart(final MouseEvent event) {
    int width = viewport.getViewWidthPixels();
    int height = viewport.getViewHeightPixels();
    JComponent parent = (JComponent)getParent();
    panImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics2D graphics = (Graphics2D)panImage.getGraphics();
    Insets insets = parent.getInsets();
    graphics.translate(-insets.left, -insets.top);
    parent.paintComponents(graphics);
    graphics.dispose();
    panning = true;
    saveCursor();
    getParent().setCursor(new Cursor(Cursor.HAND_CURSOR));
    panFirstPoint = event.getPoint();
    event.consume();
  }

  private void restoreCursor() {
    if (cursor != null) {
      getParent().setCursor(cursor);
      cursor = null;
    }
  }

  private void saveCursor() {
    cursor = getParent().getCursor();
  }

  public void zoomBoxDrag(final MouseEvent event) {
    final int eventX = event.getX();
    final int eventY = event.getY();
    final double width = Math.abs(eventX - zoomBoxFirstPoint.getX());
    final double height = Math.abs(eventY - zoomBoxFirstPoint.getY());
    final java.awt.Point topLeft = new java.awt.Point(); // java.awt.Point
    if (zoomBoxFirstPoint.getX() < eventX) {
      topLeft.setLocation(zoomBoxFirstPoint.getX(), 0);
    } else {
      topLeft.setLocation(eventX, 0);
    }

    if (zoomBoxFirstPoint.getY() < eventY) {
      topLeft.setLocation(topLeft.getX(), zoomBoxFirstPoint.getY());
    } else {
      topLeft.setLocation(topLeft.getX(), eventY);
    }
    zoomBox.setRect(topLeft.getX(), topLeft.getY(), width, height);
    repaint();
    event.consume();
  }

  public void zoomBoxFinish(final MouseEvent event) {
    // Convert first point to envelope top left in map coords.
    final int minX = (int)zoomBox.getMinX();
    final int minY = (int)zoomBox.getMinY();
    final Point topLeft = viewport.toModelPoint(minX, minY);

    // Convert second point to envelope bottom right in map coords.
    final int maxX = (int)zoomBox.getMaxX();
    final int maxY = (int)zoomBox.getMaxY();
    final Point bottomRight = viewport.toModelPoint(maxX, maxY);

    final GeometryFactory geometryFactory = map.getGeometryFactory();
    final BoundingBox extent = new BoundingBox(geometryFactory, topLeft.getX(),
      topLeft.getY(), bottomRight.getX(), bottomRight.getY());
    map.setBoundingBox(extent);

    zoomBoxFirstPoint = null;
    zoomBox = null;
    restoreCursor();
    event.consume();
  }

  public void zoomBoxStart(final MouseEvent event) {
    saveCursor();
    getParent().setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
    zoomBoxFirstPoint = event.getPoint();
    zoomBox = new Rectangle2D.Double();
    event.consume();
  }
}
