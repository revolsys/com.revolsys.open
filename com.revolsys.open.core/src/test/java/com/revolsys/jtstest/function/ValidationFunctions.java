package com.revolsys.jtstest.function;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.operation.valid.IsValidOp;
import com.revolsys.jts.operation.valid.TopologyValidationError;

public class ValidationFunctions {
  public static Geometry invalidGeoms(final Geometry g) {
    final List invalidGeoms = new ArrayList();
    for (int i = 0; i < g.getGeometryCount(); i++) {
      final Geometry geom = g.getGeometry(i);
      final IsValidOp ivop = new IsValidOp(geom);
      final TopologyValidationError err = ivop.getValidationError();
      if (err != null) {
        invalidGeoms.add(geom);
      }
    }
    return g.getGeometryFactory().buildGeometry(invalidGeoms);
  }

  /**
   * Validates all geometries in a collection independently.
   * Errors are returned as points at the invalid location
   * 
   * @param g
   * @return the invalid locations, if any
   */
  public static Geometry invalidLocations(final Geometry g) {
    final List invalidLoc = new ArrayList();
    for (int i = 0; i < g.getGeometryCount(); i++) {
      final Geometry geom = g.getGeometry(i);
      final IsValidOp ivop = new IsValidOp(geom);
      final TopologyValidationError err = ivop.getValidationError();
      if (err != null) {
        invalidLoc.add(g.getGeometryFactory().point(err.getCoordinate()));
      }
    }
    return g.getGeometryFactory().buildGeometry(invalidLoc);
  }
}
