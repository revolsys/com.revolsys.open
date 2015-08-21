package com.revolsys.swing.map.overlay;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import com.revolsys.awt.WebColors;
import com.revolsys.converter.string.BooleanStringConverter;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;
import com.revolsys.raster.BufferedGeoreferencedImage;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.raster.MappedLocation;
import com.revolsys.swing.Icons;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.map.ImageViewport;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.raster.GeoreferencedImageLayer;
import com.revolsys.swing.map.layer.raster.GeoreferencedImageLayerRenderer;
import com.revolsys.swing.map.layer.record.renderer.GeometryStyleRenderer;
import com.revolsys.swing.map.layer.record.renderer.MarkerStyleRenderer;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.swing.undo.ListAddUndo;
import com.revolsys.swing.undo.SetObjectProperty;

public class EditGeoreferencedImageOverlay extends AbstractOverlay {
  private static final String ACTION_TIE_POINT_ADD = "Add Tie Point";

  private static final Cursor CURSOR_MOVE_IMAGE = Icons.getCursor("cursor_move", 8, 7);

  private static final Cursor CURSOR_SOURCE_PIXEL_ADD = Icons.getCursor("cursor_source_pixel_add",
    5, 5);

  private static final Color COLOR_OUTLINE = WebColors.Black;

  private static final Color COLOR_SELECT = WebColors.Cyan;

  private static final long serialVersionUID = 1L;

  private static final GeometryStyle STYLE_MAPPED_LINE = GeometryStyle.line(COLOR_SELECT, 3);

  private static final GeometryStyle STYLE_IMAGE_LINE = GeometryStyle.line(COLOR_SELECT, 1);

  private static final MarkerStyle STYLE_VERTEX_FIRST_POINT = MarkerStyle
    .marker(SelectedRecordsRenderer.firstVertexShape(), 9, COLOR_OUTLINE, 1, COLOR_SELECT);

  private static final MarkerStyle STYLE_VERTEX_LAST_POINT = MarkerStyle
    .marker(SelectedRecordsRenderer.lastVertexShape(), 9, COLOR_OUTLINE, 1, COLOR_SELECT);

  static {
    STYLE_VERTEX_FIRST_POINT.setMarkerOrientationType("auto");
    STYLE_VERTEX_FIRST_POINT.setMarkerPlacementType("point(0)");
    STYLE_VERTEX_FIRST_POINT.setMarkerHorizontalAlignment("center");

    STYLE_VERTEX_LAST_POINT.setMarkerOrientationType("auto");
    STYLE_VERTEX_LAST_POINT.setMarkerPlacementType("point(n)");
    STYLE_VERTEX_LAST_POINT.setMarkerHorizontalAlignment("right");
  }

  public static final String ACTION_MOVE_IMAGE = "moveImage";

  private static final String ACTION_MOVE_IMAGE_CORNER = "moveImageCorner";

  private static final String ACTION_TIE_POINT_MOVE_SOURCE = "moveSourceTiePoint";

  private static final String ACTION_TIE_POINT_MOVE_TARGET = "moveTargetTiePoint";

  private Point addTiePointFirstPoint;

  private GeoreferencedImage image;

  private GeoreferencedImageLayer layer;

  private Point moveCornerOppositePoint;

  private Point moveCornerPoint;

  private Point moveImageFirstPoint;

  private BoundingBox moveImageBoundingBox;

  private Point addTiePointMove;

  private GeoreferencedImage cachedImage;

  private int moveTiePointIndex = -1;

  private java.awt.Point moveTiePointEventPoint;

  private List<Integer> closeSourcePixelIndexes = new ArrayList<Integer>();

  private List<Integer> closeTargetPointIndexes = new ArrayList<Integer>();

  private boolean moveTiePointStarted;

  private boolean moveTiePointSource;

  private Point moveTiePointLocation;

  public EditGeoreferencedImageOverlay(final MapPanel map) {
    super(map);
    setOverlayActionCursor(ACTION_MOVE_IMAGE, CURSOR_MOVE_IMAGE);
    setOverlayActionCursor(ACTION_TIE_POINT_ADD, CURSOR_SOURCE_PIXEL_ADD);
    setOverlayActionCursor(ACTION_TIE_POINT_MOVE_SOURCE, CURSOR_NODE_EDIT);
    setOverlayActionCursor(ACTION_TIE_POINT_MOVE_TARGET, CURSOR_NODE_EDIT);

    addOverlayActionOverride(SelectRecordsOverlay.ACTION_SELECT_RECORDS, ACTION_MOVE_IMAGE,
      ACTION_MOVE_IMAGE_CORNER);
    addOverlayActionOverride(ACTION_MOVE_IMAGE, ZoomOverlay.ACTION_PAN, ZoomOverlay.ACTION_ZOOM,
      ACTION_MOVE_IMAGE_CORNER);
    addOverlayActionOverride(ACTION_MOVE_IMAGE_CORNER, ZoomOverlay.ACTION_PAN,
      ZoomOverlay.ACTION_ZOOM, ACTION_TIE_POINT_ADD);
    addOverlayActionOverride(ACTION_TIE_POINT_ADD, ZoomOverlay.ACTION_PAN, ZoomOverlay.ACTION_ZOOM);
    addOverlayActionOverride(ACTION_TIE_POINT_MOVE_SOURCE, ZoomOverlay.ACTION_PAN,
      ZoomOverlay.ACTION_ZOOM, ACTION_MOVE_IMAGE);
    addOverlayActionOverride(ACTION_TIE_POINT_MOVE_TARGET, ZoomOverlay.ACTION_PAN,
      ZoomOverlay.ACTION_ZOOM, ACTION_MOVE_IMAGE);
  }

  protected void addTiePointClear() {
    this.addTiePointFirstPoint = null;
    this.addTiePointMove = null;
    clearCachedImage();
    clearSnapLocations();
    clearOverlayAction(ACTION_TIE_POINT_ADD);
  }

  private boolean addTiePointFinish(final MouseEvent event) {
    if (event == null || event.getButton() == MouseEvent.BUTTON1) {
      if (this.addTiePointFirstPoint != null) {
        if (clearOverlayAction(ACTION_TIE_POINT_ADD)) {
          try {
            clearMapCursor(CURSOR_SOURCE_PIXEL_ADD);
            Point mapPoint = getEventPoint();
            final Point snapPoint = getSnapPoint();
            if (snapPoint != null) {
              mapPoint = snapPoint;
            }
            final Point sourcePoint = this.addTiePointFirstPoint;
            final Point sourcePixel = this.layer.targetPointToSourcePixel(sourcePoint);
            final GeometryFactory geometryFactory = getImageGeometryFactory();
            final Point targetPoint = mapPoint.copy(geometryFactory);
            final MappedLocation mappedLocation = new MappedLocation(sourcePixel, targetPoint);
            addUndo(new ListAddUndo(this.image.getTiePoints(), mappedLocation));
          } finally {
            addTiePointClear();
          }
          return true;
        }
      }
    }
    return false;
  }

  private boolean addTiePointMove(final MouseEvent event) {
    if (this.addTiePointFirstPoint != null) {
      final BoundingBox boundingBox = getHotspotBoundingBox(event);
      hasSnapPoint(event, boundingBox);

      if (getSnapPoint() == null) {
        this.addTiePointMove = getEventPoint();
      } else {
        this.addTiePointMove = getSnapPoint();
      }
      repaint();
      return true;
    } else if (SwingUtil.isAltDown(event)) {
      if (isInImage(event)) {
        setOverlayAction(ACTION_TIE_POINT_ADD);
        return true;
      } else {
        clearOverlayAction(ACTION_TIE_POINT_ADD);
      }
    }

    return false;
  }

  private boolean addTiePointStart(final MouseEvent event) {
    if (this.layer != null) {
      if (addTiePointFinish(event)) {
      } else if (SwingUtil.isLeftButtonAndAltDown(event)) {
        if (isInImage(event)) {
          if (setOverlayAction(ACTION_TIE_POINT_ADD)) {
            final Point mousePoint = getEventPoint();
            this.addTiePointFirstPoint = mousePoint;
            event.consume();
            return true;
          }
        }
      }
    }
    return false;
  }

  protected void adjustBoundingBoxAspectRatio() {
    if (this.moveImageBoundingBox != null && this.moveCornerPoint != null) {
      final double imageAspectRatio = this.image.getImageAspectRatio();
      final BoundingBox boundingBox = this.moveImageBoundingBox;
      final double aspectRatio = boundingBox.getAspectRatio();
      double minX = boundingBox.getMinX();
      double maxX = boundingBox.getMaxX();
      double minY = boundingBox.getMinY();
      double maxY = boundingBox.getMaxY();
      final double width = boundingBox.getWidth();
      final double height = boundingBox.getHeight();
      if (aspectRatio < imageAspectRatio) {
        if (minX == this.moveCornerOppositePoint.getX()) {
          maxX = minX + height * imageAspectRatio;
        } else {
          minX = maxX - height * imageAspectRatio;
        }
      } else if (aspectRatio > imageAspectRatio) {
        if (minY == this.moveCornerOppositePoint.getY()) {
          maxY = minY + width / imageAspectRatio;
        } else {
          minY = maxY - width / imageAspectRatio;
        }
      }
      final GeometryFactory viewportGeometryFactory = getViewportGeometryFactory();
      this.moveImageBoundingBox = new BoundingBoxDoubleGf(viewportGeometryFactory, 2, minX, minY,
        maxX, maxY);
    }
  }

  protected void appendTiePointLocation(final StringBuilder toolTip,
    final List<MappedLocation> tiePoints, final List<Integer> indices, final int startNumber,
    final boolean source) {
    if (!indices.isEmpty()) {
      int i = startNumber - 1;
      toolTip.append(
        "<div style=\"border-bottom: solid black 1px; font-weight:bold;padding: 1px 3px 1px 3px\">");
      if (source) {
        toolTip.append("Move source pixel");
      } else {
        toolTip.append("Move target point");
      }
      toolTip.append("</div>");
      toolTip.append("<div style=\"padding: 1px 3px 1px 3px\">");
      toolTip.append("<ol start=\"");
      toolTip.append(startNumber);
      toolTip.append("\" style=\"margin: 1px 3px 1px 15px\">");
      for (final Integer index : indices) {
        final MappedLocation tiePoint = tiePoints.get(index);
        toolTip.append("<li style=\"padding: 2px; margin:1px;");
        if (i == this.moveTiePointIndex) {
          toolTip.append("border: 2px solid maroon");
        } else {
          toolTip.append("border: 2px solid #FFFF33");
        }
        toolTip.append("\">#");
        toolTip.append(index + 1);
        toolTip.append(" ");
        final Point point;
        if (source) {
          point = tiePoint.getSourcePixel();
        } else {
          point = tiePoint.getTargetPoint();
        }
        appendPoint(toolTip, point);
        if (!source) {
          toolTip.append(" (");
          toolTip.append(point.getSrid());
          toolTip.append(")");
        }
        toolTip.append("</li>");
        i++;
      }
      toolTip.append("</ol></div>");
      i++;
    }
  }

  protected void cancel() {
    moveImageClear();
    moveCornerClear();
    moveTiePointClear();
    addTiePointClear();
    clearCachedImage();
    repaint(0);
  }

  protected void clear() {
    this.image = null;
    this.layer = null;
    clearCachedImage();
    clearUndoHistory();
    cancel();
  }

  protected void clearCachedImage() {
    this.cachedImage = null;
    System.gc();
  }

  @Override
  public void focusLost(final FocusEvent e) {
    if (isEditing()) {
      cancel();
    }
  }

  private GeoreferencedImage getCachedImage(BoundingBox boundingBox) {
    boundingBox = boundingBox.convert(getViewportGeometryFactory());
    final Viewport2D viewport = getViewport();
    final BoundingBox viewBoundingBox = viewport.getBoundingBox();
    if (this.cachedImage == null || !this.cachedImage.getBoundingBox().equals(viewBoundingBox)) {
      try (
        final ImageViewport imageViewport = new ImageViewport(viewport)) {

        final BufferedImage image = imageViewport.getImage();
        final Graphics2D graphics = (Graphics2D)image.getGraphics();

        this.image.drawImage(graphics, viewBoundingBox, viewport.getViewWidthPixels(),
          viewport.getViewHeightPixels(), !this.layer.isShowOriginalImage());
        GeoreferencedImageLayerRenderer.render(imageViewport, graphics, this.image,
          !this.layer.isShowOriginalImage());
        this.cachedImage = new BufferedGeoreferencedImage(imageViewport.getBoundingBox(), image);
      }
    }
    return this.cachedImage;
  }

  public BoundingBox getImageBoundingBox() {
    if (this.image == null) {
      return BoundingBox.EMPTY;
    } else {
      return this.layer.getBoundingBox();
    }
  }

  public GeometryFactory getImageGeometryFactory() {
    if (this.image == null) {
      return getGeometryFactory();
    } else {
      return this.layer.getGeometryFactory();
    }
  }

  public GeoreferencedImageLayer getLayer() {
    return this.layer;
  }

  protected BoundingBox getMoveBoundingBox(final MouseEvent event) {
    BoundingBox boundingBox = getImageBoundingBox();
    final Point mousePoint = getEventPoint();
    final GeometryFactory imageGeometryFactory = getImageGeometryFactory();
    final Point imagePoint = mousePoint.convert(imageGeometryFactory);

    final double deltaX = imagePoint.getX() - this.moveImageFirstPoint.getX();
    final double deltaY = imagePoint.getY() - this.moveImageFirstPoint.getY();
    boundingBox = boundingBox.move(deltaX, deltaY);
    return boundingBox;
  }

  private MappedLocation getMoveTiePoint() {
    if (this.moveTiePointIndex > -1) {
      int tiePointIndex;
      final int targetSize = this.closeTargetPointIndexes.size();
      if (this.moveTiePointIndex < targetSize) {
        tiePointIndex = this.closeTargetPointIndexes.get(this.moveTiePointIndex);
        this.moveTiePointSource = false;
      } else if (this.moveTiePointIndex - targetSize < this.closeSourcePixelIndexes.size()) {
        tiePointIndex = this.closeSourcePixelIndexes.get(this.moveTiePointIndex - targetSize);
        this.moveTiePointSource = true;
      } else {
        return null;
      }
      return this.image.getTiePoints().get(tiePointIndex);
    }
    return null;
  }

  protected boolean isApplicable(final MouseEvent event) {
    final BoundingBox imageBoundingBox = getImageBoundingBox();
    final Point point = getPoint(event);
    final double distance = getDistance(event);

    return imageBoundingBox.distance(point) < distance * 2;
  }

  public boolean isEditing() {
    if (this.layer != null) {
      return this.layer.isEditable();
    }
    return false;
  }

  private boolean isInImage() {
    final Point mousePoint = getEventPoint();
    return isInImage(mousePoint);
  }

  private boolean isInImage(final MouseEvent event) {
    final Point mousePoint = getPoint(event);
    final boolean inImage = isInImage(mousePoint);
    return inImage;
  }

  private boolean isInImage(final Point mousePoint) {
    if (mousePoint == null) {
      return false;
    } else {
      final BoundingBox imageBoundingBox = getImageBoundingBox();
      final boolean inImage = imageBoundingBox.covers(mousePoint);
      return inImage;
    }
  }

  @Override
  public void keyPressed(final KeyEvent event) {
    if (isEditing()) {
      final int keyCode = event.getKeyCode();
      if (this.moveCornerPoint != null) {
        if (keyCode == KeyEvent.VK_SHIFT) {
          adjustBoundingBoxAspectRatio();
          repaint();
        }
      } else {
        if (keyCode == KeyEvent.VK_CONTROL || keyCode == KeyEvent.VK_META) {
          if (isInImage()) {
            setOverlayAction(ACTION_MOVE_IMAGE);
          }
        } else if (keyCode == KeyEvent.VK_ALT) {
          if (isInImage()) {
            if (setOverlayAction(ACTION_TIE_POINT_ADD)) {
              event.consume();
            }
          }
        }
      }
    }
  }

  @Override
  public void keyReleased(final KeyEvent event) {
    if (isEditing()) {
      final int keyCode = event.getKeyCode();
      if (this.moveTiePointIndex > -1 || this.addTiePointFirstPoint != null
        || this.moveTiePointStarted) {
        final char keyChar = event.getKeyChar();
        if (keyChar >= '0' && keyChar <= '9') {
          event.consume();
        }
      }
      if (keyCode == KeyEvent.VK_ESCAPE) {
        cancel();
        repaint();
      } else if (keyCode == KeyEvent.VK_CONTROL || keyCode == KeyEvent.VK_META) {
        if (this.moveImageFirstPoint == null) {
          moveImageClear();
        }
      } else if (keyCode == KeyEvent.VK_ALT) {
        if (this.addTiePointFirstPoint == null) {
          addTiePointClear();
        }
      }

    }
  }

  @Override
  public void keyTyped(final KeyEvent e) {
    if (isEditing()) {
      final char keyChar = e.getKeyChar();
      if (keyChar >= '0' && keyChar <= '9') {
        int index = keyChar - '0';
        if (!this.moveTiePointStarted && this.moveTiePointIndex > -1) {
          if (index > 0) {
            index--;
            if (index < this.closeSourcePixelIndexes.size() + this.closeTargetPointIndexes.size()) {

              this.moveTiePointIndex = index;
              setMoveTiePointToolTip();
            }
          }
        } else if (index < getSnapPointLocationMap().size()) {
          setSnapPointIndex(index);
          setSnapLocations(getSnapPointLocationMap());
        }
      }
    }
  }

  @Override
  public void mouseClicked(final MouseEvent event) {
    if (isEditing()) {
      if (isApplicable(event)) {
        if (addTiePointFinish(event)) {
        } else if (addTiePointStart(event)) {
        }
      }
    }
  }

  @Override
  public void mouseDragged(final MouseEvent event) {
    if (isEditing()) {
      if (addTiePointMove(event)) {
      } else if (moveTiePointDrag(event)) {
      } else if (moveCornerDrag(event)) {
      } else if (moveImageDrag(event)) {
      }
    }
  }

  @Override
  public void mouseMoved(final MouseEvent event) {
    if (isEditing()) {
      if (addTiePointMove(event)) {
      } else if (moveCornerMove(event)) {
      } else if (moveImageMove(event)) {
      } else if (moveTiePointMove(event)) {
      }
    }
  }

  @Override
  public void mousePressed(final MouseEvent event) {
    if (isEditing()) {
      if (moveImageStart(event)) {
      } else if (moveTiePointStart(event)) {
      } else if (isInImage() && SwingUtil.isLeftButtonAndAltDown(event)) {
        event.consume();
      } else if (moveCornerStart(event)) {
      }
    }
  }

  @Override
  public void mouseReleased(final MouseEvent event) {
    if (isEditing()) {
      if (event.getButton() == MouseEvent.BUTTON1) {
        if (moveTiePointFinish(event)) {
        } else if (moveCornerFinish(event)) {
        } else if (moveCornerFinish(event)) {
        } else if (moveImageFinish(event)) {
        }
      }
    }
  }

  protected void moveCornerClear() {
    clearOverlayAction(ACTION_MOVE_IMAGE_CORNER);
    this.moveImageBoundingBox = null;
    this.moveCornerOppositePoint = null;
    this.moveCornerPoint = null;
  }

  private boolean moveCornerDrag(final MouseEvent event) {
    if (isOverlayAction(ACTION_MOVE_IMAGE_CORNER) && this.moveCornerOppositePoint != null) {
      final GeometryFactory viewportGeometryFactory = getViewportGeometryFactory();
      final Point mousePoint = getEventPoint();
      this.moveImageBoundingBox = new BoundingBoxDoubleGf(viewportGeometryFactory, mousePoint,
        this.moveCornerOppositePoint);
      if (SwingUtil.isShiftDown(event)) {
        adjustBoundingBoxAspectRatio();
      }
      repaint();
      return true;
    }
    return false;
  }

  private boolean moveCornerFinish(final MouseEvent event) {
    if (event.getButton() == MouseEvent.BUTTON1) {
      if (clearOverlayAction(ACTION_MOVE_IMAGE_CORNER)) {
        try {
          final SetObjectProperty setBBox = new SetObjectProperty(this, "imageBoundingBox",
            getImageBoundingBox(), this.moveImageBoundingBox);
          addUndo(setBBox);
          event.consume();
        } finally {
          moveCornerClear();
        }
        repaint();
        return true;
      }
    }
    return false;
  }

  private boolean moveCornerMove(final MouseEvent event) {
    Cursor moveCornerCursor = null;
    final Point oldPoint = this.moveCornerPoint;
    this.moveCornerPoint = null;
    this.moveCornerOppositePoint = null;
    final int modifiers = event.getModifiersEx();
    if (modifiers == 0 || modifiers == InputEvent.SHIFT_DOWN_MASK) {
      if (this.layer != null) {

        final Point mousePoint = getEventPoint();
        final GeometryFactory viewportGeometryFactory = getViewportGeometryFactory();

        Point closestPoint = null;
        final double maxDistance = getDistance(event);
        double closestDistance = Double.MAX_VALUE;
        if (oldPoint != null) {
          final double distance = oldPoint.distance(mousePoint);
          if (distance < maxDistance) {
            closestPoint = oldPoint;
            closestDistance = distance;
          }
        }
        int closestIndex = -1;
        BoundingBox imageBoundingBox = getImageBoundingBox();
        if (!imageBoundingBox.isEmpty()) {
          imageBoundingBox = imageBoundingBox.convert(viewportGeometryFactory);
          for (int i = 0; i < 4; i++) {
            final Point point = imageBoundingBox.getCornerPoint(i);
            final Point mapPoint = point.convert(viewportGeometryFactory, 2);
            final double distance = mapPoint.distance(mousePoint);
            if (distance < maxDistance && distance < closestDistance) {
              closestPoint = point;
              closestDistance = distance;
              closestIndex = i;

            }
          }
          if (closestPoint == oldPoint) {
            if (oldPoint != null) {
              return true;
            }
          } else {
            this.moveCornerPoint = closestPoint;
            if (closestIndex == -1) {
              this.moveCornerOppositePoint = null;
            } else {
              this.moveCornerOppositePoint = imageBoundingBox.getCornerPoint(closestIndex + 2)
                .convert(viewportGeometryFactory);
            }
            switch (closestIndex) {
              case 0:
                moveCornerCursor = Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
              break;
              case 1:
                moveCornerCursor = Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
              break;
              case 2:
                moveCornerCursor = Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
              break;
              case 3:
                moveCornerCursor = Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
              break;
            }
          }
        }
      }
    }
    if (this.moveCornerPoint == null) {
      moveCornerClear();
      return false;
    } else {
      if (setOverlayAction(ACTION_MOVE_IMAGE_CORNER)) {
        setMapCursor(moveCornerCursor);
      }
      return true;
    }
  }

  private boolean moveCornerStart(final MouseEvent event) {
    if (this.layer != null) {
      if (event.getButton() == MouseEvent.BUTTON1 && !event.isAltDown()) {
        if (this.moveCornerPoint != null) {
          if (isOverlayAction(ACTION_MOVE_IMAGE_CORNER)) {
            event.consume();
            return true;
          }
        }
      }
    }
    return false;
  }

  protected void moveImageClear() {
    this.moveImageFirstPoint = null;
    this.moveImageBoundingBox = null;
    clearOverlayAction(ACTION_MOVE_IMAGE);
  }

  private boolean moveImageDrag(final MouseEvent event) {
    if (isOverlayAction(ACTION_MOVE_IMAGE) && this.moveImageFirstPoint != null) {
      this.moveImageBoundingBox = getMoveBoundingBox(event);
      repaint(0);
      return true;
    }
    return false;
  }

  private boolean moveImageFinish(final MouseEvent event) {
    if (event.getButton() == MouseEvent.BUTTON1) {
      if (clearOverlayAction(ACTION_MOVE_IMAGE)) {
        final BoundingBox boundingBox = getMoveBoundingBox(event);
        final SetObjectProperty setBBox = new SetObjectProperty(this, "imageBoundingBox",
          getImageBoundingBox(), boundingBox);
        addUndo(setBBox);
        moveImageClear();
        event.consume();
        repaint();
        return true;
      }
    }
    return false;
  }

  private boolean moveImageMove(final MouseEvent event) {
    if (isInImage(event)) {
      if (SwingUtil.isControlOrMetaDown(event)) {
        if (setOverlayAction(ACTION_MOVE_IMAGE)) {
          event.consume();
          return true;
        }
      }
    }
    moveImageClear();
    return false;
  }

  private boolean moveImageStart(final MouseEvent event) {
    if (this.layer != null) {
      if (event.getButton() == MouseEvent.BUTTON1 && SwingUtil.isControlOrMetaDown(event)) {
        if (isOverlayAction(ACTION_MOVE_IMAGE)) {
          final Point mousePoint = getEventPoint();
          final GeometryFactory imageGeometryFactory = getImageGeometryFactory();
          final Point imagePoint = mousePoint.convert(imageGeometryFactory);
          this.moveImageFirstPoint = imagePoint;
          event.consume();
          return true;
        }
      }
    }
    return false;
  }

  private void moveTiePointClear() {
    clearOverlayAction(ACTION_TIE_POINT_MOVE_SOURCE);
    clearOverlayAction(ACTION_TIE_POINT_MOVE_TARGET);
    this.closeSourcePixelIndexes.clear();
    this.closeTargetPointIndexes.clear();
    this.moveTiePointEventPoint = null;
    this.moveTiePointIndex = -1;
    this.moveTiePointLocation = null;
    this.moveTiePointStarted = false;
  }

  private boolean moveTiePointDrag(final MouseEvent event) {
    if (SwingUtil.isLeftButtonOnly(event)) {
      if (this.moveTiePointStarted) {
        if (this.moveTiePointSource) {
          this.moveTiePointLocation = getEventPoint();
        } else {
          final BoundingBox boundingBox = getHotspotBoundingBox(event);
          if (hasSnapPoint(event, boundingBox)) {
            this.moveTiePointLocation = getSnapPoint();
          } else {
            this.moveTiePointLocation = getEventPoint();
          }
        }
        event.consume();
        repaint();
        return true;
      }
    }
    return false;
  }

  private boolean moveTiePointFinish(final MouseEvent event) {
    if (this.moveTiePointStarted) {
      final MappedLocation tiePoint = getMoveTiePoint();
      if (tiePoint != null) {
        Point point = getPoint(event);
        String action;
        if (this.moveTiePointSource) {
          action = ACTION_TIE_POINT_MOVE_SOURCE;
        } else {
          action = ACTION_TIE_POINT_MOVE_TARGET;
        }
        if (clearOverlayAction(action)) {
          if (this.moveTiePointSource) {
            final Point sourcePixel = this.layer.targetPointToSourcePixel(point);

            final SetObjectProperty setSourcePixel = new SetObjectProperty(tiePoint, "sourcePixel",
              tiePoint.getSourcePixel(), sourcePixel);
            addUndo(setSourcePixel);
          } else {
            final Point snapPoint = getSnapPoint();
            if (snapPoint != null) {
              point = snapPoint;
            }
            final GeometryFactory imageGeometryFactory = getImageGeometryFactory();
            point = point.copy(imageGeometryFactory);
            tiePoint.setTargetPoint(point);
            final SetObjectProperty setTargetPoint = new SetObjectProperty(tiePoint, "targetPoint",
              tiePoint.getTargetPoint(), point);
            addUndo(setTargetPoint);
          }
          this.closeSourcePixelIndexes.clear();
          this.closeTargetPointIndexes.clear();
          moveTiePointClear();
          clearCachedImage();
          clearMapCursor();
          clearSnapLocations();

          if (event != null) {
            event.consume();
          }
          repaint();
          return true;
        }
      }
    }
    return false;
  }

  private boolean moveTiePointMove(final MouseEvent event) {
    boolean hasMove = false;
    if (this.image != null) {
      final List<MappedLocation> tiePoints = this.image.getTiePoints();
      if (!tiePoints.isEmpty()) {
        final List<Integer> closeSourcePixelIndexes = new ArrayList<>();
        final List<Integer> closeTargetPointIndexes = new ArrayList<>();
        final BoundingBox hotSpot = getHotspotBoundingBox(event);
        int i = 0;

        boolean hasSource = false;
        for (final MappedLocation tiePoint : tiePoints) {
          final Point sourcePoint = this.layer.sourcePixelToTargetPoint(tiePoint);
          if (hotSpot.covers(sourcePoint)) {
            closeSourcePixelIndexes.add(i);
            hasMove = true;
            hasSource = true;
          }

          final Point targetPoint = tiePoint.getTargetPoint();
          if (hotSpot.covers(targetPoint)) {
            closeTargetPointIndexes.add(i);
            hasMove = true;
          }
          i++;
        }
        final boolean changed = !closeSourcePixelIndexes.equals(this.closeSourcePixelIndexes)
          || !closeTargetPointIndexes.equals(this.closeTargetPointIndexes);
        this.closeSourcePixelIndexes = closeSourcePixelIndexes;
        this.closeTargetPointIndexes = closeTargetPointIndexes;
        if (changed && hasMove) {
          this.moveTiePointIndex = 0;
          this.moveTiePointEventPoint = event.getPoint();
        }
        final boolean tooltipSet = setMoveTiePointToolTip();
        if (changed && hasMove) {
          if (tooltipSet) {
            if (hasSource) {
              setOverlayAction(ACTION_TIE_POINT_MOVE_SOURCE);
            } else {
              setOverlayAction(ACTION_TIE_POINT_MOVE_TARGET);
            }
            this.moveTiePointSource = hasSource;
          }
        }
      }
    }
    if (hasMove) {
      return true;
    } else {
      moveTiePointClear();
      return false;
    }
  }

  private boolean moveTiePointStart(final MouseEvent event) {
    if (this.moveTiePointIndex > -1) {
      if (SwingUtilities.isLeftMouseButton(event)) {
        String action;
        if (this.moveTiePointSource) {
          action = ACTION_TIE_POINT_MOVE_SOURCE;
        } else {
          action = ACTION_TIE_POINT_MOVE_TARGET;
        }
        if (setOverlayAction(action)) {
          this.moveTiePointStarted = true;
          getMap().clearToolTipText();
          event.consume();
          repaint();
          return true;
        }
      }
    }
    return false;
  }

  @Override
  protected void paintComponent(final Graphics2D graphics) {
    final GeoreferencedImageLayer layer = this.layer;
    final GeoreferencedImage image = this.image;
    final boolean moveTiePointStarted = this.moveTiePointStarted;
    final Point moveTiePointLocation = this.moveTiePointLocation;
    final BoundingBox moveImageBoundingBox = this.moveImageBoundingBox;
    final boolean moveTiePointSource = this.moveTiePointSource;
    if (layer != null && layer.isVisible() && layer.isExists() && image != null) {
      final Viewport2D viewport = getViewport();
      final boolean showOriginalImage = layer.isShowOriginalImage();
      final BoundingBox imageBoundingBox = getImageBoundingBox();
      BoundingBox boundingBox = imageBoundingBox;
      BoundingBox outlineBoundingBox = boundingBox;
      if (moveImageBoundingBox != null) {
        if (showOriginalImage) {
          boundingBox = moveImageBoundingBox;
        }
        outlineBoundingBox = moveImageBoundingBox;
      }
      super.paintComponent(graphics);

      final GeoreferencedImage cachedImage = getCachedImage(boundingBox);
      GeoreferencedImageLayerRenderer.renderAlpha(graphics, viewport, cachedImage,
        layer.getOpacity() / 255.0, false);
      GeoreferencedImageLayerRenderer.renderDifferentCoordinateSystem(viewport, imageBoundingBox,
        graphics);

      if (outlineBoundingBox != null && !outlineBoundingBox.isEmpty()) {
        final Polygon imageBoundary = outlineBoundingBox.convert(getViewportGeometryFactory())
          .toPolygon(1);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_OFF);

        GeometryStyleRenderer.renderOutline(viewport, graphics, imageBoundary,
          GeometryStyle.line(Color.GREEN, 3));

        MarkerStyleRenderer.renderMarkerVertices(viewport, graphics, imageBoundary,
          MarkerStyle.marker("cross", 11, WebColors.Black, 1, WebColors.Lime));

        final int tiePointCount = image.getTiePoints().size();
        final GeometryFactory viewGeometryFactory = getGeometryFactory();
        if (tiePointCount > 0) {
          final MappedLocation moveTiePoint = getMoveTiePoint();
          for (int i = 0; i < tiePointCount; i++) {
            final MappedLocation mappedLocation = image.getTiePoints().get(i);
            if (!moveTiePointStarted || mappedLocation != moveTiePoint) {
              final LineString line = mappedLocation.getSourceToTargetLine(image, boundingBox,
                !showOriginalImage);
              renderTiePointLine(graphics, viewport, line);
            }
          }
          if (moveTiePointStarted && moveTiePoint != null && moveTiePointLocation != null) {
            Point sourcePoint = null;
            Point targetPoint = null;
            final GeometryFactory imageGeometryFactory = getImageGeometryFactory();

            if (moveTiePointSource) {
              sourcePoint = moveTiePointLocation.convert(imageGeometryFactory, 2);
              targetPoint = moveTiePoint.getTargetPoint();
            } else {
              sourcePoint = moveTiePoint.getSourcePoint(image, boundingBox, !showOriginalImage);
              targetPoint = moveTiePointLocation.convert(imageGeometryFactory, 2);
            }
            if (sourcePoint != null && targetPoint != null) {
              final LineString line = imageGeometryFactory.lineString(sourcePoint, targetPoint);
              renderTiePointLine(graphics, viewport, line);
            }
          }

        }
        if (!showOriginalImage) {

          final double width = image.getImageWidth() - 1;
          final double height = image.getImageHeight() - 1;
          final double[] targetCoordinates = MappedLocation.toModelCoordinates(image, boundingBox,
            true, 0, height, width, height, width, 0, 0, 0, 0, height);
          final LineString line = viewGeometryFactory.lineString(2, targetCoordinates);
          GeometryStyleRenderer.renderLineString(viewport, graphics, line, STYLE_IMAGE_LINE);
        }
        if (this.addTiePointFirstPoint != null && this.addTiePointMove != null) {
          final LineString line = viewGeometryFactory.lineString(this.addTiePointFirstPoint,
            this.addTiePointMove);
          renderTiePointLine(graphics, viewport, line);
        }
      }
    }
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    super.propertyChange(event);
    final Object source = event.getSource();
    final String propertyName = event.getPropertyName();
    if (source instanceof GeoreferencedImageLayer) {
      final GeoreferencedImageLayer layer = (GeoreferencedImageLayer)source;
      if ("editable".equals(propertyName)) {
        if (!BooleanStringConverter.getBoolean(event.getNewValue())) {
          if (this.layer == layer) {
            setLayer(null);
          }
        } else {
          setLayer(layer);
        }
      } else if (this.layer == layer) {
        clearCachedImage();
        if ("boundingBox".equals(propertyName)) {
          final BoundingBox boundingBox = layer.getBoundingBox();
          setImageBoundingBox(boundingBox);
        } else if ("hasChanges".equals(propertyName)) {
          clear();
          setLayer(layer);
        } else if ("deleted".equals(propertyName)) {
          clear();
        }
      }
    } else if (source == this.image) {
      clearCachedImage();
    } else if ("scale".equals(propertyName)) {
      clearCachedImage();
    }
  }

  protected void renderTiePointLine(final Graphics2D graphics, final Viewport2D viewport,
    LineString line) {
    if (line != null) {
      GeometryStyleRenderer.renderLineString(viewport, graphics, line, STYLE_MAPPED_LINE);
      line = line.convert(viewport.getGeometryFactory());
      MarkerStyleRenderer.renderMarkers(viewport, graphics, line, STYLE_VERTEX_FIRST_POINT,
        STYLE_VERTEX_LAST_POINT, null);
    }
  }

  public void setImageBoundingBox(BoundingBox boundingBox) {
    if (boundingBox == null) {
      boundingBox = new BoundingBoxDoubleGf(getGeometryFactory());
    }
    if (this.image != null) {
      this.image.setBoundingBox(boundingBox);
    }
    setGeometryFactory(boundingBox.getGeometryFactory());
    clearCachedImage();
  }

  public void setLayer(final GeoreferencedImageLayer layer) {
    final GeoreferencedImageLayer oldLayer = this.layer;
    if (oldLayer != layer) {
      clear();
      this.layer = layer;
      final Viewport2D viewport = getViewport();
      setGeometryFactory(viewport.getGeometryFactory());
      setEnabled(layer != null);
      if (layer != null) {
        this.image = layer.getImage();
        setImageBoundingBox(layer.getBoundingBox());
      }
      if (oldLayer != null) {
        oldLayer.setEditable(false);
      }
    }
    firePropertyChange("layer", oldLayer, layer);
  }

  protected boolean setMoveTiePointToolTip() {
    this.moveTiePointStarted = false;
    if (!this.closeSourcePixelIndexes.isEmpty() || !this.closeTargetPointIndexes.isEmpty()) {
      final List<MappedLocation> tiePoints = this.image.getTiePoints();
      final StringBuilder toolTip = new StringBuilder();
      toolTip.append("<html>");

      appendTiePointLocation(toolTip, tiePoints, this.closeTargetPointIndexes, 1, false);
      appendTiePointLocation(toolTip, tiePoints, this.closeSourcePixelIndexes,
        this.closeTargetPointIndexes.size() + 1, true);
      toolTip.append("</html>");
      getMap().setToolTipText(this.moveTiePointEventPoint, toolTip);
      return true;
    }
    return false;
  }

}
