package com.revolsys.swing.map.old;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.swing.map.ComponentViewport2D;

public interface ZoomMode {
  /**
   * Get the best bounding box matching the zoom mode policy
   *
   * @param viewport The viewport.
   * @param boundingBox The bounding box.
   * @return The bounding box.
   */
  BoundingBox getBoundingBox(ComponentViewport2D viewport,
    BoundingBox boundingBox);

  /**
   * Zoom the map so that the specified bounding box is visible.
   *
   * @param viewport The viewport.
   * @param boundingBox The bounding box.
   */
  void zoom(ComponentViewport2D viewport, BoundingBox boundingBox);

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
  void zoom(ComponentViewport2D viewport, double x1, double y1, double x2,
    double y2);

  /**
   * Zoom the map in one level at the view coordinate, with the model coordinate
   * being maintained at the same view coordinate.
   *
   * @param viewport The viewport to zoom.
   * @param x The x coordinate.
   * @param y The y coordinate.
   */
  void zoomIn(ComponentViewport2D viewport, double x, double y);

  /**
   * Zoom the map in one level at the view coordinate, re-centering the map at
   * the model coordinate represented by the view coordinate.
   *
   * @param viewport The viewport to zoom.
   * @param x The x coordinate.
   * @param y The y coordinate.
   */
  void zoomInAndRecentre(ComponentViewport2D viewport, double x, double y);

  /**
   * Zoom the map out one level at the view coordinate, with the model
   * coordinate being maintained at the same view coordinate.
   *
   * @param viewport The viewport to zoom.
   * @param x The x coordinate.
   * @param y The y coordinate.
   */
  void zoomOut(ComponentViewport2D viewport, double x, double y);

  /**
   * Zoom the map in one level at the view coordinate, re-centering the map at
   * the model coordinate represented by the view coordinate.
   *
   * @param viewport The viewport to zoom.
   * @param x The x coordinate.
   * @param y The y coordinate.
   */
  void zoomOutAndRecentre(ComponentViewport2D viewport, double x, double y);
}
