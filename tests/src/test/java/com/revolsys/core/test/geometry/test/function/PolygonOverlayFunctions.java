package com.revolsys.core.test.geometry.test.function;

import java.util.List;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.noding.snapround.GeometryNoder;
import com.revolsys.geometry.operation.polygonize.Polygonizer;

public class PolygonOverlayFunctions {

  public static Geometry overlaySnapRounded(final Geometry g1, final Geometry g2,
    final double precisionTol) {
    final GeometryFactory geometryFactory = g1.getGeometryFactory();

    final List<LineString> lines = g1.getGeometries(LineString.class);
    // add second input's linework, if any
    if (g2 != null) {
      lines.addAll(g2.getGeometries(LineString.class));
    }
    final List nodedLinework = new GeometryNoder(precisionTol).node(lines);
    // union the noded linework to remove duplicates
    final Geometry nodedDedupedLinework = geometryFactory.buildGeometry(nodedLinework).union();

    // polygonize the result
    final Polygonizer polygonizer = new Polygonizer();
    polygonizer.add(nodedDedupedLinework);
    final List<Polygon> polys = polygonizer.getPolygons();

    // convert to collection for return
    return geometryFactory.polygonal(polys);
  }

}
