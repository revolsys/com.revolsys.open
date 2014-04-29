package com.revolsys.jtstest.function;

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.CoordinateList;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.operation.buffer.Buffer;
import com.revolsys.jts.operation.buffer.BufferParameters;
import com.revolsys.jts.util.GeometricShapeFactory;

public class JTSFunctions {
  private static final double HEIGHT = 70;

  private static final double WIDTH = 150; // 125;

  private static final double J_WIDTH = 30;

  private static final double J_RADIUS = J_WIDTH - 5;

  private static final double S_RADIUS = HEIGHT / 4;

  private static final double T_WIDTH = WIDTH - 2 * S_RADIUS - J_WIDTH;

  private static Geometry create_J(final Geometry g) {
    final GeometryFactory gf = FunctionsUtil.getFactoryOrDefault(g);

    final Coordinates[] jTop = new Coordinates[] {
      new Coordinate(0, HEIGHT), new Coordinate(J_WIDTH, HEIGHT),
      new Coordinate(J_WIDTH, J_RADIUS)
    };
    final Coordinates[] jBottom = new Coordinates[] {
      new Coordinate(J_WIDTH - J_RADIUS, 0), new Coordinate(0, 0)
    };

    final GeometricShapeFactory gsf = new GeometricShapeFactory(gf);
    gsf.setBase(new Coordinate(J_WIDTH - 2 * J_RADIUS, 0));
    gsf.setSize(2 * J_RADIUS);
    gsf.setNumPoints(10);
    final LineString jArc = gsf.createArc(1.5 * Math.PI, 0.5 * Math.PI);

    final CoordinateList coordList = new CoordinateList();
    coordList.add(jTop, false);
    coordList.add(jArc.reverse().getCoordinateArray(), false, 1,
      jArc.getVertexCount() - 1);
    coordList.add(jBottom, false);

    return gf.lineString(coordList.toCoordinateArray());
  }

  private static Geometry create_S(final Geometry g) {
    final GeometryFactory gf = FunctionsUtil.getFactoryOrDefault(g);

    final double centreX = WIDTH - S_RADIUS;

    final Coordinates[] top = new Coordinates[] {
      new Coordinate(WIDTH, HEIGHT), new Coordinate(centreX, HEIGHT)
    };
    final Coordinates[] bottom = new Coordinates[] {
      new Coordinate(centreX, 0), new Coordinate(WIDTH - 2 * S_RADIUS, 0)
    };

    final GeometricShapeFactory gsf = new GeometricShapeFactory(gf);
    gsf.setCentre(new Coordinate(centreX, HEIGHT - S_RADIUS));
    gsf.setSize(2 * S_RADIUS);
    gsf.setNumPoints(10);
    final LineString arcTop = gsf.createArc(0.5 * Math.PI, Math.PI);

    final GeometricShapeFactory gsf2 = new GeometricShapeFactory(gf);
    gsf2.setCentre(new Coordinate(centreX, S_RADIUS));
    gsf2.setSize(2 * S_RADIUS);
    gsf2.setNumPoints(10);
    final LineString arcBottom = gsf2.createArc(1.5 * Math.PI, Math.PI)
      .reverse();

    final CoordinateList coordList = new CoordinateList();
    coordList.add(top, false);
    coordList.add(arcTop.getCoordinateArray(), false, 1,
      arcTop.getVertexCount() - 1);
    coordList.add(new Coordinate(centreX, HEIGHT / 2));
    coordList.add(arcBottom.getCoordinateArray(), false, 1,
      arcBottom.getVertexCount() - 1);
    coordList.add(bottom, false);

    return gf.lineString(coordList.toCoordinateArray());
  }

  private static Geometry create_T(final Geometry g) {
    final GeometryFactory gf = FunctionsUtil.getFactoryOrDefault(g);

    final Coordinates[] tTop = new Coordinates[] {
      new Coordinate(J_WIDTH, HEIGHT),
      new Coordinate(WIDTH - S_RADIUS - 5, HEIGHT)
    };
    final Coordinates[] tBottom = new Coordinates[] {
      new Coordinate(J_WIDTH + 0.5 * T_WIDTH, HEIGHT),
      new Coordinate(J_WIDTH + 0.5 * T_WIDTH, 0)
    };
    final LineString[] lines = new LineString[] {
      gf.lineString(tTop), gf.lineString(tBottom)
    };
    return gf.multiLineString(lines);
  }

  public static String jtsVersion(final Geometry g) {
    return "";
  }

  public static Geometry logoBuffer(final Geometry g, final double distance) {
    final Geometry lines = logoLines(g);
    final BufferParameters bufParams = new BufferParameters();
    bufParams.setEndCapStyle(BufferParameters.CAP_SQUARE);
    return Buffer.buffer(lines, distance, bufParams);
  }

  public static Geometry logoLines(final Geometry g) {
    return create_J(g).union(create_T(g)).union(create_S(g));
  }

}
