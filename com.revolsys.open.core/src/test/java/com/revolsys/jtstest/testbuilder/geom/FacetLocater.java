package com.revolsys.jtstest.testbuilder.geom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;
import com.revolsys.jts.geom.LineSegment;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;

/**
 * Locates the paths to facets (vertices and segments) of 
 * a {@link Geometry} which are within a given tolerance
 * of a query point.
 * 
 *  
 * @author Martin Davis
 *
 */
public class FacetLocater {
  /**
   * Creates a list containing all the vertex {@link GeometryLocation}s
   * in the input collection.
   * 
   * @param locations the source collection
   * @return a list of the vertex locations, if any
   */
  public static List filterVertexLocations(final Collection locations) {
    final ArrayList vertexLocs = new ArrayList();
    for (final Iterator i = locations.iterator(); i.hasNext();) {
      final GeometryLocation loc = (GeometryLocation)i.next();
      if (loc.isVertex()) {
        vertexLocs.add(loc);
      }
    }
    return vertexLocs;
  }

  public static int[] toIntArray(final Vector path) {
    final int[] index = new int[path.size()];
    int i = 0;
    for (final Iterator it = path.iterator(); it.hasNext();) {
      final Integer pathIndex = (Integer)it.next();
      index[i++] = pathIndex.intValue();
    }
    return index;
  }

  private final Geometry parentGeom;

  private final List locations = new ArrayList();

  private Coordinates queryPt;

  private double tolerance = 0.0;

  public FacetLocater(final Geometry parentGeom) {
    this.parentGeom = parentGeom;
  }

  private void findLocations(final Geometry geom, final List locations) {
    findLocations(new Stack(), parentGeom, locations);
  }

  private void findLocations(final Stack path, final Geometry compGeom,
    final CoordinatesList seq, final List locations) {
    findVertexLocations(path, compGeom, seq, locations);
    findSegmentLocations(path, compGeom, seq, locations);
  }

  private void findLocations(final Stack path, final Geometry geom,
    final List locations) {
    if (geom instanceof GeometryCollection) {
      for (int i = 0; i < geom.getNumGeometries(); i++) {
        final Geometry subGeom = geom.getGeometry(i);
        path.push(new Integer(i));
        findLocations(path, subGeom, locations);
        path.pop();
      }
    } else if (geom instanceof Polygon) {
      findLocations(path, (Polygon)geom, locations);

    } else {
      CoordinatesList seq;

      if (geom instanceof LineString) {
        seq = ((LineString)geom).getCoordinatesList();
      } else if (geom instanceof Point) {
        seq = ((Point)geom).getCoordinatesList();
      } else {
        throw new IllegalStateException("Unknown geometry type: "
          + geom.getClass().getName());
      }
      findLocations(path, geom, seq, locations);
    }
  }

  private void findLocations(final Stack path, final Polygon poly,
    final List locations) {
    path.push(new Integer(0));
    findLocations(path, poly.getExteriorRing(), poly.getExteriorRing()
      .getCoordinatesList(), locations);
    path.pop();

    for (int i = 0; i < poly.getNumInteriorRing(); i++) {
      path.push(new Integer(i + 1));
      findLocations(path, poly.getInteriorRingN(i), poly.getInteriorRingN(i)
        .getCoordinatesList(), locations);
      path.pop();
    }
  }

  private void findSegmentLocations(final Stack path, final Geometry compGeom,
    final CoordinatesList seq, final List locations) {
    final LineSegment seg = new LineSegment();
    for (int i = 0; i < seq.size() - 1; i++) {
      seg.setP0(seq.getCoordinate(i));
      seg.setP1(seq.getCoordinate(i + 1));
      final double dist = seg.distance(queryPt);
      if (dist <= tolerance) {
        locations.add(new GeometryLocation(parentGeom, compGeom,
          toIntArray(path), i, false, seg.getP0()));
      }
    }
  }

  private void findVertexLocations(final Stack path, final Geometry compGeom,
    final CoordinatesList seq, final List locations) {
    for (int i = 0; i < seq.size(); i++) {
      final Coordinates p = seq.getCoordinate(i);
      final double dist = p.distance(queryPt);
      if (dist <= tolerance) {
        locations.add(new GeometryLocation(parentGeom, compGeom,
          toIntArray(path), i, true, p));
      }
    }
  }

  public List getLocations(final Coordinates queryPt, final double tolerance) {
    this.queryPt = queryPt;
    this.tolerance = tolerance;
    findLocations(parentGeom, locations);
    return locations;
  }

}
