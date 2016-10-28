package com.revolsys.geometry.test.function;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineCap;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.PointList;
import com.revolsys.geometry.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.geometry.operation.buffer.BufferParameters;
import com.revolsys.geometry.util.GeometricShapeFactory;

public class JTSFunctions {
  private static final double HEIGHT = 70;

  private static final double WIDTH = 150; // 125;

  private static final double J_WIDTH = 30;

  private static final double J_RADIUS = J_WIDTH - 5;

  private static final double S_RADIUS = HEIGHT / 4;

  private static final double T_WIDTH = WIDTH - 2 * S_RADIUS - J_WIDTH;

  public static String jtsVersion(final Geometry g) {
    return "";
  }

  public static Geometry logoBuffer(final Geometry g, final double distance) {
    final Geometry lines = logoLines(g);
    final BufferParameters bufParams = new BufferParameters();
    bufParams.setEndCapStyle(LineCap.SQUARE);
    return lines.buffer(distance, bufParams);
  }

  public static Geometry logoLines(final Geometry g) {
    return newJ(g).union(newT(g)).union(newS(g));
  }

  private static Geometry newJ(final Geometry g) {
    final GeometryFactory gf = FunctionsUtil.getFactoryOrDefault(g);

    final Point[] jTop = new Point[] {
      new PointDoubleXY(0, HEIGHT), new PointDoubleXY(J_WIDTH, HEIGHT),
      new PointDoubleXY(J_WIDTH, J_RADIUS)
    };
    final Point[] jBottom = new Point[] {
      new PointDoubleXY(J_WIDTH - J_RADIUS, 0), new PointDoubleXY(0, 0)
    };

    final GeometricShapeFactory gsf = new GeometricShapeFactory(gf);
    gsf.setBase(new PointDoubleXY(J_WIDTH - 2 * J_RADIUS, 0));
    gsf.setSize(2 * J_RADIUS);
    gsf.setNumPoints(10);
    final LineString jArc = gsf.newArc(1.5 * Math.PI, 0.5 * Math.PI);

    final PointList coordList = new PointList();
    coordList.add(jTop, false);
    coordList.add(CoordinatesListUtil.getPointArray(jArc.reverse()), false, 1,
      jArc.getVertexCount() - 1);
    coordList.add(jBottom, false);

    return gf.lineString(coordList.toPointArray());
  }

  private static Geometry newS(final Geometry g) {
    final GeometryFactory gf = FunctionsUtil.getFactoryOrDefault(g);

    final double centreX = WIDTH - S_RADIUS;

    final Point[] top = new Point[] {
      new PointDoubleXY(WIDTH, HEIGHT), new PointDoubleXY(centreX, HEIGHT)
    };
    final Point[] bottom = new Point[] {
      new PointDoubleXY(centreX, 0), new PointDoubleXY(WIDTH - 2 * S_RADIUS, 0)
    };

    final GeometricShapeFactory gsf = new GeometricShapeFactory(gf);
    gsf.setCentre(new PointDoubleXY(centreX, HEIGHT - S_RADIUS));
    gsf.setSize(2 * S_RADIUS);
    gsf.setNumPoints(10);
    final LineString arcTop = gsf.newArc(0.5 * Math.PI, Math.PI);

    final GeometricShapeFactory gsf2 = new GeometricShapeFactory(gf);
    gsf2.setCentre(new PointDoubleXY(centreX, S_RADIUS));
    gsf2.setSize(2 * S_RADIUS);
    gsf2.setNumPoints(10);
    final LineString arcBottom = gsf2.newArc(1.5 * Math.PI, Math.PI).reverse();

    final PointList coordList = new PointList();
    coordList.add(top, false);
    coordList.add(CoordinatesListUtil.getPointArray(arcTop), false, 1, arcTop.getVertexCount() - 1);
    coordList.add(new PointDoubleXY(centreX, HEIGHT / 2));
    coordList.add(CoordinatesListUtil.getPointArray(arcBottom), false, 1,
      arcBottom.getVertexCount() - 1);
    coordList.add(bottom, false);

    return gf.lineString(coordList.toPointArray());
  }

  private static Geometry newT(final Geometry g) {
    final GeometryFactory gf = FunctionsUtil.getFactoryOrDefault(g);

    final Point[] tTop = new Point[] {
      new PointDoubleXY(J_WIDTH, HEIGHT), new PointDoubleXY(WIDTH - S_RADIUS - 5, HEIGHT)
    };
    final Point[] tBottom = new Point[] {
      new PointDoubleXY(J_WIDTH + 0.5 * T_WIDTH, HEIGHT),
      new PointDoubleXY(J_WIDTH + 0.5 * T_WIDTH, 0)
    };
    return gf.lineal(new LineString[] {
      gf.lineString(tTop), gf.lineString(tBottom)
    });
  }

}
