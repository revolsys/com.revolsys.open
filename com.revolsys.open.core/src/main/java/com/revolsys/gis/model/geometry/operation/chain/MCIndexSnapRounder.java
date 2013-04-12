package com.revolsys.gis.model.geometry.operation.chain;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.geometry.operation.geomgraph.index.LineIntersector;
import com.revolsys.gis.model.geometry.operation.geomgraph.index.RobustLineIntersector;
import com.revolsys.gis.model.geometry.operation.noding.snapround.HotPixel;
import com.revolsys.gis.model.geometry.operation.noding.snapround.IntersectionFinderAdder;
import com.revolsys.gis.model.geometry.operation.noding.snapround.MCIndexPointSnapper;
import com.revolsys.gis.model.geometry.operation.noding.snapround.NodingValidator;

/**
 * Uses Snap Rounding to compute a rounded,
 * fully noded arrangement from a set of {@link SegmentString}s.
 * Implements the Snap Rounding technique described in 
 * papers by Hobby, Guibas & Marimont, and Goodrich et al.
 * Snap Rounding assumes that all vertices lie on a uniform grid;
 * hence the precision model of the input must be fixed precision,
 * and all the input vertices must be rounded to that precision.
 * <p>
 * This implementation uses a monotone chains and a spatial index to
 * speed up the intersection tests.
 * <p>
 * This implementation appears to be fully robust using an integer precision model.
 * It will function with non-integer precision models, but the
 * results are not 100% guaranteed to be correctly noded.
 *
 * @version 1.7
 */
public class MCIndexSnapRounder implements Noder {
  private final CoordinatesPrecisionModel pm;

  private final LineIntersector li;

  private final double scaleFactor;

  private MCIndexNoder noder;

  private MCIndexPointSnapper pointSnapper;

  private Collection nodedSegStrings;

  public MCIndexSnapRounder(final CoordinatesPrecisionModel pm) {
    this.pm = pm;
    li = new RobustLineIntersector();
    li.setPrecisionModel(pm);
    scaleFactor = pm.getScaleXY();
  }

  private void checkCorrectness(final Collection inputSegmentStrings) {
    final Collection resultSegStrings = NodedSegmentString.getNodedSubstrings(inputSegmentStrings);
    final NodingValidator nv = new NodingValidator(resultSegStrings);
    try {
      nv.checkValid();
    } catch (final Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Computes nodes introduced as a result of snapping segments to snap points (hot pixels)
   */
  private void computeIntersectionSnaps(final Collection snapPts) {
    for (final Iterator it = snapPts.iterator(); it.hasNext();) {
      final Coordinates snapPt = (Coordinates)it.next();
      final HotPixel hotPixel = new HotPixel(snapPt, scaleFactor, li);
      pointSnapper.snap(hotPixel);
    }
  }

  @Override
  public void computeNodes(final Collection inputSegmentStrings) {
    this.nodedSegStrings = inputSegmentStrings;
    noder = new MCIndexNoder();
    pointSnapper = new MCIndexPointSnapper(noder.getMonotoneChains(),
      noder.getIndex());
    snapRound(inputSegmentStrings, li);

    // testing purposes only - remove in final version
    // checkCorrectness(inputSegmentStrings);
  }

  /**
   * Computes nodes introduced as a result of
   * snapping segments to vertices of other segments
   *
   * @param edges the list of segment strings to snap together
   */
  public void computeVertexSnaps(final Collection edges) {
    for (final Iterator i0 = edges.iterator(); i0.hasNext();) {
      final NodedSegmentString edge0 = (NodedSegmentString)i0.next();
      computeVertexSnaps(edge0);
    }
  }

  /**
   * Performs a brute-force comparison of every segment in each {@link SegmentString}.
   * This has n^2 performance.
   */
  private void computeVertexSnaps(final NodedSegmentString e) {
    final CoordinatesList pts0 = e.getCoordinates();
    for (int i = 0; i < pts0.size() - 1; i++) {
      final HotPixel hotPixel = new HotPixel(pts0.get(i), scaleFactor, li);
      final boolean isNodeAdded = pointSnapper.snap(hotPixel, e, i);
      // if a node is created for a vertex, that vertex must be noded too
      if (isNodeAdded) {
        e.addIntersection(pts0.get(i), i);
      }
    }
  }

  /**
   * Computes all interior intersections in the collection of {@link SegmentString}s,
   * and returns their @link Coordinates}s.
   *
   * Does NOT node the segStrings.
   *
   * @return a list of Coordinates for the intersections
   */
  private List findInteriorIntersections(final Collection segStrings,
    final LineIntersector li) {
    final IntersectionFinderAdder intFinderAdder = new IntersectionFinderAdder(
      li);
    noder.setSegmentIntersector(intFinderAdder);
    noder.computeNodes(segStrings);
    return intFinderAdder.getInteriorIntersections();
  }

  @Override
  public Collection getNodedSubstrings() {
    return NodedSegmentString.getNodedSubstrings(nodedSegStrings);
  }

  private void snapRound(final Collection segStrings, final LineIntersector li) {
    final List intersections = findInteriorIntersections(segStrings, li);
    computeIntersectionSnaps(intersections);
    computeVertexSnaps(segStrings);
  }

}
