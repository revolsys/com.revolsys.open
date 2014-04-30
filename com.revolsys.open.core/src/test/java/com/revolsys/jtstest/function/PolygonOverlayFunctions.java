package com.revolsys.jtstest.function;

import java.util.Collection;
import java.util.List;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.PrecisionModel;
import com.revolsys.jts.noding.snapround.GeometryNoder;
import com.revolsys.jts.operation.polygonize.Polygonizer;

public class PolygonOverlayFunctions {

  public static Geometry overlaySnapRounded(final Geometry g1,
    final Geometry g2, final double precisionTol) {
    final PrecisionModel pm = new PrecisionModel(precisionTol);
    final GeometryFactory geometryFactory = g1.getGeometryFactory();

    final List<LineString> lines = g1.getGeometries(LineString.class);
    // add second input's linework, if any
    if (g2 != null) {
      lines.addAll(g2.getGeometries(LineString.class));
    }
    final List nodedLinework = new GeometryNoder(pm).node(lines);
    // union the noded linework to remove duplicates
    final Geometry nodedDedupedLinework = geometryFactory.buildGeometry(nodedLinework)
      .union();

    // polygonize the result
    final Polygonizer polygonizer = new Polygonizer();
    polygonizer.add(nodedDedupedLinework);
    final Collection polys = polygonizer.getPolygons();

    // convert to collection for return
    return geometryFactory.geometryCollection(polys);
  }

}
