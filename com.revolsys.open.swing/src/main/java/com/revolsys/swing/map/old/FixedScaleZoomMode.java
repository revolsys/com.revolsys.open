package com.revolsys.swing.map.old;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.measure.Measurable;
import javax.measure.quantity.Length;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.swing.map.ComponentViewport2D;
import com.revolsys.swing.map.Viewport2D;

public class FixedScaleZoomMode implements ZoomMode {
  public static final FixedScaleZoomMode METRIC = new FixedScaleZoomMode(100, 250, 500, 1000, 2500,
    5000, 10000, 20000, 50000, 100000, 250000, 500000, 1000000, 2500000, 5000000, 10000000,
    25000000, 50000000, 100000000, 250000000);

  /** The ordered list of scales. */
  private final List<Double> scales = new ArrayList<Double>();

  public FixedScaleZoomMode() {
    this(1693.0, 3385.0, 6771.0, 14000.0, 27000.0, 54000.0, 108000.0, 217000.0, 433000.0, 867000.0,
      2000000.0, 3000000.0, 7000000.0, 14000000.0, 28000000.0, 55000000.0, 111000000.0);
  }

  public FixedScaleZoomMode(final double... scales) {
    for (final double scale : scales) {
      this.scales.add(scale);
    }
    Collections.sort(this.scales);
    Collections.reverse(this.scales);
  }

  private double getBestScale(final ComponentViewport2D viewport, final double scale) {
    double maxScale = viewport.getMaxScale();
    maxScale = getScale(maxScale, true);
    final double newScale = Math.min(scale, maxScale);
    return newScale;
  }

  /**
   * Get the best bounding box matching the zoom mode policy
   * <ul>
   * <li>If the coordinate system for the bounding box wasn't specified it will
   * be set to the coordinate system of the viewport.</li>
   * <li>The bounding box will be converted to the coordinate system of the
   * viewport.</li>
   * </ul>
   *
   * @param viewport The viewport.
   * @param boundingBox The bounding box.
   * @return The bounding box.
   */
  @Override
  public BoundingBox getBoundingBox(final ComponentViewport2D viewport,
    final BoundingBox boundingBox) {
    final double viewAspectRatio = viewport.getViewAspectRatio();
    if (!Double.isNaN(viewAspectRatio) && !boundingBox.isEmpty()) {

      final GeometryFactory geometryFactory = viewport.getGeometryFactory();
      final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
      final BoundingBox areaBoundingBox = coordinateSystem.getAreaBoundingBox();
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
      }

      double maxScale = viewport.getMaxScale();
      maxScale = getScale(maxScale, false);

      double scale = getScale(viewport, newBoundingBox);

      if (!Double.isNaN(scale)) {
        scale = getScale(scale, false);
        if (scale > maxScale) {
          scale = maxScale;
        }
        final double x = newBoundingBox.getCentreX();
        final double y = newBoundingBox.getCentreY();
        final BoundingBox boundingBox2 = viewport.getBoundingBox(x, y, scale);
        // double ax1 = areaBoundingBox.getMinX();
        // double ax2 = areaBoundingBox.getMaxX();
        // double ay1 = areaBoundingBox.getMinY();
        // double ay2 = areaBoundingBox.getMaxY();
        // double x1 = boundingBox2.getMinX();
        // double x2 = boundingBox2.getMaxX();
        // double y1 = boundingBox2.getMinY();
        // double y2 = boundingBox2.getMaxY();
        // double width = boundingBox2.getWidth();
        // double height = boundingBox2.getHeight();
        // if (x1 < ax1) {
        // x1 = ax1;
        // x2 = ax1 + width;
        // } else if (x2 > ax2) {
        // x1 = ax2 - width;
        // x2 = ax2;
        // }
        // if (y1 < ay1) {
        // y1 = ay1;
        // y2 = ay1 + height;
        // } else if (y2 > ay2) {
        // y1 = ay2 - height;
        // y2 = ay2;
        // }
        // boundingBox2 = new BoundingBoxDoubleGf(coordinateSystem, x1, y1, x2,
        // y2);
        return boundingBox2;
      }
      return newBoundingBox;
    }
    return boundingBox;
  }

  private double getNextScale(final ComponentViewport2D viewport, final boolean larger) {
    final double scale = viewport.getScale();
    return getNextScale(scale, larger);

  }

  private double getNextScale(final double scale, final boolean larger) {
    double previousScale = this.scales.get(0);
    if (scale - 1 > previousScale) {

      if (!larger || this.scales.size() == 1) {
        return previousScale;
      } else {
        return this.scales.get(1);
      }
    } else {
      for (int i = 1; i < this.scales.size(); i++) {
        final double nextScale = this.scales.get(i);
        if (Math.abs(scale - nextScale) < 1) {
          if (larger) {
            if (i == this.scales.size() - 1) {
              return nextScale;
            } else {
              return this.scales.get(i + 1);
            }
          } else {
            return previousScale;
          }
        } else if (scale > nextScale) {
          if (larger) {
            return nextScale;
          } else {
            return previousScale;
          }
        }
        previousScale = nextScale;
      }
      return previousScale;
    }
  }

  protected double getScale(final ComponentViewport2D viewport, final BoundingBox newBoundingBox) {
    final Measurable<Length> viewWidth = viewport.getViewWidthLength();
    final Measurable<Length> viewHeight = viewport.getViewHeightLength();
    final Measurable<Length> modelWidth = newBoundingBox.getWidthLength();
    final Measurable<Length> modelHeight = newBoundingBox.getHeightLength();
    final double horizontalScale = Viewport2D.getScale(viewWidth, modelWidth);
    final double verticalScale = Viewport2D.getScale(viewHeight, modelHeight);
    final double scale = Math.max(horizontalScale, verticalScale);
    return scale;
  }

  private double getScale(final double scale, final boolean larger) {
    double previousScale = this.scales.get(0);
    if (Double.isNaN(scale) || scale - 1 > previousScale) {
      return previousScale;
    } else {
      for (int i = 1; i < this.scales.size(); i++) {
        final double nextScale = this.scales.get(i);
        if (Math.abs(scale - nextScale) < 1) {
          return nextScale;
        } else if (scale > nextScale) {
          if (larger) {
            return nextScale;
          } else {
            return previousScale;
          }
        }
        previousScale = nextScale;
      }
      return previousScale;
    }
  }

  public double getScale(final int zoomLevel) {
    return this.scales.get(zoomLevel);
  }

  public int getZoomLevel(final ComponentViewport2D viewport) {
    final double scale = viewport.getScale();
    double previousScale = this.scales.get(0);
    if ((int)scale >= (int)previousScale) {
      return 0;
    } else {
      for (int i = 1; i < this.scales.size(); i++) {
        final double nextScale = this.scales.get(i);
        if ((int)scale >= (int)nextScale) {
          return i;
        }
        previousScale = nextScale;
      }
      return this.scales.size() - 1;
    }

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
    final double x = boundingBox.getCentreX();
    final double y = boundingBox.getCentreY();
    final Measurable<Length> modelWidth = boundingBox.getWidthLength();
    double scale = Viewport2D.getScale(viewport.getViewWidthLength(), modelWidth);
    scale = getNextScale(scale, false);
    final double newScale = getBestScale(viewport, scale);
    final BoundingBox boundingBox1 = viewport.getBoundingBox(x, y, newScale);
    viewport.setBoundingBox(boundingBox1);
  }

  /**
   * Zoom the map to the map scale at the view coordinate, re-centring the map
   * at the model coordinate represented by the view coordinate.
   *
   * @param viewport The viewport to zoom.
   * @param x The x coordinate.
   * @param y The y coordinate.
   * @param scale The new map scale.
   */
  private void zoomAndRecentre(final ComponentViewport2D viewport, final double x, final double y,
    final double scale) {
    final double newScale = getBestScale(viewport, scale);
    final BoundingBox boundingBox = viewport.getBoundingBox(x, y, newScale);
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
    final double scale = getNextScale(viewport, true);
    zoomProportional(viewport, x, y, scale);
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
    final double scale = getNextScale(viewport, true);
    final double[] coord = viewport.toModelCoordinates(x, y);
    zoomAndRecentre(viewport, coord[0], coord[1], scale);
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
    final double scale = getNextScale(viewport, false);
    zoomProportional(viewport, x, y, scale);
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
    final double scale = getNextScale(viewport, false);
    final double[] coord = viewport.toModelCoordinates(x, y);
    zoomAndRecentre(viewport, coord[0], coord[1], scale);
  }

  /**
   * Zoom the map in by the multiplication factor at the view coordinate, with
   * the model coordinate being maintained at the same view coordinate.
   *
   * @param viewport The viewport to zoom.
   * @param x The x coordinate.
   * @param y The y coordinate.
   * @param scale The new map scale.
   */
  private void zoomProportional(final ComponentViewport2D viewport, final double x, final double y,
    final double scale) {
    final GeometryFactory geometryFactory = viewport.getGeometryFactory();

    final double newScale = getBestScale(viewport, scale);
    final double[] ordinates = viewport.toModelCoordinates(x, y);
    final double mapX = ordinates[0];
    final double mapY = ordinates[1];

    final double viewWidth = viewport.getViewWidthPixels();
    final double xProportion = x / viewWidth;

    final double viewHeight = viewport.getViewHeightPixels();
    final double yProportion = (viewHeight - y) / viewHeight;

    final double width = viewport.getModelWidth(newScale);
    final double height = viewport.getModelHeight(newScale);

    final double x1 = mapX - width * xProportion;
    final double y1 = mapY - height * yProportion;
    final double x2 = x1 + width;
    final double y2 = y1 + height;
    final BoundingBox boundingBox = new BoundingBoxDoubleGf(geometryFactory, 2, x1, y1, x2, y2);
    viewport.setBoundingBox(boundingBox);
  }
}
