package com.revolsys.geometry.test.function;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.geometry.model.impl.PointDouble;
import com.revolsys.geometry.model.util.AffineTransformation;
import com.revolsys.geometry.model.util.AffineTransformationFactory;

public class AffineTransformationFunctions {
  private static Point envelopeCentre(final Geometry g) {
    return g.getBoundingBox().getCentre();
  }

  private static Point envelopeLowerLeft(final Geometry g) {
    final BoundingBox env = g.getBoundingBox();
    return new PointDouble(env.getMinX(), env.getMinY());
  }

  public static Geometry reflectInX(final Geometry g) {
    final Point centre = envelopeCentre(g);
    final AffineTransformation trans = AffineTransformation.scaleInstance(1, -1, centre.getX(),
      centre.getY());
    return trans.transform(g);
  }

  public static Geometry reflectInY(final Geometry g) {
    final Point centre = envelopeCentre(g);
    final AffineTransformation trans = AffineTransformation.scaleInstance(-1, 1, centre.getX(),
      centre.getY());
    return trans.transform(g);
  }

  public static Geometry rotate(final Geometry g, final double angle) {
    final Point centre = envelopeCentre(g);
    final AffineTransformation trans = AffineTransformation.rotationInstance(angle, centre.getX(),
      centre.getY());
    return trans.transform(g);
  }

  public static Geometry rotateByPiMultiple(final Geometry g, final double multipleOfPi) {
    final Point centre = envelopeCentre(g);
    final AffineTransformation trans = AffineTransformation.rotationInstance(multipleOfPi * Math.PI,
      centre.getX(), centre.getY());
    return trans.transform(g);
  }

  public static Geometry scale(final Geometry g, final double scale) {
    final Point centre = envelopeCentre(g);
    final AffineTransformation trans = AffineTransformation.scaleInstance(scale, scale,
      centre.getX(), centre.getY());
    return trans.transform(g);
  }

  public static Geometry transformByBaseline(final Geometry g, final Geometry destBaseline) {
    final BoundingBox env = g.getBoundingBox();
    final Point src0 = new PointDouble(env.getMinX(), env.getMinY());
    final Point src1 = new PointDouble(env.getMaxX(), env.getMinY());

    final Point[] destPts = CoordinatesListUtil.getPointArray(destBaseline);
    final Point dest0 = destPts[0];
    final Point dest1 = destPts[1];
    final AffineTransformation trans = AffineTransformationFactory.newFromBaseLines(src0, src1,
      dest0, dest1);
    return trans.transform(g);
  }

  public static Geometry transformByVectors(final Geometry g, final Geometry control) {
    final int nControl = control.getGeometryCount();
    final Point src[] = new Point[nControl];
    final Point dest[] = new Point[nControl];
    for (int i = 0; i < nControl; i++) {
      final Geometry contComp = control.getGeometry(i);
      final Point[] pts = CoordinatesListUtil.getPointArray(contComp);
      src[i] = pts[0];
      dest[i] = pts[1];
    }
    final AffineTransformation trans = AffineTransformationFactory.newFromControlVectors(src, dest);
    System.out.println(trans);
    return trans.transform(g);
  }

  public static Geometry translateCentreToOrigin(final Geometry g) {
    final Point centre = envelopeCentre(g);
    final AffineTransformation trans = AffineTransformation.translationInstance(-centre.getX(),
      -centre.getY());
    return trans.transform(g);
  }

  public static Geometry translateToOrigin(final Geometry g) {
    final Point lowerLeft = envelopeLowerLeft(g);
    final AffineTransformation trans = AffineTransformation.translationInstance(-lowerLeft.getX(),
      -lowerLeft.getY());
    return trans.transform(g);
  }
}
