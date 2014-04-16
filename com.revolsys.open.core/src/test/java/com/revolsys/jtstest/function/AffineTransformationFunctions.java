package com.revolsys.jtstest.function;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.util.AffineTransformation;
import com.revolsys.jts.geom.util.AffineTransformationFactory;

public class AffineTransformationFunctions {
  private static Coordinates envelopeCentre(final Geometry g) {
    return g.getBoundingBox().centre();
  }

  private static Coordinates envelopeLowerLeft(final Geometry g) {
    final BoundingBox env = g.getBoundingBox();
    return new Coordinate(env.getMinX(), env.getMinY());
  }

  public static Geometry reflectInX(final Geometry g) {
    final Coordinates centre = envelopeCentre(g);
    final AffineTransformation trans = AffineTransformation.scaleInstance(1,
      -1, centre.getX(), centre.getY());
    return trans.transform(g);
  }

  public static Geometry reflectInY(final Geometry g) {
    final Coordinates centre = envelopeCentre(g);
    final AffineTransformation trans = AffineTransformation.scaleInstance(-1,
      1, centre.getX(), centre.getY());
    return trans.transform(g);
  }

  public static Geometry rotate(final Geometry g, final double angle) {
    final Coordinates centre = envelopeCentre(g);
    final AffineTransformation trans = AffineTransformation.rotationInstance(
      angle, centre.getX(), centre.getY());
    return trans.transform(g);
  }

  public static Geometry rotateByPiMultiple(final Geometry g,
    final double multipleOfPi) {
    final Coordinates centre = envelopeCentre(g);
    final AffineTransformation trans = AffineTransformation.rotationInstance(
      multipleOfPi * Math.PI, centre.getX(), centre.getY());
    return trans.transform(g);
  }

  public static Geometry scale(final Geometry g, final double scale) {
    final Coordinates centre = envelopeCentre(g);
    final AffineTransformation trans = AffineTransformation.scaleInstance(
      scale, scale, centre.getX(), centre.getY());
    return trans.transform(g);
  }

  public static Geometry transformByBaseline(final Geometry g,
    final Geometry destBaseline) {
    final BoundingBox env = g.getBoundingBox();
    final Coordinates src0 = new Coordinate(env.getMinX(), env.getMinY());
    final Coordinates src1 = new Coordinate(env.getMaxX(), env.getMinY());

    final Coordinates[] destPts = destBaseline.getCoordinateArray();
    final Coordinates dest0 = destPts[0];
    final Coordinates dest1 = destPts[1];
    final AffineTransformation trans = AffineTransformationFactory.createFromBaseLines(
      src0, src1, dest0, dest1);
    return trans.transform(g);
  }

  public static Geometry transformByVectors(final Geometry g,
    final Geometry control) {
    final int nControl = control.getNumGeometries();
    final Coordinates src[] = new Coordinates[nControl];
    final Coordinates dest[] = new Coordinates[nControl];
    for (int i = 0; i < nControl; i++) {
      final Geometry contComp = control.getGeometry(i);
      final Coordinates[] pts = contComp.getCoordinateArray();
      src[i] = pts[0];
      dest[i] = pts[1];
    }
    final AffineTransformation trans = AffineTransformationFactory.createFromControlVectors(
      src, dest);
    System.out.println(trans);
    return trans.transform(g);
  }

  public static Geometry translateCentreToOrigin(final Geometry g) {
    final Coordinates centre = envelopeCentre(g);
    final AffineTransformation trans = AffineTransformation.translationInstance(
      -centre.getX(), -centre.getY());
    return trans.transform(g);
  }

  public static Geometry translateToOrigin(final Geometry g) {
    final Coordinates lowerLeft = envelopeLowerLeft(g);
    final AffineTransformation trans = AffineTransformation.translationInstance(
      -lowerLeft.getX(), -lowerLeft.getY());
    return trans.transform(g);
  }
}
