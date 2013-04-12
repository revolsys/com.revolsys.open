package com.revolsys.gis.model.geometry.operation.geomgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.revolsys.gis.model.geometry.operation.chain.BasicSegmentString;
import com.revolsys.gis.model.geometry.util.TopologyException;

/**
 * Validates that a collection of {@link Edge}s is correctly noded.
 * Throws an appropriate exception if an noding error is found.
 *
 * @version 1.7
 */
public class EdgeNodingValidator {
  /**
   * Checks whether the supplied {@link Edge}s
   * are correctly noded.  
   * Throws a  {@link TopologyException} if they are not.
   * 
   * @param edges a collection of Edges.
   * @throws TopologyException if the SegmentStrings are not correctly noded
   *
   */
  public static void checkValid(final Collection edges) {
    final EdgeNodingValidator validator = new EdgeNodingValidator(edges);
    validator.checkValid();
  }

  public static Collection toSegmentStrings(final Collection edges) {
    // convert Edges to SegmentStrings
    final Collection segStrings = new ArrayList();
    for (final Iterator i = edges.iterator(); i.hasNext();) {
      final Edge e = (Edge)i.next();
      segStrings.add(new BasicSegmentString(e.getCoordinates(), e));
    }
    return segStrings;
  }

  private final FastNodingValidator nv;

  /**
   * Creates a new validator for the given collection of {@link Edge}s.
   * 
   * @param edges a collection of Edges.
   */
  public EdgeNodingValidator(final Collection edges) {
    nv = new FastNodingValidator(toSegmentStrings(edges));
  }

  /**
   * Checks whether the supplied edges
   * are correctly noded.  Throws an exception if they are not.
   * 
   * @throws TopologyException if the SegmentStrings are not correctly noded
   *
   */
  public void checkValid() {
    nv.checkValid();
  }

}
