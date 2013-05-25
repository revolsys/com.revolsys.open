package com.revolsys.swing.map.overlay;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.renderable.ParameterBlock;
import java.beans.PropertyChangeEvent;

import javax.media.jai.InterpolationNearest;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;
import javax.media.jai.WarpPolynomial;
import javax.swing.SwingUtilities;

import org.slf4j.LoggerFactory;

import com.revolsys.awt.WebColors;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
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

  private RenderedOp warpedImage;

  private Point addTiePointFirstPoint;

  public EditGeoReferencedImageOverlay(final MapPanel map) {
    super(map);
  }

  private boolean addTiePointFinish(final MouseEvent event) {
    if (addTiePointFirstPoint != null) {
      if (event != null) {
        final Point mousePoint = getViewportPoint(event);
        tiePointsImage.add(addTiePointFirstPoint);
        tiePointsModel.add(mousePoint);
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

  public DoubleCoordinates getImagePoint(final BoundingBox boundingBox,
    final double modelX, final double modelY) {
    final double modelDeltaX = modelX - boundingBox.getMinX();
    final double modelDeltaY = modelY - boundingBox.getMinY();

    final double modelWidth = boundingBox.getWidth();
    final double modelHeight = boundingBox.getHeight();

    final double xRatio = modelDeltaX / modelWidth;
    final double yRatio = modelDeltaY / modelHeight;

    final double imageWidth = image.getImageWidth();
    final double imageHeight = image.getImageHeight();

    final double imageX = imageWidth * xRatio;
    final double imageY = imageHeight * yRatio;
    // TODO if image is already warped then this location will be different!
    final DoubleCoordinates imagePoint = new DoubleCoordinates(imageX, imageY);
    return imagePoint;
  }

  public DoubleCoordinates getImagePoint(final Coordinates modelPoint) {
    final double modelX = modelPoint.getX();
    final double modelY = modelPoint.getY();

    return getImagePoint(preMoveBoundingBox, modelX, modelY);
  }

  public DoubleCoordinates getImagePoint(final Point modelPoint) {
    final double modelX = modelPoint.getX();
    final double modelY = modelPoint.getY();

    return getImagePoint(preMoveBoundingBox, modelX, modelY);
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
          this.moveCornerOppositePoint = getImageBoundingBox().getCornerPoint(closestIndex + 2);
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
      // TODO cache image resize for scale
      final Viewport2D viewport = getViewport();
      final Composite oldComposite = graphics.getComposite();
      try {
        final Composite composite = AlphaComposite.getInstance(
          AlphaComposite.SRC_OVER, .6f);
        graphics.setComposite(composite);
        // updateWarpedImage();
        if (warpedImage == null) {
          BoundingBox boundingBox = getImageBoundingBox();
          if (tiePointsModel.size() > 0) {
            final Coordinates modelPoint = tiePointsModel.get(0);
            final Coordinates imagePoint = tiePointsImage.get(0);
            final double deltaX = modelPoint.getX() - imagePoint.getX();
            final double deltaY = modelPoint.getY() - imagePoint.getY();
            boundingBox = boundingBox.move(deltaX, deltaY);
          }
          GeoReferencedImageLayerRenderer.render(viewport, graphics, image,
            boundingBox);
        } else {

          final Point point = getImageBoundingBox().getTopLeftPoint();
          final double minX = point.getX();
          final double maxY = point.getY();
          final AffineTransform transform = new AffineTransform();

          final double[] location = viewport.toViewCoordinates(minX, maxY);
          final double screenX = location[0];
          final double screenY = location[1];
          transform.translate(screenX, screenY);
          final double imageScreenWidth = viewport.toDisplayValue(getImageBoundingBox().getWidthLength());
          final double imageScreenHeight = viewport.toDisplayValue(getImageBoundingBox().getHeightLength());

          final int imageWidth = image.getImageWidth();
          final int imageHeight = image.getImageHeight();
          final double xScaleFactor = imageScreenWidth / imageWidth;
          final double yScaleFactor = imageScreenHeight / imageHeight;
          transform.scale(xScaleFactor, yScaleFactor);

          graphics.drawRenderedImage(warpedImage, transform);
        }
      } catch (final Throwable e) {
        LoggerFactory.getLogger(getClass()).error("Unable to render image", e);
      } finally {
        graphics.setComposite(oldComposite);
      }
      if (getImageBoundingBox() != null) {
        final Polygon imageBoundary = getImageBoundingBox().toPolygon(1);

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_OFF);

        GeometryStyleRenderer.renderOutline(viewport, graphics, imageBoundary,
          GeometryStyle.line(Color.GREEN, 3));

        MarkerStyleRenderer.renderMarkerVertices(viewport, graphics,
          imageBoundary,
          MarkerStyle.marker("cross", 11, WebColors.Black, 1, WebColors.Lime));

        for (int i = 0; i < tiePointsModel.size(); i++) {
          final Coordinates modelPoint = tiePointsModel.get(i);
          final Coordinates imagePoint = tiePointsImage.get(i);
          final GeometryFactory geometryFactory = getGeometryFactory();
          final LineString line = geometryFactory.createLineString(imagePoint,
            modelPoint);
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
      }
    }
  }

  public void setImageBoundingBox(final BoundingBox boundingBox) {
    imageBoundingBox = boundingBox.convert(getGeometryFactory());
    preMoveBoundingBox = this.imageBoundingBox;
  }

  public void setLayer(final GeoReferencedImageLayer layer) {
    final GeoReferencedImageLayer oldLayer = this.layer;
    if (oldLayer != null) {
      oldLayer.setBoundingBox(getImageBoundingBox());
    }
    this.layer = layer;
    setGeometryFactory(getViewport().getGeometryFactory());
    setEnabled(layer != null);
    if (layer != null) {
      setImageBoundingBox(layer.getBoundingBox());
      this.image = layer.getImage();
    }
    this.warpedImage = null;
    tiePointsImage.clear();
    tiePointsModel.clear();
    if (oldLayer != null) {
      oldLayer.setEditable(false);
    }
    firePropertyChange("layer", oldLayer, layer);
  }

  protected float[] toDestinationImagePoints(final ListCoordinatesList points) {
    final int numPoints = points.size();
    final float[] dstCoords = new float[numPoints * 2];
    for (int i = 0; i < numPoints; i++) {
      final Coordinates modelPoint = points.get(i);
      final double modelX = modelPoint.getX();
      final double modelY = modelPoint.getY();
      final Coordinates imagePoint = getImagePoint(preMoveBoundingBox, modelX,
        modelY);
      dstCoords[i * 2] = (float)imagePoint.getX();
      dstCoords[i * 2 + 1] = (float)imagePoint.getY();
    }
    return dstCoords;
  }

  protected float[] toSourceImagePoints(final ListCoordinatesList points) {
    final int numPoints = points.size();
    final float[] dstCoords = new float[numPoints * 2];
    for (int i = 0; i < numPoints; i++) {
      final Coordinates modelPoint = points.get(i);
      final Coordinates imagePoint = getImagePoint(modelPoint);
      dstCoords[i * 2] = (float)imagePoint.getX();
      dstCoords[i * 2 + 1] = (float)imagePoint.getY();
    }
    return dstCoords;
  }

  protected void updateWarpedImage() {
    // TODO update in different thread
    final int numPoints = tiePointsModel.size();
    if (numPoints > 2) {

      int degree = 1;
      if (numPoints < 3) {
        degree = 0;
      }
      // TODO map world in image coordinates
      final float[] srcCoords = toSourceImagePoints(tiePointsImage);
      final float[] dstCoords = toDestinationImagePoints(tiePointsModel);
      final float width = image.getImageWidth();
      final float height = image.getImageHeight();

      final PlanarImage source = image.getJaiImage();
      final BufferedImage img = new BufferedImage(source.getWidth(),
        source.getHeight(), BufferedImage.TYPE_INT_ARGB);
      img.getGraphics().drawImage(source.getAsBufferedImage(), 0, 0, null);

      final WarpPolynomial warp = WarpPolynomial.createWarp(dstCoords, 0,
        srcCoords, 0, 2 * numPoints, 1.0F / width, 1.0F / height, width,
        height, degree);

      final ParameterBlock pb = new ParameterBlock();
      pb.addSource(img);
      pb.add(warp);
      pb.add(new InterpolationNearest());
      warpedImage = JAI.create("warp", pb);
    }
  }

  private BoundingBox getImageBoundingBox() {
    if (imageBoundingBox == null) {
      setImageBoundingBox(layer.getBoundingBox());
    }
    return imageBoundingBox;
  }
}
