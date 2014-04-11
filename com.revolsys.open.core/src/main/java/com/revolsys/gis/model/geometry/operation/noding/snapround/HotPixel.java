package com.revolsys.gis.model.geometry.operation.noding.snapround;

import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.geometry.impl.BoundingBox;
import com.revolsys.gis.model.geometry.operation.chain.NodedSegmentString;
import com.revolsys.gis.model.geometry.operation.geomgraph.index.LineIntersector;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.util.Assert;

/**
 * Implements a "hot pixel" as used in the Snap Rounding algorithm. A hot pixel
 * contains the interior of the tolerance square and the boundary <b>minus</b>
 * the top and right segments.
 * <p>
 * The hot pixel operations are all computed in the integer domain to avoid
 * rounding problems.
 * 
 * @version 1.7
 */
public class HotPixel {
  // testing only
  // public static int nTests = 0;

  private final LineIntersector li;

  private Coordinates pt;

  private final Coordinates originalPt;

  private Coordinates p0Scaled;

  private Coordinates p1Scaled;

  private final double scaleFactor;

  private double minx;

  private double maxx;

  private double miny;

  private double maxy;

  /**
   * The corners of the hot pixel, in the order: 10 23
   */
  private final Coordinates[] corner = new Coordinates[4];

  private BoundingBox safeEnv = null;

  private static final double SAFE_ENV_EXPANSION_FACTOR = 0.75;

  /**
   * Creates a new hot pixel.
   * 
   * @param pt the coordinate at the centre of the pixel
   * @param scaleFactor the scaleFactor determining the pixel size
   * @param li the intersector to use for testing intersection with line
   *          segments
   */
  public HotPixel(final Coordinates pt, final double scaleFactor,
    final LineIntersector li) {
    originalPt = pt;
    this.pt = pt;
    this.scaleFactor = scaleFactor;
    this.li = li;
    // tolerance = 0.5;
    if (scaleFactor != 1.0) {
      this.pt = new DoubleCoordinates(scale(pt.getX()), scale(pt.getY()));
      p0Scaled = new DoubleCoordinates();
      p1Scaled = new DoubleCoordinates();
    }
    initCorners(this.pt);
  }

  /**
   * Adds a new node (equal to the snap pt) to the specified segment if the
   * segment passes through the hot pixel
   * 
   * @param segStr
   * @param segIndex
   * @return true if a node was added to the segment
   */
  public boolean addSnappedNode(final NodedSegmentString segStr,
    final int segIndex) {
    final Coordinates p0 = segStr.getCoordinate(segIndex);
    final Coordinates p1 = segStr.getCoordinate(segIndex + 1);

    if (intersects(p0, p1)) {
      // System.out.println("snapped: " + snapPt);
      // System.out.println("POINT (" + snapPt.getX() + " " + snapPt.getY() +
      // ")");
      segStr.addIntersection(getCoordinates(), segIndex);

      return true;
    }
    return false;
  }

  private void copyScaled(final Coordinates p, final Coordinates pScaled) {
    pScaled.setX(scale(p.getX()));
    pScaled.setY(scale(p.getY()));
  }

  /**
   * Gets the coordinate this hot pixel is based at.
   * 
   * @return the coordinate of the pixel
   */
  public Coordinates getCoordinates() {
    return originalPt;
  }

  /**
   * Returns a "safe" envelope that is guaranteed to contain the hot pixel. The
   * envelope returned will be larger than the exact envelope of the pixel.
   * 
   * @return an envelope which contains the hot pixel
   */
  public BoundingBox getSafeBoundingBox() {
    if (safeEnv == null) {
      final double safeTolerance = SAFE_ENV_EXPANSION_FACTOR / scaleFactor;
      safeEnv = new BoundingBox(null, originalPt.getX() - safeTolerance,
        originalPt.getY() - safeTolerance, originalPt.getX() + safeTolerance,
        originalPt.getY() + safeTolerance);
    }
    return safeEnv;
  }

  private void initCorners(final Coordinates pt) {
    final double tolerance = 0.5;
    minx = pt.getX() - tolerance;
    maxx = pt.getX() + tolerance;
    miny = pt.getY() - tolerance;
    maxy = pt.getY() + tolerance;

    corner[0] = new DoubleCoordinates(maxx, maxy);
    corner[1] = new DoubleCoordinates(minx, maxy);
    corner[2] = new DoubleCoordinates(minx, miny);
    corner[3] = new DoubleCoordinates(maxx, miny);
  }

  /**
   * Tests whether the line segment (p0-p1) intersects this hot pixel.
   * 
   * @param p0 the first coordinate of the line segment to test
   * @param p1 the second coordinate of the line segment to test
   * @return true if the line segment intersects this hot pixel
   */
  public boolean intersects(final Coordinates p0, final Coordinates p1) {
    if (scaleFactor == 1.0) {
      return intersectsScaled(p0, p1);
    }

    copyScaled(p0, p0Scaled);
    copyScaled(p1, p1Scaled);
    return intersectsScaled(p0Scaled, p1Scaled);
  }

  private boolean intersectsScaled(final Coordinates p0, final Coordinates p1) {
    final double segMinx = Math.min(p0.getX(), p1.getX());
    final double segMaxx = Math.max(p0.getX(), p1.getX());
    final double segMiny = Math.min(p0.getY(), p1.getY());
    final double segMaxy = Math.max(p0.getY(), p1.getY());

    final boolean isOutsidePixelEnv = maxx < segMinx || minx > segMaxx
      || maxy < segMiny || miny > segMaxy;
    if (isOutsidePixelEnv) {
      return false;
    }
    final boolean intersects = intersectsToleranceSquare(p0, p1);
    // boolean intersectsPixelClosure = intersectsPixelClosure(p0, p1);

    // if (intersectsPixel != intersects) {
    // Debug.println("Found hot pixel intersection mismatch at " + pt);
    // Debug.println("Test segment: " + p0 + " " + p1);
    // }

    /*
     * if (scaleFactor != 1.0) { boolean intersectsScaled =
     * intersectsScaledTest(p0, p1); if (intersectsScaled != intersects) {
     * intersectsScaledTest(p0, p1); //
     * Debug.println("Found hot pixel scaled intersection mismatch at " + pt);
     * // Debug.println("Test segment: " + p0 + " " + p1); } return
     * intersectsScaled; }
     */

    Assert.isTrue(!(isOutsidePixelEnv && intersects), "Found bad envelope test");
    // if (isOutsideEnv && intersects) {
    // Debug.println("Found bad envelope test");
    // }

    return intersects;
    // return intersectsPixelClosure;
  }

  /**
   * Tests whether the segment p0-p1 intersects the hot pixel tolerance square.
   * Because the tolerance square point set is partially open (along the top and
   * right) the test needs to be more sophisticated than simply checking for any
   * intersection. However, it can take advantage of the fact that because the
   * hot pixel edges do not lie on the coordinate grid. It is sufficient to
   * check if there is at least one of:
   * <ul>
   * <li>a proper intersection with the segment and any hot pixel edge
   * <li>an intersection between the segment and both the left and bottom edges
   * <li>an intersection between a segment endpoint and the hot pixel coordinate
   * </ul>
   * 
   * @param p0
   * @param p1
   * @return
   */
  private boolean intersectsToleranceSquare(final Coordinates p0,
    final Coordinates p1) {
    boolean intersectsLeft = false;
    boolean intersectsBottom = false;

    li.computeIntersection(p0, p1, corner[0], corner[1]);
    if (li.isProper()) {
      return true;
    }

    li.computeIntersection(p0, p1, corner[1], corner[2]);
    if (li.isProper()) {
      return true;
    }
    if (li.hasIntersection()) {
      intersectsLeft = true;
    }

    li.computeIntersection(p0, p1, corner[2], corner[3]);
    if (li.isProper()) {
      return true;
    }
    if (li.hasIntersection()) {
      intersectsBottom = true;
    }

    li.computeIntersection(p0, p1, corner[3], corner[0]);
    if (li.isProper()) {
      return true;
    }

    if (intersectsLeft && intersectsBottom) {
      return true;
    }

    if (p0.equals(pt)) {
      return true;
    }
    if (p1.equals(pt)) {
      return true;
    }

    return false;
  }

  private double scale(final double val) {
    return Math.round(val * scaleFactor);
  }

}
