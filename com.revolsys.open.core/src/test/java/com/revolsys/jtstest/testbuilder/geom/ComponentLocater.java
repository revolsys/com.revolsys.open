package com.revolsys.jtstest.testbuilder.geom;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;

/**
 * Locates the components of a Geometry
 * which lie in a target area.
 * 
 * @author Martin Davis
 * @see FacetLocater
 */
public class ComponentLocater {

  private final Geometry parentGeom;

  private final List components = new ArrayList();

  private Geometry aoi;

  public ComponentLocater(final Geometry parentGeom) {
    this.parentGeom = parentGeom;
  }

  private Geometry createAOI(final Coordinates queryPt, final double tolerance) {
    final Envelope env = new Envelope(queryPt);
    env.expandBy(2 * tolerance);
    return parentGeom.getGeometryFactory().toGeometry(env);
  }

  private void findComponents(final Stack path, final Geometry geom,
    final List components) {
    if (geom instanceof GeometryCollection) {
      for (int i = 0; i < geom.getGeometryCount(); i++) {
        final Geometry subGeom = geom.getGeometry(i);
        path.push(new Integer(i));
        findComponents(path, subGeom, components);
        path.pop();
      }
      return;
    }
    // TODO: make this robust - do not use Geometry.intersects()
    // atomic component - check for match
    if (aoi.intersects(geom)) {
      components.add(new GeometryLocation(parentGeom, geom,
        FacetLocater.toIntArray(path)));
    }
  }

  /**
   * 
   * @param queryPt
   * @param tolerance
   * @return a List of the component Geometrys
   */
  public List getComponents(final Coordinates queryPt, final double tolerance) {
    // Coordinates queryPt = queryPt;
    // this.tolerance = tolerance;
    aoi = createAOI(queryPt, tolerance);
    return getComponents(aoi);
  }

  public List getComponents(final Geometry aoi) {
    // Coordinates queryPt = queryPt;
    // this.tolerance = tolerance;
    this.aoi = aoi;
    findComponents(new Stack(), parentGeom, components);
    return components;
  }

}
