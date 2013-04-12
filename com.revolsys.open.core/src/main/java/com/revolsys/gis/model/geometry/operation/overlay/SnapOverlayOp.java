package com.revolsys.gis.model.geometry.operation.overlay;

import com.revolsys.gis.model.geometry.Geometry;

/**
 * Performs an overlay operation using snapping and enhanced precision
 * to improve the robustness of the result.
 * This class <i>always</i> uses snapping.  
 * This is less performant than the standard JTS overlay code, 
 * and may even introduce errors which were not present in the original data.
 * For this reason, this class should only be used 
 * if the standard overlay code fails to produce a correct result. 
 *  
 * @author Martin Davis
 * @version 1.7
 */
public class SnapOverlayOp {
  public static Geometry difference(final Geometry g0, final Geometry g1) {
    return overlayOp(g0, g1, OverlayOp.DIFFERENCE);
  }

  public static Geometry intersection(final Geometry g0, final Geometry g1) {
    return overlayOp(g0, g1, OverlayOp.INTERSECTION);
  }

  public static Geometry overlayOp(final Geometry g0, final Geometry g1,
    final int opCode) {
    final SnapOverlayOp op = new SnapOverlayOp(g0, g1);
    return op.getResultGeometry(opCode);
  }

  public static Geometry symDifference(final Geometry g0, final Geometry g1) {
    return overlayOp(g0, g1, OverlayOp.SYMDIFFERENCE);
  }

  public static Geometry union(final Geometry g0, final Geometry g1) {
    return overlayOp(g0, g1, OverlayOp.UNION);
  }

  private final Geometry[] geom = new Geometry[2];

  private double snapTolerance;

  private CommonBitsRemover cbr;

  public SnapOverlayOp(final Geometry g1, final Geometry g2) {
    geom[0] = g1;
    geom[1] = g2;
    computeSnapTolerance();
  }

  private void computeSnapTolerance() {
    snapTolerance = GeometrySnapper.computeOverlaySnapTolerance(geom[0],
      geom[1]);

    // System.out.println("Snap tol = " + snapTolerance);
  }

  public Geometry getResultGeometry(final int opCode) {
    // Geometry[] selfSnapGeom = new Geometry[] { selfSnap(geom[0]),
    // selfSnap(geom[1])};
    final Geometry[] prepGeom = snap(geom);
    final Geometry result = OverlayOp.overlayOp(prepGeom[0], prepGeom[1],
      opCode);
    return prepareResult(result);
  }

  private Geometry prepareResult(final Geometry geom) {
    cbr.addCommonBits(geom);
    return geom;
  }

  private Geometry[] removeCommonBits(final Geometry[] geom) {
    cbr = new CommonBitsRemover();
    cbr.add(geom[0]);
    cbr.add(geom[1]);
    final Geometry remGeom[] = new Geometry[2];
    remGeom[0] = cbr.removeCommonBits((Geometry)geom[0].clone());
    remGeom[1] = cbr.removeCommonBits((Geometry)geom[1].clone());
    return remGeom;
  }

  private Geometry selfSnap(final Geometry geom) {
    final GeometrySnapper snapper0 = new GeometrySnapper(geom);
    final Geometry snapGeom = snapper0.snapTo(geom, snapTolerance);
    // System.out.println("Self-snapped: " + snapGeom);
    // System.out.println();
    return snapGeom;
  }

  private Geometry[] snap(final Geometry[] geom) {
    final Geometry[] remGeom = removeCommonBits(geom);

    // MD - testing only
    // Geometry[] remGeom = geom;

    final Geometry[] snapGeom = GeometrySnapper.snap(remGeom[0], remGeom[1],
      snapTolerance);
    // MD - may want to do this at some point, but it adds cycles
    // checkValid(snapGeom[0]);
    // checkValid(snapGeom[1]);

    /*
     * System.out.println("Snapped geoms: "); System.out.println(snapGeom[0]);
     * System.out.println(snapGeom[1]);
     */
    return snapGeom;
  }

}
