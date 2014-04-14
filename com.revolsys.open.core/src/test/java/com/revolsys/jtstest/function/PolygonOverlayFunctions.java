package com.revolsys.jtstest.function;

import java.util.Collection;
import java.util.List;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.geom.PrecisionModel;
import com.revolsys.jts.geom.util.LinearComponentExtracter;
import com.revolsys.jts.noding.snapround.GeometryNoder;
import com.revolsys.jts.operation.polygonize.Polygonizer;

public class PolygonOverlayFunctions {

  public static Geometry overlaySnapRounded(final Geometry g1,
    final Geometry g2, final double precisionTol) {
    final PrecisionModel pm = new PrecisionModel(precisionTol);
    final GeometryFactory geomFact = g1.getGeometryFactory();

    final List lines = LinearComponentExtracter.getLines(g1);
    // add second input's linework, if any
    if (g2 != null) {
      LinearComponentExtracter.getLines(g2, lines);
    }
    final List nodedLinework = new GeometryNoder(pm).node(lines);
    // union the noded linework to remove duplicates
    final Geometry nodedDedupedLinework = geomFact.buildGeometry(nodedLinework)
      .union();

    // polygonize the result
    final Polygonizer polygonizer = new Polygonizer();
    polygonizer.add(nodedDedupedLinework);
    final Collection polys = polygonizer.getPolygons();

    // convert to collection for return
    final Polygon[] polyArray = GeometryFactory.toPolygonArray(polys);
    return geomFact.createGeometryCollection(polyArray);
  }

}
