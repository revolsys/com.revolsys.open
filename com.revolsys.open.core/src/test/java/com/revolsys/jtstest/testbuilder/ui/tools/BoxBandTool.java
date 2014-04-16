/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.revolsys.jtstest.testbuilder.ui.tools;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jtstest.testbuilder.JTSTestBuilder;

/**
 * @version 1.7
 */
public abstract class BoxBandTool extends IndicatorTool {

  private Point zoomBoxStart = null;

  private Point zoomBoxEnd = null;

  private static final int MIN_MOVEMENT = 3;

  public BoxBandTool() {
  }

  public BoxBandTool(final Cursor cursor) {
    this();
    this.cursor = cursor;
  }

  protected void gestureFinished() {
    // basic tool does nothing.
    // Subclasses should override
  }

  protected Geometry getBox() {
    return JTSTestBuilder.getGeometryFactory().toGeometry(getEnvelope());
  }

  /**
   * Gets the coordinates for the rectangle
   * starting at the first point clicked.
   * The coordinates are oriented CW.
   * 
   * @return the coordinates for the rectangle
   */
  protected List getCoordinateArray() {
    final Coordinates start = toModelSnapped(zoomBoxStart);
    final Coordinates end = toModelSnapped(zoomBoxEnd);

    final boolean isCW = (start.getX() < end.getX() && start.getY() < end.getY())
      || (start.getX() > end.getX() && start.getY() > end.getY());

    final Coordinates mid1 = new Coordinate(start.getX(), end.getY());
    final Coordinates mid2 = new Coordinate(end.getX(), start.getY());

    /**
     * Form rectangle starting at start point, 
     * and oriented CW.
     */
    final List coords = new ArrayList();
    coords.add(new Coordinate(start));
    if (isCW) {
      coords.add(mid1);
    } else {
      coords.add(mid2);
    }

    coords.add(new Coordinate(end));

    if (isCW) {
      coords.add(mid2);
    } else {
      coords.add(mid1);
    }

    coords.add(new Coordinate(start));
    return coords;
  }

  /**
   * Getes the coordinates for the rectangle
   * starting with the lower left point.
   * The coordinates are oriented CW.
   * 
   * @return the coordinates for the rectangle
   */
  protected List getCoordinateArrayOfEnvelope() {
    final BoundingBox env = getEnvelope();

    final List coords = new ArrayList();
    coords.add(new Coordinate(env.getMinX(), env.getMinY()));
    coords.add(new Coordinate(env.getMinX(), env.getMaxY()));
    coords.add(new Coordinate(env.getMaxX(), env.getMaxY()));
    coords.add(new Coordinate(env.getMaxX(), env.getMinY()));
    coords.add(new Coordinate(env.getMinX(), env.getMinY()));
    return coords;
  }

  /**
   * Gets the envelope of the indicated rectangle,
   * in model coordinates.
   * 
   * @return
   */
  protected BoundingBox getEnvelope() {
    final Coordinates start = toModelSnapped(zoomBoxStart);
    final Coordinates end = toModelSnapped(zoomBoxEnd);
    return new Envelope(start, end);
  }

  @Override
  protected Shape getShape() {
    if (zoomBoxEnd == null) {
      return null;
    }

    final BoundingBox envModel = getEnvelope();

    final Point2D base = toView(new Coordinate(envModel.getMinX(),
      envModel.getMaxY()));
    final double width = toView(envModel.getWidth());
    final double height = toView(envModel.getHeight());
    return new Rectangle2D.Double(base.getX(), base.getY(), width, height);
  }

  private boolean isSignificantMouseMove() {
    if (zoomBoxEnd == null) {
      return false;
    }

    if (Math.abs(zoomBoxStart.getX() - zoomBoxEnd.getX()) < MIN_MOVEMENT) {
      return false;
    }
    if (Math.abs(zoomBoxStart.getY() - zoomBoxEnd.getY()) < MIN_MOVEMENT) {
      return false;
    }
    return true;
  }

  @Override
  public void mouseDragged(final MouseEvent e) {
    super.mouseDragged(e);
    zoomBoxEnd = e.getPoint();
    redrawIndicator();
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    zoomBoxStart = e.getPoint();
    zoomBoxEnd = null;
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    clearIndicator();
    // don't process this event if the mouse was clicked or dragged a very short
    // distance
    if (!isSignificantMouseMove()) {
      return;
    }

    gestureFinished();
  }

}
