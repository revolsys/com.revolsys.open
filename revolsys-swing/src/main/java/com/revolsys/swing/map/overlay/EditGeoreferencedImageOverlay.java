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

import org.jeometry.common.awt.WebColors;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.raster.BufferedGeoreferencedImage;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.raster.MappedLocation;
import com.revolsys.swing.Icons;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.map.ImageViewport;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.raster.GeoreferencedImageLayer;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.swing.map.layer.record.style.marker.MarkerRenderer;
import com.revolsys.swing.map.overlay.record.SelectRecordsOverlay;
import com.revolsys.swing.map.overlay.record.SelectedRecordsRenderer;
import com.revolsys.swing.map.overlay.record.SelectedRecordsVertexRenderer;
import com.revolsys.swing.map.view.ViewRenderer;
import com.revolsys.swing.map.view.graphics.Graphics2DViewRenderer;
import com.revolsys.swing.undo.ListAddUndo;
import com.revolsys.swing.undo.SetObjectProperty;
import com.revolsys.util.Booleans;

public class EditGeoreferencedImageOverlay extends AbstractOverlay {
  public static final String ACTION_MOVE_IMAGE = "moveImage";

  private static final String ACTION_MOVE_IMAGE_CORNER = "moveImageCorner";

  private static final String ACTION_TIE_POINT_ADD = "Add Tie Point";

  private static final String ACTION_TIE_POINT_MOVE_SOURCE = "moveSourceTiePoint";

  private static final String ACTION_TIE_POINT_MOVE_TARGET = "moveTargetTiePoint";

  private static final Color COLOR_OUTLINE = WebColors.Black;

  private static final Color COLOR_SELECT = WebColors.Cyan;

  private static final Cursor CURSOR_MOVE_IMAGE = Icons.getCursor("cursor_move", 8, 7);

  private static final Cursor CURSOR_SOURCE_PIXEL_ADD = Icons.getCursor("cursor_source_pixel_add",
    5, 5);

  private static final long serialVersionUID = 1L;

  private static final MarkerStyle STYLE_BOX_CORNER = MarkerStyle.marker("cross", 11,
    WebColors.Black, 1, WebColors.Lime);

  private static final GeometryStyle STYLE_BOX_OUTLINE = GeometryStyle.line(Color.GREEN, 3);

  private static final MarkerStyle STYLE_VERTEX_FIRST_POINT = MarkerStyle.marker("ellipse", 9,
    COLOR_OUTLINE, 1, COLOR_SELECT);

  private static final MarkerStyle STYLE_VERTEX_LAST_POINT = MarkerStyle
    .marker(SelectedRecordsVertexRenderer.LAST_VERTEX_SHAPE, 9, COLOR_OUTLINE, 1, COLOR_SELECT);

  static {
    STYLE_VERTEX_FIRST_POINT.setMarkerOrientationType("auto");
    STYLE_VERTEX_FIRST_POINT.setMarkerPlacementType("vertex(0)");
    STYLE_VERTEX_FIRST_POINT.setMarkerHorizontalAlignment("center");

    STYLE_VERTEX_LAST_POINT.setMarkerOrientationType("auto");
    STYLE_VERTEX_LAST_POINT.setMarkerPlacementType("vertex(n)");
    STYLE_VERTEX_LAST_POINT.setMarkerHorizontalAlignment("right");
  }

  private static final VertexStyleRenderer TIE_POINT_CLOSE_VERTEX_RENDERER = new VertexStyleRenderer(
    WebColors.RoyalBlue);

  private static final SelectedRecordsRenderer TIE_POINT_RENDERER = new SelectedRecordsRenderer(
    WebColors.Aqua, 127);

  private static final SelectedRecordsVertexRenderer TIE_POINT_VERTEX_RENDERER = new SelectedRecordsVertexRenderer(
    WebColors.Aqua, true);

  private Point addTiePointFirstPoint;

  private Point addTiePointMove;

  private GeoreferencedImage cachedImage;

  private List<Integer> closeSourcePixelIndexes = new ArrayList<>();

  private List<Integer> closeTargetPointIndexes = new ArrayList<>();

  private GeoreferencedImage image;

  private GeoreferencedImageLayer layer;

  private Point moveCornerOppositePoint;

  private Point moveCornerPoint;

  private BoundingBox moveImageBoundingBox;

  private Point moveImageFirstPoint;

  private java.awt.Point moveTiePointEventPoint;

  private int moveTiePointIndex = -1;

  private Point moveTiePointOpposite;

  private Point moveTiePointLocation;

  private boolean moveTiePointSource;

  private boolean moveTiePointStarted;

  public EditGeoreferencedImageOverlay(final MapPanel map) {
    super(map);
    addOverlayActionOverride( //
      SelectRecordsOverlay.ACTION_SELECT_RECORDS, //
      ACTION_MOVE_IMAGE, //
      ACTION_MOVE_IMAGE_CORNER);

    addOverlayAction( //
      ACTION_MOVE_IMAGE, //
      CURSOR_MOVE_IMAGE, //
      ZoomOverlay.ACTION_PAN, //
      ZoomOverlay.ACTION_ZOOM, //
      ACTION_MOVE_IMAGE_CORNER //
    );

    addOverlayActionOverride( //
      ACTION_MOVE_IMAGE_CORNER, //
      ZoomOverlay.ACTION_PAN, //
      ZoomOverlay.ACTION_ZOOM, //
      ACTION_TIE_POINT_ADD //
    );

    addOverlayAction( //
      ACTION_TIE_POINT_ADD, //
      CURSOR_NODE_ADD, //
      ZoomOverlay.ACTION_PAN, //
      ZoomOverlay.ACTION_ZOOM //
    );

    addOverlayAction( //
      ACTION_TIE_POINT_MOVE_SOURCE, //
      CURSOR_NODE_ADD, //
      ZoomOverlay.ACTION_PAN, //
      ZoomOverlay.ACTION_ZOOM, ACTION_MOVE_IMAGE //
    );

    addOverlayAction( //
      ACTION_TIE_POINT_MOVE_TARGET, //
      CURSOR_NODE_ADD, //
      ZoomOverlay.ACTION_PAN, //
      ZoomOverlay.ACTION_ZOOM, //
      ACTION_MOVE_IMAGE //
    );
  }

  protected void addTiePointClear() {
    this.addTiePointFirstPoint = null;
    this.addTiePointMove = null;
    clearCachedImage();
    clearSnapLocations();
    setXorGeometry(null);
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
            final Point targetPoint = mapPoint.newGeometry(geometryFactory);
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
      if (hasSnapPoint()) {
        this.addTiePointMove = getSnapPoint();
      } else {
        this.addTiePointMove = getEventPoint();
      }
      final GeometryFactory geometryFactory = getGeometryFactory();
      final Geometry xorGeometry = newXorLine(geometryFactory, this.addTiePointFirstPoint,
        this.addTiePointMove);
      setXorGeometry(xorGeometry);
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
      this.moveImageBoundingBox = viewportGeometryFactory.newBoundingBox(minX, minY, maxX, maxY);
    }
  }

  protected void appendTiePointLocations(final StringBuilder toolTip,
    final List<MappedLocation> tiePoints, final List<Integer> indices, final int startNumber,
    final boolean source) {
    if (!indices.isEmpty()) {
      int i = startNumber - 1;

      for (final Integer index : indices) {
        final MappedLocation tiePoint = tiePoints.get(index);
        toolTip.append("<div style=\"border-top: 1px solid #666666;padding: 1px;");
        if (i == this.moveTiePointIndex) {
          toolTip.append("background-color: #0000ff;color: #ffffff");
        } else {
          toolTip.append("background-color: #ffffff");
        }
        toolTip.append("\">");
        toolTip.append(i);
        toolTip.append(". ");
        final Point point;
        if (source) {
          point = tiePoint.getSourcePixel();
          toolTip.append("Source: ");
        } else {
          point = tiePoint.getTargetPoint();
          toolTip.append("Target: ");
        }
        appendPoint(toolTip, point);
        if (!source) {
          toolTip.append(" (");
          toolTip.append(point.getHorizontalCoordinateSystemId());
          toolTip.append(")");
        }
        toolTip.append("</div>");
        i++;
      }
      i++;
    }
  }

  @Override
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
  }

  @Override
  public void focusLost(final FocusEvent e) {
    if (isEditing()) {
      cancel();
    }
  }

  private GeoreferencedImage getCachedImage(BoundingBox boundingBox) {
    boundingBox = boundingBox.bboxToCs(getViewportGeometryFactory());
    final Viewport2D viewport = getViewport();
    final BoundingBox viewBoundingBox = viewport.getBoundingBox();
    if (this.cachedImage == null || !this.cachedImage.getBoundingBox().equals(viewBoundingBox)) {
      try (
        final ImageViewport imageViewport = new ImageViewport(viewport)) {
        final ViewRenderer viewRenderer = imageViewport.newViewRenderer();

        final BufferedImage image = imageViewport.getImage();
        // final Graphics2D graphics = (Graphics2D)image.getGraphics();

        // this.image.drawImage(graphics, viewBoundingBox,
        // (int)Math.ceil(viewport.getViewWidthPixels()),
        // (int)Math.ceil(viewport.getViewHeightPixels()),
        // !this.layer.isShowOriginalImage(),
        // RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        viewRenderer.drawImage(this.image, !this.layer.isShowOriginalImage());
        this.cachedImage = new BufferedGeoreferencedImage(imageViewport.getBoundingBox(), image);
      }
    }
    return this.cachedImage;
  }

  public BoundingBox getImageBoundingBox() {
    if (this.image == null) {
      return BoundingBox.empty();
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
    final Point imagePoint = mousePoint.convertGeometry(imageGeometryFactory);

    final double deltaX = imagePoint.getX() - this.moveImageFirstPoint.getX();
    final double deltaY = imagePoint.getY() - this.moveImageFirstPoint.getY();
    boundingBox = boundingBox //
      .bboxEdit(editor -> editor.move(deltaX, deltaY));
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

    return imageBoundingBox.bboxDistance(point) < distance * 2;
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
      final boolean inImage = imageBoundingBox.bboxCovers(mousePoint);
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
        final int index = keyChar - '0';
        if (!this.moveTiePointStarted && this.moveTiePointIndex > -1) {
          if (index >= 0
            && index < this.closeSourcePixelIndexes.size() + this.closeTargetPointIndexes.size()) {
            this.moveTiePointIndex = index;
            setMoveTiePointToolTip();
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
      this.moveImageBoundingBox = viewportGeometryFactory.newBoundingBox(mousePoint.getX(),
        mousePoint.getY(), this.moveCornerOppositePoint.getX(),
        this.moveCornerOppositePoint.getY());
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
          if (this.moveImageBoundingBox != null) {
            final SetObjectProperty setBBox = new SetObjectProperty(this, "imageBoundingBox",
              getImageBoundingBox(), this.moveImageBoundingBox);
            addUndo(setBBox);
            event.consume();
          }
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
          final double distance = oldPoint.distancePoint(mousePoint);
          if (distance < maxDistance) {
            closestPoint = oldPoint;
            closestDistance = distance;
          }
        }
        int closestIndex = -1;
        BoundingBox imageBoundingBox = getImageBoundingBox();
        if (!imageBoundingBox.isEmpty()) {
          imageBoundingBox = imageBoundingBox.bboxToCs(viewportGeometryFactory);
          for (int i = 0; i < 4; i++) {
            final Point point = imageBoundingBox.getCornerPoint(i);
            final Point mapPoint = point.convertPoint2d(viewportGeometryFactory);
            final double distance = mapPoint.distancePoint(mousePoint);
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
                .convertGeometry(viewportGeometryFactory);
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
          final Point imagePoint = mousePoint.convertGeometry(imageGeometryFactory);
          this.moveImageFirstPoint = imagePoint;
          event.consume();
          return true;
        }
      }
    }
    return false;
  }

  private void moveTiePointClear() {
    this.closeSourcePixelIndexes.clear();
    this.closeTargetPointIndexes.clear();
    this.moveTiePointEventPoint = null;
    this.moveTiePointIndex = -1;
    this.moveTiePointLocation = null;
    this.moveTiePointStarted = false;
    setXorGeometry(null);
    clearOverlayAction(ACTION_TIE_POINT_MOVE_SOURCE);
    clearOverlayAction(ACTION_TIE_POINT_MOVE_TARGET);
  }

  private boolean moveTiePointDrag(final MouseEvent event) {
    if (SwingUtil.isLeftButtonOnly(event)) {
      if (this.moveTiePointStarted) {
        if (this.moveTiePointSource) {
          this.moveTiePointLocation = getEventPoint();
        } else {
          if (hasSnapPoint()) {
            this.moveTiePointLocation = getSnapPoint();
          } else {
            this.moveTiePointLocation = getEventPoint();
          }
        }
        event.consume();

        final GeometryFactory geometryFactory = getGeometryFactory();
        final Geometry xorGeometry = newXorLine(geometryFactory, this.moveTiePointOpposite,
          this.moveTiePointLocation);
        setXorGeometry(xorGeometry);

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
            point = point.newGeometry(imageGeometryFactory);
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
        final BoundingBox hotSpot = getHotspotBoundingBox();
        int i = 0;

        boolean hasSource = false;
        for (final MappedLocation tiePoint : tiePoints) {
          final Point sourcePoint = this.layer.sourcePixelToTargetPoint(tiePoint);
          if (hotSpot.bboxCovers(sourcePoint)) {
            closeSourcePixelIndexes.add(i);
            hasMove = true;
            hasSource = true;
          }

          final Point targetPoint = tiePoint.getTargetPoint();
          if (hotSpot.bboxCovers(targetPoint)) {
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
        final MappedLocation tiePoint = getMoveTiePoint();
        String action;
        if (this.moveTiePointSource) {
          action = ACTION_TIE_POINT_MOVE_SOURCE;
          this.moveTiePointOpposite = tiePoint.getTargetPoint();
        } else {
          action = ACTION_TIE_POINT_MOVE_TARGET;
          final GeoreferencedImage image = this.image;
          final boolean showOriginalImage = this.layer.isShowOriginalImage();
          final BoundingBox boundingBox = getImageBoundingBox();
          this.moveTiePointOpposite = tiePoint.getSourcePoint(image, boundingBox,
            !showOriginalImage);
        }
        if (setOverlayAction(action)) {
          this.moveTiePointStarted = true;
          setMapCursor(CURSOR_NODE_ADD);
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
  protected void paintComponent(final Graphics2DViewRenderer view, final Graphics2D graphics) {
    super.paintComponent(view, graphics);
    final GeoreferencedImageLayer layer = this.layer;
    final GeoreferencedImage image = this.image;
    final BoundingBox moveImageBoundingBox = this.moveImageBoundingBox;
    if (layer != null && layer.isVisible() && layer.isExists() && image != null) {
      final GeometryFactory viewportGeometryFactory = view.getGeometryFactory();

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

      final GeoreferencedImage cachedImage = getCachedImage(boundingBox);
      view.drawImage(cachedImage, false, layer.getOpacity() / 255.0,
        RenderingHints.VALUE_INTERPOLATION_BILINEAR);

      view.drawDifferentCoordinateSystem(imageBoundingBox);

      if (outlineBoundingBox != null && !outlineBoundingBox.isEmpty()) {
        final Polygon imageBoundary = outlineBoundingBox.bboxToCs(getViewportGeometryFactory())
          .toPolygon(1);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_OFF);

        view.drawGeometryOutline(STYLE_BOX_OUTLINE, imageBoundary);

        try (
          MarkerRenderer markerRenderer = STYLE_BOX_CORNER.newMarkerRenderer(view)) {
          markerRenderer.renderMarkerVertices(imageBoundary);
        }

        final List<MappedLocation> tiePoints = image.getTiePoints();
        final int tiePointCount = tiePoints.size();
        final GeometryFactory viewGeometryFactory = getGeometryFactory();
        if (tiePointCount > 0) {
          for (final MappedLocation mappedLocation : tiePoints) {
            LineString line = mappedLocation.getSourceToTargetLine(image, boundingBox,
              !showOriginalImage);
            if (line != null) {
              line = line.convertGeometry(viewportGeometryFactory);
              TIE_POINT_RENDERER.paintSelected(view, viewportGeometryFactory, line);
            }
          }
          for (final MappedLocation mappedLocation : tiePoints) {
            LineString line = mappedLocation.getSourceToTargetLine(image, boundingBox,
              !showOriginalImage);
            if (line != null) {
              line = line.convertGeometry(viewportGeometryFactory);
              TIE_POINT_VERTEX_RENDERER.paintSelected(view, line);
            }
          }
        }
        if (!showOriginalImage) {
          final double width = image.getImageWidth() - 1;
          final double height = image.getImageHeight() - 1;
          final double[] targetCoordinates = MappedLocation.toModelCoordinates(image, boundingBox,
            true, 0, height, width, height, width, 0, 0, 0, 0, height);
          final LineString line = viewGeometryFactory.lineString(2, targetCoordinates);
          view.drawGeometry(line, STYLE_BOX_OUTLINE);
        }
      }
      final MappedLocation moveTiePoint = getMoveTiePoint();
      if (moveTiePoint != null) {
        final LineString line = moveTiePoint.getSourceToTargetLine(image, boundingBox,
          !showOriginalImage);
        Vertex vertex;
        if (this.moveTiePointSource) {
          vertex = line.getVertex(0);
        } else {
          vertex = line.getToVertex(0);
        }
        TIE_POINT_CLOSE_VERTEX_RENDERER.paintSelected(view, graphics, viewportGeometryFactory,
          vertex);
      }
    }
    drawXorGeometry(graphics);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    super.propertyChange(event);
    final Object source = event.getSource();
    final String propertyName = event.getPropertyName();
    if (source instanceof GeoreferencedImageLayer) {
      final GeoreferencedImageLayer layer = (GeoreferencedImageLayer)source;
      if ("editable".equals(propertyName)) {
        if (!Booleans.getBoolean(event.getNewValue())) {
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

  public void setImageBoundingBox(BoundingBox boundingBox) {
    if (boundingBox == null) {
      final GeometryFactory viewportGeometryFactory = getViewportGeometryFactory();
      boundingBox = viewportGeometryFactory.bboxEmpty();
    }
    setGeometryFactory(boundingBox.getGeometryFactory());
    if (this.image != null) {
      this.image.setBoundingBox(boundingBox);
    }
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

      appendTiePointLocations(toolTip, tiePoints, this.closeTargetPointIndexes, 1, false);
      appendTiePointLocations(toolTip, tiePoints, this.closeSourcePixelIndexes,
        this.closeTargetPointIndexes.size() + 1, true);
      toolTip.append("</html>");
      getMap().setToolTipText(this.moveTiePointEventPoint, toolTip);
      return true;
    }
    return false;
  }

}
