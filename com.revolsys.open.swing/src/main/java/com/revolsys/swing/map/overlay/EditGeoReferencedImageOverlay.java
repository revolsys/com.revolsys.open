package com.revolsys.swing.map.overlay;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.WebColors;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.dataobject.renderer.GeometryStyleRenderer;
import com.revolsys.swing.map.layer.dataobject.renderer.MarkerStyleRenderer;
import com.revolsys.swing.map.layer.dataobject.style.GeometryStyle;
import com.revolsys.swing.map.layer.dataobject.style.MarkerStyle;
import com.revolsys.swing.map.layer.raster.GeoReferencedImage;
import com.revolsys.swing.map.layer.raster.GeoReferencedImageLayer;
import com.revolsys.swing.map.layer.raster.GeoReferencedImageLayerRenderer;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class EditGeoReferencedImageOverlay extends AbstractOverlay {
  private static final long serialVersionUID = 1L;

  private GeoReferencedImageLayer layer;

  private Point moveCornerPoint;

  private Coordinates moveCornerOppositePoint;

  private Point moveImageFirstPoint;

  private BoundingBox imageBoundingBox;

  private BoundingBox preMoveBoundingBox;

  public EditGeoReferencedImageOverlay(final MapPanel map) {
    super(map);
  }

  public GeoReferencedImageLayer getLayer() {
    return layer;
  }

  @Override
  public void keyPressed(KeyEvent e) {
    if (moveCornerPoint != null) {
      if (e.isShiftDown()) {
        adjustBoundingBoxAspectRatio();
        repaint();
        e.consume();
      }
    }
  }

  @Override
  public void keyReleased(final KeyEvent e) {
    super.keyReleased(e);
    final int keyCode = e.getKeyCode();
    if (keyCode == KeyEvent.VK_ESCAPE) {
      if (moveCornerPoint != null) {
        moveCornerFinish(null);
      } else if (moveImageFirstPoint != null) {
        moveImageFinish(null);
      }
    }
  }

  @Override
  public void mouseDragged(final MouseEvent event) {
    if (layer != null) {
      if (moveCornerDrag(event)) {
      } else if (moveImageDrag(event)) {
      }
    }
  }

  @Override
  public void mouseMoved(final MouseEvent event) {
    if (layer != null) {
      if (moveCornerMove(event)) {
      } else if (moveImageMove(event)) {
      } else {
        clearMapCursor();
      }
    }
  }

  @Override
  public void mousePressed(final MouseEvent event) {
    if (moveCornerStart(event)) {
    } else if (moveImageStart(event)) {
    }
  }

  @Override
  public void mouseReleased(final MouseEvent event) {
    if (layer != null) {
      if (moveCornerFinish(event)) {
      } else if (moveImageFinish(event)) {
      }
    }
  }

  private boolean moveCornerDrag(final MouseEvent event) {
    if (moveCornerPoint == null) {
      return false;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final Coordinates mousePoint = CoordinatesUtil.get(getViewportPoint(event));
      imageBoundingBox = new BoundingBox(geometryFactory, mousePoint,
        moveCornerOppositePoint);

      if (event.isShiftDown()) {
        adjustBoundingBoxAspectRatio();
      }
      repaint();
      event.consume();
      return true;
    }
  }

  protected void adjustBoundingBoxAspectRatio() {
    GeoReferencedImage image = layer.getImage();
    double imageAspectRatio = image.getImageAspectRatio();
    double aspectRatio = imageBoundingBox.getAspectRatio();
    double minX = imageBoundingBox.getMinX();
    double maxX = imageBoundingBox.getMaxX();
    double minY = imageBoundingBox.getMinY();
    double maxY = imageBoundingBox.getMaxY();
    double width = imageBoundingBox.getWidth();
    double height = imageBoundingBox.getHeight();
    if (aspectRatio < imageAspectRatio) {
      if (minX == moveCornerOppositePoint.getX()) {
        maxX = minX + height * imageAspectRatio;
      } else {
        minX = maxX - height * imageAspectRatio;
      }
    } else if (aspectRatio > imageAspectRatio) {
      if (minY == moveCornerOppositePoint.getY()) {
        maxY = minY + width / imageAspectRatio;
      } else {
        minY = maxY - width / imageAspectRatio;
      }
    }
    GeometryFactory geometryFactory = getGeometryFactory();
    imageBoundingBox = new BoundingBox(geometryFactory, minX, minY, maxX, maxY);
  }

  private boolean moveCornerFinish(final MouseEvent event) {
    if (moveCornerPoint != null) {
      moveCornerPoint = null;
      moveCornerOppositePoint = null;
      if (event == null) {
        setImageBoundingBox(preMoveBoundingBox);
      } else {
        preMoveBoundingBox = imageBoundingBox;
      }
      repaint();
      if (event != null) {
        event.consume();
      }
      return true;
    }
    return false;
  }

  private boolean moveCornerMove(final MouseEvent event) {
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
      final Coordinates point = imageBoundingBox.getCornerPoint(i);
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
        this.moveCornerOppositePoint = imageBoundingBox.getCornerPoint(closestIndex + 2);
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
    return this.moveCornerPoint != null;
  }

  private boolean moveCornerStart(final MouseEvent event) {
    if (SwingUtil.isLeftButtonAndNoModifiers(event)) {
      if (moveCornerPoint != null) {
        event.consume();
        return true;
      }
    }
    return false;
  }

  private boolean moveImageDrag(final MouseEvent event) {
    if (moveImageFirstPoint == null) {
      return false;
    } else {
      final Point mousePoint = getViewportPoint(event);

      final double deltaX = mousePoint.getX() - moveImageFirstPoint.getX();
      final double deltaY = mousePoint.getY() - moveImageFirstPoint.getY();
      imageBoundingBox = preMoveBoundingBox.move(deltaX, deltaY);

      repaint();
      event.consume();
      return true;
    }
  }

  private boolean moveImageFinish(final MouseEvent event) {
    if (moveImageFirstPoint != null) {
      moveImageFirstPoint = null;
      clearMapCursor();
      if (event == null) {
        imageBoundingBox = preMoveBoundingBox;
      } else {
        setImageBoundingBox(imageBoundingBox);
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
    if (imageBoundingBox.contains(mousePoint)) {
      setMapCursor(new Cursor(Cursor.HAND_CURSOR));
      return true;
    }
    return false;
  }

  private boolean moveImageStart(final MouseEvent event) {
    if (SwingUtil.isLeftButtonAndNoModifiers(event)) {
      final Point mousePoint = getViewportPoint(event);
      if (imageBoundingBox.contains(mousePoint)) {
        setMapCursor(new Cursor(Cursor.HAND_CURSOR));
        moveImageFirstPoint = mousePoint;
        event.consume();
        return true;
      }
    }
    return false;
  }

  @Override
  protected void paintComponent(final Graphics g) {
    if (layer != null && layer.isVisible()) {
      final Graphics2D graphics2d = (Graphics2D)g;

      final Viewport2D viewport = getViewport();
      final GeoReferencedImage image = layer.getImage();
      final Composite oldComposite = graphics2d.getComposite();
      try {
        final Composite composite = AlphaComposite.getInstance(
          AlphaComposite.SRC_OVER, .6f);
        graphics2d.setComposite(composite);
        GeoReferencedImageLayerRenderer.render(viewport, graphics2d, image,
          imageBoundingBox);
      } finally {
        graphics2d.setComposite(oldComposite);
      }
      final Polygon imageBoundary = imageBoundingBox.toPolygon(1);

      graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_OFF);

      GeometryStyleRenderer.renderOutline(viewport, graphics2d, imageBoundary,
        GeometryStyle.line(Color.GREEN, 3));

      MarkerStyleRenderer.renderMarkerVertices(viewport, graphics2d,
        imageBoundary,
        MarkerStyle.marker("cross", 11, WebColors.Black, 1, WebColors.Lime));
    }
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    super.propertyChange(event);
    final Object source = event.getSource();
    if (source instanceof GeoReferencedImageLayer) {
      final GeoReferencedImageLayer layer = (GeoReferencedImageLayer)source;
      final String propertyName = event.getPropertyName();
      if ("editable".equals(propertyName)) {
        if (event.getNewValue() == Boolean.FALSE) {
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
      }
    }
  }

  public void setImageBoundingBox(final BoundingBox boundingBox) {
    imageBoundingBox = boundingBox.convert(getGeometryFactory());
    preMoveBoundingBox = this.imageBoundingBox;
  }

  public void setLayer(final GeoReferencedImageLayer layer) {
    final GeoReferencedImageLayer oldLayer = this.layer;
    this.layer = layer;
    setGeometryFactory(getViewport().getGeometryFactory());
    setEnabled(layer != null);
    if (layer != null) {
      setImageBoundingBox(layer.getBoundingBox());
    }
    if (oldLayer != null) {
      oldLayer.setEditable(false);
    }
    firePropertyChange("layer", oldLayer, layer);
  }
}
