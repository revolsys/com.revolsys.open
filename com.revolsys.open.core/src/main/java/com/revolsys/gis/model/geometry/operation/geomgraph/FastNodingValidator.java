package com.revolsys.gis.model.geometry.operation.geomgraph;

import java.util.Collection;
import java.util.List;

import com.revolsys.gis.model.geometry.operation.chain.MCIndexNoder;
import com.revolsys.gis.model.geometry.operation.chain.SegmentString;
import com.revolsys.gis.model.geometry.operation.geomgraph.index.LineIntersector;
import com.revolsys.gis.model.geometry.operation.geomgraph.index.RobustLineIntersector;
import com.revolsys.gis.model.geometry.util.TopologyException;
import com.revolsys.jts.geom.Coordinates;

/**
 * Validates that a collection of {@link SegmentString}s is correctly noded.
 * Indexing is used to improve performance. In the most common use case,
 * validation stops after a single non-noded intersection is detected. Does NOT
 * check a-b-a collapse situations. Also does not check for endpoint-interior
 * vertex intersections. This should not be a problem, since the noders should
 * be able to compute intersections between vertices correctly.
 * <p>
 * The client may either test the {@link #isValid} condition, or request that a
 * suitable {@link TopologyException} be thrown.
 * 
 * @version 1.7
 */
public class FastNodingValidator {
  private final LineIntersector li = new RobustLineIntersector();

  private final Collection segStrings;

  private boolean findAllIntersections = false;

  private InteriorIntersectionFinder segInt = null;

  private boolean isValid = true;

  /**
   * Creates a new noding validator for a given set of linework.
   * 
   * @param segStrings a collection of {@link SegmentString}s
   */
  public FastNodingValidator(final Collection segStrings) {
    this.segStrings = segStrings;
  }

  private void checkInteriorIntersections() {
    /**
     * MD - It may even be reliable to simply check whether end segments (of
     * SegmentStrings) have an interior intersection, since noding should have
     * split any true interior intersections already.
     */
    isValid = true;
    segInt = new InteriorIntersectionFinder(li);
    segInt.setFindAllIntersections(findAllIntersections);
    final MCIndexNoder noder = new MCIndexNoder();
    noder.setSegmentIntersector(segInt);
    noder.computeNodes(segStrings);
    if (segInt.hasIntersection()) {
      isValid = false;
      return;
    }
  }

  /**
   * Checks for an intersection and throws a TopologyException if one is found.
   * 
   * @throws TopologyException if an intersection is found
   */
  public void checkValid() {
    execute();
    if (!isValid) {
      throw new TopologyException(getErrorMessage(),
        segInt.getInteriorIntersection());
    }
  }

  private void execute() {
    if (segInt != null) {
      return;
    }
    checkInteriorIntersections();
  }

  /**
   * Returns an error message indicating the segments containing the
   * intersection.
   * 
   * @return an error message documenting the intersection location
   */
  public String getErrorMessage() {
    if (isValid) {
      return "no intersections found";
    }

    final Coordinates[] intSegs = segInt.getIntersectionSegments();
    return "found non-noded intersection between " + intSegs[0] + ","
      + intSegs[1] + " and " + intSegs[2] + " " + intSegs[3];
  }

  public List getIntersections() {
    return segInt.getIntersections();
  }

  /**
   * Checks for an intersection and reports if one is found.
   * 
   * @return true if the arrangement contains an interior intersection
   */
  public boolean isValid() {
    execute();
    return isValid;
  }

  public void setFindAllIntersections(final boolean findAllIntersections) {
    this.findAllIntersections = findAllIntersections;
  }

}
