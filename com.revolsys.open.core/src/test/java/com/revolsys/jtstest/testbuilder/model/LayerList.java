package com.revolsys.jtstest.testbuilder.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jtstest.testbuilder.geom.ComponentLocater;
import com.revolsys.jtstest.testbuilder.geom.GeometryLocation;

public class LayerList {
  public static final int LYR_A = 0;

  public static final int LYR_B = 1;

  public static final int LYR_RESULT = 2;

  private final Layer[] layer = new Layer[3];

  public LayerList() {
    layer[0] = new Layer("A");
    layer[1] = new Layer("B");
    layer[2] = new Layer("Result");
  }

  private Geometry extractComponents(final Geometry parentGeom,
    final Geometry aoi) {
    final ComponentLocater locater = new ComponentLocater(parentGeom);
    final List locs = locater.getComponents(aoi);
    final List geoms = extractLocationGeometry(locs);
    if (geoms.size() <= 0) {
      return null;
    }
    if (geoms.size() == 1) {
      return (Geometry)geoms.get(0);
    }
    // if parent was a GC, ensure returning a GC
    if (parentGeom.getGeometryType().equals("GeometryCollection")) {
      return parentGeom.getGeometryFactory().geometryCollection(
        GeometryFactory.toGeometryArray(geoms));
    }
    // otherwise return MultiGeom
    return parentGeom.getGeometryFactory().buildGeometry(geoms);
  }

  private List extractLocationGeometry(final List locs) {
    final List geoms = new ArrayList();
    for (final Iterator i = locs.iterator(); i.hasNext();) {
      final GeometryLocation loc = (GeometryLocation)i.next();
      geoms.add(loc.getComponent());
    }
    return geoms;
  }

  /**
   * 
   * @param pt
   * @param tolerance
   * @return component found, or null
   */
  public Geometry getComponent(final Coordinates pt, final double tolerance) {
    for (int i = 0; i < size(); i++) {

      final Layer lyr = getLayer(i);
      final Geometry geom = lyr.getGeometry();
      if (geom == null) {
        continue;
      }
      final ComponentLocater locater = new ComponentLocater(geom);
      final List locs = locater.getComponents(pt, tolerance);
      if (locs.size() > 0) {
        final GeometryLocation loc = (GeometryLocation)locs.get(0);
        return loc.getComponent();
      }
    }
    return null;
  }

  public Geometry[] getComponents(final Geometry aoi) {
    final Geometry comp[] = new Geometry[2];
    for (int i = 0; i < 2; i++) {
      final Layer lyr = getLayer(i);
      final Geometry geom = lyr.getGeometry();
      if (geom == null) {
        continue;
      }
      comp[i] = extractComponents(geom, aoi);
    }
    return comp;
  }

  public Layer getLayer(final int i) {
    return layer[i];
  }

  public int size() {
    return layer.length;
  }
}
