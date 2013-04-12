package com.revolsys.gis.model.geometry.operation.overlay;

import com.revolsys.gis.model.geometry.Geometry;
import com.revolsys.gis.model.geometry.util.TopologyException;

/**
 * Performs an overlay operation using snapping and enhanced precision to
 * improve the robustness of the result. This class only uses snapping if an
 * error is detected when running the standard JTS overlay code. Errors detected
 * include thrown exceptions (in particular, {@link TopologyException}) and
 * invalid overlay computations.
 * 
 * @author Martin Davis
 * @version 1.7
 */
public class SnapIfNeededOverlayOp {
  public static Geometry difference(final Geometry g0, final Geometry g1) {
    return overlayOp(g0, g1, OverlayOp.DIFFERENCE);
  }

  public static Geometry intersection(final Geometry g0, final Geometry g1) {
    return overlayOp(g0, g1, OverlayOp.INTERSECTION);
  }

  public static Geometry overlayOp(final Geometry geometry1,
    final Geometry geometry2, final int opCode) {
    Geometry result = null;
    boolean isSuccess = false;
    RuntimeException savedException = null;
    try {
      result = OverlayOp.overlayOp(geometry1, geometry2, opCode);
      final boolean isValid = true;
      // not needed if noding validation is used
      // boolean isValid = OverlayResultValidator.isValid(geom[0], geom[1],
      // OverlayOp.INTERSECTION, result);
      if (isValid) {
        isSuccess = true;
      }

    } catch (final RuntimeException ex) {
      savedException = ex;
      // ignore this exception, since the operation will be rerun
      // System.out.println(ex.getMessage());
      // ex.printStackTrace();
      // System.out.println(ex.getMessage());
      // System.out.println("Geom 0: " + geom[0]);
      // System.out.println("Geom 1: " + geom[1]);
    }
    if (!isSuccess) {
      // this may still throw an exception
      // if so, throw the original exception since it has the input coordinates
      try {
        result = SnapOverlayOp.overlayOp(geometry1, geometry2, opCode);
      } catch (final RuntimeException ex) {
        throw savedException;
      }
    }
    return result;
  }

  public static Geometry symDifference(final Geometry g0, final Geometry g1) {
    return overlayOp(g0, g1, OverlayOp.SYMDIFFERENCE);
  }

  public static Geometry union(final Geometry g0, final Geometry g1) {
    return overlayOp(g0, g1, OverlayOp.UNION);
  }
}
