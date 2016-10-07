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

package com.revolsys.geometry.noding.snapround;

import com.revolsys.geometry.algorithm.LineIntersector;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleXY;
import com.revolsys.geometry.model.impl.PointDouble;
import com.revolsys.geometry.noding.NodedSegmentString;
import com.revolsys.geometry.util.Assert;

/**
 * Implements a "hot pixel" as used in the Snap Rounding algorithm.
 * A hot pixel contains the interior of the tolerance square and
 * the boundary
 * <b>minus</b> the top and right segments.
 * <p>
 * The hot pixel operations are all computed in the integer domain
 * to avoid rounding problems.
 *
 * @version 1.7
 */
public class HotPixel {
  // testing only
  // public static int nTests = 0;

  private static final double SAFE_ENV_EXPANSION_FACTOR = 0.75;

  /**
   * The corners of the hot pixel, in the order:
   *  10
   *  23
   */
  private final Point[] corner = new Point[4];

  private final LineIntersector li;

  private double maxx;

  private double maxy;

  private double minx;

  private double miny;

  private final Point originalPt;

  private Point pt;

  private BoundingBox safeEnv = null;

  private final double scaleFactor;

  /**
   * Creates a new hot pixel, using a given scale factor.
   * The scale factor must be strictly positive (non-zero).
   *
   * @param pt the coordinate at the centre of the pixel
   * @param scaleFactor the scaleFactor determining the pixel size.  Must be > 0
   * @param li the intersector to use for testing intersection with line segments
   *
   */
  public HotPixel(final Point pt, final double scaleFactor, final LineIntersector li) {
    this.originalPt = pt;
    this.pt = pt;
    this.scaleFactor = scaleFactor;
    this.li = li;
    // tolerance = 0.5;
    if (scaleFactor <= 0) {
      throw new IllegalArgumentException("Scale factor must be non-zero");
    }
    if (scaleFactor != 1.0) {
      this.pt = new PointDouble(scale(pt.getX()), scale(pt.getY()), Geometry.NULL_ORDINATE);
    }
    initCorners(this.pt);
  }

  /**
   * Adds a new node (equal to the snap pt) to the specified segment
   * if the segment passes through the hot pixel
   *
   * @param segStr
   * @param segIndex
   * @return true if a node was added to the segment
   */
  public boolean addSnappedNode(final NodedSegmentString segStr, final int segIndex) {
    final Point p0 = segStr.getPoint(segIndex);
    final Point p1 = segStr.getPoint(segIndex + 1);

    if (intersects(p0, p1)) {
      // System.out.println("snapped: " + snapPt);
      // System.out.println("POINT (" + snapPt.x + " " + snapPt.y + ")");
      segStr.addIntersection(getCoordinate(), segIndex);

      return true;
    }
    return false;
  }

  /**
   * Gets the coordinate this hot pixel is based at.
   *
   * @return the coordinate of the pixel
   */
  public Point getCoordinate() {
    return this.originalPt;
  }

  /**
   * Returns a "safe" envelope that is guaranteed to contain the hot pixel.
   * The envelope returned will be larger than the exact envelope of the
   * pixel.
   *
   * @return an envelope which contains the hot pixel
   */
  public BoundingBox getSafeEnvelope() {
    if (this.safeEnv == null) {
      final double safeTolerance = SAFE_ENV_EXPANSION_FACTOR / this.scaleFactor;
      this.safeEnv = new BoundingBoxDoubleXY(this.originalPt.getX() - safeTolerance,
        this.originalPt.getY() - safeTolerance, this.originalPt.getX() + safeTolerance,
        this.originalPt.getY() + safeTolerance);
    }
    return this.safeEnv;
  }

  private Point getScaled(final Point p) {
    return new PointDouble(scale(p.getX()), scale(p.getY()));
  }

  private void initCorners(final Point pt) {
    final double tolerance = 0.5;
    this.minx = pt.getX() - tolerance;
    this.maxx = pt.getX() + tolerance;
    this.miny = pt.getY() - tolerance;
    this.maxy = pt.getY() + tolerance;

    this.corner[0] = new PointDouble(this.maxx, this.maxy, Geometry.NULL_ORDINATE);
    this.corner[1] = new PointDouble(this.minx, this.maxy, Geometry.NULL_ORDINATE);
    this.corner[2] = new PointDouble(this.minx, this.miny, Geometry.NULL_ORDINATE);
    this.corner[3] = new PointDouble(this.maxx, this.miny, Geometry.NULL_ORDINATE);
  }

  /**
   * Tests whether the line segment (p0-p1)
   * intersects this hot pixel.
   *
   * @param p0 the first coordinate of the line segment to test
   * @param p1 the second coordinate of the line segment to test
   * @return true if the line segment intersects this hot pixel
   */
  public boolean intersects(final Point p0, final Point p1) {
    if (this.scaleFactor == 1.0) {
      return intersectsScaled(p0, p1);
    }

    return intersectsScaled(getScaled(p0), getScaled(p1));
  }

  private boolean intersectsScaled(final Point p0, final Point p1) {
    final double segMinx = Math.min(p0.getX(), p1.getX());
    final double segMaxx = Math.max(p0.getX(), p1.getX());
    final double segMiny = Math.min(p0.getY(), p1.getY());
    final double segMaxy = Math.max(p0.getY(), p1.getY());

    final boolean isOutsidePixelEnv = this.maxx < segMinx || this.minx > segMaxx
      || this.maxy < segMiny || this.miny > segMaxy;
    if (isOutsidePixelEnv) {
      return false;
    }
    final boolean intersects = intersectsToleranceSquare(p0, p1);

    Assert.isTrue(!(isOutsidePixelEnv && intersects), "Found bad envelope test");

    return intersects;
  }

  /**
   * Tests whether the segment p0-p1 intersects the hot pixel tolerance square.
   * Because the tolerance square point set is partially open (along the
   * top and right) the test needs to be more sophisticated than
   * simply checking for any intersection.
   * However, it can take advantage of the fact that the hot pixel edges
   * do not lie on the coordinate grid.
   * It is sufficient to check if any of the following occur:
   * <ul>
   * <li>a proper intersection between the segment and any hot pixel edge
   * <li>an intersection between the segment and <b>both</b> the left and bottom hot pixel edges
   * (which detects the case where the segment intersects the bottom left hot pixel corner)
   * <li>an intersection between a segment endpoint and the hot pixel coordinate
   * </ul>
   *
   * @param p0
   * @param p1
   * @return
   */
  private boolean intersectsToleranceSquare(final Point p0, final Point p1) {
    boolean intersectsLeft = false;
    boolean intersectsBottom = false;

    this.li.computeIntersection(p0, p1, this.corner[0], this.corner[1]);
    if (this.li.isProper()) {
      return true;
    }

    this.li.computeIntersection(p0, p1, this.corner[1], this.corner[2]);
    if (this.li.isProper()) {
      return true;
    }
    if (this.li.hasIntersection()) {
      intersectsLeft = true;
    }

    this.li.computeIntersection(p0, p1, this.corner[2], this.corner[3]);
    if (this.li.isProper()) {
      return true;
    }
    if (this.li.hasIntersection()) {
      intersectsBottom = true;
    }

    this.li.computeIntersection(p0, p1, this.corner[3], this.corner[0]);
    if (this.li.isProper()) {
      return true;
    }

    if (intersectsLeft && intersectsBottom) {
      return true;
    }

    if (p0.equals(this.pt)) {
      return true;
    }
    if (p1.equals(this.pt)) {
      return true;
    }

    return false;
  }

  private double scale(final double val) {
    return Math.round(val * this.scaleFactor);
  }

}
