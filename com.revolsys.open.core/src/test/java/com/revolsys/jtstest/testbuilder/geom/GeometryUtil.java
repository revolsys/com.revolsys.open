package com.revolsys.jtstest.testbuilder.geom;

import com.revolsys.jts.geom.*;

public class GeometryUtil {

  public static String structureSummary(Geometry g)
  {
    String structure = "";
    if (g instanceof Polygon) {
      structure = ((Polygon) g).getNumInteriorRing() + " holes" ;
    }
    else if (g instanceof GeometryCollection)
      structure = g.getNumGeometries() + " elements";

    return
    g.getGeometryType().toUpperCase() 
    +  " - " + structure
    + (structure.length() > 0 ? ", " : "")
    + g.getVertexCount() + " pts";
  }

}
