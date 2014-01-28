package com.revolsys.swing.map.overlay;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.slf4j.LoggerFactory;

import com.revolsys.awt.WebColors;
import com.revolsys.converter.string.BooleanStringConverter;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.dataobject.renderer.GeometryStyleRenderer;
import com.revolsys.swing.map.layer.dataobject.renderer.MarkerStyleRenderer;
import com.revolsys.swing.map.layer.dataobject.style.GeometryStyle;
import com.revolsys.swing.map.layer.dataobject.style.MarkerStyle;
import com.revolsys.swing.map.layer.raster.GeoReferencedImage;
import com.revolsys.swing.map.layer.raster.GeoReferencedImageLayer;
import com.revolsys.swing.map.layer.raster.GeoReferencedImageLayerRenderer;
import com.revolsys.swing.map.layer.raster.filter.WarpFilter;
import com.revolsys.swing.undo.ListAddUndo;
import com.revolsys.swing.undo.SetObjectProperty;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class EditGeoReferencedImageOverlay extends AbstractOverlay {
  private static final Color COLOR_OUTLINE = WebColors.Black;

  private static final Color COLOR_SELECT = WebColors.Cyan;

  private static final long serialVersionUID = 1L;

  private static final GeometryStyle STYLE_MAPPED_LINE = GeometryStyle.line(
    COLOR_SELECT, 3);

  private static final GeometryStyle STYLE_IMAGE_LINE = GeometryStyle.line(
    COLOR_SELECT, 1);

  private static final MarkerStyle STYLE_VERTEX_FIRST_POINT = MarkerStyle.marker(
    SelectedRecordsRenderer.firstVertexShape(), 9, COLOR_OUTLINE, 1,
    COLOR_SELECT);

  private static final MarkerStyle STYLE_VERTEX_LAST_POINT = MarkerStyle.marker(
    SelectedRecordsRenderer.lastVertexShape(), 9, COLOR_OUTLINE, 1,
    COLOR_SELECT);

  static {
    STYLE_VERTEX_FIRST_POINT.setMarkerOrientationType("auto");
    STYLE_VERTEX_FIRST_POINT.setMarkerPlacement("point(0)");
    STYLE_VERTEX_FIRST_POINT.setMarkerHorizontalAlignment("center");

    STYLE_VERTEX_LAST_POINT.setMarkerOrientationType("auto");
    STYLE_VERTEX_LAST_POINT.setMarkerPlacement("point(n)");
    STYLE_VERTEX_LAST_POINT.setMarkerHorizontalAlignment("right");
  }

  private Point addTiePointFirstPoint;

  private GeoReferencedImage image;

  private GeoReferencedImageLayer layer;

  private Coordinates moveCornerOppositePoint;

  private Point moveCornerPoint;

  private Point moveImageFirstPoint;

  private BoundingBox moveImageBoundingBox;

  private Point addTiePointMove;

  private BufferedImage cachedImage;

  private int moveTiePointIndex = -1;

  private java.awt.Point moveTiePointEventPoint;

  private final List<Integer> closeSourcePixelIndexes = new ArrayList<Integer>();

  private final List<Integer> closeTargetPointIndexes = new ArrayList<Integer>();

  private Cursor moveCornerCursor;

  private boolean moveTiePointStarted;

  private boolean moveTiePointSource;

  private Point moveTiePointLocation;

  public EditGeoReferencedImageOverlay(final MapPanel map) {
    super(map);
  }

  private boolean addTiePointFinish(final MouseEvent event) {
    if (this.addTiePointFirstPoint != null) {
      try {
        Point mapPoint = getViewportPoint(event);
        final Point snapPoint = getSnapPoint();
        if (snapPoint != null) {
          mapPoint = snapPoint;
        }
        final Coordinates sourcePoint = CoordinatesUtil.get(this.addTiePointFirstPoint);
        final WarpFilter warpFilter = layer.getWarpFilter();
        final Coordinates sourcePixel = warpFilter.targetPointToSourcePixel(sourcePoint);
        final MappedLocation mappedLocation = new MappedLocation(sourcePixel,
          mapPoint);
        addUndo(new ListAddUndo(image.getTiePoints(), mappedLocation));
      } finally {
        this.addTiePointFirstPoint = null;
        this.addTiePointMove = null;
        clearMapCursor();
        clearCachedImage();
        clearMouseOverGeometry();
      }
      return true;
    }
    return false;
  }

  private boolean addTiePointMove(final MouseEvent event) {
    if (this.addTiePointFirstPoint != null) {
      final BoundingBox boundingBox = getHotspotBoundingBox(event);
      hasSnapPoint(event, boundingBox);

      if (getSnapPoint() == null) {
        addTiePointMove = getViewportPoint(event);
      } else {
        addTiePointMove = getSnapPoint();
      }
      repaint();
      event.consume();
      return true;
    }
    return false;
  }

  private boolean addTiePointStart(final MouseEvent event) {
    if (this.layer != null) {
      if (addTiePointFinish(event)) {
      } else if (SwingUtilities.isLeftMouseButton(event) && event.isAltDown()) {
        final Point mousePoint = getViewportPoint(event);
        if (getImageBoundingBox().contains(mousePoint)) {
          this.addTiePointFirstPoint = mousePoint;
          event.consume();
          return true;
        }
      }
    }
    return false;
  }

  protected void adjustBoundingBoxAspectRatio() {
    if (moveImageBoundingBox != null && moveCornerPoint != null) {
      final double imageAspectRatio = this.image.getImageAspectRatio();
      final BoundingBox boundingBox = moveImageBoundingBox;
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
      final GeometryFactory geometryFactory = getGeometryFactory();
      moveImageBoundingBox = new BoundingBox(geometryFactory, minX, minY, maxX,
        maxY);
    }
  }

  protected void appendTiePointLocation(final StringBuffer toolTip,
    final List<MappedLocation> tiePoints, final List<Integer> indices,
    final int startNumber, final boolean source) {
    if (!indices.isEmpty()) {
      int i = startNumber - 1;
      toolTip.append("<div style=\"border-bottom: solid black 1px; font-weight:bold;padding: 1px 3px 1px 3px\">");
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
        final Object value;
        if (source) {
          value = tiePoint.getSourcePixel();
        } else {
          value = tiePoint.getTargetPoint();
        }
        toolTip.append("<li");
        if (i == moveTiePointIndex) {
          toolTip.append(" style=\"border: 1px solid red; padding: 2px; background-color:#FFC0CB\"");
        }
        toolTip.append(">#");
        toolTip.append(index + 1);
        toolTip.append(" ");
        toolTip.append(value);
        toolTip.append("</li>");
      }
      toolTip.append("</ol></div>");
      i++;
    }
  }

  protected void cancel() {
    this.addTiePointFirstPoint = null;
    this.addTiePointMove = null;
    clearCachedImage();
    closeSourcePixelIndexes.clear();
    closeTargetPointIndexes.clear();
    this.moveCornerOppositePoint = null;
    this.moveCornerCursor = null;
    this.moveCornerOppositePoint = null;
    this.moveCornerPoint = null;
    this.moveImageFirstPoint = null;
    this.moveImageBoundingBox = null;
    this.moveImageFirstPoint = null;
    this.moveTiePointEventPoint = null;
    this.moveTiePointLocation = null;
  }

  protected void clear() {
    this.image = null;
    this.layer = null;
    clearUndoHistory();
    cancel();
  }

  protected void clearCachedImage() {
    if (this.cachedImage != null) {
      this.cachedImage.flush();
    }
    this.cachedImage = null;
    System.gc();
  }

  protected BufferedImage getCachedImage(final BoundingBox boundingBox) {
    final Viewport2D viewport = getViewport();
    if (cachedImage == null) {
      BufferedImage originalImage;
      if (layer.isShowOriginalImage()) {
        originalImage = this.image.getOriginalImage();
      } else {
        originalImage = this.image.getWarpedImage();
      }
      final int newWidth = Math.min(
        originalImage.getWidth(),
        (int)Math.ceil(Viewport2D.toDisplayValue(viewport,
          boundingBox.getWidthLength())));
      final int newHeight = Math.min(
        originalImage.getHeight(),
        (int)Math.ceil(Viewport2D.toDisplayValue(viewport,
          boundingBox.getHeightLength())));

      final BufferedImage newImage = new BufferedImage(newWidth, newHeight,
        BufferedImage.TYPE_INT_ARGB);
      final Graphics2D imageGraphics = (Graphics2D)newImage.getGraphics();
      imageGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      final Composite composite = AlphaComposite.getInstance(
        AlphaComposite.SRC, .6f);
      imageGraphics.setComposite(composite);
      imageGraphics.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
      imageGraphics.dispose();
      this.cachedImage = newImage;
    }
    return cachedImage;
  }

  public BoundingBox getImageBoundingBox() {
    if (image == null) {
      return new BoundingBox();
    } else {
      return this.layer.getBoundingBox();
    }
  }

  public GeoReferencedImageLayer getLayer() {
    return this.layer;
  }

  protected BoundingBox getMoveBoundingBox(final MouseEvent event) {
    BoundingBox boundingBox = getImageBoundingBox();
    final Point eventPoint = getPoint(event);
    final double deltaX = eventPoint.getX() - moveImageFirstPoint.getX();
    final double deltaY = eventPoint.getY() - moveImageFirstPoint.getY();
    boundingBox = boundingBox.move(deltaX, deltaY);
    return boundingBox;
  }

  private MappedLocation getMoveTiePoint() {
    if (moveTiePointIndex > -1) {
      int tiePointIndex;
      final int targetSize = closeTargetPointIndexes.size();
      if (moveTiePointIndex < targetSize) {
        tiePointIndex = closeTargetPointIndexes.get(moveTiePointIndex);
        moveTiePointSource = false;
      } else if (moveTiePointIndex - targetSize < closeSourcePixelIndexes.size()) {
        tiePointIndex = closeSourcePixelIndexes.get(moveTiePointIndex
          - targetSize);
        moveTiePointSource = true;
      } else {
        return null;
      }
      return image.getTiePoints().get(tiePointIndex);
    }
    return null;
  }

  protected boolean isApplicable(final MouseEvent event) {
    final BoundingBox imageBoundingBox = getImageBoundingBox();
    final Point point = getPoint(event);
    final double distance = getDistance(event);

    return imageBoundingBox.distance(point) < distance * 2;
  }

  @Override
  public void keyPressed(final KeyEvent e) {
    if (this.layer != null) {
      if (this.moveCornerPoint != null) {
        if (e.isShiftDown()) {
          adjustBoundingBoxAspectRatio();
          repaint();
          e.consume();
        }
      }
    }
  }

  @Override
  public void keyReleased(final KeyEvent e) {
    if (this.layer != null) {
      final int keyCode = e.getKeyCode();
      if (moveTiePointIndex > -1 || addTiePointFirstPoint != null
        || moveTiePointStarted) {
        final char keyChar = e.getKeyChar();
        if (keyChar >= '1' && keyChar <= '9') {
          e.consume();
        }
      }
      if (keyCode == KeyEvent.VK_ESCAPE) {
        cancel();
        repaint();
      }
    }
  }

  @Override
  public void keyTyped(final KeyEvent e) {
    final char keyChar = e.getKeyChar();
    if (keyChar >= '1' && keyChar <= '9') {
      final int index = keyChar - '1';
      if (!moveTiePointStarted && moveTiePointIndex > -1) {
        if (index < this.closeSourcePixelIndexes.size()
          + this.closeTargetPointIndexes.size()) {

          this.moveTiePointIndex = index;
          setMoveTiePointToolTip();
        }
        e.consume();
      } else if (index < this.snapPointLocationMap.size()) {
        this.snapPointIndex = index;
        setSnapLocations(snapPointLocationMap);
        if (moveTiePointStarted) {
          if (!moveTiePointSource) {
            moveTiePointFinish(null);
            e.consume();
          }
        } else if (addTiePointFirstPoint != null) {
          addTiePointFinish(null);
          e.consume();
        }
        getMap().repaint();
      }

    }
  }

  @Override
  public void mouseClicked(final MouseEvent event) {
    if (this.layer != null) {
      if (isApplicable(event)) {
        if (addTiePointFinish(event)) {
        } else if (addTiePointStart(event)) {
        }
      }
    }
  }

  @Override
  public void mouseDragged(final MouseEvent event) {
    if (this.layer != null) {
      if (moveTiePointDrag(event)) {
      } else if (moveCornerDrag(event)) {
      } else if (moveImageDrag(event)) {
      }
    }
  }

  @Override
  public void mouseMoved(final MouseEvent event) {
    if (this.layer != null) {
      if (moveImageDrag(event)) {
      } else if (addTiePointMove(event)) {
      } else if (moveTiePointMove(event)) {
      } else if (moveCornerMove(event)) {
      } else if (moveImageMove(event)) {
      } else {
        clearMapCursor();
      }
    }
  }

  @Override
  public void mousePressed(final MouseEvent event) {
    if (this.layer != null) {
      if (isApplicable(event)) {
        if (moveTiePointStart(event)) {
        } else if (SwingUtilities.isLeftMouseButton(event) && event.isAltDown()) {
          event.consume();
        } else if (moveCornerStart(event)) {
        } else if (moveImageStart(event)) {
        }
      }
    }
  }

  @Override
  public void mouseReleased(final MouseEvent event) {
    if (this.layer != null) {
      if (SwingUtilities.isLeftMouseButton(event)) {
        if (moveTiePointFinish(event)) {
        } else if (moveCornerFinish(event)) {
        } else if (moveCornerFinish(event)) {
        } else if (moveImageFinish(event)) {
        }
      }
    }
  }

  private boolean moveCornerDrag(final MouseEvent event) {
    if (this.moveCornerPoint == null) {
      return false;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final Coordinates mousePoint = CoordinatesUtil.get(getViewportPoint(event));
      moveImageBoundingBox = new BoundingBox(geometryFactory, mousePoint,
        this.moveCornerOppositePoint);

      if (event.isShiftDown()) {
        adjustBoundingBoxAspectRatio();
      }
      setMapCursor(moveCornerCursor);
      repaint();
      event.consume();
      return true;
    }
  }

  private boolean moveCornerFinish(final MouseEvent event) {
    if (this.moveCornerPoint != null) {
      try {
        if (event != null) {
          final SetObjectProperty setBBox = new SetObjectProperty(this,
            "imageBoundingBox", getImageBoundingBox(), moveImageBoundingBox);
          addUndo(setBBox);
          event.consume();
        }
      } finally {
        this.moveCornerPoint = null;
        this.moveCornerOppositePoint = null;
        this.moveImageBoundingBox = null;
      }
      clearMapCursor();
      repaint();
      return true;
    }
    return false;
  }

  private boolean moveCornerMove(final MouseEvent event) {
    if (this.layer != null) {
      final Point oldPoint = this.moveCornerPoint;

      final Point mousePoint = getViewportPoint(event);
      final GeometryFactory geometryFactory = getGeometryFactory();

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
      for (int i = 0; i < 4; i++) {
        final Coordinates point = getImageBoundingBox().getCornerPoint(i);
        final double distance = point.distance(CoordinatesUtil.get(mousePoint));
        if (distance < maxDistance && distance < closestDistance) {
          closestPoint = geometryFactory.createPoint(point);
          closestDistance = distance;
          closestIndex = i;
        }
      }

      if (closestPoint != oldPoint) {
        this.moveCornerPoint = closestPoint;
        if (closestIndex == -1) {
          this.moveCornerOppositePoint = null;
        } else {
          this.moveCornerOppositePoint = getImageBoundingBox().getCornerPoint(
            closestIndex + 2);
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
          default:
            moveCornerCursor = null;
          break;
        }
        setMapCursor(moveCornerCursor);
      }
    }
    if (this.moveCornerPoint == null) {
      return false;
    } else {
      event.consume();
      return true;
    }
  }

  private boolean moveCornerStart(final MouseEvent event) {
    if (this.layer != null) {
      if (SwingUtil.isLeftButtonAndNoModifiers(event)) {
        if (this.moveCornerPoint != null) {
          event.consume();
          return true;
        }
      }
    }
    return false;
  }

  private boolean moveImageDrag(final MouseEvent event) {
    if (this.moveImageFirstPoint == null) {
      return false;
    } else {
      setMapCursor(new Cursor(Cursor.HAND_CURSOR));
      moveImageBoundingBox = getMoveBoundingBox(event);
      repaint();
      event.consume();
      return true;
    }
  }

  private boolean moveImageFinish(final MouseEvent event) {
    if (this.moveImageFirstPoint != null) {
      if (event != null) {
        final BoundingBox boundingBox = getMoveBoundingBox(event);
        final SetObjectProperty setBBox = new SetObjectProperty(this,
          "imageBoundingBox", getImageBoundingBox(), boundingBox);
        addUndo(setBBox);
      }
      this.moveImageFirstPoint = null;
      this.moveImageBoundingBox = null;
      clearMapCursor();
      repaint();
      if (event != null) {
        event.consume();
      }
      return true;
    }
    return false;
  }

  private boolean moveImageMove(final MouseEvent event) {
    final Point mousePoint = getViewportPoint(event);
    if (getImageBoundingBox().contains(mousePoint)) {
      event.consume();
      return true;
    }
    return false;
  }

  private boolean moveImageStart(final MouseEvent event) {
    if (this.layer != null) {
      if (SwingUtilities.isLeftMouseButton(event)
        && (event.isControlDown() || event.isMetaDown())) {
        final Point mousePoint = getViewportPoint(event);
        if (getImageBoundingBox().contains(mousePoint)) {
          setMapCursor(new Cursor(Cursor.HAND_CURSOR));
          this.moveImageFirstPoint = mousePoint;
          event.consume();
          return true;
        }
      }
    }
    return false;
  }

  private boolean moveTiePointDrag(final MouseEvent event) {
    if (moveTiePointStarted) {
      if (moveTiePointSource) {
        moveTiePointLocation = getViewportPoint(event);
      } else {
        final BoundingBox boundingBox = getHotspotBoundingBox(event);
        if (hasSnapPoint(event, boundingBox)) {
          moveTiePointLocation = getSnapPoint();
        } else {
          moveTiePointLocation = getViewportPoint(event);
        }
      }
      event.consume();
      repaint();
      return true;
    }
    return false;
  }

  private boolean moveTiePointFinish(final MouseEvent event) {
    if (moveTiePointStarted) {
      final MappedLocation tiePoint = getMoveTiePoint();
      if (tiePoint != null) {
        Point point = getPoint(event);
        if (moveTiePointSource) {
          final Coordinates sourcePoint = CoordinatesUtil.get(point);
          final WarpFilter warpFilter = layer.getWarpFilter();
          final Coordinates sourcePixel = warpFilter.targetPointToSourcePixel(sourcePoint);

          final SetObjectProperty setSourcePixel = new SetObjectProperty(
            tiePoint, "sourcePixel", tiePoint.getSourcePixel(), sourcePixel);
          addUndo(setSourcePixel);
        } else {
          final Point snapPoint = getSnapPoint();
          if (snapPoint != null) {
            point = snapPoint;
          }
          tiePoint.setTargetPoint(point);
          final SetObjectProperty setTargetPoint = new SetObjectProperty(
            tiePoint, "targetPoint", tiePoint.getTargetPoint(), point);
          addUndo(setTargetPoint);
        }
        closeSourcePixelIndexes.clear();
        closeTargetPointIndexes.clear();
        moveTiePointLocation = null;
        moveTiePointStarted = false;
        moveTiePointIndex = -1;
        clearCachedImage();
        clearMapCursor();
        clearMouseOverGeometry();
        if (event != null) {
          event.consume();
        }
        repaint();
        return true;
      }
    }
    return false;
  }

  private boolean moveTiePointMove(final MouseEvent event) {
    if (image != null) {
      final List<MappedLocation> tiePoints = image.getTiePoints();
      if (!tiePoints.isEmpty()) {
        if (!closeSourcePixelIndexes.isEmpty()
          && !closeTargetPointIndexes.isEmpty()) {
          clearMapCursor();
        }
        closeSourcePixelIndexes.clear();
        closeTargetPointIndexes.clear();
        final WarpFilter filter = layer.getWarpFilter();
        final BoundingBox hotSpot = getHotspotBoundingBox(event);
        int i = 0;

        for (final MappedLocation tiePoint : tiePoints) {
          final Point targetPoint = tiePoint.getTargetPoint();
          final Point sourcePoint = filter.sourcePixelToTargetPoint(tiePoint);
          if (hotSpot.contains(sourcePoint)) {
            closeSourcePixelIndexes.add(i);
          }
          if (hotSpot.contains(targetPoint)) {
            closeTargetPointIndexes.add(i);
          }
          i++;
        }
        moveTiePointIndex = 0;
        moveTiePointEventPoint = event.getPoint();
        if (setMoveTiePointToolTip()) {
          setMapCursor(CURSOR_NODE_EDIT);
          event.consume();
          return true;
        } else {
          moveTiePointEventPoint = null;
          moveTiePointIndex = -1;
          getMap().clearToolTipText();
          return false;
        }
      }
    }
    return false;
  }

  // TODO escape and undo for move tie point
  private boolean moveTiePointStart(final MouseEvent event) {
    if (moveTiePointIndex > -1) {
      moveTiePointStarted = true;
      getMap().clearToolTipText();
      event.consume();
      repaint();
      return true;
    } else {
      return false;
    }
  }

  @Override
  protected void paintComponent(final Graphics2D graphics) {
    if (this.layer != null && this.layer.isVisible() && layer.isExists()
      && this.image != null) {
      final boolean showOriginalImage = layer.isShowOriginalImage();
      BoundingBox boundingBox = getImageBoundingBox();
      BoundingBox outlineBoundingBox = boundingBox;
      if (moveImageBoundingBox != null) {
        if (showOriginalImage) {
          boundingBox = moveImageBoundingBox;
        }
        outlineBoundingBox = moveImageBoundingBox;
      }
      final Viewport2D viewport = getViewport();

      final BufferedImage renderImage = getCachedImage(boundingBox);
      try {
        final BoundingBox renderBoundingBox = boundingBox.convert(getViewportGeometryFactory());
        GeoReferencedImageLayerRenderer.render(viewport, graphics, this.image,
          renderImage, renderBoundingBox);
      } catch (final Throwable e) {
        LoggerFactory.getLogger(getClass()).error("Unable to render image", e);
      }

      if (outlineBoundingBox != null && !outlineBoundingBox.isEmpty()) {
        final Polygon imageBoundary = outlineBoundingBox.toPolygon(1);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_OFF);

        GeometryStyleRenderer.renderOutline(viewport, graphics, imageBoundary,
          GeometryStyle.line(Color.GREEN, 3));

        MarkerStyleRenderer.renderMarkerVertices(viewport, graphics,
          imageBoundary,
          MarkerStyle.marker("cross", 11, WebColors.Black, 1, WebColors.Lime));
        final WarpFilter warpFilter = layer.getWarpFilter();
        final int tiePointCount = image.getTiePoints().size();
        if (this.image != null && tiePointCount > 0) {
          final MappedLocation moveTiePoint = getMoveTiePoint();
          for (int i = 0; i < tiePointCount; i++) {
            final MappedLocation mappedLocation = image.getTiePoints().get(i);
            if (!moveTiePointStarted || mappedLocation != moveTiePoint) {
              final LineString line = mappedLocation.getSourceToTargetLine(
                warpFilter, boundingBox);
              renderTiePointLine(graphics, viewport, line);
            }
          }
          if (moveTiePointStarted && moveTiePoint != null) {
            Point sourcePoint = null;
            Point targetPoint = null;

            if (moveTiePointSource) {
              sourcePoint = moveTiePointLocation;
              targetPoint = moveTiePoint.getTargetPoint();
            } else {
              sourcePoint = moveTiePoint.getSourcePoint(warpFilter, boundingBox);
              targetPoint = moveTiePointLocation;
            }
            if (sourcePoint != null && targetPoint != null) {
              final LineString line = getGeometryFactory().createLineString(
                sourcePoint, targetPoint);
              renderTiePointLine(graphics, viewport, line);
            }
          }

        }
        if (!showOriginalImage) {

          final double width = image.getImage().getWidth() - 1;
          final double height = image.getImage().getHeight() - 1;
          final Point topLeft = warpFilter.sourcePixelToTargetPoint(0.0, height);
          final Point topRight = warpFilter.sourcePixelToTargetPoint(width,
            height);
          final Point bottomLeft = warpFilter.sourcePixelToTargetPoint(0.0, 0.0);
          final Point bottomRight = warpFilter.sourcePixelToTargetPoint(width,
            0.0);
          final LineString line = getGeometryFactory().createLineString(
            topLeft, topRight, bottomRight, bottomLeft, topLeft);
          GeometryStyleRenderer.renderLineString(viewport, graphics, line,
            STYLE_IMAGE_LINE);
        }
        if (addTiePointFirstPoint != null) {
          final LineString line = getGeometryFactory().createLineString(
            addTiePointFirstPoint, addTiePointMove);
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
    if (source instanceof GeoReferencedImageLayer) {
      final GeoReferencedImageLayer layer = (GeoReferencedImageLayer)source;
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
    } else if (source == image) {
      clearCachedImage();
    } else if ("scale".equals(propertyName)) {
      clearCachedImage();
    }
  }

  protected void renderTiePointLine(final Graphics2D graphics,
    final Viewport2D viewport, final LineString line) {
    GeometryStyleRenderer.renderLineString(viewport, graphics, line,
      STYLE_MAPPED_LINE);
    MarkerStyleRenderer.renderMarkers(viewport, graphics,
      CoordinatesListUtil.get(line), STYLE_VERTEX_FIRST_POINT,
      STYLE_VERTEX_LAST_POINT, null);
  }

  public void setImageBoundingBox(BoundingBox boundingBox) {
    if (boundingBox == null) {
      boundingBox = new BoundingBox(getGeometryFactory());
    }
    if (image != null) {
      image.setBoundingBox(boundingBox);
    }
    setGeometryFactory(boundingBox.getGeometryFactory());
    clearCachedImage();
  }

  public void setLayer(final GeoReferencedImageLayer layer) {
    final GeoReferencedImageLayer oldLayer = this.layer;
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
    moveTiePointStarted = false;
    if (!closeSourcePixelIndexes.isEmpty()
      || !closeTargetPointIndexes.isEmpty()) {
      final List<MappedLocation> tiePoints = image.getTiePoints();
      final StringBuffer toolTip = new StringBuffer();
      toolTip.append("<html>");

      appendTiePointLocation(toolTip, tiePoints, closeTargetPointIndexes, 1,
        false);
      appendTiePointLocation(toolTip, tiePoints, closeSourcePixelIndexes,
        closeTargetPointIndexes.size() + 1, true);
      toolTip.append("</html>");
      getMap().setToolTipText(moveTiePointEventPoint, toolTip);
      return true;
    }
    return false;
  }

}
