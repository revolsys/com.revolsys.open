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

import javax.swing.SwingUtilities;

import org.slf4j.LoggerFactory;

import com.revolsys.awt.WebColors;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.list.ListCoordinatesList;
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
import com.vividsolutions.jts.geom.LineString;
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

  private final ListCoordinatesList tiePointsModel = new ListCoordinatesList(2);

  private final ListCoordinatesList tiePointsImage = new ListCoordinatesList(2);

  private GeoReferencedImage image;

  private GeoReferencedImage warpedImage;

  private Point addTiePointFirstPoint;

  private WarpFilter warpFilter = WarpFilter.createFilter();

  final int degree = 1;

  public EditGeoReferencedImageOverlay(final MapPanel map) {
    super(map);
  }

  private boolean addTiePointFinish(final MouseEvent event) {
    if (addTiePointFirstPoint != null) {
      if (event != null) {
        final Point mapPoint = getViewportPoint(event);
        final Coordinates modelPoint = CoordinatesUtil.get(addTiePointFirstPoint);
        final int imageWidth = image.getImageWidth();
        final int imageHeight = image.getImageHeight();
        final Coordinates imagePoint = warpFilter.toSourceImagePoint(
          imageBoundingBox, modelPoint, imageWidth, imageHeight);
        tiePointsImage.add(imagePoint);
        tiePointsModel.add(mapPoint);
        updateWarpedImage();
      }
      addTiePointFirstPoint = null;
      clearMapCursor();

      setXorGeometry(null);
      return true;
    }
    return false;
  }

  private boolean addTiePointMove(final MouseEvent event) {
    if (layer == null || addTiePointFirstPoint == null) {
      return false;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final Point mousePoint = getViewportPoint(event);
      final LineString line = geometryFactory.createLineString(
        addTiePointFirstPoint, mousePoint);
      final Graphics2D graphics = getGraphics();
      setXorGeometry(graphics, line);
      // TODO make into an arrow
      return true;
    }
  }

  private boolean addTiePointStart(final MouseEvent event) {
    if (layer != null) {
      if (addTiePointFinish(event)) {
      } else if (SwingUtilities.isLeftMouseButton(event) && event.isAltDown()) {
        final Point mousePoint = getViewportPoint(event);
        if (getImageBoundingBox().contains(mousePoint)) {
          addTiePointFirstPoint = mousePoint;
          event.consume();
          return true;
        }
      }
    }
    return false;
  }

  protected void adjustBoundingBoxAspectRatio() {
    final double imageAspectRatio = image.getImageAspectRatio();
    final double aspectRatio = getImageBoundingBox().getAspectRatio();
    double minX = getImageBoundingBox().getMinX();
    double maxX = getImageBoundingBox().getMaxX();
    double minY = getImageBoundingBox().getMinY();
    double maxY = getImageBoundingBox().getMaxY();
    final double width = getImageBoundingBox().getWidth();
    final double height = getImageBoundingBox().getHeight();
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
    final GeometryFactory geometryFactory = getGeometryFactory();
    setImageBoundingBox(new BoundingBox(geometryFactory, minX, minY, maxX, maxY));
  }

  protected void clear() {
    this.addTiePointFirstPoint = null;
    this.image = null;
    this.imageBoundingBox = null;
    this.layer = null;
    this.moveCornerOppositePoint = null;
    this.moveCornerPoint = null;
    this.moveImageFirstPoint = null;
    this.preMoveBoundingBox = null;
    tiePointsImage.clear();
    tiePointsModel.clear();
    this.warpedImage = null;
    this.warpFilter = WarpFilter.createFilter();
  }

  private BoundingBox getImageBoundingBox() {
    if (imageBoundingBox == null) {
      setImageBoundingBox(layer.getBoundingBox());
    }
    return imageBoundingBox;
  }

  public GeoReferencedImageLayer getLayer() {
    return layer;
  }

  @Override
  public void keyPressed(final KeyEvent e) {
    if (layer != null) {
      if (moveCornerPoint != null) {
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
    if (layer != null) {
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
    if (layer != null) {
      if (addTiePointFinish(event)) {
      } else if (addTiePointStart(event)) {
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
      if (addTiePointMove(event)) {
      } else if (moveCornerMove(event)) {
      } else if (moveImageMove(event)) {
      } else {
        clearMapCursor();
      }
    }
  }

  @Override
  public void mousePressed(final MouseEvent event) {
    if (layer != null) {
      if (SwingUtilities.isLeftMouseButton(event) && event.isAltDown()) {
        event.consume();
      } else if (moveCornerStart(event)) {
      } else if (moveImageStart(event)) {
      }
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
      setImageBoundingBox(new BoundingBox(geometryFactory, mousePoint,
        moveCornerOppositePoint));

      if (event.isShiftDown()) {
        adjustBoundingBoxAspectRatio();
      }
      repaint();
      event.consume();
      return true;
    }
  }

  private boolean moveCornerFinish(final MouseEvent event) {
    if (moveCornerPoint != null) {
      moveCornerPoint = null;
      moveCornerOppositePoint = null;
      if (event == null) {
        setImageBoundingBox(preMoveBoundingBox);
      } else {
        preMoveBoundingBox = getImageBoundingBox();
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
    if (layer != null) {
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
    if (layer != null) {
      if (tiePointsModel.size() == 0
        && SwingUtil.isLeftButtonAndNoModifiers(event)) {
        if (moveCornerPoint != null) {
          event.consume();
          return true;
        }
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
      setImageBoundingBox(preMoveBoundingBox.move(deltaX, deltaY));

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
        setImageBoundingBox(preMoveBoundingBox);
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
    if (layer != null) {
      if (tiePointsModel.size() == 0 && SwingUtilities.isLeftMouseButton(event)
        && (event.isControlDown() || event.isMetaDown())) {
        final Point mousePoint = getViewportPoint(event);
        if (getImageBoundingBox().contains(mousePoint)) {
          setMapCursor(new Cursor(Cursor.HAND_CURSOR));
          moveImageFirstPoint = mousePoint;
          event.consume();
          return true;
        }
      }
    }
    return false;
  }

  @Override
  protected void paintComponent(final Graphics2D graphics) {
    if (layer != null && layer.isVisible()) {
      final BoundingBox boundingBox = getImageBoundingBox();
      // TODO cache image resize for scale
      final Viewport2D viewport = getViewport();
      final Composite oldComposite = graphics.getComposite();
      try {
        final Composite composite = AlphaComposite.getInstance(
          AlphaComposite.SRC_OVER, .6f);
        graphics.setComposite(composite);
        // updateWarpedImage();
        if (warpedImage == null) {
          GeoReferencedImageLayerRenderer.render(viewport, graphics, image,
            boundingBox);
        } else {
          GeoReferencedImageLayerRenderer.render(viewport, graphics,
            warpedImage);
        }
      } catch (final Throwable e) {
        LoggerFactory.getLogger(getClass()).error("Unable to render image", e);
      } finally {
        graphics.setComposite(oldComposite);
      }
      if (boundingBox != null) {
        final Polygon imageBoundary = getImageBoundingBox().toPolygon(1);

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_OFF);

        GeometryStyleRenderer.renderOutline(viewport, graphics, imageBoundary,
          GeometryStyle.line(Color.GREEN, 3));

        MarkerStyleRenderer.renderMarkerVertices(viewport, graphics,
          imageBoundary,
          MarkerStyle.marker("cross", 11, WebColors.Black, 1, WebColors.Lime));

        final int imageWidth = image.getImageWidth();
        final int imageHeight = image.getImageHeight();
        for (int i = 1; i < tiePointsModel.size(); i++) {
          final Coordinates modelPoint = tiePointsModel.get(i);
          final Coordinates imagePoint = tiePointsImage.get(i);
          final Coordinates imageModelPoint = warpFilter.toModelPoint(
            boundingBox, imagePoint, imageWidth, imageHeight);
          final GeometryFactory geometryFactory = getGeometryFactory();
          final LineString line = geometryFactory.createLineString(
            imageModelPoint, modelPoint);
          GeometryStyleRenderer.renderLineString(viewport, graphics, line,
            GeometryStyle.line(Color.CYAN, 3));

        }
        drawXorGeometry(graphics);
      }
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

  public void setImageBoundingBox(final BoundingBox boundingBox) {
    if (boundingBox == null) {
      setImageBoundingBox(new BoundingBox());
    } else {
      imageBoundingBox = boundingBox.convert(getGeometryFactory());
      preMoveBoundingBox = this.imageBoundingBox;
      updateWarpedImage();
    }
  }

  public void setLayer(final GeoReferencedImageLayer layer) {
    final GeoReferencedImageLayer oldLayer = this.layer;
    if (oldLayer != null) {
      // TODO save tie points
      // oldLayer.setBoundingBox(getImageBoundingBox());
    }
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

  protected void updateWarpedImage() {
    final int numPoints = tiePointsModel.size();
    final BufferedImage image = this.image.getImage();
    final int imageWidth = image.getWidth();
    final int imageHeight = image.getHeight();
    final BoundingBox boundingBox = imageBoundingBox;
    warpFilter = WarpFilter.createWarpFilter(boundingBox, tiePointsImage,
      tiePointsModel, numPoints, degree, imageWidth, imageHeight);
    final BufferedImage warpedImage = warpFilter.filter(image);
    this.warpedImage = new GeoReferencedImage(boundingBox, warpedImage);
  }
}
