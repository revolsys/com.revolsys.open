package com.revolsys.gis.model.geometry.operation.overlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.gis.model.coordinates.list.InPlaceIterator;
import com.revolsys.gis.model.coordinates.list.ListCoordinatesList;
import com.revolsys.gis.model.geometry.Geometry;
import com.revolsys.gis.model.geometry.GeometryFactory;
import com.revolsys.gis.model.geometry.LineString;
import com.revolsys.gis.model.geometry.Polygonal;
import com.revolsys.gis.model.geometry.impl.BoundingBox;
import com.revolsys.jts.geom.Coordinate;

/**
 * Snaps the vertices and segments of a {@link Geometry} to another Geometry's
 * vertices. A snap distance tolerance is used to control where snapping is
 * performed. Snapping one geometry to another can improve robustness for
 * overlay operations by eliminating nearly-coincident edges (which cause
 * problems during noding and intersection calculation). Too much snapping can
 * result in invalid topology beging created, so the number and location of
 * snapped vertices is decided using heuristics to determine when it is safe to
 * snap. This can result in some potential snaps being omitted, however.
 * 
 * @author Martin Davis
 * @version 1.7
 */
public class GeometrySnapper {
  private static final double SNAP_PRECISION_FACTOR = 1e-9;

  /**
   * Estimates the snap tolerance for a Geometry, taking into account its
   * precision model.
   * 
   * @param g a Geometry
   * @return the estimated snap tolerance
   */
  public static double computeOverlaySnapTolerance(final Geometry g) {
    double snapTolerance = computeSizeBasedSnapTolerance(g);

    /**
     * Overlay is carried out in the precision model of the two inputs. If this
     * precision model is of type FIXED, then the snap tolerance must reflect
     * the precision grid size. Specifically, the snap tolerance should be at
     * least the distance from a corner of a precision grid cell to the centre
     * point of the cell.
     */
    final GeometryFactory pm = g.getGeometryFactory();
    final double scaleXY = pm.getScaleXY();
    if (scaleXY > 0) {
      final double fixedSnapTol = (1 / scaleXY) * 2 / 1.415;
      if (fixedSnapTol > snapTolerance) {
        snapTolerance = fixedSnapTol;
      }
    }
    return snapTolerance;
  }

  public static double computeOverlaySnapTolerance(final Geometry g0,
    final Geometry g1) {
    return Math.min(computeOverlaySnapTolerance(g0),
      computeOverlaySnapTolerance(g1));
  }

  public static double computeSizeBasedSnapTolerance(final Geometry g) {
    final BoundingBox env = g.getBoundingBox();
    final double minDimension = Math.min(env.getHeight(), env.getWidth());
    final double snapTol = minDimension * SNAP_PRECISION_FACTOR;
    return snapTol;
  }

  /**
   * Snaps two geometries together with a given tolerance.
   * 
   * @param g0 a geometry to snap
   * @param g1 a geometry to snap
   * @param snapTolerance the tolerance to use
   * @return the snapped geometries
   */
  public static Geometry[] snap(final Geometry g0, final Geometry g1,
    final double snapTolerance) {
    final Geometry[] snapGeom = new Geometry[2];
    final GeometrySnapper snapper0 = new GeometrySnapper(g0);
    snapGeom[0] = snapper0.snapTo(g1, snapTolerance);

    /**
     * Snap the second geometry to the snapped first geometry (this strategy
     * minimizes the number of possible different points in the result)
     */
    final GeometrySnapper snapper1 = new GeometrySnapper(g1);
    snapGeom[1] = snapper1.snapTo(snapGeom[0], snapTolerance);

    // System.out.println(snap[0]);
    // System.out.println(snap[1]);
    return snapGeom;
  }

  public static Geometry snapToSelf(final Geometry g0,
    final double snapTolerance, final boolean cleanResult) {
    final GeometrySnapper snapper0 = new GeometrySnapper(g0);
    return snapper0.snapToSelf(snapTolerance, cleanResult);
  }

  private final Geometry srcGeom;

  /**
   * Creates a new snapper acting on the given geometry
   * 
   * @param srcGeom the geometry to snap
   */
  public GeometrySnapper(final Geometry srcGeom) {
    this.srcGeom = srcGeom;
  }

  private double computeMinimumSegmentLength(final Coordinate[] pts) {
    double minSegLen = Double.MAX_VALUE;
    for (int i = 0; i < pts.length - 1; i++) {
      final double segLen = pts[i].distance(pts[i + 1]);
      if (segLen < minSegLen) {
        minSegLen = segLen;
      }
    }
    return minSegLen;
  }

  /**
   * Computes the snap tolerance based on the input geometries.
   * 
   * @param ringPts
   * @return
   */
  private double computeSnapTolerance(final Coordinate[] ringPts) {
    final double minSegLen = computeMinimumSegmentLength(ringPts);
    // use a small percentage of this to be safe
    final double snapTol = minSegLen / 10;
    return snapTol;
  }

  public List<Coordinates> extractTargetCoordinates(final Geometry g) {
    // TODO: should do this more efficiently. Use CoordSeq filter to get points,
    // KDTree for uniqueness & queries
    final Set<Coordinates> ptSet = new TreeSet<Coordinates>();
    for (final CoordinatesList points : g.getCoordinatesLists()) {
      for (final Coordinates point : new InPlaceIterator(points)) {
        ptSet.add(new DoubleCoordinates(point));
      }
    }

    return new ArrayList<Coordinates>(ptSet);
  }

  /**
   * Snaps the vertices in the component {@link LineString}s of the source
   * geometry to the vertices of the given snap geometry.
   * 
   * @param snapGeom a geometry to snap the source to
   * @return a new snapped Geometry
   */
  public Geometry snapTo(final Geometry snapGeom, final double snapTolerance) {
    final List<Coordinates> snapPts = extractTargetCoordinates(snapGeom);

    final SnapTransformer snapTrans = new SnapTransformer(snapTolerance,
      snapPts);
    return snapTrans.transform(srcGeom);
  }

  /**
   * Snaps the vertices in the component {@link LineString}s of the source
   * geometry to the vertices of the given snap geometry.
   * 
   * @param snapGeom a geometry to snap the source to
   * @return a new snapped Geometry
   */
  public Geometry snapToSelf(final double snapTolerance,
    final boolean cleanResult) {
    final List<Coordinates> snapPts = extractTargetCoordinates(srcGeom);

    final SnapTransformer snapTrans = new SnapTransformer(snapTolerance,
      snapPts, true);
    final Geometry snappedGeom = snapTrans.transform(srcGeom);
    Geometry result = snappedGeom;
    if (cleanResult && result instanceof Polygonal) {
      // TODO: use better cleaning approach
      result = snappedGeom.buffer(0);
    }
    return result;
  }

}

class SnapTransformer extends GeometryTransformer {
  private final double snapTolerance;

  private final List<Coordinates> snapPts;

  private boolean isSelfSnap = false;

  SnapTransformer(final double snapTolerance, final List<Coordinates> snapPts) {
    this.snapTolerance = snapTolerance;
    this.snapPts = snapPts;
  }

  SnapTransformer(final double snapTolerance, final List<Coordinates> snapPts,
    final boolean isSelfSnap) {
    this.snapTolerance = snapTolerance;
    this.snapPts = snapPts;
    this.isSelfSnap = isSelfSnap;
  }

  private List<Coordinates> snapLine(final CoordinatesList srcPts,
    final List<Coordinates> snapPts) {
    final LineStringSnapper snapper = new LineStringSnapper(srcPts,
      snapTolerance);
    snapper.setAllowSnappingToSourceVertices(isSelfSnap);
    return snapper.snapTo(snapPts);
  }

  @Override
  protected Coordinates transformCoordinates(final Coordinates coords,
    final Geometry parent) {
    final List<Coordinates> newPts = snapLine(
      new ListCoordinatesList(coords.getNumAxis(), coords), snapPts);
    return new DoubleCoordinates(newPts.get(0));
  }

  @Override
  protected CoordinatesList transformCoordinates(final CoordinatesList coords,
    final Geometry parent) {
    final List<Coordinates> newPts = snapLine(coords, snapPts);
    return new DoubleCoordinatesList(coords.getNumAxis(), newPts);
  }
}
