package com.revolsys.swing.map.overlay;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;

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
import com.revolsys.swing.undo.ListAddUndo;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class EditGeoReferencedImageOverlay extends AbstractOverlay {
  private static final Color COLOR_OUTLINE = WebColors.Black;

  private static final Color COLOR_SELECT = WebColors.Cyan;

  private static final long serialVersionUID = 1L;

  private static final GeometryStyle STYLE_MAPPED_LINE = GeometryStyle.line(
    COLOR_SELECT, 3);

  private static final MarkerStyle STYLE_VERTEX_FIRST_POINT = MarkerStyle.marker(
    SelectRecordsOverlay.firstVertexShape(), 9, COLOR_OUTLINE, 1, COLOR_SELECT);

  private static final MarkerStyle STYLE_VERTEX_LAST_POINT = MarkerStyle.marker(
    SelectRecordsOverlay.lastVertexShape(), 9, COLOR_OUTLINE, 1, COLOR_SELECT);

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

  private BoundingBox preMoveBoundingBox;

  public EditGeoReferencedImageOverlay(final MapPanel map) {
    super(map);
  }

  private boolean addTiePointFinish(final MouseEvent event) {
    if (this.addTiePointFirstPoint != null) {
      if (event != null) {
        final Point mapPoint = getViewportPoint(event);
        final Coordinates sourcePoint = CoordinatesUtil.get(this.addTiePointFirstPoint);
        final Coordinates sourcePixel = image.getWarpFilter()
          .targetPointToSourcePixel(sourcePoint);

        final MappedLocation mappedLocation = new MappedLocation(sourcePixel,
          mapPoint);
        addUndo(new ListAddUndo(image.getTiePoints(), mappedLocation));
      }
      this.addTiePointFirstPoint = null;
      clearMapCursor();

      setXorGeometry(null);
      return true;
    }
    return false;
  }

  private boolean addTiePointMove(final MouseEvent event) {
    if (this.layer == null || this.addTiePointFirstPoint == null) {
      return false;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final Point mousePoint = getViewportPoint(event);
      final LineString line = geometryFactory.createLineString(
        this.addTiePointFirstPoint, mousePoint);
      setXorGeometry(line);
      // TODO make into an arrow
      return true;
    }
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
    final double imageAspectRatio = this.image.getImageAspectRatio();
    final double aspectRatio = getImageBoundingBox().getAspectRatio();
    double minX = getImageBoundingBox().getMinX();
    double maxX = getImageBoundingBox().getMaxX();
    double minY = getImageBoundingBox().getMinY();
    double maxY = getImageBoundingBox().getMaxY();
    final double width = getImageBoundingBox().getWidth();
    final double height = getImageBoundingBox().getHeight();
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
    setImageBoundingBox(new BoundingBox(geometryFactory, minX, minY, maxX, maxY));
  }

  protected void clear() {
    this.addTiePointFirstPoint = null;
    this.image = null;
    this.layer = null;
    this.moveCornerOppositePoint = null;
    this.moveCornerPoint = null;
    this.moveImageFirstPoint = null;
    this.preMoveBoundingBox = null;
  }

  private BoundingBox getImageBoundingBox() {
    return this.layer.getBoundingBox();
  }

  public GeoReferencedImageLayer getLayer() {
    return this.layer;
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
      if (keyCode == KeyEvent.VK_ESCAPE) {
        if (moveCornerFinish(null)) {
        } else if (moveImageFinish(null)) {
        } else if (addTiePointFinish(null)) {
        }
        setXorGeometry(null);
        repaint();
      }
    }
  }

  @Override
  public void mouseClicked(final MouseEvent event) {
    if (this.layer != null) {
      if (addTiePointFinish(event)) {
      } else if (addTiePointStart(event)) {
      }
    }
  }

  @Override
  public void mouseDragged(final MouseEvent event) {
    if (this.layer != null) {
      if (moveCornerDrag(event)) {
      } else if (moveImageDrag(event)) {
      }
    }
  }

  @Override
  public void mouseMoved(final MouseEvent event) {
    if (this.layer != null) {
      if (addTiePointFirstPoint != null) {
        final BoundingBox boundingBox = getHotspotBoundingBox(event);
        final java.awt.Point eventPoint = event.getPoint();
        hasSnapPoint(eventPoint, boundingBox);
        event.consume();
      } else if (addTiePointMove(event)) {
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
      if (SwingUtilities.isLeftMouseButton(event) && event.isAltDown()) {
        event.consume();
      } else if (moveCornerStart(event)) {
      } else if (moveImageStart(event)) {
      }
    }
  }

  @Override
  public void mouseReleased(final MouseEvent event) {
    if (this.layer != null) {
      if (moveCornerFinish(event)) {
      } else if (moveImageFinish(event)) {
      }
    }
  }

  private boolean moveCornerDrag(final MouseEvent event) {
    if (this.moveCornerPoint == null) {
      return false;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final Coordinates mousePoint = CoordinatesUtil.get(getViewportPoint(event));
      setImageBoundingBox(new BoundingBox(geometryFactory, mousePoint,
        this.moveCornerOppositePoint));

      if (event.isShiftDown()) {
        adjustBoundingBoxAspectRatio();
      }
      repaint();
      event.consume();
      return true;
    }
  }

  private boolean moveCornerFinish(final MouseEvent event) {
    if (this.moveCornerPoint != null) {
      this.moveCornerPoint = null;
      this.moveCornerOppositePoint = null;
      if (event == null) {
        setImageBoundingBox(this.preMoveBoundingBox);
      } else {
        this.preMoveBoundingBox = getImageBoundingBox();
      }
      if (event != null) {
        event.consume();
      }
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
            setMapCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
          break;
          case 1:
            setMapCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
          break;
          case 2:
            setMapCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
          break;
          case 3:
            setMapCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
          break;
          default:
            clearMapCursor();
          break;
        }
      }
    }
    return this.moveCornerPoint != null;
  }

  private boolean moveCornerStart(final MouseEvent event) {
    if (this.layer != null) {
      if (image.getTiePoints().size() == 0
        && SwingUtil.isLeftButtonAndNoModifiers(event)) {
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
      final Point mousePoint = getViewportPoint(event);

      final double deltaX = mousePoint.getX() - this.moveImageFirstPoint.getX();
      final double deltaY = mousePoint.getY() - this.moveImageFirstPoint.getY();
      setImageBoundingBox(this.preMoveBoundingBox.move(deltaX, deltaY));

      repaint();
      event.consume();
      return true;
    }
  }

  private boolean moveImageFinish(final MouseEvent event) {
    if (this.moveImageFirstPoint != null) {
      this.moveImageFirstPoint = null;
      clearMapCursor();
      if (event == null) {
        setImageBoundingBox(this.preMoveBoundingBox);
      } else {
        setImageBoundingBox(getImageBoundingBox());
      }
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
      return true;
    }
    return false;
  }

  private boolean moveImageStart(final MouseEvent event) {
    if (this.layer != null) {
      if (image.getTiePoints().isEmpty()
        && SwingUtilities.isLeftMouseButton(event)
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

  @Override
  protected void paintComponent(final Graphics2D graphics) {
    if (this.layer != null && this.layer.isVisible()) {
      final BoundingBox boundingBox = getImageBoundingBox();
      // TODO cache image resize for scale
      final Viewport2D viewport = getViewport();
      final Composite oldComposite = graphics.getComposite();
      try {
        final Composite composite = AlphaComposite.getInstance(
          AlphaComposite.SRC_OVER, .6f);
        graphics.setComposite(composite);
        GeoReferencedImageLayerRenderer.render(viewport, graphics, this.image,
          boundingBox);
      } catch (final Throwable e) {
        LoggerFactory.getLogger(getClass()).error("Unable to render image", e);
      } finally {
        graphics.setComposite(oldComposite);
      }
      if (boundingBox != null && !boundingBox.isNull()) {
        final Polygon imageBoundary = getImageBoundingBox().toPolygon(1);

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_OFF);

        GeometryStyleRenderer.renderOutline(viewport, graphics, imageBoundary,
          GeometryStyle.line(Color.GREEN, 3));

        MarkerStyleRenderer.renderMarkerVertices(viewport, graphics,
          imageBoundary,
          MarkerStyle.marker("cross", 11, WebColors.Black, 1, WebColors.Lime));
        final int tiePointCount = image.getTiePoints().size();
        if (this.image != null && tiePointCount > 0) {
          for (int i = 0; i < tiePointCount; i++) {
            final MappedLocation mappedLocation = image.getTiePoints().get(i);
            final LineString line = mappedLocation.getSourceToTargetLine(image.getWarpFilter());
            GeometryStyleRenderer.renderLineString(viewport, graphics, line,
              STYLE_MAPPED_LINE);
            MarkerStyleRenderer.renderMarkers(viewport, graphics,
              CoordinatesListUtil.get(line), STYLE_VERTEX_FIRST_POINT,
              STYLE_VERTEX_LAST_POINT, null);
          }
        }
        drawXorGeometry(graphics);
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
      } else if ("boundingBox".equals(propertyName)) {
        if (this.layer == layer) {
          final BoundingBox boundingBox = layer.getBoundingBox();
          setImageBoundingBox(boundingBox);
        }
      } else if ("revert".equals(propertyName)) {
        if (this.layer == layer) {
          clear();
          setLayer(layer);
        }
      } else if ("deleted".equals(propertyName)) {
        if (this.layer == layer) {
          clear();
        }
      }
    }
  }

  public void setImageBoundingBox(BoundingBox boundingBox) {
    if (boundingBox == null) {
      boundingBox = new BoundingBox(getGeometryFactory());
    }
    image.setBoundingBox(boundingBox);
    this.preMoveBoundingBox = getImageBoundingBox();
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

}
