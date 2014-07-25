package com.revolsys.swing.map.overlay;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import com.revolsys.converter.string.BooleanStringConverter;
import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.BoundingBoxDoubleGf;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.preferences.PreferencesDialog;
import com.revolsys.util.OS;

public class ZoomOverlay extends AbstractOverlay {
  private static final String ACTION_PAN = "pan";

  private static final String ACTION_ZOOM_BOX = "zoomBox";

  private static final Cursor CURSOR_PAN = new Cursor(Cursor.HAND_CURSOR);

  private static final Cursor CURSOR_ZOOM_BOX = SilkIconLoader.getCursor(
    "cursor_zoom_box", 9, 9);

  private static final long serialVersionUID = 1L;

  private static final Color TRANS_BG = new Color(0, 0, 0, 30);

  static {
    PreferencesDialog.get().addPreference("Zoom", "com.revolsys.gis",
      "/com/revolsys/gis/zoom", "wheelForwardsZoomIn", Boolean.class, true);
  }

  private java.awt.Point panFromPoint;

  private BufferedImage panImage;

  private int panModifiers;

  private boolean panning;

  private java.awt.Point panToPoint;

  private String prePanAction;

  private Cursor prePanCursor;

  private Rectangle2D zoomBox;

  private java.awt.Point zoomBoxFirstPoint;

  public ZoomOverlay(final MapPanel map) {
    super(map);
  }

  protected void cancelPan() {
    try {
      clearMapCursor(CURSOR_PAN);
      clearOverlayAction(ACTION_PAN);
      if (this.prePanAction != null) {
        setOverlayAction(this.prePanAction);
      }
      if (this.prePanCursor != null) {
        setMapCursor(this.prePanCursor);
      }
    } finally {
      getMap().clearVisibleOverlay(this);
      this.panning = false;
      this.panFromPoint = null;
      this.panImage = null;
      this.panToPoint = null;
      this.prePanAction = null;
      this.prePanCursor = null;
    }
  }

  protected void cancelZoomBox(final KeyEvent event) {
    this.zoomBox = null;
    this.zoomBoxFirstPoint = null;
    if (isOverlayAction(ACTION_ZOOM_BOX)) {
      if (event == null || !event.isShiftDown()) {
        clearOverlayAction(ACTION_ZOOM_BOX);
        clearMapCursor(CURSOR_ZOOM_BOX);
      }
    }
  }

  @Override
  public void focusGained(final FocusEvent e) {
    if (isOverlayAction(ACTION_ZOOM_BOX)) {
      setMapCursor(null);
      setMapCursor(CURSOR_ZOOM_BOX);
    }
    if (isOverlayAction(ACTION_PAN)) {
      setMapCursor(null);
      setMapCursor(CURSOR_PAN);
    }
  }

  @Override
  public void focusLost(final FocusEvent e) {
    cancelZoomBox(null);
    cancelPan();
  }

  public boolean isWheelForwardsZoomIn() {
    final Object wheelForwardsZoomIn = OS.getPreference("com.revolsys.gis",
      "/com/revolsys/gis/zoom", "wheelForwardsZoomIn");
    return !BooleanStringConverter.isFalse(wheelForwardsZoomIn);
  }

  protected boolean isZoomBox() {
    return this.zoomBoxFirstPoint != null;
  }

  @Override
  public void keyPressed(final KeyEvent event) {
    final int keyCode = event.getKeyCode();
    if (keyCode == KeyEvent.VK_ESCAPE) {
      cancelPan();
      cancelZoomBox(event);
      repaint();
    } else if (keyCode == KeyEvent.VK_SHIFT) {
      if (setOverlayAction(ACTION_ZOOM_BOX)) {
        setMapCursor(CURSOR_ZOOM_BOX);
      }
    }
  }

  @Override
  public void keyReleased(final KeyEvent event) {
    if (isOverlayAction(ACTION_ZOOM_BOX)) {
      if (this.zoomBoxFirstPoint == null && !event.isShiftDown()) {
        clearOverlayAction(ACTION_ZOOM_BOX);
        clearMapCursor(CURSOR_ZOOM_BOX);
      }
    }
  }

  @Override
  public void keyTyped(final KeyEvent event) {
  }

  @Override
  public void mouseClicked(final MouseEvent event) {
    if (!isOverlayAction(ACTION_ZOOM_BOX)) {
      if (event.getClickCount() == 2) {
        final int x = event.getX();
        final int y = event.getY();
        int numSteps = 0;
        if (SwingUtilities.isLeftMouseButton(event)) {
          numSteps = -1;
        } else if (SwingUtilities.isRightMouseButton(event)
            && !SwingUtil.isControlDown(event)) {
          numSteps = 1;
        }
        if (numSteps != 0) {
          final Point mapPoint = getViewport().toModelPoint(x, y);
          getMap().zoom(mapPoint, numSteps);
          event.consume();
        }
      }
    }
  }

  @Override
  public void mouseDragged(final MouseEvent event) {
    if (this.zoomBoxFirstPoint != null) {
      zoomBoxDrag(event);
    } else if (panDrag(event)) {
    }
  }

  @Override
  public void mouseEntered(final MouseEvent e) {

  }

  @Override
  public void mouseExited(final MouseEvent e) {
  }

  @Override
  public void mouseMoved(final MouseEvent event) {
    final boolean shiftDown = SwingUtil.isShiftDown(event);
    if (shiftDown && !SwingUtil.isControlOrMetaDown(event)) {
      setMapCursor(CURSOR_ZOOM_BOX);
      event.consume();
    }
    this.panning = false;
  }

  @Override
  public void mousePressed(final MouseEvent event) {
    clearOverlayAction(ACTION_PAN);
    if (zoomBoxStart(event)) {
    } else if (panStart(event, false)) {
    }
  }

  @Override
  public void mouseReleased(final MouseEvent event) {
    if (panFinish(event)) {
    } else if (zoomBoxFinish(event)) {
    }
  }

  @Override
  public void mouseWheelMoved(final MouseWheelEvent event) {
    if (!isOverlayAction(ACTION_ZOOM_BOX)) {
      int numSteps = event.getWheelRotation();
      if (SwingUtil.isScrollReversed()) {
        numSteps = -numSteps;
      }
      if (!isWheelForwardsZoomIn()) {
        numSteps = -numSteps;
      }
      final int scrollType = event.getScrollType();
      if (scrollType == MouseWheelEvent.WHEEL_BLOCK_SCROLL) {
        numSteps *= 2;
      }
      final int x = event.getX();
      final int y = event.getY();
      final Point mapPoint = getViewport().toModelPoint(x, y);
      getMap().zoom(mapPoint, numSteps);
      event.consume();
    }
  }

  @Override
  public void paintComponent(final Graphics graphics) {
    if (this.zoomBox != null) {
      final Graphics2D g = (Graphics2D)graphics;
      g.setColor(Color.DARK_GRAY);
      g.setStroke(new BasicStroke(2, BasicStroke.CAP_SQUARE,
        BasicStroke.JOIN_MITER, 2, new float[] {
        6, 6
      }, 0f));
      g.draw(this.zoomBox);
      g.setPaint(TRANS_BG);
      g.fill(this.zoomBox);
    }
    if (this.panFromPoint != null && this.panToPoint != null) {
      final int dx = (int)(this.panToPoint.getX() - this.panFromPoint.getX());
      final int dy = (int)(this.panToPoint.getY() - this.panFromPoint.getY());
      final Container parent = getParent();
      final int width = getViewport().getViewWidthPixels();
      final int height = getViewport().getViewHeightPixels();
      graphics.setColor(Color.WHITE);
      graphics.fillRect(0, 0, width, height);
      graphics.drawImage(this.panImage, dx, dy, parent);
    }
  }

  public boolean panDrag(final MouseEvent event) {
    if (this.panning || panStart(event, true)) {
      this.panToPoint = event.getPoint();
      repaint();
      event.consume();
      return true;
    }
    return false;
  }

  public boolean panFinish(final MouseEvent event) {
    if (event.getModifiers() == this.panModifiers) {
      if (clearOverlayAction(ACTION_PAN) && this.panning) {
        final java.awt.Point point = event.getPoint();
        final Point fromPoint = getViewport().toModelPoint(this.panFromPoint);
        final Point toPoint = getViewport().toModelPoint(point);

        final double deltaX = fromPoint.getX() - toPoint.getX();
        final double deltaY = fromPoint.getY() - toPoint.getY();

        final BoundingBox boundingBox = getViewport().getBoundingBox();
        final BoundingBox newBoundingBox = boundingBox.move(deltaX, deltaY);
        getMap().setBoundingBox(newBoundingBox);

        cancelPan();
        event.consume();
        return true;
      }
    }
    return false;
  }

  public boolean panStart(final MouseEvent event, final boolean drag) {
    boolean pan = false;
    if (SwingUtilities.isMiddleMouseButton(event)) {
      this.prePanAction = getOverlayAction();
      clearOverlayAction(this.prePanAction);
      pan = true;
    } else if (!drag && !hasOverlayAction()
        && SwingUtilities.isLeftMouseButton(event)) {
      pan = true;
    }
    if (pan) {
      if (setOverlayAction(ACTION_PAN)) {
        setMapCursor(CURSOR_ZOOM_BOX);
        this.panModifiers = event.getModifiers();

        final int width = getViewport().getViewWidthPixels();
        final int height = getViewport().getViewHeightPixels();
        if (width > 0 && height > 0) {
          final JComponent parent = (JComponent)getParent();
          this.panImage = new BufferedImage(width, height,
            BufferedImage.TYPE_INT_RGB);
          final Graphics2D graphics = (Graphics2D)this.panImage.getGraphics();
          try {
            final Insets insets = parent.getInsets();
            graphics.translate(-insets.left, -insets.top);
            graphics.setColor(Color.WHITE);
            graphics.fillRect(insets.left, insets.top, width, height);
            parent.paintComponents(graphics);
          } finally {
            graphics.dispose();
          }
          this.panning = true;
          setMapCursor(CURSOR_PAN);
          this.panFromPoint = event.getPoint();
          this.panToPoint = this.panFromPoint;
          event.consume();
          getMap().setVisibleOverlay(this);
        }
        return true;
      }
    }
    return false;
  }

  public void setZoomBoxCursor(final InputEvent event) {
    if (isOverlayAction(ACTION_ZOOM_BOX) || !hasOverlayAction()
        && SwingUtil.isShiftDown(event) && !SwingUtil.isAltDown(event)
        && !SwingUtil.isControlOrMetaDown(event)) {
      setMapCursor(CURSOR_ZOOM_BOX);
    } else {
      clearMapCursor(CURSOR_ZOOM_BOX);
    }
  }

  public void zoomBoxDrag(final MouseEvent event) {
    final int eventX = event.getX();
    final int eventY = event.getY();
    final double width = Math.abs(eventX - this.zoomBoxFirstPoint.getX());
    final double height = Math.abs(eventY - this.zoomBoxFirstPoint.getY());
    final java.awt.Point topLeft = new java.awt.Point(); // java.awt.Point
    if (this.zoomBoxFirstPoint.getX() < eventX) {
      topLeft.setLocation(this.zoomBoxFirstPoint.getX(), 0);
    } else {
      topLeft.setLocation(eventX, 0);
    }

    if (this.zoomBoxFirstPoint.getY() < eventY) {
      topLeft.setLocation(topLeft.getX(), this.zoomBoxFirstPoint.getY());
    } else {
      topLeft.setLocation(topLeft.getX(), eventY);
    }
    this.zoomBox.setRect(topLeft.getX(), topLeft.getY(), width, height);
    repaint();
    event.consume();
  }

  public boolean zoomBoxFinish(final MouseEvent event) {
    if (SwingUtilities.isLeftMouseButton(event)
        && clearOverlayAction(ACTION_ZOOM_BOX)) {
      // Convert first point to envelope top left in map coords.
      final int minX = (int)this.zoomBox.getMinX();
      final int minY = (int)this.zoomBox.getMinY();
      final Point topLeft = getViewport().toModelPoint(minX, minY);

      // Convert second point to envelope bottom right in map coords.
      final int maxX = (int)this.zoomBox.getMaxX();
      final int maxY = (int)this.zoomBox.getMaxY();
      final Point bottomRight = getViewport().toModelPoint(maxX, maxY);

      final GeometryFactory geometryFactory = getMap().getGeometryFactory();
      final BoundingBox boundingBox = new BoundingBoxDoubleGf(geometryFactory,
        2, topLeft.getX(), topLeft.getY(), bottomRight.getX(),
        bottomRight.getY());

      this.zoomBoxFirstPoint = null;
      this.zoomBox = null;
      if (!boundingBox.isEmpty()) {
        clearMapCursor(CURSOR_ZOOM_BOX);
        clearOverlayAction(ACTION_ZOOM_BOX);
        getMap().setBoundingBox(boundingBox);
        event.consume();
      }
      return true;
    }
    return false;
  }

  public boolean zoomBoxStart(final MouseEvent event) {
    if (SwingUtil.isShiftDown(event) && SwingUtilities.isLeftMouseButton(event)) {
      if (setOverlayAction(ACTION_ZOOM_BOX)) {
        setMapCursor(CURSOR_ZOOM_BOX);
        this.zoomBoxFirstPoint = event.getPoint();
        this.zoomBox = new Rectangle2D.Double();
        event.consume();
        return true;
      }
    }
    return false;
  }
}
