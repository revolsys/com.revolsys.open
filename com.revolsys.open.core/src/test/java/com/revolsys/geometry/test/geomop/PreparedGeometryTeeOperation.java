package com.revolsys.geometry.test.geomop;

import com.revolsys.geometry.model.Geometry;

public class PreparedGeometryTeeOperation extends TeeGeometryOperation {
  static class GeometryOp {
    public static boolean contains(final Geometry g1, final Geometry g2) {
      final Geometry prepGeom = g1.prepare();
      return prepGeom.contains(g2);
    }

    public static boolean covers(final Geometry g1, final Geometry g2) {
      final Geometry prepGeom = g1.prepare();
      return prepGeom.contains(g2);
    }

    public static boolean intersects(final Geometry g1, final Geometry g2) {
      final Geometry prepGeom = g1.prepare();
      return prepGeom.intersects(g2);
    }
  }

  private static boolean containsProperly(final Geometry g1, final Geometry g2) {
    return g1.relate(g2, "T**FF*FF*");
  }

  public PreparedGeometryTeeOperation() {
    super();
  }

  /**
   * Creates a new operation which chains to the given {@link GeometryMethodOperation}
   * for non-intercepted methods.
   *
   * @param chainOp the operation to chain to
   */
  public PreparedGeometryTeeOperation(final GeometryMethodOperation chainOp) {
    super(chainOp);
  }

  private void checkAllPrepOps(final Geometry g1, final Geometry g2) {
    final Geometry prepGeom = g1.prepare();

    checkIntersects(g1, prepGeom, g2);
    checkContains(g1, prepGeom, g2);
    checkContainsProperly(g1, prepGeom, g2);
    checkCovers(g1, prepGeom, g2);
  }

  private void checkContains(final Geometry g1, final Geometry pg, final Geometry g2) {
    final boolean pgResult = pg.contains(g2);
    final boolean expected = g1.contains(g2);

    if (pgResult != expected) {
      throw new IllegalStateException("Geometry.contains result does not match expected");
    }

    // System.out.println("Results match!");
  }

  private void checkContainsProperly(final Geometry g1, final Geometry pg, final Geometry g2) {
    final boolean pgResult = pg.containsProperly(g2);
    final boolean expected = containsProperly(g1, g2);

    if (pgResult != expected) {
      throw new IllegalStateException("Geometry.containsProperly result does not match expected");
    }

    // System.out.println("Results match!");
  }

  private void checkCovers(final Geometry g1, final Geometry pg, final Geometry g2) {
    final boolean pgResult = pg.covers(g2);
    final boolean expected = g1.covers(g2);

    if (pgResult != expected) {
      throw new IllegalStateException("Geometry.covers result does not match expected");
    }

    // System.out.println("Results match!");
  }

  private void checkIntersects(final Geometry g1, final Geometry pg, final Geometry g2) {
    final boolean pgResult = pg.intersects(g2);
    final boolean expected = g1.intersects(g2);

    if (pgResult != expected) {
      // pg.intersects(g2);
      throw new IllegalStateException("Geometry.intersects result does not match expected");
    }

    // System.out.println("Results match!");
  }

  @Override
  protected void runTeeOp(final String opName, final Geometry geometry, final Object[] args) {
    if (args.length < 1) {
      return;
    }
    if (!(args[0] instanceof Geometry)) {
      return;
    }
    final Geometry g2 = (Geometry)args[0];

    if (!geometry.isValid()) {
      throw new IllegalStateException("Input geometry A is not valid");
    }
    if (!g2.isValid()) {
      throw new IllegalStateException("Input geometry B is not valid");
    }

    checkAllPrepOps(geometry, g2);
    checkAllPrepOps(g2, geometry);
  }

}
