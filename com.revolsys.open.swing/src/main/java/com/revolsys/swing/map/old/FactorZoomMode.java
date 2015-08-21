package com.revolsys.swing.map.old;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.swing.map.ComponentViewport2D;

public class FactorZoomMode implements ZoomMode {
  /** The multiplication Factor to zoom by. */
  private final double factor;

  public FactorZoomMode(final double factor) {
    this.factor = factor;
  }

  /**
   * Get the best bounding box matching the zoom mode policy
   *
   * @param viewport The viewport.
   * @param boundingBox The bounding box.
   * @return The bounding box.
   */
  @Override
  public BoundingBox getBoundingBox(final ComponentViewport2D viewport,
    final BoundingBox boundingBox) {
    final GeometryFactory geometryFactory = viewport.getGeometryFactory();
    final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
    final GeometryFactory bboxGeometryFactory = boundingBox.getGeometryFactory();
    BoundingBox newBoundingBox = boundingBox;
    if (bboxGeometryFactory == null) {
      newBoundingBox = boundingBox.convert(geometryFactory);
    } else {
      if (bboxGeometryFactory.equals(geometryFactory)) {
        newBoundingBox = boundingBox;
      } else {
        newBoundingBox = boundingBox.convert(geometryFactory);
      }

      final BoundingBox areaBoundingBox = coordinateSystem.getAreaBoundingBox();
      newBoundingBox = boundingBox.intersection(areaBoundingBox);
    }
    final double viewAspectRatio = viewport.getViewAspectRatio();
    final double modelAspectRatio = newBoundingBox.getAspectRatio();
    if (viewAspectRatio != modelAspectRatio) {
      final double width = newBoundingBox.getWidth();
      final double height = newBoundingBox.getHeight();
      if (viewAspectRatio > modelAspectRatio) {
        final double newWidth = height * viewAspectRatio;
        final double deltaX = (newWidth - width) / 2;

        newBoundingBox = newBoundingBox.expand(deltaX, 0);
      } else if (viewAspectRatio < modelAspectRatio) {
        final double newHeight = width / viewAspectRatio;
        final double deltaY = (newHeight - height) / 2;
        newBoundingBox = newBoundingBox.expand(0, deltaY);
      }
    }
    return newBoundingBox;
  }

  /**
   * Zoom the map so that the specified bounding box is visible.
   *
   * @param viewport The viewport.
   * @param boundingBox The bounding box.
   */
  @Override
  public void zoom(final ComponentViewport2D viewport, final BoundingBox boundingBox) {
    final BoundingBox newBoundingBox = getBoundingBox(viewport, boundingBox);
    viewport.setBoundingBox(newBoundingBox);
  }

  /**
   * Zoom the map to include the bounding box specified by the model coordinate
   * pair.
   *
   * @param viewport The viewport.
   * @param x1 The first x coordinate.
   * @param y1 The first y coordinate.
   * @param x2 The second x coordinate.
   * @param y2 The second y coordinate.
   */
  @Override
  public void zoom(final ComponentViewport2D viewport, final double x1, final double y1,
    final double x2, final double y2) {
    final double viewWidth = viewport.getViewWidthPixels();
    final double viewHeight = viewport.getViewHeightPixels();

    final double xc1 = Math.max(Math.min(x1, viewWidth - 1), 0);
    final double yc1 = Math.max(Math.min(y1, viewHeight - 1), 0);
    final double xc2 = Math.max(Math.min(x2, viewWidth - 1), 0);
    final double yc2 = Math.max(Math.min(y2, viewHeight - 1), 0);

    final BoundingBox boundingBox = viewport.getBoundingBox(xc1, yc1, xc2, yc2);
    viewport.setBoundingBox(boundingBox);
  }

  /**
   * Zoom the map in by the multiplication at the view coordinate, re-centring
   * the map at the model coordinate represented by the view coordinate.
   *
   * @param viewport The viewport to zoom.
   * @param x The x coordinate.
   * @param y The y coordinate.
   * @param factor The multiplication factor to zoom by.
   */
  private void zoomAndRecentre(final ComponentViewport2D viewport, final double x, final double y,
    final double factor) {
    final double[] ordinates = viewport.toModelCoordinates(x, y);
    final double mapX = ordinates[0];
    final double mapY = ordinates[1];

    final double scale = Math.min(viewport.getScale() * factor, viewport.getMaxScale());
    final double width = viewport.getModelWidth(scale);
    final double height = viewport.getModelHeight(scale);

    final GeometryFactory geometryFactory = viewport.getGeometryFactory();

    final double x1 = mapX - width / 2;
    final double y1 = mapY - height / 2;
    final double x2 = x1 + width;
    final double y2 = y1 + height;
    final BoundingBox boundingBox = new BoundingBoxDoubleGf(geometryFactory, 2, x1, y1, x2, y2);
    viewport.setBoundingBox(boundingBox);
  }

  /**
   * Zoom the map in one level at the view coordinate, with the model coordinate
   * being maintained at the same view coordinate.
   *
   * @param viewport The viewport to zoom.
   * @param x The x coordinate.
   * @param y The y coordinate.
   */
  @Override
  public void zoomIn(final ComponentViewport2D viewport, final double x, final double y) {
    zoomProportional(viewport, x, y, 1 / this.factor);
  }

  /**
   * Zoom the map in one level at the view coordinate, re-centring the map at
   * the model coordinate represented by the view coordinate.
   *
   * @param viewport The viewport to zoom.
   * @param x The x coordinate.
   * @param y The y coordinate.
   */
  @Override
  public void zoomInAndRecentre(final ComponentViewport2D viewport, final double x,
    final double y) {
    zoomAndRecentre(viewport, x, y, 1 / this.factor);
  }

  /**
   * Zoom the map out one level at the view coordinate, with the model
   * coordinate being maintained at the same view coordinate.
   *
   * @param viewport The viewport to zoom.
   * @param x The x coordinate.
   * @param y The y coordinate.
   */
  @Override
  public void zoomOut(final ComponentViewport2D viewport, final double x, final double y) {
    zoomProportional(viewport, x, y, this.factor);
  }

  /**
   * Zoom the map in one level at the view coordinate, re-centring the map at
   * the model coordinate represented by the view coordinate.
   *
   * @param viewport The viewport to zoom.
   * @param x The x coordinate.
   * @param y The y coordinate.
   */
  @Override
  public void zoomOutAndRecentre(final ComponentViewport2D viewport, final double x,
    final double y) {
    zoomAndRecentre(viewport, x, y, this.factor);
  }

  /**
   * Zoom the map in by the multiplication factor at the view coordinate, with
   * the model coordinate being maintained at the same view coordinate.
   *
   * @param viewport The viewport to zoom.
   * @param x The x coordinate.
   * @param y The y coordinate.
   * @param factor The multiplication factor to zoom by.
   */
  private void zoomProportional(final ComponentViewport2D viewport, final double x, final double y,
    final double factor) {
    final double[] ordinates = viewport.toModelCoordinates(x, y);
    final double mapX = ordinates[0];
    final double mapY = ordinates[1];

    final double scale = Math.min(viewport.getScale() * factor, viewport.getMaxScale());
    final double width = viewport.getModelWidth(scale);
    final double height = viewport.getModelHeight(scale);

    final double viewWidth = viewport.getViewWidthPixels();
    final double xProportion = x / viewWidth;
    final double viewHeight = viewport.getViewHeightPixels();
    final double yProportion = (viewHeight - y) / viewHeight;

    final GeometryFactory geometryFactory = viewport.getGeometryFactory();

    final double x1 = mapX - width * xProportion;
    final double y1 = mapY - height * yProportion;
    final double x2 = x1 + width;
    final double y2 = y1 + height;
    final BoundingBox boundingBox = new BoundingBoxDoubleGf(geometryFactory, 2, x1, y1, x2, y2);
    viewport.setBoundingBox(boundingBox);
  }
}
